package fr.insee.sugoi.ldap.rest.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.junit.jupiter.api.Test;
import fr.insee.sugoi.converter.ouganext.Contact;
import fr.insee.sugoi.converter.ouganext.InfoFormattage;

public class TestEnvoiLoginIt extends InitTests {

  static String password;
  static Contact cTest;
  static InfoFormattage info;

  @Test
  public void testNotFound() {
    info = new InfoFormattage();
    info.setUrlSite("http://test.insee.test/");
    WebTarget ressource = targets.get("contact");
    Response response = ressource.path("nimportequoi/login").queryParam("modeEnvoi", "mail")
        .request().post(Entity.entity(info, MediaType.APPLICATION_JSON));
    assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }

  @Test
  public void testEnvoiXml() {
    info = new InfoFormattage();
    info.setUrlSite("http://test.insee.test/");
    // Suppression du contact
    targets.get("contact").path("test-envoi-login").request().delete();
    Contact contactTest = new Contact();
    contactTest.setAdresseMessagerie("test@test.fr");
    /* Response response = */ targets.get("contacts").queryParam("domaine", "domaine2").request()
        .header("Slug", "test-envoi-login")
        .post(Entity.entity(contactTest, MediaType.APPLICATION_JSON));
    WebTarget ressource = targets.get("contact");
    Response response2 = ressource.path("test-envoi-login/login").queryParam("modeEnvoi", "mail")
        .request().post(Entity.entity(info, MediaType.APPLICATION_JSON));
    assertEquals(Status.NO_CONTENT.getStatusCode(), response2.getStatus());

  }

  @Test
  public void testEnvoiJson() {
    info = new InfoFormattage();
    info.setUrlSite("http://test.insee.test/");
    // Suppression du contact
    targets.get("contact").path("test-envoi-login").request().delete();
    Contact contactTest = new Contact();
    contactTest.setAdresseMessagerie("test@test.fr");
    /* Response response = */ targets.get("contacts").queryParam("domaine", "domaine2").request()
        .accept(MediaType.APPLICATION_JSON).header("Slug", "test-envoi-login")
        .post(Entity.entity(contactTest, MediaType.APPLICATION_JSON));
    WebTarget ressource = targets.get("contact");
    Response response2 = ressource.path("test-envoi-login/login").queryParam("modeEnvoi", "mail")
        .request().accept(MediaType.APPLICATION_JSON)
        .post(Entity.entity(info, MediaType.APPLICATION_JSON));
    assertEquals(Status.NO_CONTENT.getStatusCode(), response2.getStatus());

  }

}
