package org.wyona.yarep.core;

/**
 *
 */
public class NoSuchNodeException extends RepositoryException {

    /**
     *
     */
    public NoSuchNodeException() {
        super();
    }

    /**
     *
     */
    public NoSuchNodeException(Throwable t) {
        super(t);
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
    public NoSuchNodeException(String s, Throwable t) {
        super(s, t);
    }

    /**
     *
     */
    public NoSuchNodeException(Path path, Repository repo) {
        super("No such node: " + path + " (Repository: " + repo + ")");
    }
}
