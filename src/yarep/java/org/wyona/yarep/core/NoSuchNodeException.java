package org.wyona.yarep.core;

import java.io.IOException;

/**
 *
 */
public class NoSuchNodeException extends IOException {

    /**
     *
     */
    public NoSuchNodeException() {
	super();
    }

    /**
     *
     */
    public NoSuchNodeException(Path path) {
	super("No such node: " + path);
    }
}
