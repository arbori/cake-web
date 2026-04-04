package cake.web;

import java.io.File;
import java.util.function.Consumer;
import java.util.logging.Logger;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

import cake.web.configuration.CakeWebConfig;

public class CakeWebApplication {
    private static Logger logger = Logger.getLogger(CakeWebApplication.class.getName());

    private CakeWebApplication() {
        // Prevent instantiation
    }

    public static void run(Consumer<CakeWebConfig> configurer) throws LifecycleException {
        CakeWebConfig config = new CakeWebConfig();

        // Apply user configuration
        if (configurer != null) {
            configurer.accept(config);
        }

        startTomcat(config);
    }

    private static void startTomcat(CakeWebConfig config) throws LifecycleException {
        Tomcat tomcat = new Tomcat();

        tomcat.setPort(config.getPort());
        tomcat.setBaseDir(config.getBaseDir());

        tomcat.getConnector();

        String docBase = new File(".").getAbsolutePath();
        var ctx = tomcat.addContext(config.getContextPath(), docBase);

        Tomcat.addServlet(ctx, "cake", RootServlet.class.getName());
        ctx.addServletMappingDecoded("/*", "cake");
        
        tomcat.start();

        logger.info("🚀 Cake-Web started at " + tomcat.getServer().getAddress() + ":" + config.getPort() + config.getContextPath());

        tomcat.getServer().await();
    }
}

