package fr.insee.sugoi.ldap.client.typebean;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import fr.insee.sugoi.converter.ouganext.Contact;
import fr.insee.sugoi.converter.ouganext.Organisation;

/**
 * Enumeration permettant d'englober les classes constituant les beans du
 * web-service gestion des contacts.
 */
public enum BeanType {

  CONTACT(Contact.class), ORGANISATION(Organisation.class), AUCUN(null);

  private Class<?> beanClass;
  private String namespace;
  private String name;

  private BeanType(Class<?> beanClass) {
    initNameAndNamespace(beanClass);
    this.beanClass = beanClass;
  }

  /**
   * Methode d'initialisation. Permet de renseigner nom et espace de nom du
   * {@link BeanType} avec les donnees de la classe a encapsuler.
   * 
   * @param beanClass La classe du bean a encapsuler.
   */
  private void initNameAndNamespace(Class<?> beanClass) {
    if (beanClass != null) {
      if (!beanClass.isAnnotationPresent(JacksonXmlRootElement.class)) {
        throw new IllegalArgumentException("Pour etre initialise, un BeanType necessite d'encapsuler "
            + "une classe comportant l'annotation XmlRootElement.");
      }
      JacksonXmlRootElement xmlRoot = beanClass.getAnnotation(JacksonXmlRootElement.class);
      namespace = xmlRoot.namespace();
      name = xmlRoot.localName();
      return;
    }
    initNameAndNamespaceDefaultBean();
  }

  /**
   * Methode d'initialisation pour le TypeBean par defaut ({@link #AUCUN}).
   */
  private void initNameAndNamespaceDefaultBean() {
    namespace = "";
    name = "";
  }

  /**
   * Methode centrale de cette enumeration. Permet d'obtenir un {@link BeanType} a
   * partir d'un lien recupere par une reponse HTTP.
   * 
   * @param linkRel Le lien recupere au sein de la requete.
   * @return Le TypeBean associe a ce lien.
   */
  public static BeanType getBeanTypeFromLinkRel(String linkRel) {
    if (linkRel != null) {
      for (BeanType t : values()) {
        String beanLinkRel = t.getLinkRel();
        if (beanLinkRel.equals(linkRel)) {
          return t;
        }
      }
    }
    return AUCUN;
  }

  /**
   * Methode utilitaire permettant de construire le rel d'un link associe a ce
   * type de bean.
   * 
   * @return Le rel de ce type de bean.
   */
  private String getLinkRel() {
    return namespace + "/" + name;
  }

  /**
   * Getter de la classe de Bean englobee par ce {@link BeanType}.
   * 
   * @return La classe englobee
   */
  public Class<?> getBeanClass() {
    return beanClass;
  }

}
