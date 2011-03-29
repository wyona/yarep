package org.wyona.yarep.impl.repo.vfs;

import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.Revision;

import java.io.File;

import org.apache.log4j.Logger;

/**
 * Iterator to access revisions by date
 */
public class VFSRevisionIterator implements java.util.Iterator {

    private static Logger log = Logger.getLogger(VFSRevisionIterator.class);

    private Revision currentRevision;
    private DateIndexerSearcher dis;

    /**
     * @param node Yarep node which is supposed to have revisions
     * @param metaDir Meta directory containing revisions index
     */
    public VFSRevisionIterator(Node node, File metaDir) throws Exception {
        dis = new DateIndexerSearcher(node, metaDir);
        if (!dis.indexExists()) {
            dis.buildDateIndex();
        }
        currentRevision = null;
    }

    /**
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
        if (currentRevision == null) {
            if (dis.getMostRecentRevision() != null) {
                return true;
            } else {
                return false;
            }
        } else {
            log.warn("TODO ...");
            return false;
        }
    }

    /**
     * @see java.util.Iterator#next()
     */
    public Object next() throws java.util.NoSuchElementException {
        if (currentRevision == null) {
            currentRevision = dis.getMostRecentRevision();
            return currentRevision;
        } else {
            log.warn("TODO ...");
            return null;
        }
    }

    /**
     * @see java.util.Iterator#remove()
     */
    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not implemented.");
    }
}
