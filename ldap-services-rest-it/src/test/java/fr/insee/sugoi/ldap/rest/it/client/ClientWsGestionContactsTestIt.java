package fr.insee.sugoi.ldap.rest.it.client;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import fr.insee.sugoi.ldap.LdapServiceTestServer;
import fr.insee.sugoi.ldap.client.exception.WsGestionDesContactsException;
import fr.insee.sugoi.ldap.client.service.impl.ClientWsGestionContacts;
import fr.insee.sugoi.ldap.client.service.impl.ClientWsGestionContactsBuilder;
import fr.insee.sugoi.ldap.client.utils.ModeEnvoi;
import fr.insee.sugoi.converter.ouganext.Contact;
import fr.insee.sugoi.converter.ouganext.Habilitations;
import fr.insee.sugoi.converter.ouganext.Organisation;
import fr.insee.sugoi.converter.ouganext.PasswordChangeRequest;

public class ClientWsGestionContactsTestIt {

  private static ClientWsGestionContacts clientTest;

  /**
   * Préparation du client.
   */
  @BeforeAll
  public static void setUpBeforeClass() throws Exception {
    // Gestion du truststore
    File truststoreFile = new File(URLDecoder
        .decode(LdapServiceTestServer.class.getResource("/ssl/cacerts.jks").getFile(), "UTF-8"));
    System.setProperty("javax.net.ssl.trustStore", truststoreFile.getAbsolutePath());
    System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
    System.setProperty("javax.net.ssl.trustStoreType", "JKS");

    clientTest = new ClientWsGestionContactsBuilder()
        .connectToUrl(
            "https://localhost:" + LdapServiceTestServer.TOMCAT_PORT_HTTPS + "/api/annuaire")
        .connectWithUsername("webservicesldap").connectWithPassword("webservices")
        .runInModeDebug(true).build();

    try {
      // suppression en cas d'existence
      clientTest.supprimerContact("A59WVVN", "domaine1");
      clientTest.supprimerContact("A59WVVN-bis", "domaine1");
    } catch (WsGestionDesContactsException e) {
      // il n'existait pas encore
    }
    try {
      // suppression en cas d'existence
      clientTest.supprimerOrganisation("S9RFU3N", "domaine1");
    } catch (WsGestionDesContactsException e) {
      // il n'existait pas encore
    }

    Contact contact = new Contact();
    contact.setIdentifiant("A59WVVN");
    contact.setDomaineDeGestion("domaine1");
    contact.setAdresseMessagerie("pouet@non.existent.domain");
    clientTest.creerEntiteAvecIdentifiant(contact);

    PasswordChangeRequest pcr = new PasswordChangeRequest();
    pcr.setNouveauMotDePasse("ABC4%2SF");
    clientTest.initPassword("A59WVVN", "domaine1", pcr);

    List<String> roles = new ArrayList<>();
    roles.add("role");
    List<String> proprietes = new ArrayList<>();
    proprietes.add("propriete");
    clientTest.addHabilitationsWithoutProperty("A59WVVN", "domaine1", "application", roles);
    clientTest.addHabilitationsWithProperty("A59WVVN", "domaine1", "application", "role",
        proprietes);

    Contact contactBis = new Contact();
    contactBis.setIdentifiant("A59WVVN-bis");
    contactBis.setDomaineDeGestion("domaine1");
    contactBis.setAdresseMessagerie("pouet@non.existent.domain");
    clientTest.creerEntiteAvecIdentifiant(contactBis);

    PasswordChangeRequest pcrBis = new PasswordChangeRequest();
    pcrBis.setNouveauMotDePasse("ABC4%2SF");
    clientTest.initPassword("A59WVVN-bis", "domaine1", pcrBis);

    Organisation orga = new Organisation();
    orga.setIdentifiant("S9RFU3N");
    orga.setDomaineDeGestion("domaine1");
    clientTest.creerEntiteAvecIdentifiant(orga);
  }

  @Test
  public void authentifierContactCasMdpOk() throws WsGestionDesContactsException {
    boolean contactAuthenticated =
        clientTest.authentifierContact("A59WVVN", "domaine1", "ABC4%2SF");
    assertTrue(contactAuthenticated, "Les informations d'authentification devraient etre valides");
  }

  @Test
  public void authentifierContactCasMdpPasOk() throws WsGestionDesContactsException {
    boolean contactAuthenticated =
        clientTest.authentifierContact("A59WVVN", "domaine1", "Test12345");
    assertFalse(contactAuthenticated,
        "Les informations d'authentification ne devraient pas etre valides");
  }

  @Test
  public void authentifierContactCasContactInexistant() throws WsGestionDesContactsException {
    boolean contactAuthenticated =
        clientTest.authentifierContact("NIMPORTEQUOI", "domaine1", "Test?234");
    assertFalse(contactAuthenticated,
        "Les informations d'authentification ne devraient pas etre valides");
  }

  @Test
  public void recupererHabilitationsCasContactExistantAvecHabilitations()
      throws WsGestionDesContactsException {
    Habilitations habs = clientTest.getHabilitationsByIdAndDomaine("A59WVVN", "domaine1");
    assertNotNull(habs, "Le contact devrait avoir des habilitations non null");
  }

  @Test
  public void recupererHabilitationsCasContactExistantSansHabilitations()
      throws WsGestionDesContactsException {
    Habilitations habs = clientTest.getHabilitationsByIdAndDomaine("A59WVVN", "domaine1");
    assertNotNull(habs, "Le contact devrait avoir des habilitations non null");
  }

  @Test
  public void recupererHabilitationsCasContactInexistant() throws WsGestionDesContactsException {
    Habilitations habs = clientTest.getHabilitationsByIdAndDomaine("BIDULETRUC", "domaine1");
    assertNull(habs, "Le contact devrait avoir des habilitations null");
  }

  @Test
  public void getContactByIdCasContactExistant() throws WsGestionDesContactsException {
    Contact contact = clientTest.getContact("A59WVVN");
    assertNotNull(contact, "Le contact ne devrait pas être null");
  }

  @Test
  public void getContactByIdCasContactInexistant() throws WsGestionDesContactsException {
    Contact contact = clientTest.getContact("BIDULETRUC");
    assertNull(contact, "Le contact devrait être null");
  }

  // GET Organisation par id. Organisation pas existante
  @Test
  public void getOrganisationByIdCasOrganisationPasExistante()
      throws WsGestionDesContactsException {
    Organisation organisation = clientTest.getOrganisation("toto");
    assertNull(organisation, "L'organisation ne devrait pas être null");
  }

  // GET Organisation par id. Organisation existante
  @Test
  public void getOrganisationByIdCasOrganisationExistante() throws WsGestionDesContactsException {
    Organisation organisation = clientTest.getOrganisation("S9RFU3N");
    assertNotNull(organisation, "L'organisation ne devrait pas être null");
  }

  // PUT Contact par par id et domaine. Contact pas existant
  @Test
  public void putContactByIdAndDomaineCasContactPasExistant() {
    Contact contactAModif = new Contact();
    contactAModif.setIdentifiant("BIDULETRUC");
    contactAModif.setDomaineDeGestion("domaine1");
    contactAModif.setDescription("yololololo");
    try {
      clientTest.mettreAjour(contactAModif);
      fail("Une WsGestionDesContactsException aurait du etre levée");
    } catch (WsGestionDesContactsException e) {
      // Cas normal : une exception a été levée
    }
  }

  // PUT Contact par par id et domaine. Contact existant
  @Test
  public void putContactByIdAndDomaineCasContactExistant() throws WsGestionDesContactsException {
    Contact contactAModif = clientTest.getContact("A59WVVN");
    contactAModif.setNomCommun("azazazaz");

    Contact contact = (Contact) clientTest.mettreAjour(contactAModif);
    assertNotNull(contact, "Le contact ne devrait pas être null");
  }

  // PUT Organisation par id et domaine. Organisation pas existante
  @Test
  public void putOrganisationByIdAndDomaineCasOrganisationPasExistante() {
    Organisation organisationAModif = new Organisation();
    organisationAModif.setIdentifiant("BIDULETRUC");
    organisationAModif.setDomaineDeGestion("tst");
    organisationAModif.setDescription("bonjour");
    try {
      clientTest.mettreAjour(organisationAModif);
      fail("Une WsGestionDesContactsException aurait du etre levee");
    } catch (WsGestionDesContactsException e) {
      // Normal
    }
  }

  // PUT Organisation par id et domaine. Organisation existante
  @Test
  public void putOrganisationByIdAndDomaineCasOrganisationExistante()
      throws WsGestionDesContactsException {
    Organisation organisationAModif = clientTest.getOrganisation("S9RFU3N");
    organisationAModif.setDescription("bonjour");
    Organisation organisation = (Organisation) clientTest.mettreAjour(organisationAModif);
    assertNotNull(organisation, "L'organisation ne devrait pas être null");
  }

  // POST Contact par domaine. Domaine existant
  @Test
  public void postAndDeleteContactByDomaineCasDomaineExistant()
      throws WsGestionDesContactsException {
    Contact contactACreer = new Contact();
    contactACreer.setNomCommun("Superman");
    contactACreer.setDomaineDeGestion("domaine1");
    contactACreer.setDescription("azazazaz");

    Contact contact = (Contact) clientTest.creerEntiteSansIdentifiant(contactACreer);
    assertNotNull(contact, "Le contact ne devrait pas être null");
    try {
      clientTest.supprimerContact(contact.getIdentifiant(), "domaine1");
    } catch (WsGestionDesContactsException e) {
      fail(e.getMessage(), e);
    }
  }

  // POST Organisation par domaine. Domaine existant
  @Test
  public void postAndDeleteOrganisationByDomaineCasDomaineExistant()
      throws WsGestionDesContactsException {
    Organisation organisationACreer = new Organisation();
    organisationACreer.setNomCommun("Organisation !!!!!");
    organisationACreer.setDomaineDeGestion("domaine1");
    organisationACreer.setDescription("Une organisation :)");
    organisationACreer.setAdresseMessagerie("organisation@free.fr");

    Organisation organisation =
        (Organisation) clientTest.creerEntiteSansIdentifiant(organisationACreer);
    assertNotNull(organisation, "L'organisation ne devrait pas être null");

    try {
      clientTest.supprimerOrganisation(organisation.getIdentifiant(), "domaine1");
    } catch (WsGestionDesContactsException e) {
      fail(e.getMessage(), e);
    }
  }

  // DELETE Organisation par id. Organisation existante
  @Test
  public void createDeleteOrganisationById() throws WsGestionDesContactsException {
    Organisation orga = new Organisation();
    orga.setIdentifiant("K98HUUK");
    orga.setDomaineDeGestion("domaine1");
    assertNotNull(clientTest.creerEntiteAvecIdentifiant(orga));

    try {
      clientTest.supprimerOrganisation("K98HUUK", "domaine1");
    } catch (WsGestionDesContactsException e) {
      fail("L'organisation n'a pas été supprimée");
    }
  }

  // CREATION Contact avec id fourni et domaine. Domaine existant

  @Test
  public void createAndDeleteContactByIdAndDomaineCasDomaineExistant() {
    Contact contactACreer = new Contact();
    contactACreer.setIdentifiant("KH9GX9N");
    contactACreer.setNomCommun("TESTO");
    contactACreer.setDomaineDeGestion("domaine1");
    contactACreer.setDescription("Descriptionness !");

    Contact contact;
    try {
      contact = (Contact) clientTest.creerEntiteAvecIdentifiant(contactACreer);
      assertNotNull(contact, "Le contact ne devrait pas être null");
      clientTest.supprimerContact("KH9GX9N", "domaine1");
    } catch (WsGestionDesContactsException e) {
      fail(e.getMessage(), e);
    }

  }

  // CREATION Organisation avec id fourni et domaine. Domaine existant

  @Test
  public void createAnddeleteOrganisationByIdAndDomaineCasDomaineExistant() {
    Organisation organisationACreer = new Organisation();
    organisationACreer.setIdentifiant("1313131");
    organisationACreer.setNomCommun("Organisation !!!!!");
    organisationACreer.setDomaineDeGestion("domaine1");
    organisationACreer.setDescription("Une organisation :)");
    organisationACreer.setAdresseMessagerie("organisation@free.fr");

    Organisation organisation;
    try {
      organisation = (Organisation) clientTest.creerEntiteAvecIdentifiant(organisationACreer);
      assertNotNull(organisation, "L'organisation ne devrait pas être null");
      clientTest.supprimerOrganisation("1313131", "domaine1");
    } catch (WsGestionDesContactsException e) {
      fail(e.getMessage(), e);
    }

  }

  // AJOUTER Habilitations sans propriétés
  @Test
  public void addHabilitationsWithoutProperty() {
    List<String> roles = new ArrayList<>();
    roles.add("admin");
    roles.add("salut");
    try {
      clientTest.addHabilitationsWithoutProperty("A59WVVN", "domaine1", "test-cc", roles);
    } catch (WsGestionDesContactsException e) {
      fail(e.getMessage());
    }
  }

  // DELETE Habilitations
  @Test
  public void deleteHabilitations() {
    List<String> roles = new ArrayList<>();
    roles.add("admin");
    roles.add("salut");
    try {
      clientTest.addHabilitationsWithoutProperty("A59WVVN", "domaine1", "test-cc", roles);
    } catch (WsGestionDesContactsException e) {
      fail(e.getMessage());
    }
    try {
      clientTest.deleteHabilitations("A59WVVN", "domaine1", "test-cc", roles);
    } catch (WsGestionDesContactsException e) {
      fail(e.getMessage());
    }

  }

  // AJOUTER Habilitations avec propriétés
  @Test
  public void addHabilitationsWithProperty() {
    List<String> proprietes = new ArrayList<>();
    proprietes.add("rue");
    proprietes.add("garage");
    try {
      clientTest.addHabilitationsWithProperty("A59WVVN", "domaine1", "test-cc", "bonjour",
          proprietes);
    } catch (WsGestionDesContactsException e) {
      fail(e.getMessage());
    }

  }

  // DELETE Propriétés d'une habilitation
  @Test
  public void deletePropertyofHabilitation() {
    List<String> proprietes = new ArrayList<>();
    proprietes.add("garage");
    try {
      clientTest.deletePropertyofHabilitation("A59WVVN", "domaine1", "test-cc", "bonjour",
          proprietes);
    } catch (WsGestionDesContactsException e) {
      fail(e.getMessage());
    }

  }

  // Réinitialisation du mot de passe d'un contact
  @Test
  public void reinitPassword() {
    PasswordChangeRequest password = new PasswordChangeRequest();
    password.setAdresseMessagerie("test@no.domain");
    try {
      clientTest.reinitPassword("A59WVVN-bis", "domaine1", ModeEnvoi.MAIL);
    } catch (WsGestionDesContactsException e) {
      fail(e.getMessage(), e);
    }
  }

  // Changement du mot de passe d'un contact
  @Test
  public void changePassword() throws WsGestionDesContactsException {
    PasswordChangeRequest password = new PasswordChangeRequest();
    password.setAncienMotDePasse("ABC4%2SF");
    password.setNouveauMotDePasse("ABC4%2SF2");
    try {
      clientTest.changePassword("A59WVVN", "domaine1", password);
    } catch (WsGestionDesContactsException e) {
      fail(e.getMessage(), e);
    }

    boolean contactAuthenticated =
        clientTest.authentifierContact("A59WVVN", "domaine1", "ABC4%2SF2");
    assertTrue(contactAuthenticated, "Les informations d'authentification devraient etre valides");

    PasswordChangeRequest password2 = new PasswordChangeRequest();
    password2.setAncienMotDePasse("ABC4%2SF2");
    password2.setNouveauMotDePasse("ABC4%2SF");
    try {
      clientTest.changePassword("A59WVVN", "domaine1", password2);
    } catch (WsGestionDesContactsException e) {
      fail(e.getMessage(), e);
    }
  }

  @Test
  public void recupererHabilitationsCasContactExistantAvecHabilitations2()
      throws WsGestionDesContactsException {
    Habilitations habs = clientTest.getHabilitationsByIdAndDomaine("A59WVVN", "domaine1");
    assertNotNull(habs, "Le contact devrait avoir des habilitations non null");
  }

}
