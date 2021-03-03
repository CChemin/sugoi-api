package fr.insee.sugoi.ldap.client.service.impl;

import static fr.insee.sugoi.ldap.client.exception.WsGestionDesContactsException.determinerErreur;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import fr.insee.sugoi.ldap.client.exception.WsGestionDesContactsException;
import fr.insee.sugoi.ldap.client.resources.TargetsWsGestionContacts;
import fr.insee.sugoi.ldap.client.utils.ModeEnvoi;
import fr.insee.sugoi.converter.ouganext.Contact;
import fr.insee.sugoi.converter.ouganext.Contacts;
import fr.insee.sugoi.converter.ouganext.Groupe;
import fr.insee.sugoi.converter.ouganext.InfoFormattage;
import fr.insee.sugoi.converter.ouganext.PasswordChangeRequest;

public class ClientWsContact extends ClientWsEntite<Contact> {

  private static final Logger LOG = LogManager.getFormatterLogger(ClientWsContact.class);

  public ClientWsContact(TargetsWsGestionContacts targets) {
    this.targets = targets;
  }

  @Override
  protected Class<Contact> getClasse() {
    return Contact.class;
  }

  @Override
  protected Contacts recupererListeEntites(Response response) {
    return response.readEntity(Contacts.class);
  }

  @Override
  protected WebTarget getTargetUnique(String id) {
    return targets.getContactTarget(id);
  }

  @Override
  protected WebTarget getTargetUnique(String id, String domaine) {
    return targets.getContactTarget(id, domaine);
  }

  @Override
  protected WebTarget getTargetMultiple(String domaine) {
    return targets.getContactsTarget(domaine);
  }

  @Override
  protected String getNomLog() {
    return "contact";
  }

  /**
   * RÃ©initialise le mot de passe de l'entitÃ© dÃ©signÃ©e par id et envoi le
   * nouveau mot de passe selon le modeEnvoi dÃ©signÃ©.
   * 
   * @param id        l'id de l'entitÃ© dont le mot de passe doit Ãªtre
   *                  rÃ©initialisÃ©
   * @param modeEnvoi le mode d'envoi, courrier ou mail
   * @throws WsGestionDesContactsException si l'entitÃ© n'existe pas ou que
   *                                       l'utilisateur n'a pas le droit de
   *                                       reinitialiser un mot de passe
   */
  public void reinitPassword(String id, String domaine, ModeEnvoi modeEnvoi) throws WsGestionDesContactsException {
    reinitPassword(id, domaine, modeEnvoi, null, null);
  }

  /**
   * RÃ©initialise le mot de passe de l'entitÃ© dÃ©signÃ©e par id et envoi le
   * nouveau mot de passe selon le modeEnvoi dÃ©signÃ©.
   * 
   * @param id        l'id de l'entitÃ© dont le mot de passe doit Ãªtre
   *                  rÃ©initialisÃ©
   * @param modeEnvoi le mode d'envoi, courrier ou mail
   * @throws WsGestionDesContactsException si l'entitÃ© n'existe pas ou que
   *                                       l'utilisateur n'a pas le droit de
   *                                       reinitialiser un mot de passe
   */
  public void reinitPassword(String id, String domaine, ModeEnvoi modeEnvoi, InfoFormattage infoFormattage, String mail)
      throws WsGestionDesContactsException {

    PasswordChangeRequest pcr = new PasswordChangeRequest();
    pcr.setInfoFormattageEnvoi(infoFormattage);
    if (mail != null && !mail.isEmpty()) {
      pcr.setAdresseMessagerie(mail);
    }
    Response response = targets.getContactPasswordTarget(id, domaine).queryParam("modeEnvoi", modeEnvoi.getMode())
        .request().post(Entity.entity(pcr, MediaType.APPLICATION_JSON));
    if (response.getStatus() == Status.NO_CONTENT.getStatusCode()) {
      LOG.debug("Changement du mot de passe pour le contact %s reussi", id);
    } else {
      LOG.error("Changement du mot de passe pour le contact %s en echec. Erreur %s", id, response.getStatus());
      throw new WsGestionDesContactsException(determinerErreur(response));
    }
  }

  /**
   * Envoi le login selon le modeEnvoi designe.
   * 
   * @param id        l'id de l'entitÃ© dont le login est a envoyer
   * @param modeEnvoi le mode d'envoi, courrier ou mail
   * @throws WsGestionDesContactsException si l'entitÃ© n'existe pas
   */
  public void envoiLogin(String id, String domaine, ModeEnvoi modeEnvoi, InfoFormattage infoFormattage)
      throws WsGestionDesContactsException {

    Response response = targets.getContactLoginTarget(id, domaine).queryParam("modeEnvoi", modeEnvoi.getMode())
        .request().post(Entity.entity(infoFormattage, MediaType.APPLICATION_JSON));
    if (response.getStatus() == Status.NO_CONTENT.getStatusCode()) {
      LOG.debug("Envoi du login effectué pour le contact %s", id);
    } else {
      LOG.error("Erreur dans l'envoi du login pour le contact %s. Erreur %s", id, response.getStatus());
      throw new WsGestionDesContactsException(determinerErreur(response));
    }
  }

  /**
   * Change le mot de passe de l'entitÃ© dÃ©signÃ©e par id.
   * 
   * @param id                    l'id de l'entitÃ© dont le mot de passe doit
   *                              Ãªtre changÃ©
   * @param passwordChangeRequest la requete de changement de mot de passe
   * @throws WsGestionDesContactsException si l'entitÃ© n'existe pas ou que
   *                                       l'utilisateur n'a pas le droit de
   *                                       changer un mot de passe
   */
  public void changePassword(String id, String domaine, PasswordChangeRequest passwordChangeRequest)
      throws WsGestionDesContactsException {
    Response response = targets.getContactPasswordTarget(id, domaine).queryParam("modeEnvoi", "mail").request()
        .put(Entity.entity(passwordChangeRequest, MediaType.APPLICATION_JSON));
    if (response.getStatus() == Status.NO_CONTENT.getStatusCode()) {
      LOG.info("Le mot de passe de %s a ete change", id);
    } else {
      throw new WsGestionDesContactsException(determinerErreur(response));
    }
  }

  /**
   * Initialise le mot de passe d'une entitÃ©.
   * 
   * @param id                    l'id de l'entitÃ© dont le mot de passe doit
   *                              Ãªtre initialisÃ©
   * @param passwordChangeRequest la requete d'initialisation du mot de passe
   * @throws WsGestionDesContactsException si l'entitÃ© n'existe pas ou que
   *                                       l'utilisateur n'a pas le droit
   *                                       d'initialiser un mot de passe
   */
  public void initPassword(String id, String domaine, PasswordChangeRequest passwordChangeRequest)
      throws WsGestionDesContactsException {
    Response response = targets.getContactInitPasswordTarget(id, domaine).request()
        .post(Entity.entity(passwordChangeRequest, MediaType.APPLICATION_JSON));
    if (response.getStatus() == Status.NO_CONTENT.getStatusCode()) {
      LOG.debug("Deroulement normal");
    } else {
      throw new WsGestionDesContactsException(determinerErreur(response));
    }
  }

  /**
   * Retourne true si le contact est authentifiÃ© (couple identifiant/mot de passe
   * valide), et false sinon.
   * 
   * @param idContact  : identifiant du contact Ã  authentifier.
   * @param motDePasse : mot de passe Ã  vÃ©rifier.
   * @return true si le couple identifiant/mot de passe est valide, false sinon.
   * @throws WsGestionDesContactsException si erreur cotÃ© WS
   */
  public boolean authentifierContact(String idContact, String domaine, String motDePasse)
      throws WsGestionDesContactsException {
    Response response = targets.getCredentialsTarget(idContact, domaine).request()
        .post(Entity.entity(motDePasse, MediaType.TEXT_PLAIN));
    if (LOG.isDebugEnabled()) {
      LOG.debug(String.format("Statut contact authentifiÃ© : %s", response.getStatus()));
    }
    if (response.getStatus() == Status.OK.getStatusCode()) {
      return true;
    } else if (response.getStatus() == Status.NOT_FOUND.getStatusCode()
        || response.getStatus() == Status.UNAUTHORIZED.getStatusCode()) {
      return false;
    } else {
      throw new WsGestionDesContactsException(determinerErreur(response));
    }
  }

  public int getSize(String domaine) throws WsGestionDesContactsException {
    Response response = targets.getContactsTarget(domaine).path("size").request().get();
    if (response.getStatus() == Status.NO_CONTENT.getStatusCode()) {
      return Integer.valueOf(response.getHeaderString("X-Total-Size"));
    } else {
      throw new WsGestionDesContactsException(determinerErreur(response));
    }
  }

  public List<Contact> getContactbyDomaineAndGroupe(String domaine, Groupe groupe)
      throws WsGestionDesContactsException {
    Response response = targets.getContactsInGroupAndDomainTarget(domaine).path(groupe.getApplication())
        .path(groupe.getNom()).request(MediaType.APPLICATION_JSON).get();
    List<Contact> listeContact = new ArrayList<>();
    if (response.getStatus() == Status.OK.getStatusCode()) {
      listeContact = response.readEntity(Contacts.class).getListe();
    } else if (response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
      LOG.debug("Aucun résultat trouvé à  la recherche ");
    } else {
      throw new WsGestionDesContactsException(determinerErreur(response));
    }
    return listeContact;
  }

}
