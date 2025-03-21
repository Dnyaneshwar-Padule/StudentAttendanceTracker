package com.attendance;

import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;
import java.util.logging.Logger;

/**
 * Main class to launch the embedded Tomcat server
 */
public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws Exception {
        // Get the port from environment variable or use 5000 as default
        String webPort = System.getenv("PORT");
        if (webPort == null || webPort.isEmpty()) {
            webPort = "5000";
        }

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(Integer.parseInt(webPort));

        // Set the context path and web app directory
        String webappDirLocation = "src/main/webapp/";
        StandardContext ctx = (StandardContext) tomcat.addWebapp("", 
                new File(webappDirLocation).getAbsolutePath());
        LOGGER.info("Configuring app with basedir: " + new File(webappDirLocation).getAbsolutePath());

        // Declare an alternate location for your "WEB-INF/classes" directory
        File additionWebInfClasses = new File("target/classes");
        WebResourceRoot resources = new StandardRoot(ctx);
        resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
                additionWebInfClasses.getAbsolutePath(), "/"));
        ctx.setResources(resources);

        // Start the server
        tomcat.start();
        LOGGER.info("Server started on port: " + webPort);
        LOGGER.info("Application available at http://localhost:" + webPort);
        tomcat.getServer().await();
    }
}