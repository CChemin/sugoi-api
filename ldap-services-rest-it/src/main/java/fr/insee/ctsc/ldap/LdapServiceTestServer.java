package fr.insee.ctsc.ldap;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import javax.servlet.ServletException;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Loader;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.valves.RemoteIpValve;
import org.apache.tomcat.JarScanType;
import org.apache.tomcat.util.descriptor.web.LoginConfig;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.apache.tomcat.util.net.SSLHostConfig;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.ThreadPoolFactory;
import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.schema.Schema;
import com.unboundid.ldif.LDIFException;
import org.apache.tomcat.util.scan.StandardJarScanFilter;
import wiremock.org.eclipse.jetty.util.thread.ThreadPool;

public class LdapServiceTestServer {

    private static final String CHARSET = "UTF-8";
    private static String VERSION;
    public static int LDAP_PORT = 10389;
    public static int SPOC_PORT = 10100;
    public static int TOMCAT_PORT = 8181;
    public static int TOMCAT_PORT_HTTPS = 8144;
    public static boolean fork = false;
    public static String apiWarUri;
    public static String ihmWarUri;
    static Path userDir;
    static Tomcat tomcat;
    static WireMockServer spocServer;
    static InMemoryDirectoryServer ds;

    public static boolean tomcatReady = false;
    public static boolean ldapReady = false;
    public static boolean spocReady = false;

    /**
     * Démarrage des services de test.
     */
    public static void main(String[] args) throws InterruptedException, IOException {

        if (args.length == 0 || "start".equalsIgnoreCase(args[0])) {
            if (args.length > 1 && args[1].equals("fork")) {
                fork = true;
            }
            startServers();
        }
        if (args.length > 0 && "stop".equalsIgnoreCase(args[0])) {
            Socket socket = new Socket("localhost", 4567);
            socket.getInputStream();
            socket.close();
        }

    }

    private static void startServers() throws InterruptedException, UnsupportedEncodingException {
        userDir = Paths.get(System.getProperty("user.dir"));
        if (userDir.endsWith("ldap-services")) {
            userDir = userDir.resolve("ldap-services-rest-it");
            System.out.println(userDir);
        }

        // Gestion du truststore
        File truststoreFile = new File(
                URLDecoder.decode(LdapServiceTestServer.class.getResource("/ssl/cacerts.jks").getFile(), CHARSET));
        System.setProperty("javax.net.ssl.trustStore", truststoreFile.getAbsolutePath());
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");

        Properties props = new Properties();
        if (System.getProperty("VERSION") != null) {
            VERSION = System.getProperty("VERSION");
        } else {
            try {
                props.load(LdapServiceTestServer.class.getResourceAsStream("/version.properties"));
                VERSION = props.getProperty("version");

            } catch (Exception e) {

                throw new RuntimeException("Impossible de trouver la version");
            }
        }
        File folder = new File(userDir.toAbsolutePath() + "/../ldap-services-rest-ihm/target/");
        if (folder.exists()) {
            apiWarUri = userDir.toAbsolutePath() + "/../ldap-services-rest-ihm/target/contacts.war";
            ihmWarUri = userDir.toAbsolutePath() + "/../ldap-services-ihm/target/ouganext.war";
        } else {
            apiWarUri = userDir.toAbsolutePath() + "/ldap-services-rest-ihm/target/contacts.war";
            ihmWarUri = userDir.toAbsolutePath() + "/ldap-services-ihm/target/ouganext.war";
        }
        System.out.println("VERSION : " + VERSION);
        System.out.println(new File(".").getAbsolutePath());
        System.out.println(userDir.toFile().getAbsolutePath());
        System.out.println(apiWarUri);
        System.out.println(ihmWarUri);
        if (!new File(apiWarUri).exists() || !new File(ihmWarUri).exists()) {
            throw new RuntimeException("Les war n'existent pas");
        }

        if (System.getProperty("tomcat.port") != null) {
            TOMCAT_PORT = Integer.parseInt(System.getProperty("tomcat.port"));
        }
        if (System.getProperty("ldap.port") != null) {
            LDAP_PORT = Integer.parseInt(System.getProperty("ldap.port"));
        }
        if (System.getProperty("spoc.port") != null) {
            SPOC_PORT = Integer.parseInt(System.getProperty("spoc.port"));
        }

        System.out.println("TOMCAT_PORT : " + TOMCAT_PORT);
        System.out.println("LDAP_PORT : " + LDAP_PORT);
        System.out.println("SPOC_PORT : " + SPOC_PORT);

        ExecutorService execs = Executors.newFixedThreadPool(4, new ThreadFactory() {

            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable);
                thread.setDaemon(true);
                return thread;
            }

        });
        // Ldap server
        execs.submit(() -> {
            try {
                launchLdapServer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        execs.submit(() -> {
            try {
                launchTomcat();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        execs.submit(() -> {
            try {
                launchSpoc();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        execs.submit(() -> {
            try {
                ServerSocket shutdownSocket = new ServerSocket(4567);
                Socket sock = shutdownSocket.accept();
                System.out.println("Arrêt du serveur de test");
                sock.close();
                shutdownSocket.close();
                tomcat.stop();
                ds.shutDown(true);
                spocServer.stop();
            } catch (Exception e) {

                e.printStackTrace();
            }
        });
        while (!spocReady || !tomcatReady || !ldapReady) {
            Thread.sleep(1000);
        }
        if (!fork) {

            execs.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        }

    }

    private static void launchSpoc() {
        spocServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(SPOC_PORT)
                .notifier(new Slf4jNotifier(true)).threadPoolFactory(new ThreadPoolFactory() {

                    @Override
                    public ThreadPool buildThreadPool(Options options) {
                        ThreadPool tp = new QueuedDaemonThreadPool(options.containerThreads());
                        return tp;

                    }
                }));
        spocServer.start();

        spocServer.stubFor(post("/spoc/send").willReturn(aResponse().withStatus(200)));
        spocReady = true;
    }

    private static void launchTomcat() throws IOException, ServletException, LifecycleException {

        System.setProperty("fr.insee.ctsc.ldap.organisation.url.format",
                "http://localhost:" + TOMCAT_PORT + "/api/annuaire/organisation/");

        System.setProperty("fr.insee.ctsc.ldap.profils.url", "localhost");
        System.setProperty("fr.insee.ctsc.ldap.profils.port", Integer.toString(LDAP_PORT));

        System.setProperty("fr.insee.ctsc.ldap.profils.branche",
                "cn=profil-contact-WebServicesLdap,ou=WebServicesLdap_Objets,ou=WebServicesLdap,ou=applications,o=insee,c=fr");

        System.setProperty("fr.insee.ctsc.ldap.connexion.user", "cn= Directory Manager");
        System.setProperty("fr.insee.ctsc.ldap.connexion.pass", "password");

        System.setProperty("fr.insee.ctsc.ldap.log4j.xml", "log4j-it.xml");

        System.setProperty("fr.insee.ctsc.ldap.debug", "false");

        System.setProperty("fr.insee.ctsc.ldap.spoc.url", "http://localhost:" + SPOC_PORT + "/spoc/send");
        System.setProperty("fr.insee.ctsc.ldap.spoc.user", "user");
        System.setProperty("fr.insee.ctsc.ldap.spoc.password", "password");

        System.setProperty("fr.insee.ctsc.ldap.mail.from", "noreply@insee.fr");

        System.setProperty("fr.insee.liste.domaine", "insee.fr;stat-publique.fr");

        System.setProperty("fr.insee.database.wscontact.username", "sa");
        System.setProperty("fr.insee.database.wscontact.password", "");
        System.setProperty("fr.insee.database.wscontact.url",
                "jdbc:h2:mem:test;init=runscript from 'classpath:/db-data/init.sql'");
        System.setProperty("fr.insee.database.wscontact.driverClassName", "org.h2.Driver");
        System.setProperty("fr.insee.database.wscontact.maxActive", "15");
        System.setProperty("fr.insee.database.wscontact.initialSize", "1");
        System.setProperty("java.util.logging.config.file",
                Paths.get(System.getProperty("user.dir")).relativize(userDir)
                        + "/src/main/resources/logging.properties");
        System.setProperty("fr.insee.ctsc.ldap.debugMode", "true");
        System.setProperty("fr.insee.ctsc.ldap.server", "https://localhost:" + TOMCAT_PORT_HTTPS + "/api/annuaire");
        System.setProperty("fr.insee.ctsc.ldap.ws.contact.compte", "webservicesldap");
        System.setProperty("fr.insee.ctsc.ldap.ws.contact.mdp", "webservices");

        System.setProperty("fr.insee.ctsc.fallback.list.domaines", "none");

        System.setProperty("fr.insee.keycloak.realm", "agents-insee-interne");
        System.setProperty("fr.insee.keycloak.server", "https://auth.insee.test/auth");
        System.setProperty("fr.insee.keycloak.resource", "ouganext");
        System.setProperty("fr.insee.keycloak.credentials.secret", "808c94a2-7323-4b75-8f76-692612f5a8b5");

        System.setProperty("log4j.configurationFile", URLDecoder
                .decode(LdapServiceTestServer.class.getResource("/log4j2.xml").getFile(), CHARSET).toString());

        tomcat = new Tomcat();
        File tomcatDir = new File(userDir.toAbsolutePath() + "/target/tomcatit/");
        File webapps = new File(userDir.toAbsolutePath() + "/target/tomcatit/webapps");

        tomcatDir.mkdirs();
        webapps.mkdir();
        tomcat.setBaseDir(tomcatDir.getAbsolutePath());

        // Connecteur HTTP
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setPort(TOMCAT_PORT);
        connector.setScheme("http");
        connector.setSecure(false);
        connector.setRedirectPort(TOMCAT_PORT_HTTPS);
        tomcat.getService().addConnector(connector);

        // Connecteur HTTPS
        Connector connectorHttps = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connectorHttps.setPort(TOMCAT_PORT_HTTPS);
        connectorHttps.setScheme("https");
        connectorHttps.setSecure(true);
        connectorHttps.setAttribute("SSLEnabled", true);
        SSLHostConfig sslHostConfig = new SSLHostConfig();
        sslHostConfig.setSslProtocol("TLS");
        File keystoreFile = new File(
                URLDecoder.decode(LdapServiceTestServer.class.getResource("/ssl/server.p12").getFile(), CHARSET));
        sslHostConfig.setCertificateKeystoreFile(keystoreFile.getAbsolutePath());
        sslHostConfig.setCertificateKeystorePassword("changeit");
        sslHostConfig.setCertificateKeystoreType("pkcs12");
        connectorHttps.addSslHostConfig(sslHostConfig);
        tomcat.getService().addConnector(connectorHttps);

        // Valve pour HTTPS via HAProxy
        RemoteIpValve remoteIpValve = new RemoteIpValve();
        remoteIpValve.setProtocolHeader("X-Forwarded-Proto");

        tomcat.getHost().getPipeline().addValve(remoteIpValve);

        System.out.println(apiWarUri);
        System.out.println(ihmWarUri);
        // contournement bug chelou :
        // https://stackoverflow.com/questions/31374726/spring-boot-how-to-add-another-war-files-to-the-embedded-tomcat
        Context ctxApi = tomcat.addWebapp("/api", new File(apiWarUri).getAbsolutePath());
        ctxApi.setConfigFile(LdapServiceTestServer.class.getResource("/context/context.xml"));
        ctxApi.getJarScanner().setJarScanFilter(new StandardJarScanFilter() {

            @Override
            public boolean check(JarScanType jarScanType, String jarName) {
                if (jarName.contains("xalan") || jarName.contains("xml") || jarName.contains("jaxb")) {
                    return false;
                }
                return super.check(jarScanType, jarName);
            }
        });

        Context ctxIhm = tomcat.addWebapp("/ihm", new File(ihmWarUri).getAbsolutePath());
        LoginConfig config = new LoginConfig();
        config.setAuthMethod("BASIC");

        // adding constraint with role "test"
        SecurityConstraint constraint = new SecurityConstraint();
        constraint.addAuthRole(SecurityConstraint.ROLE_ALL_ROLES);
        ctxIhm.setLoginConfig(config);
        ctxIhm.addConstraint(constraint);
        // ctxIhm.setAltDDName(LdapServiceTestServer.class.getResource("/web/web.xml").toString());
        ctxIhm.getJarScanner().setJarScanFilter(new StandardJarScanFilter() {
            @Override
            public boolean check(JarScanType jarScanType, String jarName) {/**/
                if (jarName.contains("xalan") || jarName.contains("xml") || jarName.contains("jaxb")) {
                    return false;
                }
                return super.check(jarScanType, jarName);
            }
        });
        // contournement bug chelou :
        // https://stackoverflow.com/questions/31374726/spring-boot-how-to-add-another-war-files-to-the-embedded-tomcat

        ctxIhm.setParentClassLoader(LdapServiceTestServer.class.getClassLoader());
        ctxApi.setParentClassLoader(tomcat.getClass().getClassLoader());

        // //Context ctx = tomcat.addWebapp("", System.getProperty("user.dir") +
        // "/src/test/demo");
        // //ctx.getJarScanner().setJarScanFilter(new StandardJarScanFilter() {
        //
        // @Override
        // public boolean check(JarScanType jarScanType, String jarName) {
        // if (jarName.contains("xalan") || jarName.contains("xml"))
        // return false;
        // return super.check(jarScanType, jarName);
        // }
        // });
        // Loader loader = new
        // WebappLoader(tomcat.getServer().getParentClassLoader());
        // loader.setDelegate(true);
        // ctxIhm.setLoader(loader);
        //
        // ctxApi.setLoader(loader);
        tomcat.addUser("admin", "admin");
        tomcat.addRole("admin", "Administrateurs_Ouganext");
        tomcat.start();

        while (tomcat.getServer().getState() != LifecycleState.STARTED) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        tomcatReady = true;
        tomcat.getServer().await();

    }

    private static void launchLdapServer() throws LDAPException, LDIFException, IOException {
        int port = LDAP_PORT;
        InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig("o=insee,c=fr");
        config.setAccessLogHandler(new ConsoleHandler());
        config.addAdditionalBindCredentials("cn=Directory Manager", "password");
        File file = new File(URLDecoder.decode(
                LdapServiceTestServer.class.getResource("/ldap-schema/schema-ldap-test.txt").getFile(), CHARSET));
        config.setSchema(Schema.getSchema(file));
        config.setEnforceSingleStructuralObjectClass(false);
        config.setEnforceAttributeSyntaxCompliance(false);
        config.setGenerateOperationalAttributes(true);

        InMemoryListenerConfig listenerConfig = new InMemoryListenerConfig("listen-10389",
                InetAddress.getLoopbackAddress(), port, null, null, null);
        config.setListenerConfigs(listenerConfig);

        ds = new InMemoryDirectoryServer(config);
        ds.importFromLDIF(true, URLDecoder
                .decode(LdapServiceTestServer.class.getResource("/ldap-data/initldap.ldif").getFile(), CHARSET));

        System.out.println("Started Ldap Server (localhost:" + port + ")");
        ds.startListening();
        ldapReady = true;

    }

}
