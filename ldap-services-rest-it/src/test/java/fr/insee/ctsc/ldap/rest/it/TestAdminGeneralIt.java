package fr.insee.ctsc.ldap.rest.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import com.unboundid.util.Base64;
import fr.insee.sugoi.converter.ouganext.Contact;
import fr.insee.sugoi.converter.ouganext.Adresse;
import fr.insee.sugoi.converter.ouganext.Organisation;

public class TestAdminGeneralIt extends InitTests {

  // private String contactId;
  private static final String ID = "TESTIT-CERT";
  private static final String DDG_OPENLDAP = "domaine1";


  @Test
  @Disabled
  public void getContacts() {
    // récupération de la ressource contacts
    WebTarget target = targets.get("contacts");
    // Construction de la requête avec un paramêtre de
    // requête (domaine=SIR)
    Response response =
        target.queryParam("domaine", "TST").queryParam("nomCommun", "test").request().get();
    assertEquals("04", StringUtils.right(String.valueOf(response.getStatus()), 2));
  }

  @Test
  public void testContact() {
    WebTarget ressource = targets.get("contacts");
    Contact contactTest = new Contact();
    contactTest.setNomCommun("Test IT");
    Response response = ressource.queryParam("domaine", "domaine2").queryParam("nomCommun", "Test")
        .request().post(Entity.entity(contactTest, MediaType.APPLICATION_JSON));
    assertEquals(201, response.getStatus());
    String contactUrl = response.getHeaderString("Link");
    String identifiant = contactUrl.split(";")[0];
    identifiant = identifiant.substring(identifiant.lastIndexOf("/") + 1);
    identifiant = identifiant.substring(0, identifiant.indexOf(">"));
    WebTarget r2 = targets.get("contact");
    // contactId = identifiant;
    Contact contact = r2.path(identifiant).request().get().readEntity(Contact.class);
    contact.setAdresseMessagerie("test@test.fr");
    contact.setNomCommun("test");
    Adresse adresse = new Adresse();
    adresse.setLigneUne("15 rue Gabriel Peri");
    adresse.setLigneDeux("");
    adresse.setLigneTrois("");
    adresse.setLigneQuatre("");
    adresse.setLigneCinq("");
    adresse.setLigneSix("");
    adresse.setLigneSept("92240 Malakoff");
    contact.setAdressePostale(adresse);

    Organisation organisation = new Organisation();
    organisation.setIdentifiant("tsto-tesjgvdk");
    organisation.setNomCommun("Test Organisation");

    Organisation o2 = new Organisation();
    o2.setIdentifiant("tsto-133546545");
    o2.setNomCommun("Test Organisation");

    contact.setOrganisationDeRattachement(organisation);

    Response responseContact = r2.path(identifiant).queryParam("domaine", "domaine2").request()
        .put(Entity.entity(contact, MediaType.APPLICATION_JSON));

    Contact c1 = responseContact.readEntity(Contact.class);
    assertEquals("test@test.fr", c1.getAdresseMessagerie());
    assertEquals(200, responseContact.getStatus());
    WebTarget r3 = targets.get("contact");
    Response responseDelete = r3.path(identifiant).request().delete();
    assertEquals(204, responseDelete.getStatus());
  }

  @Test
  public void testOrganisation() {
    WebTarget ressource = targets.get("organisations");
    Organisation organisation = new Organisation();
    organisation.setNomCommun("Test IT");
    Response response = ressource.queryParam("domaine", "domaine2").request()
        .post(Entity.entity(organisation, MediaType.APPLICATION_JSON));
    assertEquals(201, response.getStatus());
    String contactUrl = response.getHeaderString("Link");
    String identifiant = contactUrl.split(";")[0];
    identifiant = identifiant.substring(identifiant.lastIndexOf("/") + 1);
    identifiant = identifiant.substring(0, identifiant.indexOf(">"));
    WebTarget r2 = targets.get("organisation");
    Organisation organisation2 = r2.path(identifiant).request().get(Organisation.class);
    organisation2.setAdresseMessagerie("test@test.fr");
    organisation2.setNomCommun("test");
    Adresse adresse = new Adresse();
    adresse.setLigneUne("15 rue Gabriel Peri");
    adresse.setLigneDeux("");
    adresse.setLigneTrois("");
    adresse.setLigneQuatre("");
    adresse.setLigneCinq("");
    adresse.setLigneSix("");
    adresse.setLigneSept("92240 Malakoff");
    organisation2.setAdressePostale(adresse);

    Organisation o2 = new Organisation();
    o2.setIdentifiant("tsto-133546545");
    o2.setNomCommun("Test Organisation");

    organisation2.setOrganisationDeRattachement(o2);
    Response responseOrganisation = r2.path(identifiant).queryParam("domaine", "domaine2").request()
        .put(Entity.entity(organisation2, MediaType.APPLICATION_JSON));
    Organisation organisationBack = responseOrganisation.readEntity(Organisation.class);
    assertEquals("test@test.fr", organisationBack.getAdresseMessagerie());
    assertEquals(200, responseOrganisation.getStatus());
    WebTarget r3 = targets.get("organisation");
    Response responseDelete = r3.path(identifiant).request().delete();
    assertEquals(204, responseDelete.getStatus());
  }

  @Test
  public void testReinitPassword() {
    WebTarget ressource = targets.get("contacts");
    Contact contactTest = new Contact();
    contactTest.setNomCommun("Test IT");
    Response response = ressource.queryParam("domaine", "domaine2").request()
        .post(Entity.entity(contactTest, MediaType.APPLICATION_JSON));
    assertEquals(201, response.getStatus());

  }

  @Test
  public void createContactOpenldapWithoutCertificate() {
    targets.get("contact").path(ID).request().delete();
    Contact contactTest = new Contact();
    Response response = targets.get("contacts").queryParam("domaine", DDG_OPENLDAP).request()
        .header("Slug", ID).post(Entity.entity(contactTest, MediaType.APPLICATION_JSON));
    assertEquals(201, response.getStatus(), "Le contact aurait dû être créé");
  }

  @Test
  public void createContactOpenldapWithCertificate() throws CertificateException, ParseException {
    // Suppression du contact
    targets.get("contact").path(ID).request().delete();
    Contact contactTest = new Contact();
    String pem =
        "MIIF6zCCBVSgAwIBAgIQf/2X2OiJ6FpZeLIjK2A7bDANBgkqhkiG9w0BAQQFADBxMRwwGgYDVQQKExNBdXRvcml0ZSBDb25zdWxhaXJlMSYwJAYDVQQLEx1DZXJ0aWZpY2F0aW9uIF"
            + "Byb2Zlc3Npb25uZWxsZTEpMCcGA1UEAxMgQ1NGIC0gQ2xhc3NlIElJSSAtIFNpZ24gZXQgQ3J5cHQwHhcNMTAxMjEzMTIxMTA1WhcNMTIxMjEyMTIxMTA1WjCB/TELMAkGA1UEBh"
            + "MCRlIxEjAQBgNVBAcTCVBBVSBDRURFWDEbMBkGA1UEDBMSU2VjcmV0YWlyZSBHZW5lcmFsMTgwNgYDVQQKEy9DSEFNQlJFIERFUyBNRVRJRVJTIEVUIERFIEwgQVJUSVNBTkFUIER"
            + "FUyBQQSA2NDEcMBoGA1UECxMTU2VjcmV0YXJpYXQgR2VuZXJhbDEXMBUGA1UECxMOMDAwMiAxODY0MDAwNDAxFjAUBgIrAhMOMTg2NDAwMDQwMDAwMzMxFTATBgNVBAMTDEZyYW5j"
            + "b2lzIEdBWTEdMBsGCSqGSIb3DQEJARYOZi5nYXlAY202NC5jb20wgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAKWccChCDwGf7guxYE33KdzMxaauZkzpgGWRdiEp4yi8D930"
            + "aAFQN48/CS4XMeemkWdYzENKl1qtGwdvFDgsz6DyE0PVjvY6dI07jukwQBuCIKGvMqgHOtYJUd063U+5FdtqdL5KE/rd3fQQwDi7Je76jEAxeGCQFe1fzwgX9mR1AgMBAAGjggL1"
            + "MIIC8TAJBgNVHRMEAjAAMAsGA1UdDwQEAwIFoDCCAQ8GA1UdIASCAQYwggECMIH/BggqgXoBYAEDBDCB8jBCBggrBgEFBQcCARY2aHR0cDovL3d3dy5jaGFtYmVyc2lnbi50bS5mc"
            + "i9wYy9jc2YtY2xhc3NlMy1zaWduY3J5cHQvMIGrBggrBgEFBQcCAjCBnhqBm0NlIGNlcnRpZmljYXQgcmVwb25kIGF1eCBzcGVjaWZpY2F0aW9ucyBldCBkaXJlY3RpdmVzIGRlIG"
            + "wnVW5pb24gRXVyb3BlZW5uZSwgZGUgQ2hhbWJlclNpZ24gRXVyb3BlIGV0IGRlIGxhIFBDIFYzIGR1IE1pbmVmaS4KSWwgZXN0IGRlbGl2cmUgZW4gZmFjZSBhIGZhY2UuMIIBIgYD"
            + "VR0fBIIBGTCCARUwV6BVoFOGUWh0dHA6Ly9vbnNpdGVjcmwuY2VydHBsdXMuY29tL0F1dG9yaXRlQ29uc3VsYWlyZUNTRmNsYXNzZUlJSVNpZ25ldENyeXB0L0xhdGVzdENSTDCBua"
            + "CBtqCBs4aBsGxkYXA6Ly9kaXJlY3RvcnkuY2VydHBsdXMuY29tL0NOPUNTRiAtIENsYXNzZSBJSUkgLSBTaWduIGV0IENyeXB0LE9VPUNlcnRpZmljYXRpb24gUHJvZmVzc2lvbm5l"
            + "bGxlLE89QXV0b3JpdGUgQ29uc3VsYWlyZT9jZXJ0aWZpY2F0ZXJldm9jYXRpb25saXN0O2JpbmFyeT9iYXNlP29iamVjdGNsYXNzPXBraUNBMBkGA1UdEQQSMBCBDmYuZ2F5QGNtNj"
            + "QuY29tMEMGA1UdEgQ8MDqBGmF1dG9yaXRlQGNoYW1iZXJzaWduLnRtLmZyhhxodHRwOi8vd3d3LmNoYW1iZXJzaWduLnRtLmZyMB0GA1UdDgQWBBSUBKVpArxugtUWY/zfY81PXK0i"
            + "RjAfBgNVHSMEGDAWgBSIZeCPSxpirxugS8IRL1SgDkc6GTANBgkqhkiG9w0BAQQFAAOBgQAWrqhEEPRTQlf/AHHw2i0r/jb7R7D4Ev1zzcWFaxaUNA3K9msxXPTjRmxGQSpXztkS1X"
            + "M9W1XdXZXtNegNztGMnLZymagSB3XtTrZPKbdeHF20hGEOZWhEm6nabLIoOUlo8P/FEnf49N5Xue114U7xSw3g0tJmtTWzK+bLep2XuA==";
    byte[] decoded = Base64.decode(pem);

    X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance("X.509")
        .generateCertificate(new ByteArrayInputStream(decoded));
    contactTest.setCertificate(certificate);
    Response response = targets.get("contacts").queryParam("domaine", DDG_OPENLDAP).request()
        .header("Slug", ID).post(Entity.entity(contactTest, MediaType.APPLICATION_JSON));
    assertEquals(201, response.getStatus(), "Le contact aurait dû être créé");
    Response response2 = targets.get("contact").path(ID).request().get();
    Contact contact = response2.readEntity(Contact.class);
    assertNotNull(contact.getCertificate(), "Le contact aurait dû avoir un certificat");
    assertTrue(
        contact.getPropriete()
            .contains("certificateId$" + encodeCertificate(contact.getCertificate())),
        "Le contact aurait dû avoir la propriété associée au certificat");
  }

  @Test
  public void modifyContactOpenldapAddCertificate() throws CertificateException, ParseException {
    // Suppression du contact
    targets.get("contact").path(ID).request().delete();
    Contact contactTest = new Contact();
    targets.get("contacts").queryParam("domaine", DDG_OPENLDAP).request().header("Slug", ID)
        .post(Entity.entity(contactTest, MediaType.APPLICATION_JSON));
    String pem =
        "MIIF6zCCBVSgAwIBAgIQf/2X2OiJ6FpZeLIjK2A7bDANBgkqhkiG9w0BAQQFADBxMRwwGgYDVQQKExNBdXRvcml0ZSBDb25zdWxhaXJlMSYwJAYDVQQLEx1DZXJ0aWZpY2F0a"
            + "W9uIFByb2Zlc3Npb25uZWxsZTEpMCcGA1UEAxMgQ1NGIC0gQ2xhc3NlIElJSSAtIFNpZ24gZXQgQ3J5cHQwHhcNMTAxMjEzMTIxMTA1WhcNMTIxMjEyMTIxMTA1WjCB/TEL"
            + "MAkGA1UEBhMCRlIxEjAQBgNVBAcTCVBBVSBDRURFWDEbMBkGA1UEDBMSU2VjcmV0YWlyZSBHZW5lcmFsMTgwNgYDVQQKEy9DSEFNQlJFIERFUyBNRVRJRVJTIEVUIERFIEw"
            + "gQVJUSVNBTkFUIERFUyBQQSA2NDEcMBoGA1UECxMTU2VjcmV0YXJpYXQgR2VuZXJhbDEXMBUGA1UECxMOMDAwMiAxODY0MDAwNDAxFjAUBgIrAhMOMTg2NDAwMDQwMDAwMz"
            + "MxFTATBgNVBAMTDEZyYW5jb2lzIEdBWTEdMBsGCSqGSIb3DQEJARYOZi5nYXlAY202NC5jb20wgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAKWccChCDwGf7guxYE33K"
            + "dzMxaauZkzpgGWRdiEp4yi8D930aAFQN48/CS4XMeemkWdYzENKl1qtGwdvFDgsz6DyE0PVjvY6dI07jukwQBuCIKGvMqgHOtYJUd063U+5FdtqdL5KE/rd3fQQwDi7Je76"
            + "jEAxeGCQFe1fzwgX9mR1AgMBAAGjggL1MIIC8TAJBgNVHRMEAjAAMAsGA1UdDwQEAwIFoDCCAQ8GA1UdIASCAQYwggECMIH/BggqgXoBYAEDBDCB8jBCBggrBgEFBQcCARY"
            + "2aHR0cDovL3d3dy5jaGFtYmVyc2lnbi50bS5mci9wYy9jc2YtY2xhc3NlMy1zaWduY3J5cHQvMIGrBggrBgEFBQcCAjCBnhqBm0NlIGNlcnRpZmljYXQgcmVwb25kIGF1eC"
            + "BzcGVjaWZpY2F0aW9ucyBldCBkaXJlY3RpdmVzIGRlIGwnVW5pb24gRXVyb3BlZW5uZSwgZGUgQ2hhbWJlclNpZ24gRXVyb3BlIGV0IGRlIGxhIFBDIFYzIGR1IE1pbmVma"
            + "S4KSWwgZXN0IGRlbGl2cmUgZW4gZmFjZSBhIGZhY2UuMIIBIgYDVR0fBIIBGTCCARUwV6BVoFOGUWh0dHA6Ly9vbnNpdGVjcmwuY2VydHBsdXMuY29tL0F1dG9yaXRlQ29u"
            + "c3VsYWlyZUNTRmNsYXNzZUlJSVNpZ25ldENyeXB0L0xhdGVzdENSTDCBuaCBtqCBs4aBsGxkYXA6Ly9kaXJlY3RvcnkuY2VydHBsdXMuY29tL0NOPUNTRiAtIENsYXNzZSB"
            + "JSUkgLSBTaWduIGV0IENyeXB0LE9VPUNlcnRpZmljYXRpb24gUHJvZmVzc2lvbm5lbGxlLE89QXV0b3JpdGUgQ29uc3VsYWlyZT9jZXJ0aWZpY2F0ZXJldm9jYXRpb25saX"
            + "N0O2JpbmFyeT9iYXNlP29iamVjdGNsYXNzPXBraUNBMBkGA1UdEQQSMBCBDmYuZ2F5QGNtNjQuY29tMEMGA1UdEgQ8MDqBGmF1dG9yaXRlQGNoYW1iZXJzaWduLnRtLmZyh"
            + "hxodHRwOi8vd3d3LmNoYW1iZXJzaWduLnRtLmZyMB0GA1UdDgQWBBSUBKVpArxugtUWY/zfY81PXK0iRjAfBgNVHSMEGDAWgBSIZeCPSxpirxugS8IRL1SgDkc6GTANBgkq"
            + "hkiG9w0BAQQFAAOBgQAWrqhEEPRTQlf/AHHw2i0r/jb7R7D4Ev1zzcWFaxaUNA3K9msxXPTjRmxGQSpXztkS1XM9W1XdXZXtNegNztGMnLZymagSB3XtTrZPKbdeHF20hGE"
            + "OZWhEm6nabLIoOUlo8P/FEnf49N5Xue114U7xSw3g0tJmtTWzK+bLep2XuA==";
    byte[] decoded = Base64.decode(pem);
    X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance("X.509")
        .generateCertificate(new ByteArrayInputStream(decoded));
    contactTest.setCertificate(certificate);
    Response response3 = targets.get("contact").path(ID).queryParam("domaine", DDG_OPENLDAP)
        .request().put(Entity.entity(contactTest, MediaType.APPLICATION_JSON));
    assertEquals(200, response3.getStatus(), "Le contact aurait dû être modifié");
    Response response2 = targets.get("contact").path(ID).request().get();
    Contact contact = response2.readEntity(Contact.class);
    assertNotNull(contact.getCertificate(), "Le contact aurait dû avoir un certificat");
    assertTrue(
        contact.getPropriete()
            .contains("certificateId$" + encodeCertificate(contact.getCertificate())),
        "Le contact aurait dû avoir la propriété associée au certificat");
  }

  @Test
  public void modifyContactOpenldapReplaceCertificate()
      throws CertificateException, ParseException {
    // Suppression du contact
    targets.get("contact").path(ID).request().delete();
    Contact contactTest = new Contact();
    String pem =
        "MIIF6zCCBVSgAwIBAgIQf/2X2OiJ6FpZeLIjK2A7bDANBgkqhkiG9w0BAQQFADBxMRwwGgYDVQQKExNBdXRvcml0ZSBDb25zdWxhaXJlMSYwJAYDVQQLEx1DZXJ0aWZpY2"
            + "F0aW9uIFByb2Zlc3Npb25uZWxsZTEpMCcGA1UEAxMgQ1NGIC0gQ2xhc3NlIElJSSAtIFNpZ24gZXQgQ3J5cHQwHhcNMTAxMjEzMTIxMTA1WhcNMTIxMjEyMTIxMTA1Wj"
            + "CB/TELMAkGA1UEBhMCRlIxEjAQBgNVBAcTCVBBVSBDRURFWDEbMBkGA1UEDBMSU2VjcmV0YWlyZSBHZW5lcmFsMTgwNgYDVQQKEy9DSEFNQlJFIERFUyBNRVRJRVJTIE"
            + "VUIERFIEwgQVJUSVNBTkFUIERFUyBQQSA2NDEcMBoGA1UECxMTU2VjcmV0YXJpYXQgR2VuZXJhbDEXMBUGA1UECxMOMDAwMiAxODY0MDAwNDAxFjAUBgIrAhMOMTg2ND"
            + "AwMDQwMDAwMzMxFTATBgNVBAMTDEZyYW5jb2lzIEdBWTEdMBsGCSqGSIb3DQEJARYOZi5nYXlAY202NC5jb20wgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAKWccC"
            + "hCDwGf7guxYE33KdzMxaauZkzpgGWRdiEp4yi8D930aAFQN48/CS4XMeemkWdYzENKl1qtGwdvFDgsz6DyE0PVjvY6dI07jukwQBuCIKGvMqgHOtYJUd063U+5FdtqdL"
            + "5KE/rd3fQQwDi7Je76jEAxeGCQFe1fzwgX9mR1AgMBAAGjggL1MIIC8TAJBgNVHRMEAjAAMAsGA1UdDwQEAwIFoDCCAQ8GA1UdIASCAQYwggECMIH/BggqgXoBYAEDBD"
            + "CB8jBCBggrBgEFBQcCARY2aHR0cDovL3d3dy5jaGFtYmVyc2lnbi50bS5mci9wYy9jc2YtY2xhc3NlMy1zaWduY3J5cHQvMIGrBggrBgEFBQcCAjCBnhqBm0NlIGNlcn"
            + "RpZmljYXQgcmVwb25kIGF1eCBzcGVjaWZpY2F0aW9ucyBldCBkaXJlY3RpdmVzIGRlIGwnVW5pb24gRXVyb3BlZW5uZSwgZGUgQ2hhbWJlclNpZ24gRXVyb3BlIGV0IG"
            + "RlIGxhIFBDIFYzIGR1IE1pbmVmaS4KSWwgZXN0IGRlbGl2cmUgZW4gZmFjZSBhIGZhY2UuMIIBIgYDVR0fBIIBGTCCARUwV6BVoFOGUWh0dHA6Ly9vbnNpdGVjcmwuY2"
            + "VydHBsdXMuY29tL0F1dG9yaXRlQ29uc3VsYWlyZUNTRmNsYXNzZUlJSVNpZ25ldENyeXB0L0xhdGVzdENSTDCBuaCBtqCBs4aBsGxkYXA6Ly9kaXJlY3RvcnkuY2VydH"
            + "BsdXMuY29tL0NOPUNTRiAtIENsYXNzZSBJSUkgLSBTaWduIGV0IENyeXB0LE9VPUNlcnRpZmljYXRpb24gUHJvZmVzc2lvbm5lbGxlLE89QXV0b3JpdGUgQ29uc3VsYW"
            + "lyZT9jZXJ0aWZpY2F0ZXJldm9jYXRpb25saXN0O2JpbmFyeT9iYXNlP29iamVjdGNsYXNzPXBraUNBMBkGA1UdEQQSMBCBDmYuZ2F5QGNtNjQuY29tMEMGA1UdEgQ8MD"
            + "qBGmF1dG9yaXRlQGNoYW1iZXJzaWduLnRtLmZyhhxodHRwOi8vd3d3LmNoYW1iZXJzaWduLnRtLmZyMB0GA1UdDgQWBBSUBKVpArxugtUWY/zfY81PXK0iRjAfBgNVHS"
            + "MEGDAWgBSIZeCPSxpirxugS8IRL1SgDkc6GTANBgkqhkiG9w0BAQQFAAOBgQAWrqhEEPRTQlf/AHHw2i0r/jb7R7D4Ev1zzcWFaxaUNA3K9msxXPTjRmxGQSpXztkS1X"
            + "M9W1XdXZXtNegNztGMnLZymagSB3XtTrZPKbdeHF20hGEOZWhEm6nabLIoOUlo8P/FEnf49N5Xue114U7xSw3g0tJmtTWzK+bLep2XuA==";
    byte[] decoded = Base64.decode(pem);
    X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance("X.509")
        .generateCertificate(new ByteArrayInputStream(decoded));
    contactTest.setCertificate(certificate);
    targets.get("contacts").queryParam("domaine", DDG_OPENLDAP).request().header("Slug", ID)
        .post(Entity.entity(contactTest, MediaType.APPLICATION_JSON));
    String pemModif = "MIIFRDCCBCygAwIBAgISESHffihSxL4z9/N7zOjVHaKbMA0GCSqGSIb3DQEBBQUA"
        + "MF0xCzAJBgNVBAYTAkJFMRkwFwYDVQQKExBHbG9iYWxTaWduIG52LXNhMTMwMQYD"
        + "VQQDEypHbG9iYWxTaWduIE9yZ2FuaXphdGlvbiBWYWxpZGF0aW9uIENBIC0gRzIw"
        + "HhcNMTMwNjEyMDc0MTM1WhcNMTUwNjEzMDc0MTM1WjB9MQswCQYDVQQGEwJESzEW"
        + "MBQGA1UECBMNRnJlZGVyaWtzYmVyZzEWMBQGA1UEBxMNRnJlZGVyaWtzYmVyZzEL"
        + "MAkGA1UECxMCSVQxEzARBgNVBAoTClN0YXRlbnMtSVQxHDAaBgNVBAMMEyouc29m"
        + "dHdhcmVib3JzZW4uZGswggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDZ"
        + "t6bED/UR0aiLklE2kZOP6m8WZo4UinV+9gtoqPyuoTEXcHF9+ws+2PpRgFqD5p8T"
        + "9qYFEK5ROgv58lK4S/0dXdUgEEMZjCOVC9xgtLZc6eE3ykHI/9lyrxg1Ygk3AFXx"
        + "jYkLREhbvOt9A6k2bMdDL52fXAkLiOjCTttrYndW0PkZ8bMx6CCdXWV9VODxFa7V"
        + "u4JHUn74ZbKHQLNEcrZ6HHGLWLlQNnfE83cNhJG4O2eX1GiOa329OsabcJuco+0b"
        + "wIahk+j7YdYldnN0LQIavE6KQ9lqndpOBR+UQ+gxezcotgAeoj7kWiBGk6rEbpn/"
        + "CsQd0Cv1nl/xLC3XztNhAgMBAAGjggHcMIIB2DAOBgNVHQ8BAf8EBAMCBaAwSQYD"
        + "VR0gBEIwQDA+BgZngQwBAgIwNDAyBggrBgEFBQcCARYmaHR0cHM6Ly93d3cuZ2xv"
        + "YmFsc2lnbi5jb20vcmVwb3NpdG9yeS8wMQYDVR0RBCowKIITKi5zb2Z0d2FyZWJv"
        + "cnNlbi5ka4IRc29mdHdhcmVib3JzZW4uZGswCQYDVR0TBAIwADAdBgNVHSUEFjAU"
        + "BggrBgEFBQcDAQYIKwYBBQUHAwIwRQYDVR0fBD4wPDA6oDigNoY0aHR0cDovL2Ny"
        + "bC5nbG9iYWxzaWduLmNvbS9ncy9nc29yZ2FuaXphdGlvbnZhbGcyLmNybDCBlgYI"
        + "KwYBBQUHAQEEgYkwgYYwRwYIKwYBBQUHMAKGO2h0dHA6Ly9zZWN1cmUuZ2xvYmFs"
        + "c2lnbi5jb20vY2FjZXJ0L2dzb3JnYW5pemF0aW9udmFsZzIuY3J0MDsGCCsGAQUF"
        + "BzABhi9odHRwOi8vb2NzcDIuZ2xvYmFsc2lnbi5jb20vZ3Nvcmdhbml6YXRpb252"
        + "YWxnMjAdBgNVHQ4EFgQUvsr7yv5NGrZDP9ZDveLHvZkoSFwwHwYDVR0jBBgwFoAU"
        + "XUayjcRLdBy77fVztjq3OI91nn4wDQYJKoZIhvcNAQEFBQADggEBAE5YZjL4lxtt"
        + "HKUnM2kfx98qsbTkF7dD2ucoL2ahTHD6M/WCoItKMEUK15crq9dUcDoaR//RLkEj"
        + "ia8jeLYtM6/vi5biJqXhWzULBJUp/lJ3y/MzDcyOiyyX2787lKC5vm/Y1V1hs4p6"
        + "HU1cfwo++orqsqTwdj3U+FW4if+iNk4f0jLw5Cl2p3jruIL6rS22vEgv4digKSOy"
        + "bcdHmVztzNSwJiDQrJbDuMTRCupoifZIeJigi2UJRHoWzJGEGDMEbQNHJ62rkA+b"
        + "AYgmy/pjRM0/40iJF8Uoo6AsdTtCpxGG/A299Rl5PZE6RfYEtD5QJV0oluI3/ZxO" + "x8Y5UtFw9XU=";
    byte[] decodedModif = Base64.decode(pemModif);
    X509Certificate certificateModif = (X509Certificate) CertificateFactory.getInstance("X.509")
        .generateCertificate(new ByteArrayInputStream(decodedModif));
    contactTest.setCertificate(certificateModif);
    Response response3 = targets.get("contact").path(ID).queryParam("domaine", DDG_OPENLDAP)
        .request().put(Entity.entity(contactTest, MediaType.APPLICATION_JSON));
    assertEquals(200, response3.getStatus(), "Le contact aurait dû être modifié");
    Response response2 = targets.get("contact").path(ID).request().get();
    Contact contact = response2.readEntity(Contact.class);
    assertEquals(certificateModif, contact.getCertificate(),
        "Le contact aurait dû avoir le nouveau certificat");
    assertTrue(
        contact.getPropriete().contains("certificateId$" + encodeCertificate(certificateModif)),
        "Le contact aurait dû avoir la propriété associée au certificat");
    assertFalse(contact.getPropriete().contains("certificateId$" + encodeCertificate(certificate)),
        "Le contact ne devrait plus avoir la propriété associée à l'ancien certificat");
  }

  @Test
  public void modifyContactOpenldapSuppressCertificate()
      throws CertificateException, ParseException {
    // Suppression du contact
    targets.get("contact").path(ID).request().delete();
    Contact contactTest = new Contact();
    String pem =
        "MIIF6zCCBVSgAwIBAgIQf/2X2OiJ6FpZeLIjK2A7bDANBgkqhkiG9w0BAQQFADBxMRwwGgYDVQQKExNBdXRvcml0ZSBDb25zdWxhaXJlMSYwJAYDVQQLEx1DZXJ0aWZpY2F0aW9uIFBy"
            + "b2Zlc3Npb25uZWxsZTEpMCcGA1UEAxMgQ1NGIC0gQ2xhc3NlIElJSSAtIFNpZ24gZXQgQ3J5cHQwHhcNMTAxMjEzMTIxMTA1WhcNMTIxMjEyMTIxMTA1WjCB/TELMAkGA1UEBhMCR"
            + "lIxEjAQBgNVBAcTCVBBVSBDRURFWDEbMBkGA1UEDBMSU2VjcmV0YWlyZSBHZW5lcmFsMTgwNgYDVQQKEy9DSEFNQlJFIERFUyBNRVRJRVJTIEVUIERFIEwgQVJUSVNBTkFUIERFUy"
            + "BQQSA2NDEcMBoGA1UECxMTU2VjcmV0YXJpYXQgR2VuZXJhbDEXMBUGA1UECxMOMDAwMiAxODY0MDAwNDAxFjAUBgIrAhMOMTg2NDAwMDQwMDAwMzMxFTATBgNVBAMTDEZyYW5jb2l"
            + "zIEdBWTEdMBsGCSqGSIb3DQEJARYOZi5nYXlAY202NC5jb20wgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAKWccChCDwGf7guxYE33KdzMxaauZkzpgGWRdiEp4yi8D930aAFQ"
            + "N48/CS4XMeemkWdYzENKl1qtGwdvFDgsz6DyE0PVjvY6dI07jukwQBuCIKGvMqgHOtYJUd063U+5FdtqdL5KE/rd3fQQwDi7Je76jEAxeGCQFe1fzwgX9mR1AgMBAAGjggL1MIIC8"
            + "TAJBgNVHRMEAjAAMAsGA1UdDwQEAwIFoDCCAQ8GA1UdIASCAQYwggECMIH/BggqgXoBYAEDBDCB8jBCBggrBgEFBQcCARY2aHR0cDovL3d3dy5jaGFtYmVyc2lnbi50bS5mci9wYy"
            + "9jc2YtY2xhc3NlMy1zaWduY3J5cHQvMIGrBggrBgEFBQcCAjCBnhqBm0NlIGNlcnRpZmljYXQgcmVwb25kIGF1eCBzcGVjaWZpY2F0aW9ucyBldCBkaXJlY3RpdmVzIGRlIGwnVW5"
            + "pb24gRXVyb3BlZW5uZSwgZGUgQ2hhbWJlclNpZ24gRXVyb3BlIGV0IGRlIGxhIFBDIFYzIGR1IE1pbmVmaS4KSWwgZXN0IGRlbGl2cmUgZW4gZmFjZSBhIGZhY2UuMIIBIgYDVR0f"
            + "BIIBGTCCARUwV6BVoFOGUWh0dHA6Ly9vbnNpdGVjcmwuY2VydHBsdXMuY29tL0F1dG9yaXRlQ29uc3VsYWlyZUNTRmNsYXNzZUlJSVNpZ25ldENyeXB0L0xhdGVzdENSTDCBuaCBt"
            + "qCBs4aBsGxkYXA6Ly9kaXJlY3RvcnkuY2VydHBsdXMuY29tL0NOPUNTRiAtIENsYXNzZSBJSUkgLSBTaWduIGV0IENyeXB0LE9VPUNlcnRpZmljYXRpb24gUHJvZmVzc2lvbm5lbG"
            + "xlLE89QXV0b3JpdGUgQ29uc3VsYWlyZT9jZXJ0aWZpY2F0ZXJldm9jYXRpb25saXN0O2JpbmFyeT9iYXNlP29iamVjdGNsYXNzPXBraUNBMBkGA1UdEQQSMBCBDmYuZ2F5QGNtNj"
            + "QuY29tMEMGA1UdEgQ8MDqBGmF1dG9yaXRlQGNoYW1iZXJzaWduLnRtLmZyhhxodHRwOi8vd3d3LmNoYW1iZXJzaWduLnRtLmZyMB0GA1UdDgQWBBSUBKVpArxugtUWY/zfY81PXK"
            + "0iRjAfBgNVHSMEGDAWgBSIZeCPSxpirxugS8IRL1SgDkc6GTANBgkqhkiG9w0BAQQFAAOBgQAWrqhEEPRTQlf/AHHw2i0r/jb7R7D4Ev1zzcWFaxaUNA3K9msxXPTjRmxGQSpXzt"
            + "kS1XM9W1XdXZXtNegNztGMnLZymagSB3XtTrZPKbdeHF20hGEOZWhEm6nabLIoOUlo8P/FEnf49N5Xue114U7xSw3g0tJmtTWzK+bLep2XuA==";
    byte[] decoded = Base64.decode(pem);

    X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance("X.509")
        .generateCertificate(new ByteArrayInputStream(decoded));
    contactTest.setCertificate(certificate);
    targets.get("contacts").queryParam("domaine", DDG_OPENLDAP).request().header("Slug", ID)
        .post(Entity.entity(contactTest, MediaType.APPLICATION_JSON));
    contactTest.setCertificate(null);
    Response response3 = targets.get("contact").path(ID).queryParam("domaine", DDG_OPENLDAP)
        .request().put(Entity.entity(contactTest, MediaType.APPLICATION_JSON));
    assertEquals(200, response3.getStatus(), "Le contact aurait dû être modifié");
    Response response2 = targets.get("contact").path(ID).request().get();
    Contact contact = response2.readEntity(Contact.class);
    assertNull(contact.getCertificate(), "Le contact ne devrait plus avoir de certificat");
    assertFalse(contact.getPropriete().contains("certificateId$" + encodeCertificate(certificate)),
        "Le contact ne devrait plus avoir la propriété associée à l'ancien certificat");
  }

  @Test
  public void modifyContactOpenldapWithoutCertificate() {
    // Suppression du contact
    targets.get("contact").path(ID).request().delete();
    Contact contactTest = new Contact();
    targets.get("contacts").queryParam("domaine", DDG_OPENLDAP).request().header("Slug", ID)
        .post(Entity.entity(contactTest, MediaType.APPLICATION_JSON));
    Response response3 = targets.get("contact").path(ID).queryParam("domaine", DDG_OPENLDAP)
        .request().put(Entity.entity(contactTest, MediaType.APPLICATION_JSON));
    assertEquals(200, response3.getStatus(), "Le contact aurait dû être modifié");
    Response response2 = targets.get("contact").path(ID).request().get();
    Contact contact = response2.readEntity(Contact.class);
    assertNull(contact.getCertificate(), "Le contact ne devrait pas avoir de certificat");
  }

  private static String encodeCertificate(X509Certificate cert)
      throws CertificateEncodingException {
    byte[] encodedDer = cert.getEncoded();
    MessageDigest digestSha256;
    try {
      digestSha256 = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new CertificateEncodingException("L'algorithme de hash n'existe pas \\n " + e);
    }
    digestSha256.update(encodedDer);
    byte[] hash256 = new byte[digestSha256.getDigestLength()];
    hash256 = digestSha256.digest();
    return getHexString(hash256);
  }

  private static String getHexString(byte[] bytes) {
    StringBuilder sb = new StringBuilder(bytes.length * 2);
    for (byte by : bytes) {
      sb.append(String.format("%02x", by));
    }
    return sb.toString();
  }
}
