package org.wyona.yarep.impl.repo.vfs;

import org.wyona.yarep.core.Revision;

import java.util.Date;

/**
 * Utility class to index and search revisions of a node by date
 */
public interface DateIndexerSearcher {

    /**
     * Check if date index already exists
     */
    public boolean indexExists();

    /**
     * Get (next) revision older than a specific date
     * @param date Date which is used as reference
     */
    public Revision getRevisionOlderThan(Date date) throws Exception;

    /**
     * Get most recent revision
     * @return Most recent (head) revision, and if no such revision exists, then return null
     */
    public Revision getMostRecentRevision();

    /**
     * Get oldest revision
     * @return Oldest revision, and if no such revision exists, then return null
     */
    public Revision getOldestRevision();

    /**
     * Get revision (based on date index) with a creation date which is equal or just the next older revision than the specified date, e.g. specified date=2011.03.17T17:23:57:09:690, then a revision which has either exactly this creation date or which is the next older revision, e.g. 2011.03.17T17:23:57:09:698
     * @param date Date for which a revision shall be found
     */
    public Revision getRevision(Date date) throws Exception;

    /**
     * Delete revision from date index
     * @param revisionName Name of revision to be deleted
     */
    public void deleteRevision(String revisionName) throws Exception;

    /**
     * Add revision to date index
     */
    public void addRevision(String revisionName) throws Exception;

    /**
     * Build date index in order to retrieve revisions more quickly based on creation date
     */
    public void buildDateIndex() throws Exception;
}
