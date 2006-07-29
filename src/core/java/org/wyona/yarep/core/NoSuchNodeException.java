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
    public NoSuchNodeException(String s) {
	super(s);
    }

    /**
     *
     */
    public NoSuchNodeException(Path path, Repository repo) {
	super("No such node: " + path + " (Repository: " + repo + ")");
    }
}
