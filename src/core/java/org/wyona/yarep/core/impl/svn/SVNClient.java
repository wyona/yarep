package org.wyona.yarep.core.impl.svn;

import org.wyona.commons.io.FileUtil;
import org.wyona.yarep.core.NoSuchNodeException;
import org.wyona.yarep.core.Path;
import org.wyona.yarep.core.Storage;
import org.wyona.yarep.core.UID;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Date;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.log4j.Category;

import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusType;

/**
 * The SVNClient provides standard svn methods for commit, checkout, update,
 * delete, etc. It uses the SVNKit library, see www.svnkit.com
 */
public class SVNClient {

    private static Category log = Category.getInstance(SVNClient.class);

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

}
