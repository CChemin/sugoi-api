package fr.insee.sugoi.ldap.client.typebean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import fr.insee.sugoi.converter.ouganext.Contact;
import fr.insee.sugoi.converter.ouganext.Namespace;
import fr.insee.sugoi.converter.ouganext.Organisation;

public class BeanTypeTest {

  private static final String LINK_REL_CONTACT = Namespace.ANNUAIRE + "/Contact";
  private static final String LINK_REL_ORGANISATION = Namespace.ANNUAIRE + "/Organisation";

  @Test
  public void testGetBeanTypeFromLinkRel_contact() {
    BeanType typeBean = BeanType.getBeanTypeFromLinkRel(LINK_REL_CONTACT);
    assertEquals(BeanType.CONTACT, typeBean, "Le type de bean recupere n'est pas bon ici.");
  }

  @Test
  public void testGetBeanTypeFromLinkRel_organisation() {
    BeanType typeBean = BeanType.getBeanTypeFromLinkRel(LINK_REL_ORGANISATION);
    assertEquals(BeanType.ORGANISATION, typeBean, "Le type de bean recupere n'est pas bon ici.");
  }

  @Test
  public void testGetBeanTypeFromLinkRel_aucun() {
    BeanType typeBean = BeanType.getBeanTypeFromLinkRel("");
    assertEquals(BeanType.AUCUN, typeBean, "Le type de bean recupere n'est pas bon ici.");
  }

  @Test
  public void testGetBeanTypeFromLinkRel_null() {
    BeanType typeBean = BeanType.getBeanTypeFromLinkRel(null);
    assertEquals(BeanType.AUCUN, typeBean, "Le type de bean recupere n'est pas bon ici.");
  }

  @Test
  public void testGetBeanClass_contact() {
    assertEquals(Contact.class, BeanType.CONTACT.getBeanClass(), "La classe du type de bean n'est pas bonne ici");
  }

  @Test
  public void testGetBeanClass_organisation() {
    assertEquals(Organisation.class, BeanType.ORGANISATION.getBeanClass(),
        "La classe du type de bean n'est pas bonne ici");
  }

  @Test
  public void testGetBeanClass_aucun() {
    assertNull(BeanType.AUCUN.getBeanClass(), "La classe du type de bean n'est pas bonne ici");
  }

}
