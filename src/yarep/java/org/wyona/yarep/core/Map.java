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
    public void readConfig(Configuration storageConfig, File repoConfigFile);
}
