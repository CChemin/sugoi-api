package fr.insee.sugoi.ldap.client.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CertificateUtils {
  private static final Logger LOG = LogManager.getFormatterLogger(CertificateUtils.class);

  private static final int EMPLACEMENT_KEY_USAGE_SIGNATURE = 0;
  private static final int EMPLACEMENT_KEY_USAGE_AC = 5;

  /**
   * Extrait le certificat d'un fichier
   * 
   * @param file : le fichier proposé par l'utilisateur pour en extraire un
   *             certificat
   * @return le certificat s'il est correct et qu'il correspond bien à un
   *         certificat client.
   * @throws CertificateException  si le certificat n'est pas correctement
   *                               construit ou que ce n'est pas un certificat
   *                               client auquel cas l'erreur est plus spécifique
   *                               : PasUnCertificatClientException. S'il est en
   *                               plus détecté que c'est une autorité de
   *                               confiance l'exception est encore plus
   *                               spécifique :
   *                               PasUnCertificatClientCarACException
   * @throws FileNotFoundException si le fichier proposé est introuvable
   */
  public static X509Certificate getCertificateClientFromFile(File file)
      throws CertificateException, FileNotFoundException {
    X509Certificate cert = getCertificateFromFile(file);
    if (!isSignatureCertificate(cert)) {
      if (isAcCertificate(cert)) {
        throw new PasUnCertificatClientCarAcException("Le certifcat proposé est celui d'une autorité de confiance");
      }
      throw new PasUnCertificatClientException("Le certificat ne peux pas être utilisé comme certificat client");
    }
    return cert;
  }

  /**
   * Vérifie si le fichier fourni est un certificat client.
   * 
   * @param file le fichier à vérifier
   * @return true si le fichier est un certificat client ou faux si le fichier
   *         n'est pas trouvé, si ce n'est pas un certificat ou si c'est un
   *         certificat mais pas client
   */
  public static boolean verifierCertificatClientFromFile(File file) {
    try {
      X509Certificate cert = getCertificateFromFile(file);
      return isSignatureCertificate(cert);
    } catch (CertificateException | FileNotFoundException e) {
      return false;
    }

  }

  /**
   * 
   * @param file : le fichier proposé par l'utilisateur pour en extraire un
   *             certificat
   * @return le certificat s'il est correct.
   * @throws CertificateException  si le certificat n'est pas correctement
   *                               construit
   * @throws FileNotFoundException si le fichier proposé est introuvable
   */
  public static X509Certificate getCertificateFromFile(File file) throws CertificateException, FileNotFoundException {
    FileInputStream inputStream = null;
    try {
      inputStream = new FileInputStream(file);
      CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
      X509Certificate cert = (X509Certificate) certFactory.generateCertificate(inputStream);
      LOG.debug("Certificat importé : " + cert);
      return cert;

    } catch (CertificateException e) {
      LOG.error("Problème à l'import du certificat", e);
      throw e;
    } catch (FileNotFoundException e) {
      LOG.error("Problème de récupération du fichier", e);
      throw e;
    } finally {
      try {
        inputStream.close();
      } catch (IOException e) {
        LOG.warn("Erreur à la fermeture du fichier");
      }
    }
  }

  /**
   * Le certificat renseigné doit être de type certificat client. Il faut
   * s'assurer que le certificat n'est pas par exemple celui de l'autorité de
   * confiance qui l'a signé.
   * 
   * <p>
   * Vérifie le type du certificat à l'aide de l'attribut KeyUsage qui suit la RFC
   * 3280.
   * 
   * <p>
   * Gets a boolean array representing bits of the KeyUsage extension, (OID =
   * 2.5.29.15). The key usage extension defines the purpose (e.g., encipherment,
   * signature, certificate signing) of the key contained in the certificate. The
   * ASN.1 definition for this is: KeyUsage ::= BIT STRING { digitalSignature (0),
   * nonRepudiation (1), keyEncipherment (2), dataEncipherment (3), keyAgreement
   * (4), keyCertSign (5), cRLSign (6), encipherOnly (7), decipherOnly (8) }
   * 
   * <p>
   * RFC 3280 recommends that when used, this be marked as a critical
   * extension.Returns:the KeyUsage extension of this certificate, represented as
   * an array of booleans. The order of KeyUsage values in the array is the same
   * as in the above ASN.1 definition. The array will contain a value for each
   * KeyUsage defined above. If the KeyUsage list encoded in the certificate is
   * longer than the above list, it will not be truncated. Returns null if this
   * certificate does not contain a KeyUsage extension.
   * 
   * 
   * @param certificate le certificat à vérifier
   * @return true si c'est un certificat client
   */
  public static boolean isSignatureCertificate(X509Certificate certificate) {

    try {
      if (certificate.getKeyUsage() != null) {
        return certificate.getKeyUsage()[EMPLACEMENT_KEY_USAGE_SIGNATURE];
      } else if (certificate.getExtendedKeyUsage() != null) {
        // Si le champs KeyUsage normalement obligatoire n'est pas pas
        // présent on tolère la seule présence de extended key usage
        // OID 1.3.6.1.5.5.7.3.2 = utilisation client
        return certificate.getExtendedKeyUsage().contains("1.3.6.1.5.5.7.3.2");
      } else {
        // Dans le doute on refuse le certificat, on verra si le cas se
        // présente un jour
        return false;
      }
    } catch (CertificateParsingException e) {
      // Se produit si le champ OBLIGATOIRE keyusage est absent et que le
      // champ extended key usage est présent mais indéchiffrable. On a
      // donc pas de moyen de vérifier que le certificat est client
      return false;
    }

  }

  private static boolean isAcCertificate(X509Certificate certificate) {
    if (certificate.getKeyUsage() != null) {
      return certificate.getKeyUsage()[EMPLACEMENT_KEY_USAGE_AC];
    } else {
      return false;
    }
  }

  // private static void debugKeyUsage(X509Certificate cert) {
  // String[] keyUsages = { "digitalSignature", "nonRepudiation",
  // "keyEncipherment", "dataEncipherment",
  // "keyAgreement", "keyCertSign", "cRLSign", "encipherOnly", "decipherOnly"
  // };
  // for (int i = 0; i < cert.getKeyUsage().length; i++) {
  // LOG.debug(keyUsages[i] + " : " + cert.getKeyUsage()[i]);
  // }
  // LOG.debug(cert.getKeyUsage());
  // }

}
