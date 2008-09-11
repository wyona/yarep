package org.wyona.yarep.core.search;

import org.apache.log4j.Logger;

import java.util.HashMap;

/**
 * Metatdata is used to pass some additional information to the indexer
 */
public class Metadata {
    
    static Logger log = Logger.getLogger(Metadata.class);
    HashMap hm = new HashMap();

    /**
     *
     */
    public String get(String name) {
        return (String) hm.get(name);
    }

    /**
     *
     */
    public void set(String name, String value) {
        hm.put(name, value);
    }
}
