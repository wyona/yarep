package org.wyona.yarep.core.impl.svn;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Category;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.ISVNPropertyHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNPropertyData;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatusType;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.wyona.yarep.core.Property;
import org.wyona.yarep.core.RepositoryException;

/**
 * The SVNClient provides standard svn methods for commit, checkout, update,
 * delete, etc. It uses the SVNKit library, see www.svnkit.com
 */
public class SVNClient {

    private static Category log = Category.getInstance(SVNClient.class);
    
    protected DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

    protected SVNClientManager clientManager;

    /**
     * Creates a client manager and sets up the repository factories. 
     */
    public SVNClient(String username, String password) throws SVNException {
        ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
        clientManager = SVNClientManager.newInstance(options, username, password);

        DAVRepositoryFactory.setup(); // http:// and https://
        SVNRepositoryFactoryImpl.setup(); // svn:// and svn+xxx://
        FSRepositoryFactory.setup(); // file:///
    }

    /**
     * Commit a single file (not recursive).
     * 
     * @param file
     * @param commitMessage
     * @throws SVNException
     */
    public void commit(File file, String commitMessage) throws SVNException {
        SVNCommitInfo info = clientManager.getCommitClient().doCommit(new File[] { file }, false,
                commitMessage, false, false);
        if (info.getErrorMessage() != null) {
            throw new SVNException(info.getErrorMessage());
        }
    }

    /**
     * Commit a number of paths (not recursive).
     * 
     * @param files
     * @param commitMessage
     * @throws SVNException
     */
    public void commit(File[] files, String commitMessage) throws SVNException {
        SVNCommitInfo info = clientManager.getCommitClient().doCommit(files, false, commitMessage,
                false, false);
        if (info.getErrorMessage() != null) {
            throw new SVNException(info.getErrorMessage());
        }
    }

    public long checkout(SVNURL svnRepoUrl, File svnWorkingDir) throws SVNException {
        SVNUpdateClient updateClient = clientManager.getUpdateClient();
        updateClient.setIgnoreExternals(false);
        return updateClient.doCheckout(svnRepoUrl, svnWorkingDir, SVNRevision.HEAD,
                SVNRevision.HEAD, true);
    }

    public void delete(File file) throws SVNException {
        clientManager.getWCClient().doDelete(file, false, false);
    }

    public long update(File file, SVNRevision updateToRevision, boolean isRecursive)
            throws SVNException {

        SVNUpdateClient updateClient = clientManager.getUpdateClient();
        updateClient.setIgnoreExternals(false);
        return updateClient.doUpdate(file, updateToRevision, isRecursive);
    }

    public SVNStatusType getStatus(File file) throws SVNException {
        return clientManager.getStatusClient().doStatus(file, false).getContentsStatus();
    }

    /**
     * Recursively checks the status and throws an exception if there are any
     * conflicts or if something else is wrong with the local working copy.
     */
    public void checkStatus(File file) throws SVNException {
        clientManager.getStatusClient().doStatus(file, true, false, false, false,
                new ConsistencyStatusHandler());
    }

    /**
     * Adds a file (not recursive).
     * 
     * @param file
     * @throws SVNException
     */
    public void addFile(File file) throws SVNException {
        clientManager.getWCClient().doAdd(file, false, false, false, false);
    }

    /**
     * Adds a directory and creates it (with parents) if it does not exist. Not
     * recursive.
     * 
     * @param file
     * @throws SVNException
     */
    public void addDirectory(File file) throws SVNException {
        clientManager.getWCClient().doAdd(file, false, true, false, false);
    }

    public Date getCommittedDate(File file) throws SVNException {
        SVNInfo info = clientManager.getWCClient().doInfo(file, SVNRevision.HEAD);
        return info.getCommittedDate();
    }

    /*public long[] getRevisionNumbers(File file) throws SVNException {
        RevisionLogEntryHandler logHandler = new RevisionLogEntryHandler();
        clientManager.getLogClient().doLog(new File[] { file }, SVNRevision.HEAD, SVNRevision.BASE, 
                SVNRevision.create(0), false, false, 100, logHandler);
        return logHandler.getRevisions();
    }*/

    public String[] getRevisionStrings(File file) throws SVNException {
        RevisionLogEntryHandler logHandler = new RevisionLogEntryHandler();
        clientManager.getLogClient().doLog(new File[] { file }, SVNRevision.HEAD, SVNRevision.BASE, 
                SVNRevision.create(0), false, false, 100, logHandler);
        long[] revisions = logHandler.getRevisions();
        //String[] messages = logHandler.getMessages();
        Date[] dates = logHandler.getDates();
        
        String[] revStrings = new String[revisions.length];
        for (int i=0; i<revisions.length; i++) {
            revStrings[i] = String.valueOf(revisions[i]) + "|" + dateFormat.format(dates[i]);
        }
        return revStrings;
    }

    //TODO: this is alpha-quality incomplete code, please do not use as is or rely on the exact interface yet.
    public Map<String, String> getProperties(File file) throws SVNException {
        final Map<String, String> properties = new HashMap<String, String>();
        ISVNPropertyHandler handler = new ISVNPropertyHandler() {
            public void handleProperty(File arg0, SVNPropertyData data)
                    throws SVNException {
                properties.put(data.getName(), data.getValue());
            }
            public void handleProperty(SVNURL arg0, SVNPropertyData arg1)
                    throws SVNException {
                throw new RuntimeException("Not implemented!");
            }
            public void handleProperty(long arg0, SVNPropertyData arg1)
                    throws SVNException {
                throw new RuntimeException("Not implemented!");
            }
        };
        clientManager.getWCClient().doGetProperty(file, null, null, null, false, handler);
        return properties;
    }

    //TODO: this is alpha-quality incomplete code, please do not use as is or rely on the exact interface yet.
    public void setProperties(File file, Property[] properties) throws SVNException {
        for (Property property : properties) {
            try {
                clientManager.getWCClient().doSetProperty(file, property.getName(), property.getValueAsString(), true, false, null);
            } catch (RepositoryException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

}
