package fr.insee.sugoi.ldap.client.utils;

import java.security.SecureRandom;
import java.util.List;
import fr.insee.sugoi.converter.ouganext.Entite;
import fr.insee.sugoi.converter.ouganext.TypeRecherche;

public class Recherche {
  // TODO ajouter tyoerecherche

  // identifiant de l'entité recherchée
  //
  // organisationId de l'organisation de rattachement de l'entité
  // recherchée
  //
  // nomCommun de l'entité recherchée description de l'entité recherchée
  //
  // mail de l'entité
  // recherchée domaine de l'entité recherchée
  //
  // habilitations liste des habilitations de l'entité
  // recherchée
  //
  // application d'une habilitation de l'entité recherchée
  //
  // role d'une habilitation de
  // l'entité recherchée propriete d'une habilitation de l'entité recherchée
  //
  // typeRecherche OU ou ET

  private SecureRandom alea;
  private Class<? extends Entite> classeRecherchee;
  private String domaine;
  private String identifiant;
  private String nomCommun;
  private String description;
  private String organisationId;
  private String mail;
  private List<String> habilitations;
  private String application;
  private String role;
  private String propriete;
  private String certificat;
  private TypeRecherche typeRecherche = TypeRecherche.OU;

  public Recherche() {
    this.alea = new SecureRandom();
  }

  @Override
  public int hashCode() {
    String concatenation = alea + identifiant + organisationId + nomCommun + description + mail + domaine
        + habilitations + application + role + propriete;
    return concatenation.hashCode();
  }

  public String getIdentifiant() {
    return identifiant;
  }

  public String getOrganisationId() {
    return organisationId;
  }

  public String getNomCommun() {
    return nomCommun;
  }

  public String getDescription() {
    return description;
  }

  public String getMail() {
    return mail;
  }

  public String getDomaine() {
    return domaine;
  }

  public List<String> getHabilitations() {
    return habilitations;
  }

  public String getApplication() {
    return application;
  }

  public String getRole() {
    return role;
  }

  public String getPropriete() {
    return propriete;
  }

  public TypeRecherche getTypeRecherche() {
    return typeRecherche;
  }

  @Override
  public String toString() {
    return "Recherche [alea=" + alea + ", classeRecherchee=" + classeRecherchee + ", domaine=" + domaine
        + ", identifiant=" + identifiant + ", nomCommun=" + nomCommun + ", description=" + description
        + ", organisationId=" + organisationId + ", mail=" + mail + ", habilitations=" + habilitations
        + ", application=" + application + ", role=" + role + ", propriete=" + propriete + ", certificat=" + certificat
        + ", typeRecherche=" + typeRecherche + "]";
  }

  void setDomaine(String domaine) {
    this.domaine = domaine;
  }

  void setIdentifiant(String identifiant) {
    this.identifiant = identifiant;
  }

  void setNomCommun(String nomCommun) {
    this.nomCommun = nomCommun;
  }

  void setDescription(String description) {
    this.description = description;
  }

  void setOrganisationId(String organisationId) {
    this.organisationId = organisationId;
  }

  void setMail(String mail) {
    this.mail = mail;
  }

  void setHabilitations(List<String> habilitations) {
    this.habilitations = habilitations;
  }

  void setApplication(String application) {
    this.application = application;
  }

  void setRole(String role) {
    this.role = role;
  }

  void setPropriete(String propriete) {
    this.propriete = propriete;
  }

  void setTypeRecherche(TypeRecherche typeRecherche) {
    this.typeRecherche = typeRecherche;
  }

  public Class<? extends Entite> getClasseRecherchee() {
    return classeRecherchee;
  }

  public void setClasseRecherchee(Class<? extends Entite> classeRecherchee) {
    this.classeRecherchee = classeRecherchee;
  }

  public String getCertificat() {
    return certificat;
  }

  public void setCertificat(String certificat) {
    this.certificat = certificat;
  }

}
