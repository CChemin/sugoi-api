package fr.insee.ctsc.ldap.rest.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import fr.insee.sugoi.converter.ouganext.Contact;
import fr.insee.sugoi.converter.ouganext.Habilitations;

public class TestHabilitationsIt extends InitTests {
  static List<WebTarget> contactsTemps = new ArrayList<WebTarget>();

  @Test
  public void testGetHabs() {
    WebTarget ressource = targets.get("contact");
    Response response = ressource.path("/testc/habilitations").request().get();
    response.readEntity(Habilitations.class);

    assertEquals(Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void testGetHabsVideXml() {
    WebTarget ressource = targets.get("contacts");
    Contact contactTest = new Contact();
    contactTest.setNomCommun("Test IT");
    Response response = ressource.queryParam("domaine", "domaine1").queryParam("nomCommun", "Test")
        .request().post(Entity.entity(contactTest, MediaType.APPLICATION_JSON));
    assertEquals(201, response.getStatus());
    WebTarget ressource2 =
        targets.get("contact").path(getIdentifiantFromLink(response.getHeaderString("Link")));
    contactsTemps.add(ressource2);
    Response response2 = ressource2.path("/habilitations").request().get();
    response2.readEntity(Habilitations.class);
    assertEquals(Status.OK.getStatusCode(), response2.getStatus());
  }

  @Test
  public void testGetHabsVideJson() {
    WebTarget ressource = targets.get("contacts");
    Contact contactTest = new Contact();
    contactTest.setNomCommun("Test IT");
    Response response = ressource.queryParam("domaine", "domaine1").queryParam("nomCommun", "Test")
        .request().accept(MediaType.APPLICATION_JSON)
        .post(Entity.entity(contactTest, MediaType.APPLICATION_JSON));
    assertEquals(201, response.getStatus());
    WebTarget resource2 =
        targets.get("contact").path(getIdentifiantFromLink(response.getHeaderString("Link")));
    contactsTemps.add(resource2);
    Response response2 = resource2.path("/habilitations").request().get();
    response2.readEntity(Habilitations.class);
    assertEquals(Status.OK.getStatusCode(), response2.getStatus());
  }

  @Test
  public void testaddNewHabs() {
    WebTarget ressource = targets.get("contacts");
    Contact contactTest = new Contact();
    contactTest.setNomCommun("Test IT");
    Response response = ressource.queryParam("domaine", "domaine1").queryParam("nomCommun", "Test")
        .request().accept(MediaType.APPLICATION_JSON)
        .post(Entity.entity(contactTest, MediaType.APPLICATION_JSON));
    assertEquals(201, response.getStatus());
    WebTarget resource2 =
        targets.get("contact").path(getIdentifiantFromLink(response.getHeaderString("Link")));
    contactsTemps.add(resource2);
    resource2.register(HttpAuthenticationFeature.basicBuilder().nonPreemptive()
        .credentials("applitest", "applitest").build());
    Response response2 = resource2.path("/habilitations/applitest").queryParam("role", "download")
        .request().put(Entity.json(""));
    assertEquals(Status.NO_CONTENT.getStatusCode(), response2.getStatus());
  }

  @Test
  public void testaddHabs() {
    WebTarget ressource = targets.get("contact");
    Response response =
        ressource.path("/testc/habilitations/applitest").queryParam("role", "download").request()
            .accept(MediaType.APPLICATION_JSON).put(Entity.json(""));
    assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());
    Response response2 = ressource.path("/testc/habilitations").request().get();
    /* Habilitations habs = */ response2.readEntity(Habilitations.class);
  }

  @Test
  public void testdeleteHabs() {
    WebTarget ressource = targets.get("contact");
    Response response = ressource.path("/testc/habilitations/applitest").queryParam("role", "role")
        .request().delete();
    assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());

    response = ressource.path("/testc/habilitations/applitest/testignorecase")
        .queryParam("propriete", "prop2").request().delete();
    assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());

    Habilitations habs = ressource.path("/testc/habilitations").request().get(Habilitations.class);
    assertEquals(2, habs.toListString().size());

  }

  @Test
  public void testaddNewHabsWithProperty() {
    WebTarget ressource = targets.get("contacts");
    Contact contactTest = new Contact();
    contactTest.setNomCommun("Test IT");
    Response response = ressource.queryParam("domaine", "domaine1").queryParam("nomCommun", "Test")
        .request().post(Entity.entity(contactTest, MediaType.APPLICATION_JSON));
    assertEquals(201, response.getStatus());
    WebTarget ressource2 =
        targets.get("contact").path(getIdentifiantFromLink(response.getHeaderString("Link")));
    contactsTemps.add(ressource2);
    ressource2.register(HttpAuthenticationFeature.basicBuilder().nonPreemptive()
        .credentials("applitest", "applitest").build());
    Response response2 = ressource2.path("/habilitations/applitest").path("coltrane")
        .queryParam("propriete", "prop1").request().put(Entity.json(""));
    assertEquals(Status.NO_CONTENT.getStatusCode(), response2.getStatus());

  }

  @Test
  public void testaddHabsWithProperty() {
    WebTarget ressource = targets.get("contacts");
    Contact contactTest = new Contact();
    contactTest.setNomCommun("Test IT");
    Response response = ressource.queryParam("domaine", "domaine1").queryParam("nomCommun", "Test")
        .request().post(Entity.entity(contactTest, MediaType.APPLICATION_JSON));
    assertEquals(201, response.getStatus());
    WebTarget ressource2 =
        targets.get("contact").path(getIdentifiantFromLink(response.getHeaderString("Link")));
    contactsTemps.add(ressource2);
    ressource2.register(HttpAuthenticationFeature.basicBuilder().nonPreemptive()
        .credentials("applitest", "applitest").build());
    Response response2 = ressource2.path("/habilitations/applitest").path("download")
        .queryParam("propriete", "prop1").request().put(Entity.json(""));
    assertEquals(Status.NO_CONTENT.getStatusCode(), response2.getStatus());

    response2 = ressource2.path("/habilitations/applitest").path("Download")
        .queryParam("propriete", "Prop1").request().put(Entity.json(""));
    assertEquals(Status.NO_CONTENT.getStatusCode(), response2.getStatus());

  }

  /**
   * Suppression des WebTargets générés le temps des tests.
   */
  @AfterEach
  public void shutdown() {
    for (WebTarget ressource : contactsTemps) {
      ressource.register(HttpAuthenticationFeature.basicBuilder().nonPreemptive()
          .credentials("test-cc", "test").build());
      ressource.request().delete();
    }
  }


  private String getIdentifiantFromLink(String link) {
    try {
      // <http://contacts.insee.test/annuaire/contact/portableetalbis>;rel="http://xml.insee.fr/schema/annuaire/Contact"
      String[] urlParsee = link.split(";")[0].split("<|>")[1].split("\\/");
      return urlParsee[urlParsee.length - 1];
    } catch (IndexOutOfBoundsException e) {
      return "";
    }
  }

}
