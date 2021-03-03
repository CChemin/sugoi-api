package fr.insee.sugoi.ldap.rest.it;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.logging.LoggingFeature.Verbosity;
import org.junit.jupiter.api.BeforeAll;
import fr.insee.sugoi.ldap.LdapServiceTestServer;

public class InitTests {
  protected static Map<String, WebTarget> targets;
  protected static Client client;

  /**
   * Préparation des tests.
   */
  @BeforeAll
  public static void init() {

    ClientConfig confDuClient = new ClientConfig();
    confDuClient.register(HttpAuthenticationFeature.basicBuilder()
        .credentials("webservicesldap", "webservices").build());
    confDuClient.register(getSpecialLoggingFeature());
    client =
        ClientBuilder.newBuilder().sslContext(getUntrustContext()).withConfig(confDuClient).build();

    prepareTargets(client);

  }

  // TODO Utile ?? ou génère les logs en double ?
  private static LoggingFeature getSpecialLoggingFeature() {
    java.util.logging.Logger loggerJersey =
        java.util.logging.Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);
    loggerJersey.addHandler(new Handler() {
      @Override
      public void publish(LogRecord record) {
        System.out.println(record.getMessage());
      }

      @Override
      public void flush() {
        // Do nothing
      }

      @Override
      public void close() throws SecurityException {
        // Do nothing
      }
    });
    loggerJersey.setUseParentHandlers(false);
    loggerJersey.setLevel(Level.FINEST);
    return new LoggingFeature(loggerJersey, Level.FINEST, Verbosity.PAYLOAD_ANY,
        LoggingFeature.DEFAULT_MAX_ENTITY_SIZE);

  }

  private static void prepareTargets(Client client) {
    String server =
        "https://localhost:" + LdapServiceTestServer.TOMCAT_PORT_HTTPS + "/api/annuaire";
    targets = new HashMap<String, WebTarget>();
    targets.put("contacts", client.target(server + "/contacts"));
    targets.put("contact", client.target(server + "/contact"));
    targets.put("organisation", client.target(server + "/organisation"));
    targets.put("organisations", client.target(server + "/organisations"));
    targets.put("/", client.target(server + "/"));
  }

  private static SSLContext getUntrustContext() {
    // Create a trust manager that does not validate certificate chains
    TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
      public X509Certificate[] getAcceptedIssuers() {
        return null;
      }

      public void checkClientTrusted(X509Certificate[] certs, String authType) {}

      public void checkServerTrusted(X509Certificate[] certs, String authType) {}
    }

    };

    // Install the all-trusting trust manager
    try {
      SSLContext sc = SSLContext.getInstance("TLS");
      sc.init(null, trustAllCerts, new SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
      return sc;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

}
