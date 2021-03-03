package fr.insee.sugoi.ldap.client.utils;

import java.security.cert.CertificateException;

public class PasUnCertificatClientException extends CertificateException {

  private static final long serialVersionUID = 2587045231870786156L;

  public PasUnCertificatClientException(String message) {
    super(message);
  }

}
