package org.wyona.yarep.impl.repo.vfs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

import org.wyona.yarep.core.NoSuchRevisionException;
import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.NodeStateException;
import org.wyona.yarep.core.NodeType;
import org.wyona.yarep.core.Path;
import org.wyona.yarep.core.Property;
import org.wyona.yarep.core.PropertyType;
import org.wyona.yarep.core.RepositoryException;
import org.wyona.yarep.core.Revision;
import org.wyona.yarep.core.UID;
import org.wyona.yarep.core.attributes.VersionableV1;
import org.wyona.yarep.impl.AbstractNode;
import org.wyona.yarep.impl.DefaultProperty;

/**
 * Utility class based on lucene to index and search revisions of a node by date
 */
public class DateIndexerSearcherImplV2 implements DateIndexerSearcher {
    private static Logger log = Logger.getLogger(DateIndexerSearcherImplV2.class);

    private File metaDir;
    private Node node;
    private File indexDir;

    /**
     * @param node Node for revisions shall be indexed by date
     * @param metaDir Meta directory of this node
     */
    public DateIndexerSearcherImplV2(Node node, File metaDir) {
        this.node = node;
        this.metaDir = metaDir;
        indexDir = new File(metaDir, "lucene_index_data_utc");
    }

    /**
     * Check if date index already exists
     */
    public boolean indexExists() {
        return indexDir.isDirectory();
    }

    /**
     * Get (next) revision older than a specific date
     * @param date Date which is used as reference
     */
    public Revision getRevisionOlderThan(Date date) throws Exception {
        //log.debug("Get revision older than: " + DateIndexerSearcherImplV1.format(date));
        Date olderThanDate = new Date(date.getTime() - 1);

        Revision revision = getRevision(olderThanDate);
        if (revision != null && date.getTime() > revision.getCreationDate().getTime()) {
            return revision;
        } else {
            log.warn("There seems to be NO revision older than: " + DateIndexerSearcherImplV1.format(date));
            return null;
        }
    }

    /**
     * Get most recent revision
     * @return Most recent (head) revision, and if no such revision exists, then return null
     */
    public Revision getMostRecentRevision() {
        try {
/*
            File dateIndexBaseDir = new File(this.metaDir, DATE_INDEX_BASE_DIR);
            String[] years = DateIndexerSearcherImplV1.sortAlphabeticallyAscending(dateIndexBaseDir.list());
            if (years != null && years.length > 0) {
                //log.debug("Year: " + years[years.length - 1]); // INFO: Descend, e.g. 2012, 2011, 2010, 2009, ...
                //return getRevisionFromIndexFile(getYoungestRevisionOfYear(new File(dateIndexBaseDir, years[years.length - 1])));
                return null; // TODO
            }
            log.warn("No year and hence no revision: " + dateIndexBaseDir);
*/
            return null;
        } catch(Exception e) {
            log.error(e, e);
            return null;
        }
    }

    /**
     * Get oldest revision
     * @return Oldest revision, and if no such revision exists, then return null
     */
    public Revision getOldestRevision() {
        try {
            // TODO: Find oldest year, set date to one year below oldest year and then use getRevisionOlderThan(Date date)
            log.warn("Implementation not finished yet!");
/*
            File dateIndexBaseDir = new File(this.metaDir, DATE_INDEX_BASE_DIR);
            String[] years = DateIndexerSearcherImplV1.sortAlphabeticallyAscending(dateIndexBaseDir.list());
            if (years != null && years.length > 0) {
                //log.debug("Year: " + years[years.length - 1]); // INFO: Descend, e.g. 2012, 2011, 2010, 2009, ...
                //return getRevisionFromIndexFile(getYoungestRevisionOfYear(new File(dateIndexBaseDir, years[years.length - 1])));
                return null; // TODO
            }
            log.warn("No year and hence no revision: " + dateIndexBaseDir);
*/
            return null;
        } catch(Exception e) {
            log.error(e, e);
            return null;
        }
    }

    /**
     * @see org.wyona.yarep.impl.repo.vfs.DateIndexerSearcher#getRevision(Date)
     */
    public Revision getRevision(Date date) throws Exception {
        //log.debug("Get revision for date: " + DateIndexerSearcherImplV1.format(date));
/*
        File dateIndexBaseDir = new File(this.metaDir, DATE_INDEX_BASE_DIR);
        log.debug("Use vfs-repo specific implementation: " + node.getPath() + ", " + date);
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(java.util.TimeZone.getTimeZone(TIME_ZONE_ID));
        cal.setTime(date);
*/

        return null; // TODO
/*
        String path = getRevisionByYear(dateIndexBaseDir, cal);
        if (path == null) {
            log.debug("No index file found for date: " + date);
            return null;
        }
        if (path != null && !new File(path).isFile()) {
            log.warn("No such index file: " + path);
            return null;
        }
        String revisionName = getRevisionName(path);
        try {
            return node.getRevision(revisionName);
        } catch(NoSuchRevisionException e) {
            if (new File(path).isFile()) {
                log.warn("It seems that the index is out of sync, because an index file exists '" + path + "', but no such revision: " + revisionName);
            }
            throw e;
        }
        //return getRevisionByYear(dateIndexBaseDir, cal);
*/
    }

    /**
     * Delete revision from date index
     * @param revisionName Name of revision to be deleted
     */
    public void deleteRevision(String revisionName) throws Exception {
        if (!indexExists()) {
            log.warn("No index yet, hence cannot delete revision '" + revisionName + "' from index.");
            return;
        }

/*
        File dateDirF = getRevisionDateDir(revisionName);
        File revisionIdFile = new File(dateDirF, DATE_INDEX_ID_FILENAME);
        if (revisionIdFile.isFile()) {
            String indexedRevisionName = getRevisionName(revisionIdFile.getAbsolutePath());
            if (indexedRevisionName.equals(revisionName)) {
                log.warn("Delete revision date/ID file: " + revisionIdFile);
                revisionIdFile.delete();
            } else {
                log.warn("Revision name '" + revisionName + "' is not equals revision name inside index file: " + revisionIdFile);
            }
        } else {
           log.warn("No such revision date file: " + revisionIdFile);
        }
*/
    }

    /**
     * @see org.wyona.yarep.impl.repo.vfs.DateIndexerSearcher#addRevision(String)
     */
    public void addRevision(String revisionName) throws Exception {
        if (!indexExists()) {
            buildDateIndex();
        }

        Date creationDate = node.getRevision(revisionName).getCreationDate(); // WARN: Older creation dates might not have milliseconds and hence are not corresponding exactly with the revision name, hence in order to build the date index correctly one needs to use the creation date
        log.warn("DEBUG: Add revision '" + revisionName + "' with creation date '" + creationDate + "' to date index ...");

        Document doc = new Document();
        doc.add(new Field("cdate", "" + creationDate.getTime(), Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("rname", revisionName, Field.Store.YES, Field.Index.NOT_ANALYZED));

        IndexWriter iw = getIndexWriter();
        Term revisionNameTerm = new Term("rname", revisionName);
        iw.updateDocument(revisionNameTerm, doc);
        iw.optimize();
        iw.close();
    }

    /**
     * Build date index in order to retrieve revisions more quickly based on creation date
     */
    public void buildDateIndex() throws Exception {
/*
        File dateIndexBaseDir = new File(this.metaDir, DATE_INDEX_BASE_DIR);

        if (!dateIndexBaseDir.isDirectory()) {
            dateIndexBaseDir.mkdirs();
        }

        log.warn("Build date index '" + dateIndexBaseDir + "', whereas this should happen only once when no index exists yet (or has been manually deleted again). Please note that the reading of the revisions must be based on the implementation VirtualFileSystemNode#readRevisions()!");
        Revision[] revisions = node.getRevisions();
        for (int i = revisions.length - 1; i >= 0; i--) {
            addRevision(revisions[i].getRevisionName());
        }
*/
    }

    /**
     * Get lucene index writer
     */
    private IndexWriter getIndexWriter() throws Exception {
        IndexWriter iw = null;
        org.apache.lucene.analysis.Analyzer analyzer = new org.apache.lucene.analysis.WhitespaceAnalyzer();
        if (indexDir.isDirectory()) {
           try {
               iw = new IndexWriter(indexDir.getAbsolutePath(), analyzer, false);
           } catch (FileNotFoundException e) {
               log.warn("Index directory '" + indexDir.getAbsolutePath() + "' seems to exist, but probably no segment files yet!");
               iw = new IndexWriter(indexDir.getAbsolutePath(), analyzer, true);
           }
        } else {
            indexDir.mkdirs();
            iw = new IndexWriter(indexDir.getAbsolutePath(), analyzer, true);
        }
        return iw;
    }
}
