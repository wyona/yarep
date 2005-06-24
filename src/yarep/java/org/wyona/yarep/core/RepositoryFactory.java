package org.wyona.yarep.core;

import java.io.File;
import java.net.URL;
import java.net.URI;
import java.util.Properties;

import org.apache.log4j.Category;

import org.wyona.util.FileUtil;

/**
 *
 */
public class RepositoryFactory {

    private static Category log = Category.getInstance(RepositoryFactory.class);

    public static final String DEFAULT_CONFIGURATION_FILE = "yarep.properties";

    private Repository[] repositories;

    /**
     *
     */
    public RepositoryFactory() {
        URL propertiesURL = RepositoryFactory.class.getClassLoader().getResource(DEFAULT_CONFIGURATION_FILE);
        Properties props = new Properties();
        try {
            props.load(propertiesURL.openStream());
            File propsFile = new File(new URI(propertiesURL.toString()));
            log.info(propsFile);

            String[] repositoryConfigs = props.getProperty("configurations").split(",");
            repositories = new Repository[repositoryConfigs.length];
            for (int i = 0;i < repositoryConfigs.length; i++) {
                String idFile[] = repositoryConfigs[i].split(":");
                Repository rt = new Repository(idFile[0], FileUtil.file(propsFile.getParent(), idFile[1]));
                log.info(rt.toString());
                repositories[i] = rt;
            }

            // see src/java/org/wyona/meguni/parser/Parser.java
        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    /**
     *
     */
    public String toString() {
        String s = "Show all repositories listed within yarep.properties:";
        for (int i = 0;i < repositories.length; i++) {
            s = s + "\n" + repositories[i];
        }
        return s;
    }

    /**
     * Get repository from yarep.properties
     *
     * @param rid Repository ID
     */
    public Repository newRepository(String rid) {
        for (int i = 0;i < repositories.length; i++) {
            if (repositories[i].getID().equals(rid)) return repositories[i];
        }
        log.error("No such repository: " + rid);
        return null;
    }

    /**
     * Get repository from specified config, whereas config is being resolved relative to classpath
     */
    public Repository newRepository(File config) {
        if (!config.isAbsolute()) {
            URL configURL = RepositoryFactory.class.getClassLoader().getResource(config.toString());
            try {
                File configFile = new File(new URI(configURL.toString()));
                log.debug("Config file: " + configFile);
                // TODO: what about the repository ID?
                return new Repository("null", configFile);
            } catch (Exception e) {
                log.error(e);
                return null;
            }
        }
        // TODO: what about the repository ID?
        return new Repository("null", config);
    }
}
