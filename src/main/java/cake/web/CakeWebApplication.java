package cake.web;

import java.io.File;

import org.apache.catalina.startup.Tomcat;

public class CakeWebApplication {
    private CakeWebApplication() {
        // Prevent instantiation
    }

    public static void start(String[] args) throws Exception {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.getConnector();

        String docBase = new File(".").getAbsolutePath();
        var ctx = tomcat.addContext("", docBase);

        Tomcat.addServlet(ctx, "cake", RootServlet.class.getName());
        ctx.addServletMappingDecoded("/*", "cake");
        
        tomcat.start();

        System.out.println("🚀 Cake-Web started at " + tomcat.getServer().getAddress() + ":8080/");

        tomcat.getServer().await();
    }
}

