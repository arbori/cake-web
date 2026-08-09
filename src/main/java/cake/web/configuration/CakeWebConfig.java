package cake.web.configuration;

/**
 * CakeWebConfig is the start node class for configuration strategy.
 * 
 * @since 0.0.45
 * @author Marcelo Arbori Nogueira (marcelo.arbori@gmial.com) 
 */
public class CakeWebConfig {
    private int port = 8080;
    private String contextPath = "";
    private String baseDir = System.getProperty("java.io.tmpdir");

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getContextPath() { return contextPath; }
    public void setContextPath(String contextPath) { this.contextPath = contextPath; }

    public String getBaseDir() { return baseDir; }
    public void setBaseDir(String baseDir) { this.baseDir = baseDir; }
}