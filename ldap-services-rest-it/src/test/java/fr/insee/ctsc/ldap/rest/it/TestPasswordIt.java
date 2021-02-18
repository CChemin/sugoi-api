package fr.insee.ctsc.ldap.rest.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import fr.insee.sugoi.converter.ouganext.Contact;
import fr.insee.sugoi.converter.ouganext.Adresse;
import fr.insee.sugoi.converter.ouganext.InfoFormattage;
import fr.insee.sugoi.converter.ouganext.PasswordChangeRequest;

public class TestPasswordIt extends InitTests {
  static String password;
  static Contact contactTest;

  @Test
  public void testReinitPasswordXml() {
    String id = "A59WVVN";

    WebTarget ressource = targets.get("contact");
    try {
      ressource.path(id).request().delete();
    } catch (Exception e) {
      // Pas de ressource déja présente ou suppression impossible
    }
    contactTest = new Contact();
    contactTest.setNomCommun("Test IT");
    contactTest.setAdresseMessagerie("test@test.fr");

    Adresse adresse = new Adresse();
    adresse.setLigneUne("15 rue Gabriel Peri");
    adresse.setLigneDeux("");
    adresse.setLigneTrois("");
    adresse.setLigneQuatre("");
    adresse.setLigneCinq("");
    adresse.setLigneSix("92240 Malakoff");
    adresse.setLigneSept("");
    contactTest.setAdressePostale(adresse);
    Response response =
        ressource.path(id).queryParam("domaine", "domaine1").queryParam("creation", "true")
            .request().put(Entity.entity(contactTest, MediaType.APPLICATION_JSON));

    assertEquals(200, response.getStatus());

    InfoFormattage info = new InfoFormattage();
    info.setNomSignataire("test");
    info.setHotlineMail("test@test.fr");
    info.setModeleCourrier("ESA_LETMDP");
    PasswordChangeRequest pcr = new PasswordChangeRequest();
    pcr.setAdresseMessagerie("contact@insee.fr");
    pcr.setInfoFormattageEnvoi(info);
    WebTarget r2 = targets.get("contact");

    Response response2 = r2.path(id).path("password").queryParam("modeEnvoi", "mail")
        .queryParam("modeEnvoi", "courrier").request()
        .post(Entity.entity(pcr, MediaType.APPLICATION_JSON));

    assertEquals(204, response2.getStatus());
  }

  @Test
  public void testReinitPasswordJson() {
    String id = "A59WVVN";

    WebTarget ressource = targets.get("contact");
    try {
      ressource.path(id).request().delete();
    } catch (Exception e) {
      // Pas de ressource déja présente ou suppression impossible
    }
    contactTest = new Contact();
    contactTest.setNomCommun("Test IT");
    contactTest.setAdresseMessagerie("test@test.fr");

    Adresse adresse = new Adresse();
    adresse.setLigneUne("15 rue Gabriel Peri");
    adresse.setLigneDeux("");
    adresse.setLigneTrois("");
    adresse.setLigneQuatre("");
    adresse.setLigneCinq("");
    adresse.setLigneSix("92240 Malakoff");
    adresse.setLigneSept("");
    contactTest.setAdressePostale(adresse);
    Response response = ressource.path(id).queryParam("domaine", "domaine1")
        .queryParam("creation", "true").request().accept(MediaType.APPLICATION_JSON)
        .put(Entity.entity(contactTest, MediaType.APPLICATION_JSON));

    assertEquals(200, response.getStatus());

    InfoFormattage info = new InfoFormattage();
    info.setNomSignataire("test");
    info.setHotlineMail("test@test.fr");
    info.setModeleCourrier("ESA_LETMDP");
    PasswordChangeRequest pcr = new PasswordChangeRequest();
    pcr.setAdresseMessagerie("contact@insee.fr");
    pcr.setInfoFormattageEnvoi(info);
    WebTarget r2 = targets.get("contact");

    Response response2 = r2.path(id).path("password").queryParam("modeEnvoi", "mail")
        .queryParam("modeEnvoi", "courrier").request()
        .post(Entity.entity(pcr, MediaType.APPLICATION_JSON));

    assertEquals(204, response2.getStatus());
  }

  /**
   * Test désactivé en attendant de trouver un moyen pour corriger.
   * 
   */
  public void testChangePassword() {

    PasswordChangeRequest pcr = new PasswordChangeRequest();
    pcr.setAncienMotDePasse("testc");
    pcr.setNouveauMotDePasse("Test@jgdls");
    InfoFormattage info = new InfoFormattage();
    info.setNomSignataire("test");
    info.setHotlineMail("test@test.fr");
    info.setModeleCourrier("ESA_LETMDP");
    pcr.setAdresseMessagerie("elise.hamelin@insee.fr");
    pcr.setInfoFormattageEnvoi(info);
    WebTarget r2 = targets.get("contact");

    Response response2 = r2.path("testc").path("password").queryParam("modeEnvoi", "mail").request()
        .put(Entity.entity(pcr, MediaType.APPLICATION_JSON));
    assertEquals(200, response2.getStatus());

  }

  @Test
  public void testReinitPasswordMail() {
    String id = "A59WVVN";

    WebTarget ressource = targets.get("contact");
    try {
      ressource.path(id).request().delete();
    } catch (Exception e) {
      // Pas de ressource déja présente ou suppression impossible
    }
    contactTest = new Contact();
    contactTest.setNomCommun("Test IT");
    contactTest.setAdresseMessagerie("test@test.fr");

    Adresse adresse = new Adresse();
    adresse.setLigneUne("15 rue Gabriel Peri");
    adresse.setLigneDeux("");
    adresse.setLigneTrois("");
    adresse.setLigneQuatre("");
    adresse.setLigneCinq("");
    adresse.setLigneSix("92240 Malakoff");
    adresse.setLigneSept("");
    contactTest.setAdressePostale(adresse);
    /* Response response = */ ressource.path(id).queryParam("domaine", "domaine1")
        .queryParam("creation", "true").request()
        .put(Entity.entity(contactTest, MediaType.APPLICATION_JSON));
    PasswordChangeRequest pcr = new PasswordChangeRequest();

    WebTarget r2 = targets.get("contact");

    Response response2 = r2.path("A59WVVN").path("password").queryParam("modeEnvoi", "mail")
        .request().post(Entity.entity(pcr, MediaType.APPLICATION_JSON));
    assertEquals(204, response2.getStatus());
  }

  @AfterAll
  public static void tearDown() {

  }

  @Test
  public void initPasswordCasContactSansMdp() {
    String id = "TESTIT4";
    // Suppression du contact
    targets.get("contact").path(id).request().delete();
    contactTest = new Contact();
    targets.get("contacts").queryParam("domaine", "domaine1").request().header("Slug", id)
        .post(Entity.entity(contactTest, MediaType.APPLICATION_JSON));
    PasswordChangeRequest pcr = new PasswordChangeRequest();
    pcr.setNouveauMotDePasse("TST3%HKL");
    pcr.setAdresseMessagerie("testbidule");
    Response response2 = targets.get("contact").path(id).path("/password/first").request()
        .post(Entity.entity(pcr, MediaType.APPLICATION_JSON));
    assertEquals(204, response2.getStatus(),
        String.format("Le mot de passe aurait dû être ajouté correctement à %s", id));
  }

  @Test
  public void initPasswordCasContactAvecMdp() {
    String id = "TESTIT4";
    // Suppression du contact
    /* Response response4 = */ targets.get("contact").path(id).request().delete();
    contactTest = new Contact();
    /* Response response = */ targets.get("contacts").queryParam("domaine", "domaine1").request()
        .header("Slug", id).post(Entity.entity(contactTest, MediaType.APPLICATION_JSON));
    PasswordChangeRequest pcr = new PasswordChangeRequest();
    pcr.setNouveauMotDePasse("TST3%HKL");
    WebTarget r2 = targets.get("contact").path(id).path("/password/first");
    /* Response response2 = */ r2.request().post(Entity.entity(pcr, MediaType.APPLICATION_JSON));
    pcr.setNouveauMotDePasse("TST3%HKF");
    Response response3 = r2.request().post(Entity.entity(pcr, MediaType.APPLICATION_JSON));
    assertEquals(409, response3.getStatus(),
        String.format("Le mot de passe n'aurait pas dû être ajouté correctement à %s", id));
    assertEquals("fr.insee.ctsc.ldap.service.exception.AlreadyExistPasswordException",
        response3.readEntity(ErrorResult.class).getException(),
        "Une exception de type AlreadyExistPasswordException auraît dû être retournée");

  }

  @Test
  public void initPasswordCasContactMdpNonConforme() {
    String id = "TESTIT4";
    // Suppression du contact
    targets.get("contact").path(id).request().delete();
    contactTest = new Contact();
    targets.get("contacts").queryParam("domaine", "domaine1").request().header("Slug", id)
        .post(Entity.entity(contactTest, MediaType.APPLICATION_JSON));
    PasswordChangeRequest pcr = new PasswordChangeRequest();
    pcr.setNouveauMotDePasse("TST3%HKL%");
    WebTarget r2 = targets.get("contact").path(id).path("/password/first");
    Response response3 = r2.request().post(Entity.entity(pcr, MediaType.APPLICATION_JSON));
    assertEquals(409, response3.getStatus(), String.format(
        "Le mot de passe n'aurait pas dû être ajouté correctement à %s " + response3.getStatus(),
        id));
    assertEquals("fr.insee.ctsc.ldap.service.exception.InvalidFormatPasswordException",
        response3.readEntity(ErrorResult.class).getException(),
        "Une exception de type InvalidFormatPasswordException auraît dû être retournée");
  }

  @Test
  public void initPasswordCasContactMdpNull() {
    String id = "TESTIT4";
    // Suppression du contact
    targets.get("contact").path(id).request().delete();
    contactTest = new Contact();
    targets.get("contacts").queryParam("domaine", "domaine1").request().header("Slug", id)
        .post(Entity.entity(contactTest, MediaType.APPLICATION_JSON));
    PasswordChangeRequest pcr = new PasswordChangeRequest();
    pcr.setNouveauMotDePasse(null);
    WebTarget r2 = targets.get("contact").path(id).path("/password/first");
    Response response3 = r2.request().post(Entity.entity(pcr, MediaType.APPLICATION_JSON));
    assertEquals(409, response3.getStatus(),
        String.format("Le mot de passe n'aurait pas dû être ajouté correctement à %s", id));
    assertEquals("fr.insee.ctsc.ldap.service.exception.InvalidFormatPasswordException",
        response3.readEntity(ErrorResult.class).getException(),
        "Une exception de type InvalidFormatPasswordException auraît dû être retournée");
  }
}
