package fr.insee.sugoi.ldap.client.utils;

public enum ModeEnvoi {

  MAIL("mail"), COURRIER("courrier");

  private String mode;

  private ModeEnvoi(String mode) {
    this.mode = mode;
  }

  public String getMode() {
    return mode;
  }

}
