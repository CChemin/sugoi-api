package fr.insee.sugoi.ldap.client.service.impl;

import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.logging.LoggingFeature.Verbosity;
import fr.insee.sugoi.ldap.client.exception.WsGestionDesContactsException;
import fr.insee.sugoi.ldap.client.resources.TargetsWsGestionContacts;
import fr.insee.sugoi.ldap.client.utils.ModeEnvoi;
import fr.insee.sugoi.ldap.client.utils.Recherche;
import fr.insee.sugoi.ldap.client.utils.ResultatsPartiels;
import fr.insee.sugoi.converter.ouganext.Entite;
import fr.insee.sugoi.converter.ouganext.Contact;
import fr.insee.sugoi.converter.ouganext.Groupe;
import fr.insee.sugoi.converter.ouganext.Habilitations;
import fr.insee.sugoi.converter.ouganext.InfoFormattage;
import fr.insee.sugoi.converter.ouganext.Organisation;
import fr.insee.sugoi.converter.ouganext.PasswordChangeRequest;
import fr.insee.sugoi.converter.ouganext.Profil;

/**
 * Client Java pour appel au WS gestion des contacts. Classe centralisant
 * l'ensemble des fonctionnalités.
 *
 */
public class ClientWsGestionContacts {

  private static final Logger LOG = LogManager.getFormatterLogger(ClientWsGestionContacts.class);

  private static final int NOMBRE_DEFAUT_DE_REPONSE = 50;

  private static ClientWsContact clientWsContact;
  private static ClientWsOrganisation clientWsOrganisation;
  private static ClientWsHabilitations clientWsHabilitations;
  private static ClientWsProfil clientWsProfil;

  // Constructeurs réservé au package

  ClientWsGestionContacts() {
  }

  ClientWsGestionContacts(String serverUrl, String compte, String password, boolean modeDebug) {

    ClientConfig confDuClient = new ClientConfig();
    if (modeDebug) {
      initialiserModeDebug(confDuClient);
    }
    confDuClient.register(HttpAuthenticationFeature.basicBuilder().credentials(compte, password).build());
    Client client = ClientBuilder.newClient(confDuClient);

    TargetsWsGestionContacts targets = new TargetsWsGestionContacts(client, serverUrl);
    clientWsContact = new ClientWsContact(targets);
    clientWsHabilitations = new ClientWsHabilitations(targets);
    clientWsOrganisation = new ClientWsOrganisation(targets);
    clientWsProfil = new ClientWsProfil(targets);
  }

  private void initialiserModeDebug(ClientConfig confDuClient) {
    LOG.info("Le mode DEBUG de Jersey 2 est activé");
    java.util.logging.Logger loggerJersey = java.util.logging.Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);
    loggerJersey.addHandler(new Handler() {
      @Override
      public void publish(LogRecord record) {
        LOG.debug(record.getMessage());
      }

      @Override
      public void flush() {
        // Do nothing
      }

      @Override
      public void close() throws SecurityException {
        // Do nothing
      }
    });
    loggerJersey.setUseParentHandlers(false);
    loggerJersey.setLevel(Level.FINEST);
    LoggingFeature loggingFeature = new LoggingFeature(loggerJersey, Level.FINEST, Verbosity.PAYLOAD_ANY,
        LoggingFeature.DEFAULT_MAX_ENTITY_SIZE);
    confDuClient.register(loggingFeature);
  }

  /* GESTION DES CONTACTS */

  /**
   * Recherche le contact par identifiant. Si aucun contact n'est trouvé, renvoie
   * null.
   * 
   * @param idContact : identifiant du contact à rechercher.
   * @return le contact trouvé, null sinon.
   */
  public Contact getContact(String idContact) throws WsGestionDesContactsException {
    return clientWsContact.getById(idContact);
  }

  /**
   * Recherche le contact par identifiant. Si aucun contact n'est trouvé, renvoie
   * null.
   * 
   * @param idContact : identifiant du contact à rechercher.
   * @return le contact trouvé, null sinon.
   */
  public Contact getContact(String idContact, String domaine) throws WsGestionDesContactsException {
    return clientWsContact.getByIdAndDomaine(idContact, domaine);
  }

  /**
   * Retourne true si le contact est authentifié (couple identifiant/mot de passe
   * valide), et false sinon.
   * 
   * @param idContact  : identifiant du contact à authentifier.
   * @param motDePasse : mot de passe à vérifier.
   * @return true si le couple identifiant/mot de passe est valide, false sinon.
   * @throws WsGestionDesContactsException si une erreur se produit lors de
   *                                       l'appel au Web Service
   */
  public boolean authentifierContact(String idContact, String domaine, String motDePasse)
      throws WsGestionDesContactsException {
    return clientWsContact.authentifierContact(idContact, domaine, motDePasse);
  }

  /**
   * Envoi le login au contact id selon le modeEnvoi demandé.
   * 
   * @param id        dont le login est à envoyer
   * @param modeEnvoi courrier ou mail
   * @throws WsGestionDesContactsException si une erreur se produit lors de
   *                                       l'appel au Web Service
   */
  public void envoyerLogin(String id, String domaine, ModeEnvoi modeEnvoi, InfoFormattage infoFormattage)
      throws WsGestionDesContactsException {
    clientWsContact.envoiLogin(id, domaine, modeEnvoi, infoFormattage);
  }

  /**
   * Réinitialise le mot de passe du contact id et lui envoie selon le modeEnvoi
   * demandé.
   * 
   * @param id        dont le mot de passe est à réinitialiser
   * @param modeEnvoi courrier ou mail
   * @throws WsGestionDesContactsException si une erreur se produit lors de
   *                                       l'appel au Web Service
   */
  public void reinitPassword(String id, String domaine, ModeEnvoi modeEnvoi) throws WsGestionDesContactsException {
    clientWsContact.reinitPassword(id, domaine, modeEnvoi);
  }

  /**
   * Réinitialise le mot de passe du contact id et lui envoie selon le modeEnvoi
   * demandé et le formattage demandé.
   * 
   * @param id             dont le mot de passe est à réinitialiser
   * @param modeEnvoi      courrier ou mail
   * @param infoFormattage info sur le formattage de l'envoi
   * @throws WsGestionDesContactsException si une erreur se produit lors de
   *                                       l'appel au Web Service
   */
  public void reinitPassword(String id, String domaine, ModeEnvoi modeEnvoi, InfoFormattage infoFormattage)
      throws WsGestionDesContactsException {
    clientWsContact.reinitPassword(id, domaine, modeEnvoi, infoFormattage, null);
  }

  /**
   * Réinitialise le mot de passe du contact id et lui envoie selon le modeEnvoi
   * demandé et le formattage demandé, et au mail demandé.
   * 
   * @param id             dont le mot de passe est à réinitialiser
   * @param modeEnvoi      courrier ou mail
   * @param infoFormattage info sur le formattage de l'envoi
   * @throws WsGestionDesContactsException si une erreur se produit lors de
   *                                       l'appel au Web Service
   */
  public void reinitPassword(String id, String domaine, ModeEnvoi modeEnvoi, InfoFormattage infoFormattage, String mail)
      throws WsGestionDesContactsException {
    clientWsContact.reinitPassword(id, domaine, modeEnvoi, infoFormattage, mail);
  }

  /**
   * Change le mot de passe du contact id.
   * 
   * @param id                    dont le mot de passe est à changer
   * @param passwordChangeRequest la requete devant contenir au moins l'ancien et
   *                              le nouveau mot de passe
   * @throws WsGestionDesContactsException si une erreur se produit lors de
   *                                       l'appel au Web Service
   */
  public void changePassword(String id, String domaine, PasswordChangeRequest passwordChangeRequest)
      throws WsGestionDesContactsException {
    clientWsContact.changePassword(id, domaine, passwordChangeRequest);
  }

  /**
   * Donne un mot de passe défini au contact id.
   * 
   * @param id                    dont le mot de passe est à initialiser.
   * @param passwordChangerequest la requete devant contenir au moins le nouveau
   *                              mot de passe (à initialiser)
   * @throws WsGestionDesContactsException si une erreur se produit lors de
   *                                       l'appel au Web Service
   */
  public void initPassword(String id, String domaine, PasswordChangeRequest passwordChangerequest)
      throws WsGestionDesContactsException {
    clientWsContact.initPassword(id, domaine, passwordChangerequest);
  }

  /**
   * Supprime le contact id.
   * 
   * @param id du contact à supprimer
   * @throws WsGestionDesContactsException si une erreur se produit lors de
   *                                       l'appel au Web Service
   */
  public void supprimerContact(String id, String domaine) throws WsGestionDesContactsException {
    clientWsContact.deleteByIdAndDomaine(id, domaine);
  }

  /* GESTION DES ORGANISATIONS */

  /**
   * Recherche l'organisation par identifiant. Si aucun contact n'est trouvé,
   * renvoie null.
   * 
   * @param id identifiant de l'organisation à rechercher.
   * @return l'organisation trouvée, null sinon.
   * @throws WsGestionDesContactsException si une erreur se produit lors de
   *                                       l'appel au Web Service
   */
  public Organisation getOrganisation(String id) throws WsGestionDesContactsException {
    return clientWsOrganisation.getById(id);
  }

  /**
   * Recherche l'organisation par identifiant. Si aucun contact n'est trouvé,
   * renvoie null.
   * 
   * @param id identifiant de l'organisation à rechercher.
   * @return l'organisation trouvée, null sinon.
   * @throws WsGestionDesContactsException si une erreur se produit lors de
   *                                       l'appel au Web Service
   */
  public Organisation getOrganisation(String id, String domaine) throws WsGestionDesContactsException {
    return clientWsOrganisation.getByIdAndDomaine(id, domaine);
  }

  /**
   * Supprime l'organisation id.
   * 
   * @param id du contact à supprimer
   * @throws WsGestionDesContactsException si une erreur se produit lors de
   *                                       l'appel au Web Service
   */
  public void supprimerOrganisation(String id, String domaine) throws WsGestionDesContactsException {
    clientWsOrganisation.deleteByIdAndDomaine(id, domaine);
  }

  /* GESTION DES HABILITATIONS */

  /**
   * Recherche les habilitations d'un contact d'après son identifiant. Si aucun
   * contact n'est trouvé, renvoie null. Les habilitations du contact peuvent être
   * vides.
   * 
   * @param idContact : identifiant du contact pour lequel on souhaite récupérer
   *                  les habilitations.
   * @return les habilitations du contact.
   * @throws WsGestionDesContactsException si une erreur se produit lors de
   *                                       l'appel au Web Service
   */
  public Habilitations getHabilitationsByIdAndDomaine(String idContact, String domaine)
      throws WsGestionDesContactsException {
    return clientWsHabilitations.getHabilitationsByIdAndDomaine(idContact, domaine);
  }

  /**
   * Ajoute une liste de role sans propriete au contact id.
   * 
   * @param idContact   concerné
   * @param application concerné
   * @param roles       liste des roles à rajouter
   * @throws WsGestionDesContactsException si une erreur se produit lors de
   *                                       l'appel au Web Service
   */
  public void addHabilitationsWithoutProperty(String idContact, String domaine, String application, List<String> roles)
      throws WsGestionDesContactsException {
    clientWsHabilitations.addHabilitationsWithoutProperty(idContact, domaine, application, roles);
  }

  /**
   * Supprime une liste de rôles (et toutes les propriétés associées le cas
   * échéant).
   * 
   * @param idContact   concerné
   * @param application concerné
   * @param roles       liste des roles à supprimer
   * @throws WsGestionDesContactsException si une erreur se produit lors de
   *                                       l'appel au Web Service
   */
  public void deleteHabilitations(String idContact, String domaine, String application, List<String> roles)
      throws WsGestionDesContactsException {
    clientWsHabilitations.deleteHabilitations(idContact, domaine, application, roles);
  }

  /**
   * Ajoute un role avec une liste de propriété.
   * 
   * @param idContact   concerné
   * @param application concerné
   * @param role        concerné
   * @param proprietes  liste des propriétés à ajouter
   * @throws WsGestionDesContactsException si une erreur se produit lors de
   *                                       l'appel au Web Service
   */
  public void addHabilitationsWithProperty(String idContact, String domaine, String application, String role,
      List<String> proprietes) throws WsGestionDesContactsException {
    clientWsHabilitations.addHabilitationsWithProperty(idContact, domaine, application, role, proprietes);
  }

  /**
   * Supprime des propriétés d'un role.
   * 
   * @param idContact   concerné
   * @param application concerné
   * @param role        concerné
   * @param proprietes  liste des propriétés à supprimer
   * @throws WsGestionDesContactsException si une erreur se produit lors de
   *                                       l'appel au Web Service
   */
  public void deletePropertyofHabilitation(String idContact, String domaine, String application, String role,
      List<String> proprietes) throws WsGestionDesContactsException {
    clientWsHabilitations.deletePropertyofHabilitation(idContact, domaine, application, role, proprietes);
  }

  /* RECHERCHE CONTACTS ET ORGANISATIONS */

  /**
   * Recherche tous les identifiants d'un domaine selon une recherche de contacts.
   * 
   * @param recherche critères de la recherche
   * @return la liste des identifiants concernés
   * @throws WsGestionDesContactsException si une erreur se produit lors de
   *                                       l'appel au Web Service
   */
  public List<String> rechercheIdentifiantContact(Recherche recherche) throws WsGestionDesContactsException {
    return clientWsContact.rechercheIdentifiants(recherche);
  }

  /**
   * Recherche tous les identifiants d'un domaine selon une recherche
   * d'organisations.
   * 
   * @param recherche critères de la recherche
   * @return la liste des identifiants concernés
   * @throws WsGestionDesContactsException si une erreur se produit lors de
   *                                       l'appel au Web Service
   */
  public List<String> rechercheIdentifiantOrganisation(Recherche recherche) throws WsGestionDesContactsException {
    return clientWsOrganisation.rechercheIdentifiants(recherche);
  }

  /* METHODES COMMUNES TOUTES ENTITE */

  /**
   * Recherche les 50 premiers résulats d'une recherche.
   * 
   * @param recherche critères de la recherche
   * @return un ResultatsPartiels contenant la liste de résultat et un lien vers
   *         les résulats suivant le cas échéant
   * @throws WsGestionDesContactsException si une erreur se produit lors de
   *                                       l'appel au Web Service
   */
  public <T extends Entite> ResultatsPartiels<T> recherchePartielle(Recherche recherche)
      throws WsGestionDesContactsException {
    return recherchePartielle(NOMBRE_DEFAUT_DE_REPONSE, 0, recherche);
  }

  /**
   * Recherche les "size" premiers résulats d'une recherche.
   * 
   * @param size      nombre de résultats souhaités
   * @param recherche critères de la recherche
   * @return un ResultatsPartiels contenant la liste de résultat et un lien vers
   *         les résulats suivant le cas échéant
   * @throws WsGestionDesContactsException si une erreur se produit lors de
   *                                       l'appel au Web Service
   */
  public <T extends Entite> ResultatsPartiels<T> recherchePartielle(int size, Recherche recherche)
      throws WsGestionDesContactsException {
    return recherchePartielle(size, 0, recherche);
  }

  /**
   * Recherche les "size" premiers résulats d'une recherche à partir du résultat
   * "start". Le paramètre start ne fonctionne correctment que si la recherche VLV
   * est opérationnelle sur le domaine.
   * 
   * @param size      nombre de résultats souhaités
   * @param start     premier résultat à renvoyer
   * @param recherche critères de la recherche
   * @return un ResultatsPartiels contenant la liste de résultat et un lien vers
   *         les résulats suivant le cas échéant
   * @throws WsGestionDesContactsException si une erreur se produit lors de
   *                                       l'appel au Web Service
   */
  @SuppressWarnings("unchecked")
  public <T extends Entite> ResultatsPartiels<T> recherchePartielle(int size, int start, Recherche recherche)
      throws WsGestionDesContactsException {
    if (recherche.getClasseRecherchee().equals(Organisation.class)) {
      return (ResultatsPartiels<T>) clientWsOrganisation.rechercheEntites(size, start, recherche);
    } else if (Contact.class.isAssignableFrom(recherche.getClasseRecherchee())) {
      return (ResultatsPartiels<T>) clientWsContact.rechercheEntites(size, start, recherche);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * Renvoit les résultats suivants si existants.
   * 
   * @param resultatsPartiels la recherche en cours
   * @return le ResultatsPartiel suivant
   * @throws WsGestionDesContactsException si une erreur se produit lors de
   *                                       l'appel au Web Service
   */
  @SuppressWarnings("unchecked")
  public <T extends Entite> ResultatsPartiels<T> nextRecherchePartielle(
      ResultatsPartiels<? extends Entite> resultatsPartiels) throws WsGestionDesContactsException {
    if (resultatsPartiels.getListeResultats().get(0) instanceof Organisation) {
      return (ResultatsPartiels<T>) clientWsOrganisation
          .nextRechercheEntites((ResultatsPartiels<Organisation>) resultatsPartiels);
    } else if (resultatsPartiels.getListeResultats().get(0) instanceof Contact) {
      return (ResultatsPartiels<T>) clientWsContact
          .nextRechercheEntites((ResultatsPartiels<Contact>) resultatsPartiels);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * Met à jour l'entité. Les atributs identfiants et domaine permettent de
   * chercher le contact et ne peuvent donc être mis à jour.
   * 
   * @param entite à metre à jour
   * @return l'entite mise à jour
   * @throws WsGestionDesContactsException si une erreur se produit lors de
   *                                       l'appel au Web Service
   */
  public Entite mettreAjour(Entite entite) throws WsGestionDesContactsException {
    if (entite instanceof Contact) {
      return clientWsContact.update((Contact) entite);
    } else if (entite instanceof Organisation) {
      return clientWsOrganisation.update((Organisation) entite);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * Crée l'entite. L'entite fournie doit être complete et posséder en particulier
   * les attributs identifiant et domaine correctement remplis.
   * 
   * @param entite à créer
   * @return le contact créé
   * @throws WsGestionDesContactsException si une erreur se produit lors de
   *                                       l'appel au Web Service
   */
  public Entite creerEntiteAvecIdentifiant(Entite entite) throws WsGestionDesContactsException {
    if (entite instanceof Contact) {
      return clientWsContact.create((Contact) entite);
    } else if (entite instanceof Organisation) {
      return clientWsOrganisation.create((Organisation) entite);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * Crée une entite avec les attributs donnés. L' attributs identifiant n'est pas
   * pris en compte. Le domaine doit être renseigné
   * 
   * @param entite à créer
   * @return le contact muni en particulier de son identifiant
   * @throws WsGestionDesContactsException si une erreur se produit lors de
   *                                       l'appel au Web Service
   */
  public Entite creerEntiteSansIdentifiant(Entite entite) throws WsGestionDesContactsException {
    if (entite instanceof Contact) {
      return clientWsContact.createWithoutIdentifiant((Contact) entite);
    } else if (entite instanceof Organisation) {
      return clientWsOrganisation.createWithoutIdentifiant((Organisation) entite);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  /* GESTION DES PROFILS */

  /**
   * Récupère la liste des profils définis sur le web service gestion des
   * contacts.
   * 
   * @return la liste des profils
   * @throws WsGestionDesContactsException si une erreur se produit lors de
   *                                       l'appel au Web Service
   */
  public List<Profil> getProfils() throws WsGestionDesContactsException {
    return clientWsProfil.getProfils();
  }

  /**
   * Récupère la liste des profils définis sur le web service gestion des
   * contacts.
   * 
   * @return la liste des profils
   * @throws WsGestionDesContactsException si une erreur se produit lors de
   *                                       l'appel au Web Service
   */
  public List<Groupe> getGroupesByDomaine(String domaine) throws WsGestionDesContactsException {
    return clientWsProfil.getGroupeByDomain(domaine);
  }

  /**
   * Récupère la configuration d'un profil à partir de son nom.
   * 
   * @param name le nom du profil
   * @return l'objet profil associé selon la réponse du WS
   * @throws WsGestionDesContactsException si une erreur se produit lors de
   *                                       l'appel au Web Service
   */
  public Profil getProfil(String name) throws WsGestionDesContactsException {
    return clientWsProfil.getProfil(name);
  }

  /**
   * Modifie le profil. Il sera créé si non existant.
   * 
   * @param profil à modifier ou créer
   * @throws WsGestionDesContactsException si une erreur se produit lors de
   *                                       l'appel au Web Service
   */
  public void creerOuMettreAjourProfil(Profil profil) throws WsGestionDesContactsException {
    clientWsProfil.setProfil(profil);
  }

  /**
   * Supprime le profil.
   * 
   * @param name nom du profil à supprimer
   * @throws WsGestionDesContactsException si une erreur se produit lors de
   *                                       l'appel au Web Service
   */
  public void deleteProfil(String name) throws WsGestionDesContactsException {
    clientWsProfil.deleteProfil(name);
  }

  public void addContactToGroup(String identifiant, String domaineDeGestion, String nomApplicationGroupe,
      String nomGroupe) throws WsGestionDesContactsException {
    clientWsHabilitations.addContactToGroup(identifiant, domaineDeGestion, nomApplicationGroupe, nomGroupe);
  }

  public void deleteContactFromGroup(String identifiant, String domaineDeGestion, String nomApplicationGroupe,
      String nomGroupe) throws WsGestionDesContactsException {
    clientWsHabilitations.deleteContactFromGroup(identifiant, domaineDeGestion, nomApplicationGroupe, nomGroupe);
  }

  public List<Groupe> getGroupesByIdAndDomaine(String identifiant, String domaine)
      throws WsGestionDesContactsException {
    return clientWsHabilitations.getGroupesByIdAndDomaine(identifiant, domaine);
  }

  public void addInseeRoleToContact(String identifiant, String domaineDeGestion, String inseeRole)
      throws WsGestionDesContactsException {
    clientWsHabilitations.addInseeRoleToContact(identifiant, domaineDeGestion, inseeRole);

  }

  public void deleteInseeRoleToContact(String identifiant, String domaineDeGestion, String inseeRole)
      throws WsGestionDesContactsException {
    clientWsHabilitations.deleteInseeRoleToContact(identifiant, domaineDeGestion, inseeRole);

  }

  public int getContactSize(String domaine) throws WsGestionDesContactsException {
    return clientWsContact.getSize(domaine);
  }

  public List<Contact> getContactbyDomaineAndGroupe(String domaine, Groupe groupe)
      throws WsGestionDesContactsException {
    return clientWsContact.getContactbyDomaineAndGroupe(domaine, groupe);
  }

}
