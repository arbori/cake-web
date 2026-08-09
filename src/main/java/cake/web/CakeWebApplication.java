package cake.web;

import java.io.File;
import java.util.function.Consumer;
import java.util.logging.Logger;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

import cake.web.configuration.CakeWebConfig;

/**
 * Main entry point for the Cake Web application.
 *
 * <p>This class configures and starts an embedded Tomcat server using the
 * provided {@link CakeWebConfig}. It is not intended to be instantiated.
 * 
 * @since 0.0.45
 * @author Marcelo Arbori Nogueira (marcelo.arbori@gmial.com) 
 */
public class CakeWebApplication {
    private static Logger logger = Logger.getLogger(CakeWebApplication.class.getName());

    private CakeWebApplication() {
        // Prevent instantiation
    }

    /**
     * Run the Cake Web application with an optional configuration callback.
     *
     * @param configurer consumer that can customize the application configuration
     * @throws LifecycleException if Tomcat fails to start
     */
    public static void run(Consumer<CakeWebConfig> configurer) throws LifecycleException {
        CakeWebConfig config = new CakeWebConfig();

        // Apply user configuration if provided.
        if (configurer != null) {
            configurer.accept(config);
        }

        startTomcat(config);
    }

    /**
     * Configure and start the embedded Tomcat server.
     *
     * @param config application configuration settings
     * @throws LifecycleException if Tomcat fails to start
     */
    private static void startTomcat(CakeWebConfig config) throws LifecycleException {
        Tomcat tomcat = new Tomcat();

        tomcat.setPort(config.getPort());
        tomcat.setBaseDir(config.getBaseDir());

        // Initialize the connector before creating the context.
        tomcat.getConnector();

        String docBase = new File(".").getAbsolutePath();
        var ctx = tomcat.addContext(config.getContextPath(), docBase);

        // Register the root servlet and map it to all incoming requests.
        Tomcat.addServlet(ctx, "cake", RootServlet.class.getName());
        ctx.addServletMappingDecoded("/*", "cake");
        
        tomcat.start();

        logger.info("🚀 Cake-Web started at " + tomcat.getServer().getAddress() + ":" + config.getPort() + config.getContextPath());

        tomcat.getServer().await();
    }
}

