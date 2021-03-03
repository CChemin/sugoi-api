package fr.insee.sugoi.ldap.client.service.impl;

import static fr.insee.sugoi.ldap.client.exception.WsGestionDesContactsException.determinerErreur;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import fr.insee.sugoi.ldap.client.exception.WsGestionDesContactsException;
import fr.insee.sugoi.ldap.client.resources.TargetsWsGestionContacts;
import fr.insee.sugoi.ldap.client.utils.Recherche;
import fr.insee.sugoi.ldap.client.utils.ResultatsPartiels;
import fr.insee.sugoi.converter.ouganext.Entite;
import fr.insee.sugoi.converter.ouganext.Entites;

public abstract class ClientWsEntite<T extends Entite> {

  protected TargetsWsGestionContacts targets;

  protected static final int NOMBRE_MAXIMAL_DE_REPONSE = 50;
  private static final Logger LOG = LogManager.getFormatterLogger(ClientWsEntite.class);

  /**
   * Recherche d'une entité par identifiant.
   * 
   * @param id identifiant de recherche
   * @return l'entité trouvé ou null si aucune entité n'a cette identifiant
   * @throws WsGestionDesContactsException si une erreur se produit côté WS
   */
  public T getById(String id) throws WsGestionDesContactsException {
    T entiteRecherchee = null;
    Response response = getTargetUnique(id).request(MediaType.APPLICATION_JSON).get();
    if (response.getStatus() == Status.OK.getStatusCode()) {
      LOG.debug("%s %s trouvé", getNomLog(), id);
      entiteRecherchee = response.readEntity(getClasse());
    } else if (response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
      LOG.debug("%s %s non trouvé(e)", getNomLog(), id);
    } else {
      LOG.error("Erreur lors de la recherche de %s %s. Statut : %s", getNomLog(), id, response.getStatus());
      throw new WsGestionDesContactsException(determinerErreur(response));
    }
    return entiteRecherchee;
  }

  /**
   * Recherche d'une entité par identifiant et domaine.
   * 
   * @param id identifiant de recherche
   * @return l'entité trouvé ou null si aucune entité n'a cette identifiant
   * @throws WsGestionDesContactsException si une erreur se produit côté WS
   */
  public T getByIdAndDomaine(String id, String domaine) throws WsGestionDesContactsException {
    T entiteRecherchee = null;
    Response response = getTargetUnique(id, domaine).request(MediaType.APPLICATION_JSON).get();
    if (response.getStatus() == Status.OK.getStatusCode()) {
      LOG.debug("%s %s trouvé", getNomLog(), id);
      entiteRecherchee = response.readEntity(getClasse());
    } else if (response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
      LOG.debug("%s %s non trouvé(e)", getNomLog(), id);
    } else {
      LOG.error("Erreur lors de la recherche de %s %s. Statut : %s", getNomLog(), id, response.getStatus());
      throw new WsGestionDesContactsException(determinerErreur(response));
    }
    return entiteRecherchee;
  }

  /**
   * Modifie l'entité.
   * 
   * @param entiteModifiee l'entité dans son état modifié
   * @return l'entité modifiée
   * @throws WsGestionDesContactsException si la modification ne s'est pas faite
   *                                       côté WS
   */
  public T update(T entiteModifiee) throws WsGestionDesContactsException {
    T entiteRecherchee = null;
    Response response = getTargetUnique(entiteModifiee.getIdentifiant(), entiteModifiee.getDomaineDeGestion())
        .queryParam("domaine", entiteModifiee.getDomaineDeGestion()).request(MediaType.APPLICATION_JSON)
        .put(Entity.entity(entiteModifiee, MediaType.APPLICATION_JSON));
    if (response.getStatus() == Status.OK.getStatusCode()) {
      LOG.debug("Modification du %s %s reussie", getNomLog(), entiteModifiee.getIdentifiant());
      entiteRecherchee = response.readEntity(getClasse());
    } else {
      LOG.error("Modification du %s %s en echec, erreur %s", getNomLog(), entiteModifiee.getIdentifiant(),
          "" + response.getStatus());
      throw new WsGestionDesContactsException(determinerErreur(response));
    }
    return entiteRecherchee;
  }

  /**
   * Crée une entité. l'entité doit avoir un identifiant et un domaine de gestion
   * valable.
   * 
   * @param entiteAcreer l'entité a creer
   * @return l'entité modifiée
   * @throws WsGestionDesContactsException si la creation ne s'est pas faite côté
   *                                       WS (doublon par exemple)
   */
  public T create(T entiteAcreer) throws WsGestionDesContactsException {
    T entiteRecherchee = null;
    Response response = getTargetUnique(entiteAcreer.getIdentifiant(), entiteAcreer.getDomaineDeGestion())
        .queryParam("domaine", entiteAcreer.getDomaineDeGestion()).queryParam("creation", "true")
        .request(MediaType.APPLICATION_JSON).put(Entity.entity(entiteAcreer, MediaType.APPLICATION_JSON));
    if (response.getStatus() == Status.OK.getStatusCode()) {
      LOG.debug("Modification du %s %s reussie", getNomLog(), entiteAcreer.getIdentifiant());
      entiteRecherchee = response.readEntity(getClasse());
    } else {
      LOG.error("Modification du %s %s en echec, erreur %s", getNomLog(), entiteAcreer.getIdentifiant(),
          response.getStatus());
      throw new WsGestionDesContactsException(determinerErreur(response));
    }
    return entiteRecherchee;
  }

  /**
   * Création de l'entité sur le domaine en laissant le système choisir un
   * identifiant.
   * 
   * @param entiteAcreer l'entité à créer
   * @return l'entité créée avec potentiellement un identifiant différent de celui
   *         proposé si doublon
   * @throws WsGestionDesContactsException si la création ne s'est pas faite cote
   *                                       WS
   */
  public T createWithoutIdentifiant(T entiteAcreer) throws WsGestionDesContactsException {
    T entiteRecherchee = null;
    Response response;
    response = getTargetMultiple(entiteAcreer.getDomaineDeGestion()).request(MediaType.APPLICATION_JSON)
        .post(Entity.entity(entiteAcreer, MediaType.APPLICATION_JSON));

    if (response.getStatus() == Status.CREATED.getStatusCode()) {

      // Récupération de l'entité créée
      entiteRecherchee = targets.getClient().target(response.getLocation()).request(MediaType.APPLICATION_JSON)
          .get(getClasse());
      LOG.debug("Creation du %s %s reussie", getNomLog(), entiteRecherchee.getIdentifiant());

    } else {
      LOG.error("Creation du %s en echec, erreur %s", getNomLog(), response.getStatus());
      throw new WsGestionDesContactsException(determinerErreur(response));
    }
    return entiteRecherchee;
  }

  /**
   * Recherche des entités selon les attributs.
   * 
   * @param size      la taille des résultats souhaités
   * @param start     l'index du premier résultats de la recherche (recherche VLV)
   * @param recherche les attributs de recherche
   * @return un objet {@link ResultatsPartiels} contenant les résultats
   *         potentiellement partiels selon la taille des résultats et l'attribut
   *         size
   * @throws WsGestionDesContactsException si une erreur survient coté WS
   */
  public ResultatsPartiels<T> rechercheEntites(int size, int start, Recherche recherche)
      throws WsGestionDesContactsException {
    // body=true Pour obtenir les résultats dans le corps de la réponse
    Response response = prepareQueryParameters(recherche, getTargetMultiple(recherche.getDomaine()))
        .queryParam("body", "true").queryParam("size", size).queryParam("start", start)
        .request(MediaType.APPLICATION_JSON).get();

    int status = response.getStatus();
    ResultatsPartiels<T> resultatsPartiels = new ResultatsPartiels<>();
    if (status == Status.OK.getStatusCode()) {
      Entites<T> listeEntites = recupererListeEntites(response);
      resultatsPartiels.setListeResultats(listeEntites.getListe());
      resultatsPartiels.setNombreResultatsTotaux(Integer.parseInt(response.getHeaderString("X-Total-Size")));
      String nextLocation = response.getHeaderString("nextLocation");
      if (nextLocation != null) {
        resultatsPartiels.setNextlocation(URI.create(nextLocation));
      }
      if (start > 1) {
        resultatsPartiels.setFirstIndexPartiel(start);
      } else {
        resultatsPartiels.setFirstIndexPartiel(1);
      }

    } else if (status == Status.NOT_FOUND.getStatusCode()) {
      LOG.debug("Aucun résultat trouvé à  la recherche " + recherche);
    } else {
      throw new WsGestionDesContactsException(determinerErreur(response));
    }

    return resultatsPartiels;
  }

  /**
   * Recherche les résultats suivants si la réponse est partielle.
   * 
   * @param resultatsPartiels l'objet {@link ResultatsPartiels} à actualiser
   * @return l'objet {@link ResultatsPartiels} actualisé (pour un éventuel
   *         chainage)
   * @throws WsGestionDesContactsException si une erreur survient côté WS
   */
  public ResultatsPartiels<T> nextRechercheEntites(ResultatsPartiels<T> resultatsPartiels)
      throws WsGestionDesContactsException {

    Response response = targets.getClient().target(resultatsPartiels.getNextlocation())
        .request(MediaType.APPLICATION_JSON).get();

    int status = response.getStatus();
    if (status == Status.OK.getStatusCode()) {
      Entites<T> listeContacts = recupererListeEntites(response);
      resultatsPartiels.setListeResultats(listeContacts.getListe());
      resultatsPartiels.setNombreResultatsTotaux(Integer.parseInt(response.getHeaderString("X-Total-Size")));
      String nextLocation = response.getHeaderString("nextLocation");
      if (nextLocation != null) {
        resultatsPartiels.setNextlocation(URI.create(nextLocation));
      } else {
        resultatsPartiels.setNextlocation(null);
      }
      // TODO : changer start (first element)

    } else {
      // Pas normal, il y aurait du y avoir un resultat suivant
      // on arrete le processus
      resultatsPartiels.setNextlocation(null);
      throw new WsGestionDesContactsException(determinerErreur(response));
    }

    return resultatsPartiels;
  }

  /**
   * Recherche uniquement et totalement les identifiants corrspondant aux
   * attributs de recherches.
   * 
   * @param recherche pour spécifier la recherche
   * @return la liste complète des identifiants
   * @throws WsGestionDesContactsException si erreur coté WS
   */
  public List<String> rechercheIdentifiants(Recherche recherche) throws WsGestionDesContactsException {
    Response response = prepareQueryParameters(recherche, getTargetMultiple(recherche.getDomaine()))
        .queryParam("idOnly", "true").queryParam("body", "true").queryParam("size", "1000")
        .request(MediaType.APPLICATION_JSON).get();

    List<String> stringResult = new ArrayList<String>();

    if (response.getStatus() == Status.OK.getStatusCode()) {
      LOG.debug("Recherche d'identifiants de %s effectuée avec resultats", getNomLog());
      // Récupération potentielle du nextLocation
      String nextLocation;

      do {
        Entites<T> listeIdEntites = recupererListeEntites(response);
        nextLocation = response.getHeaderString("nextLocation");

        if (!listeIdEntites.getListe().isEmpty()) {
          stringResult.addAll(
              listeIdEntites.getListe().stream().map(entite -> entite.getIdentifiant()).collect(Collectors.toList()));
        }

        if (nextLocation != null) {
          response.close();
          response = targets.getClient().target(nextLocation).request(MediaType.APPLICATION_JSON).get();
        }
      } while (nextLocation != null && response.getStatus() == Status.NO_CONTENT.getStatusCode());

    } else if (response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
      LOG.debug("Recherche d'identifiants de %s effectuée : Aucun resultat", getNomLog());
    } else {
      LOG.error("Recherche d'identifiants de %s en echec, erreur %s", getNomLog(), response.getStatus());
      throw new WsGestionDesContactsException(determinerErreur(response));
    }

    return stringResult;
  }

  /**
   * Supprime l'entité désigné par l'identifiant.
   * 
   * @param id identifiant de l'entité à supprimer
   * @throws WsGestionDesContactsException si une erreur survient côté WS
   */
  public void deleteByIdAndDomaine(String id, String domaine) throws WsGestionDesContactsException {
    Response response = getTargetUnique(id, domaine).request().delete();
    if (response.getStatus() == Status.NO_CONTENT.getStatusCode()) {
      LOG.debug("Suppression de %s %s reussie", getNomLog(), id);
    } else {
      LOG.error("Suppression de %s %s en echec. Erreur %s", getNomLog(), id, response.getStatus());
      throw new WsGestionDesContactsException(determinerErreur(response));
    }
  }

  protected abstract Class<T> getClasse();

  protected abstract WebTarget getTargetUnique(String id);

  protected abstract WebTarget getTargetUnique(String id, String domaine);

  protected abstract WebTarget getTargetMultiple(String domaine);

  protected abstract Entites<T> recupererListeEntites(Response response);

  protected abstract String getNomLog();

  protected WebTarget prepareQueryParameters(Recherche recherche, WebTarget target) {
    target = target.queryParam("typeRecherche", recherche.getTypeRecherche().getTypeRecherche());
    target = ajouterIdentifiantSiDemande(recherche, target);
    target = ajouterNomCommunSiDemande(recherche, target);
    target = ajouterDescriptionSiDemande(recherche, target);
    target = ajoutermailSiDemande(recherche, target);
    target = ajouterOrganisationIdSiDemande(recherche, target);
    target = ajouterhabilitationsSiDemande(recherche, target);
    target = ajouterApplicationSiDemande(recherche, target);
    target = ajouterRoleSiDemande(recherche, target);
    target = ajouterProprieteSiDemande(recherche, target);
    target = ajouterCertificatDemande(recherche, target);
    return target;
  }

  private WebTarget ajouterCertificatDemande(Recherche recherche, WebTarget target) {
    if (recherche.getCertificat() != null && recherche.getCertificat().length() > 0) {
      target = target.queryParam("certificat", recherche.getCertificat());
    }
    return target;
  }

  private WebTarget ajouterProprieteSiDemande(Recherche recherche, WebTarget target) {
    if (recherche.getPropriete() != null && recherche.getPropriete().length() > 0) {
      target = target.queryParam("propriete", recherche.getPropriete());
    }
    return target;
  }

  private WebTarget ajouterRoleSiDemande(Recherche recherche, WebTarget target) {
    if (recherche.getRole() != null && recherche.getRole().length() > 0) {
      target = target.queryParam("role", recherche.getRole());
    }
    return target;
  }

  private WebTarget ajouterApplicationSiDemande(Recherche recherche, WebTarget target) {
    if (recherche.getApplication() != null && recherche.getApplication().length() > 0) {
      target = target.queryParam("application", recherche.getApplication());
    }
    return target;
  }

  private WebTarget ajouterhabilitationsSiDemande(Recherche recherche, WebTarget target) {
    if (recherche.getHabilitations() != null && recherche.getHabilitations().size() > 0) {
      target = target.queryParam("habilitation", recherche.getHabilitations().toArray());
    }
    return target;
  }

  private WebTarget ajouterOrganisationIdSiDemande(Recherche recherche, WebTarget target) {
    if (recherche.getOrganisationId() != null && recherche.getOrganisationId().length() > 0) {
      target = target.queryParam("organisationId", recherche.getOrganisationId());
    }
    return target;
  }

  private WebTarget ajoutermailSiDemande(Recherche recherche, WebTarget target) {
    if (recherche.getMail() != null && recherche.getMail().length() > 0) {
      target = target.queryParam("mail", recherche.getMail());
    }
    return target;
  }

  private WebTarget ajouterDescriptionSiDemande(Recherche recherche, WebTarget target) {
    if (recherche.getDescription() != null && recherche.getDescription().length() > 0) {
      target = target.queryParam("description", recherche.getDescription());
    }
    return target;
  }

  private WebTarget ajouterNomCommunSiDemande(Recherche recherche, WebTarget target) {
    if (recherche.getNomCommun() != null && recherche.getNomCommun().length() > 0) {
      target = target.queryParam("nomCommun", recherche.getNomCommun());
    }
    return target;
  }

  private WebTarget ajouterIdentifiantSiDemande(Recherche recherche, WebTarget target) {
    if (recherche.getIdentifiant() != null && recherche.getIdentifiant().length() > 0) {
      target = target.queryParam("identifiant", recherche.getIdentifiant());
    }
    return target;
  }

}
