package org.wyona.yarep.core;

import org.apache.avalon.framework.configuration.Configuration;

import java.io.File;

/**
 *
 */
public interface Map {

    /**
     *
     */
    public void readConfig(Configuration mapConfig, File repoConfigFile);

    /**
     *
     */
    public boolean isResource(Path path);

    /**
     *
     */
    public boolean isCollection(Path path);

    /**
     *
     */
    public boolean exists(Path path);

    /**
     *
     */
    public boolean delete(Path path);

    /**
     *
     */
    public Path[] getChildren(Path path);

    /**
     *
     */
    public UID getUID(Path path);

    /**
     *
     */
    public UID createUID(Path path);
}
