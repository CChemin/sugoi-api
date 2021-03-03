
package fr.insee.sugoi.converter.ouganext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public interface Entite {

  public static final String EMAIL_ADDRESS_PATTERN =
      "\\b^['_a-z0-9-\\+]+(\\.['_a-z0-9-\\+]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*\\.([a-z]{2,6})$\\b";

  public String getIdentifiant();

  public void setIdentifiant(String value);

  public String getDomaineDeGestion();

  public void setDomaineDeGestion(String value);

  public String getNomCommun();

  public void setNomCommun(String value);

  public String getAdresseMessagerie();

  public void setAdresseMessagerie(String value);

  public static final int TAILLE_NUMERO_TELEPHONE = 25;

  public String getDescription();

  public void setDescription(String value);

  public String getNumeroTelephone();

  public void setNumeroTelephone(String value);

  public String getFacSimile();

  public void setFacSimile(String facSimile);

  public Adresse getAdressePostale();

  public void setAdressePostale(Adresse value);

  public void setOrganisationDeRattachement(Organisation organisation);

  public Organisation getOrganisationDeRattachement();

  public String getRepertoireDeDistribution();

  public void setRepertoireDeDistribution(String value);

  /**
   * @return la liste des propriétés ou une liste vide.
   */
  public Collection<String> getPropriete();

  /**
   * Vérifie si les champs de l'objet sont cohérents.
   * 
   * @throws PasValideException si un field est mal renseigne
   */
  public default void validerLesChamps() {
    Map<String, String> erreurs = new HashMap<>();

    if (this.getDomaineDeGestion() != null && this.getDomaineDeGestion().equals("-1")) {
      erreurs.put("domaineDeGestion", "Le domaine de gestion est obligatoire");
    }

    controlerPresenceEtFormatNomCommun(erreurs);
    controlerFormatAdresseMessagerie(erreurs);
    controlerFormatNumeroTelephone(erreurs);
    conrolerFormatFax(erreurs);

    if (erreurs.size() > 0) {
      throw new RuntimeException("Pas valide exception");
    }
  }

  /**
   * Verifie le format du numero de fax et ajoute l'erreur dans la map fournie le cas échéant.
   * 
   * @param erreurs la map d'erreur à constituer
   */
  public default void conrolerFormatFax(Map<String, String> erreurs) {
    if (this.getFacSimile() != null && this.getFacSimile().length() > 0) {
      String pattern = "^([0-9]|\\.|\\-|\\+|\\(|\\)|\\ )*$";
      String messageErreur = "Entre 1 et " + TAILLE_NUMERO_TELEPHONE + " caractères numériques, points, tirets, parenthèses, signe plus, et espaces uniquement";
      if (!this.getFacSimile().matches(pattern)
          || (this.getFacSimile().length() > TAILLE_NUMERO_TELEPHONE
              && this.getFacSimile().length() > 0)) {
        erreurs.put("facSimile", messageErreur);
      }
    }
  }

  /**
   * Verifie le format du numero de telephone et ajoute l'erreur dans la map fournie le cas échéant.
   * 
   * @param erreurs la map d'erreur à constituer
   */
  public default void controlerFormatNumeroTelephone(Map<String, String> erreurs) {
    if (this.getNumeroTelephone() != null && this.getNumeroTelephone().length() > 0) {
      String pattern = "^([0-9]|\\.|\\-|\\+|\\(|\\)|\\ )*$";
      String messageErreur = "Entre 1 et " + TAILLE_NUMERO_TELEPHONE + " caractères numériques, points, tirets, parenthèses, signe plus, et espaces uniquement";
      if (!this.getNumeroTelephone().matches(pattern)
          || (this.getNumeroTelephone().length() > TAILLE_NUMERO_TELEPHONE
              && this.getNumeroTelephone().length() > 0)) {
        erreurs.put("numeroTelephone", messageErreur);
      }
    }
  }

  /**
   * Verifie le format du mail et ajoute l'erreur dans la map fournie le cas échéant.
   * 
   * @param erreurs la map d'erreur à constituer
   */
  public default void controlerFormatAdresseMessagerie(Map<String, String> erreurs) {
    if (this.getAdresseMessagerie() != null && this.getAdresseMessagerie().length() > 0) {
      String pattern = EMAIL_ADDRESS_PATTERN;
      if (!this.getAdresseMessagerie().matches(pattern)) {
        erreurs.put("adresseMessagerie", "Format invalide");
      }
    }
  }

  /**
   * Verifie le format du nom commun et sa presence et ajoute l'erreur dans la map fournie le cas
   * échéant.
   * 
   * @param erreurs la map d'erreur à constituer
   */
  public default void controlerPresenceEtFormatNomCommun(Map<String, String> erreurs) {
    if (this.getNomCommun().length() > 0) {
      String patternNom = "^[a-zA-Z0-9 \\p{L}* \'-]*$";
      if (!this.getNomCommun().matches(patternNom)) {
        erreurs.put("nomCommun",
            "Caractères alphabétiques, chiffres, apostrophes, espaces et tirets uniquement");
      }
    } else {
      erreurs.put("nomCommun", "Un nom commun est obligatoire");
    }
  }

}