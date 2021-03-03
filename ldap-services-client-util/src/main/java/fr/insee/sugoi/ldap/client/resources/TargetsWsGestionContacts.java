package fr.insee.sugoi.ldap.client.resources;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;

public class TargetsWsGestionContacts {

  Client client;
  WebTarget targetGlobale;

  public TargetsWsGestionContacts(Client client, String uriServeur) {
    this.client = client;
    this.targetGlobale = client.target(uriServeur);
  }

  public Client getClient() {
    return this.client;
  }

  public WebTarget getContactTarget(String id) {
    return targetGlobale.path(Path.CONTACT.getUri()).path(id);
  }

  public WebTarget getContactTarget(String id, String domaine) {
    return targetGlobale.path(domaine).path(Path.CONTACT.getUri()).path(id);
  }

  public WebTarget getContactLoginTarget(String id, String domaine) {
    return targetGlobale.path(domaine).path(Path.CONTACT.getUri()).path(id).path(Path.LOGIN.getUri());
  }

  public WebTarget getContactPasswordTarget(String id, String domaine) {
    return targetGlobale.path(domaine).path(Path.CONTACT.getUri()).path(id).path(Path.PASSWORD.getUri());
  }

  public WebTarget getContactInitPasswordTarget(String id, String domaine) {
    return targetGlobale.path(domaine).path(Path.CONTACT.getUri()).path(id).path(Path.INITPASSWORD.getUri());
  }

  public WebTarget getContactsTarget(String domaine) {
    return targetGlobale.path(domaine).path(Path.CONTACTS.getUri());
  }

  public WebTarget getContactsInGroupAndDomainTarget(String domaine) {
    return targetGlobale.path(domaine).path(Path.CONTACTS.getUri()).path(Path.GROUPE.getUri());
  }

  public WebTarget getOrganisationTarget(String id) {
    return targetGlobale.path(Path.ORGANISATION.getUri()).path(id);
  }

  public WebTarget getOrganisationTarget(String id, String domaine) {
    return targetGlobale.path(domaine).path(Path.ORGANISATION.getUri()).path(id);
  }

  public WebTarget getOrganisationsTarget(String domaine) {
    return targetGlobale.path(domaine).path(Path.ORGANISATIONS.getUri());
  }

  public WebTarget getProfilTarget() {
    return targetGlobale.path(Path.PROFIL.getUri());
  }

  public WebTarget getProfilTarget(String nomProfil) {
    return targetGlobale.path(Path.PROFIL.getUri()).path(nomProfil);
  }

  public WebTarget getProfilsTarget() {
    return targetGlobale.path(Path.PROFILS.getUri());
  }

  public WebTarget getCredentialsTarget(String id, String domaine) {
    return targetGlobale.path(domaine).path(Path.CREDENTIALS.getUri()).path(id);
  }

  public WebTarget getHabilitationsTarget(String id, String domaine) {
    return targetGlobale.path(domaine).path(Path.CONTACT.getUri()).path(id).path(Path.HABILITATIONS.getUri());
  }

  public WebTarget getGroupesTarget(String id, String domaine) {
    return targetGlobale.path(domaine).path(Path.CONTACT.getUri()).path(id).path(Path.GROUPES.getUri());
  }

  public WebTarget getInseeRoleTarget(String id, String domaine) {
    return targetGlobale.path(domaine).path(Path.CONTACT.getUri()).path(id).path(Path.INSEE_ROLES.getUri());
  }

  public WebTarget getGroupesByDomaine(String domaine) {
    return targetGlobale.path(Path.PROFIL.getUri()).path(domaine).path(Path.GROUPES.getUri());
  }

  private enum Path {

    CONTACT("/contact"), CONTACTS("/contacts"), CREDENTIALS("/credentials"), ORGANISATION("/organisation"),
    ORGANISATIONS("/organisations"), LOGIN("/login"), INITPASSWORD("/password/first"), PASSWORD("/password"),
    HABILITATIONS("/habilitations"), PROFIL("/profil"), PROFILS("/profils"), GROUPES("/groupes"),
    INSEE_ROLES("/inseeroles"), GROUPE("/groupe");

    private String uri;

    private Path(String uri) {
      this.uri = uri;
    }

    public String getUri() {
      return uri;
    }

  }

}
