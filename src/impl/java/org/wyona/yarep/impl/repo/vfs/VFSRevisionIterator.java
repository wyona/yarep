package org.wyona.yarep.impl.repo.vfs;

import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.Revision;

import java.io.File;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 * Iterator to access revisions by date
 */
public class VFSRevisionIterator implements java.util.Iterator {

    private static Logger log = Logger.getLogger(VFSRevisionIterator.class);

    private Revision currentRevision;

    private DateIndexerSearcher dis;
    private Date pointInTime;
    private boolean reverse;

    /**
     * @param dis Yarep node specific date indexer searche whuch is supposed to have revisions
     * @param date Point in time (from where iteration shall start or stop)
     * @param reverse TODO
     */
    public VFSRevisionIterator(DateIndexerSearcher dis, Date date, boolean reverse) throws Exception {
        this.pointInTime = date;
        this.dis = dis;

        this.reverse = reverse;
        if (reverse) {
            log.warn("TODO: Reverse order feature not implemented yet!");
        }

        if (!dis.indexExists()) {
            dis.buildDateIndex();
        }
        currentRevision = null;
    }

    /**
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
        if (currentRevision == null) { // INFO: Iterator is still at the beginning
            if (pointInTime != null) {
                // TODO: What exactly means 'reverse' when using pointInTime?!
                try {
                    if (dis.getRevision(pointInTime) != null) {
                        return true;
                    } else {
                        return false;
                    }
                } catch(Exception e) {
                    log.error(e, e);
                    return false;
                }
            } else {
                if (reverse && dis.getOldestRevision() != null) {
                    return true;
                } else if (dis.getMostRecentRevision() != null) {
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            try {
                // TODO: Implement 'reverse'
                log.debug("Check if there is another revision older than: " + format(currentRevision.getCreationDate()));
                if (dis.getRevisionOlderThan(currentRevision.getCreationDate()) != null) {
                    return true;
                } else {
                    return false;
                }
            } catch(Exception e) {
                log.error(e, e);
                return false;
            }
        }
    }

    /**
     * @see java.util.Iterator#next()
     */
    public Object next() throws java.util.NoSuchElementException {
        if (currentRevision == null) {
            if (pointInTime != null) {
                try {
                    currentRevision = dis.getRevision(pointInTime);
                    //log.debug("First revision found '" + format(currentRevision.getCreationDate()) + "' for point in time: " + format(pointInTime));
                } catch(Exception e) {
                    throw new java.util.NoSuchElementException(e.getMessage());
                }
            } else {
                currentRevision = dis.getMostRecentRevision();
            }
            return currentRevision;
        } else {
            try {
                log.debug("Get revision older than: " + format(currentRevision.getCreationDate()));
                currentRevision = dis.getRevisionOlderThan(currentRevision.getCreationDate());
                if (currentRevision != null) {
                    return currentRevision;
                } else {
                    throw new java.util.NoSuchElementException();
                }
            } catch(Exception e) {
                throw new java.util.NoSuchElementException(e.getMessage());
            }
        }
    }

    /**
     * @see java.util.Iterator#remove()
     */
    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Format date
     */
    private String format(java.util.Date date) {
        return new java.text.SimpleDateFormat("yyyy.MM.dd'T'HH:mm:ss/SZ").format(date);
    }
}
