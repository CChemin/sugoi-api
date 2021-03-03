package fr.insee.sugoi.ldap.client.utils;

import java.util.List;
import fr.insee.sugoi.converter.ouganext.Entite;
import fr.insee.sugoi.converter.ouganext.TypeRecherche;

public class RechercheBuilder {

  Recherche recherche = new Recherche();

  public RechercheBuilder(Class<? extends Entite> classeRecherchee) {
    recherche.setClasseRecherchee(classeRecherchee);
  }

  public Recherche build() {
    return recherche;
  }

  public RechercheBuilder chercherDansleDomaine(String domaine) {
    recherche.setDomaine(domaine);
    return this;
  }

  public RechercheBuilder chercherIdentifiantLike(String identifiant) {
    recherche.setIdentifiant(identifiant);
    return this;
  }

  public RechercheBuilder chercherCertificatLike(String certificat) {
    recherche.setCertificat(certificat);
    return this;
  }

  public RechercheBuilder chercherNomCommunLike(String nomCommun) {
    recherche.setNomCommun(nomCommun);
    return this;
  }

  public RechercheBuilder chercherDescriptionLike(String description) {
    recherche.setDescription(description);
    return this;
  }

  public RechercheBuilder chercherOrganisationRattacheIdLike(String organisationId) {
    recherche.setOrganisationId(organisationId);
    return this;
  }

  public RechercheBuilder chercherMailExact(String mail) {
    recherche.setMail(mail);
    return this;
  }

  public RechercheBuilder chercherhabilitations(List<String> habilitations) {
    recherche.setHabilitations(habilitations);
    return this;
  }

  public RechercheBuilder chercherHabilitationApplication(String application) {
    recherche.setApplication(application);
    return this;
  }

  public RechercheBuilder chercherHabilitationRole(String role) {
    recherche.setRole(role);
    return this;
  }

  public RechercheBuilder chercherHabilitationPropriete(String propriete) {
    recherche.setPropriete(propriete);
    return this;
  }

  public RechercheBuilder rechercheDeType(TypeRecherche typeRecherche) {
    recherche.setTypeRecherche(typeRecherche);
    return this;
  }

}
