package fr.insee.sugoi.ldap.client.service.impl;

public class ClientWsGestionContactsBuilder {

  String serverUrl = null;
  String compte = null;
  String password = null;
  boolean modeDebug = false;

  /**
   * Permet de définir l'url du WS gestion des contacts.
   * 
   * @param serverUrl l'url complète à définir (url+port)
   * @return le builder pour chainage
   */
  public ClientWsGestionContactsBuilder connectToUrl(String serverUrl) {
    this.serverUrl = serverUrl;
    return this;
  }

  /**
   * Permet de définir le nom du compte applicatif pour se connecter au web
   * service.
   * 
   * @param compte le username de connexion
   * @return le builder pour chainage
   */
  public ClientWsGestionContactsBuilder connectWithUsername(String compte) {
    this.compte = compte;
    return this;
  }

  /**
   * Permet de définir le mot de passe du compte applicatif pour se connecter au
   * web service.
   * 
   * @param password le password de connexion
   * @return le builder pour chainage
   */
  public ClientWsGestionContactsBuilder connectWithPassword(String password) {
    this.password = password;
    return this;
  }

  /**
   * Permet de définir si le client doit s'éxécuter en mode debug. L'activation du
   * mode debug permet d'avoir une trace de l'ensemble des requêtes et réponses
   * échangées avec le web service.
   * 
   * @param modeDebug mode logging debug activé
   * @return le builder pour chainage
   */
  public ClientWsGestionContactsBuilder runInModeDebug(boolean modeDebug) {
    this.modeDebug = modeDebug;
    return this;
  }

  /**
   * Génère une instance du client pour le web service gestion des contacts selon
   * les paramètres passés au builder.
   * 
   * @return L'instance du client selon les paramètres
   */
  public ClientWsGestionContacts build() {
    if (serverUrl == null) {
      throw new UnsupportedOperationException("La définition d'une url est obligatoire");
    }
    if (compte == null || password == null) {
      throw new UnsupportedOperationException("La définition d'une compte est obligatoire (username et password)");
    }
    return new ClientWsGestionContacts(serverUrl, compte, password, modeDebug);
  }

}
