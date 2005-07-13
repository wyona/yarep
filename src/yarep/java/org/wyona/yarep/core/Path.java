package org.wyona.yarep.core;

/**
 *
 */
public class Path {

    private String path;

    /**
     *
     */
    public Path() {
    }

    /**
     *
     */
    public Path(String path) {
        this.path = path;
    }

    /**
     *
     */
    public String getName() {
        // Quick and dirty
        return new java.io.File(path).getName();
    }

    /**
     *
     */
    public String toString() {
        return path;
    }
}
