package fr.insee.sugoi.ldap.client.service.impl;

import static fr.insee.sugoi.ldap.client.exception.WsGestionDesContactsException.determinerErreur;
import java.util.List;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import fr.insee.sugoi.ldap.client.exception.WsGestionDesContactsException;
import fr.insee.sugoi.ldap.client.resources.TargetsWsGestionContacts;
import fr.insee.sugoi.converter.ouganext.Groupe;
import fr.insee.sugoi.converter.ouganext.Habilitations;

public class ClientWsHabilitations {

  private TargetsWsGestionContacts targets;

  private static final Logger LOG = LogManager.getFormatterLogger(ClientWsHabilitations.class);

  private static final Entity PAS_D_ENTITE = Entity.json("");

  public ClientWsHabilitations(TargetsWsGestionContacts targets) {
    this.targets = targets;
  }

  /**
   * Recherche les habilitations d'un contact d'après son identifiant. Si aucun
   * contact n'est trouvé, renvoie null. Les habilitations du contact peuvent être
   * vides.
   * 
   * @param idContact : identifiant du contact pour lequel on souhaite récupérer
   *                  les habilitations.
   * @return les habilitations du contact.
   * @throws WsGestionDesContactsException si erreur WS
   */
  public Habilitations getHabilitationsByIdAndDomaine(String idContact, String domaine)
      throws WsGestionDesContactsException {
    Habilitations habilitations = null;
    Response response = targets.getHabilitationsTarget(idContact, domaine).request(MediaType.APPLICATION_JSON).get();

    if (response.getStatus() == Status.OK.getStatusCode()) {
      LOG.debug("Habilitations du contact %s trouvées", idContact);
      habilitations = response.readEntity(Habilitations.class);
    } else if (response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
      LOG.debug("Habilitations du contact %s non trouvées", idContact);
    } else {
      LOG.error("Erreur lors de la recherche des habilitations du contact %s. Statut : %s", idContact,
          response.getStatus());
      throw new WsGestionDesContactsException(determinerErreur(response));
    }

    return habilitations;
  }

  /**
   * Ajout une habilitation de type role_application au contact idContact.
   * 
   * @param idContact   identifiant du contact concerné
   * @param application concernée
   * @param roles       liste des roles à rajouter
   * @throws WsGestionDesContactsException si l'ajout est impossible
   */
  public void addHabilitationsWithoutProperty(String idContact, String domaine, String application, List<String> roles)
      throws WsGestionDesContactsException {
    Response response = targets.getHabilitationsTarget(idContact, domaine).path(application)
        .queryParam("role", roles.toArray()).request(MediaType.APPLICATION_JSON).put(Entity.json(""));
    if (response.getStatus() == Status.NO_CONTENT.getStatusCode()) {
      LOG.debug("Déroulement normal");
    } else {
      throw new WsGestionDesContactsException(determinerErreur(response));
    }

  }

  /**
   * Supprime les habilitations du contact idContact.
   * 
   * @param idContact   identifiant du contact concerné
   * @param application concernée
   * @param roles       liste des roles à supprimer
   * @throws WsGestionDesContactsException si la suppression est impossible
   */
  public void deleteHabilitations(String idContact, String domaine, String application, List<String> roles)
      throws WsGestionDesContactsException {
    Response response = targets.getHabilitationsTarget(idContact, domaine).path(application)
        .queryParam("role", roles.toArray()).request(MediaType.APPLICATION_JSON).delete(Response.class);
    if (response.getStatus() == Status.NO_CONTENT.getStatusCode()) {
      LOG.debug("Déroulement normal");
    } else {
      throw new WsGestionDesContactsException(determinerErreur(response));
    }
  }

  /**
   * Ajoute une habilitation de type propriete_role_application au contact
   * idContact.
   * 
   * @param idContact   identifiant du contact concerné
   * @param application concernée
   * @param role        concerné
   * @param proprietes  liste des propriétés à ajouter
   * @throws WsGestionDesContactsException si ajout impossible
   */
  public void addHabilitationsWithProperty(String idContact, String domaine, String application, String role,
      List<String> proprietes) throws WsGestionDesContactsException {
    Response response = targets.getHabilitationsTarget(idContact, domaine).path(application).path(role)
        .queryParam("propriete", proprietes.toArray()).request(MediaType.APPLICATION_JSON).put(PAS_D_ENTITE);
    if (response.getStatus() == Status.NO_CONTENT.getStatusCode()) {
      LOG.debug("Déroulement normal");
    } else {
      throw new WsGestionDesContactsException(determinerErreur(response));
    }

  }

  /**
   * Supprime une habilitation de type propriete_role_application au contact
   * idContact.
   * 
   * @param idContact   identifiant du contact concerné
   * @param application concernée
   * @param role        concerné
   * @param proprietes  liste des propriétés à supprimer
   * @throws WsGestionDesContactsException si suppression impossible
   */
  public void deletePropertyofHabilitation(String idContact, String domaine, String application, String role,
      List<String> proprietes) throws WsGestionDesContactsException {
    Response response = targets.getHabilitationsTarget(idContact, domaine).path(application).path(role)
        .queryParam("propriete", proprietes.toArray()).request(MediaType.APPLICATION_JSON).delete();
    if (response.getStatus() == Status.NO_CONTENT.getStatusCode()) {
      LOG.debug("Déroulement normal");
    } else {
      throw new WsGestionDesContactsException(determinerErreur(response));
    }
  }

  public void addContactToGroup(String identifiant, String domaineDeGestion, String nomApplicationGroupe,
      String nomGroupe) throws WsGestionDesContactsException {
    Response response = targets.getGroupesTarget(identifiant, domaineDeGestion).path(nomApplicationGroupe)
        .path(nomGroupe).request(MediaType.APPLICATION_JSON).put(PAS_D_ENTITE);
    if (response.getStatus() == Status.NO_CONTENT.getStatusCode()) {
      LOG.debug("Déroulement normal");
    } else {
      throw new WsGestionDesContactsException(determinerErreur(response));
    }
  }

  public void deleteContactFromGroup(String identifiant, String domaineDeGestion, String nomApplicationGroupe,
      String nomGroupe) throws WsGestionDesContactsException {
    Response response = targets.getGroupesTarget(identifiant, domaineDeGestion).path(nomApplicationGroupe)
        .path(nomGroupe).request(MediaType.APPLICATION_JSON).delete();
    if (response.getStatus() == Status.NO_CONTENT.getStatusCode()) {
      LOG.debug("Déroulement normal");
    } else {
      throw new WsGestionDesContactsException(determinerErreur(response));
    }
  }

  public List<Groupe> getGroupesByIdAndDomaine(String identifiant, String domaine)
      throws WsGestionDesContactsException {
    Response response = targets.getGroupesTarget(identifiant, domaine).request(MediaType.APPLICATION_JSON).get();
    if (response.getStatus() == Status.OK.getStatusCode()) {
      LOG.debug("Déroulement normal");
      return response.readEntity(new GenericType<List<Groupe>>() {
      });
    } else {
      throw new WsGestionDesContactsException(determinerErreur(response));
    }
  }

  public void addInseeRoleToContact(String identifiant, String domaine, String inseeRole)
      throws WsGestionDesContactsException {
    Response response = targets.getInseeRoleTarget(identifiant, domaine).path(inseeRole)
        .request(MediaType.APPLICATION_JSON).put(PAS_D_ENTITE);
    if (response.getStatus() == Status.NO_CONTENT.getStatusCode()) {
      LOG.debug("Déroulement normal");
    } else {
      throw new WsGestionDesContactsException(determinerErreur(response));
    }
  }

  public void deleteInseeRoleToContact(String identifiant, String domaine, String inseeRole)
      throws WsGestionDesContactsException {
    Response response = targets.getInseeRoleTarget(identifiant, domaine).path(inseeRole)
        .request(MediaType.APPLICATION_JSON).delete();
    if (response.getStatus() == Status.NO_CONTENT.getStatusCode()) {
      LOG.debug("Déroulement normal");
    } else {
      throw new WsGestionDesContactsException(determinerErreur(response));
    }
  }

  public List<String> getInseeRolesByIdAndDomaine(String identifiant, String domaine)
      throws WsGestionDesContactsException {
    Response response = targets.getInseeRoleTarget(identifiant, domaine).request(MediaType.APPLICATION_JSON).get();
    if (response.getStatus() == Status.OK.getStatusCode()) {
      LOG.debug("Déroulement normal");
      return response.readEntity(new GenericType<List<String>>() {
      });
    } else {
      throw new WsGestionDesContactsException(determinerErreur(response));
    }
  }

}
