package fr.insee.sugoi.ldap.client.service.impl;

import static fr.insee.sugoi.ldap.client.exception.WsGestionDesContactsException.determinerErreur;
import java.util.List;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import fr.insee.sugoi.ldap.client.exception.WsGestionDesContactsException;
import fr.insee.sugoi.ldap.client.resources.TargetsWsGestionContacts;
import fr.insee.sugoi.converter.ouganext.Groupe;
import fr.insee.sugoi.converter.ouganext.Groupes;
import fr.insee.sugoi.converter.ouganext.Profil;
import fr.insee.sugoi.converter.ouganext.Profils;

public class ClientWsProfil {

  private TargetsWsGestionContacts targets;

  public ClientWsProfil(TargetsWsGestionContacts targets) {
    this.targets = targets;
  }

  /**
   * Récupère la liste des profils définis sur le web service gestion des
   * contacts.
   * 
   * @return la liste des profils
   * @throws WsGestionDesContactsException si erreur côté WS
   */
  public List<Profil> getProfils() throws WsGestionDesContactsException {
    Response response = targets.getProfilsTarget().request(MediaType.APPLICATION_JSON).get();
    if (response.getStatus() == Status.OK.getStatusCode()) {
      Profils listeProfils = response.readEntity(Profils.class);
      return listeProfils.getListe();
    } else {
      throw new WsGestionDesContactsException(determinerErreur(response));
    }
  }

  /**
   * Récupère la liste des groupes possibles d'un domaine
   * 
   * @param domaine
   * @return la liste des groupes
   * @throws WsGestionDesContactsException si erreur côté WS
   */
  public List<Groupe> getGroupeByDomain(String domaine) throws WsGestionDesContactsException {
    Response response = targets.getGroupesByDomaine(domaine).request(MediaType.APPLICATION_JSON).get();
    if (response.getStatus() == Status.OK.getStatusCode()) {
      Groupes groupesPossible = response.readEntity(Groupes.class);
      return groupesPossible.getListe();
    } else {
      throw new WsGestionDesContactsException(determinerErreur(response));
    }
  }

  /**
   * Récupère un profil par son nom.
   * 
   * @param name du profil recharché
   * @return le profil ou null si le profil est introuvable
   * @throws WsGestionDesContactsException si erreur côté WS
   */
  public Profil getProfil(String name) throws WsGestionDesContactsException {
    Response response = targets.getProfilTarget(name).request(MediaType.APPLICATION_JSON).get();
    if (response.getStatus() == Status.OK.getStatusCode()) {
      return response.readEntity(Profil.class);
    } else if (response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
      return null;
    } else {
      throw new WsGestionDesContactsException(determinerErreur(response));
    }

  }

  /**
   * Modifie le profil. Il sera créé si non existant.
   * 
   * @param profil à modifier ou créer
   * @throws WsGestionDesContactsException si problème
   */
  public void setProfil(Profil profil) throws WsGestionDesContactsException {
    Response response = targets.getProfilTarget().request().put(Entity.entity(profil, MediaType.APPLICATION_JSON));
    if (!((response.getStatus() == Status.NO_CONTENT.getStatusCode())
        || (response.getStatus() == Status.CREATED.getStatusCode()))) {
      throw new WsGestionDesContactsException(determinerErreur(response));
    }
  }

  /**
   * Supprime le profil.
   * 
   * @param name nom du profil à supprimer
   * @throws WsGestionDesContactsException si problème
   */
  public void deleteProfil(String name) throws WsGestionDesContactsException {
    Response response = targets.getProfilTarget(name).request().delete();
    if (response.getStatus() != Status.OK.getStatusCode()) {
      throw new WsGestionDesContactsException(determinerErreur(response));
    }

  }

}
