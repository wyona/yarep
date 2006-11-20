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
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;

/**
 * Subversion based storage implementation.
 *  
 * Configuration parameters: 
 *   - src: URL of subversion repository (including repository path) 
 *   - workingdir: directory where the working copy of the repository will be checked out 
 *   - username: svn username 
 *   - password: svn password
 * 
 * When the storage is started, the working copy will be updated or checked out.
 * The working dir will be created automatically in case it does not exist. In
 * the current implementation, read requests don't do an update before reading for
 * performance reasons. 
 * Write requests are committed when the streams are being closed.
 * Locking is not implemented yet.
 */
public class SVNStorage implements Storage {

    private static Category log = Category.getInstance(SVNStorage.class);

    protected SVNClient svnClient;

    protected SVNURL svnRepoUrl;

    protected File svnWorkingDir;

    /**
     * Reads repository configuration and checks out / updates the local working
     * copy. 
     * TODO: checkout/update should be moved to a separate init() method.
     */
    public void readConfig(Configuration storageConfig, File repoConfigFile) throws Exception {
        Configuration contentConfig = storageConfig.getChild("content", false);
        svnRepoUrl = SVNURL.parseURIEncoded(contentConfig.getAttribute("src"));
        svnWorkingDir = new File(contentConfig.getAttribute("workdir"));
        if (!svnWorkingDir.isAbsolute()) {
            svnWorkingDir = FileUtil.file(repoConfigFile.getParent(), svnWorkingDir.toString());
        }
        String username = contentConfig.getAttribute("username");
        String password = contentConfig.getAttribute("password");

        log.debug("SVN host URL: " + svnRepoUrl.toString());
        log.debug("SVN working dir: " + svnWorkingDir.getAbsolutePath());

        if (!svnWorkingDir.isDirectory()) {
            svnWorkingDir.mkdirs();
        }

        svnClient = new SVNClient(username, password);

        // check out or update repository:
        if (svnWorkingDir.listFiles().length == 0) {
            log.info("checking out repository " + svnRepoUrl + " to " + svnWorkingDir);
            long rev = svnClient.checkout(svnRepoUrl, svnWorkingDir);
            log.info("checked out revision " + rev);
        } else {
            log.info("updating " + svnWorkingDir);
            long rev = svnClient.update(svnWorkingDir, SVNRevision.HEAD, true);
            svnClient.checkStatus(svnWorkingDir);
            log.info("updated to revison " + rev);
        }
    }

    /**
     * 
     */
    public OutputStream getOutputStream(UID uid, Path path) throws IOException {
        File file = getFile(uid);
        return new SVNRepositoryOutputStream(file, svnClient);
    }

    /**
     * 
     */
    public InputStream getInputStream(UID uid, Path path) throws IOException {
        File file = getFile(uid);
        return new SVNRepositoryInputStream(file);
    }

    /**
     * 
     */
    public long getLastModified(UID uid, Path path) {
        File file = getFile(uid);
        try {
            Date date = svnClient.getCommittedDate(file);
            return date.getTime();
        } catch (SVNException e) {
            // TODO
            log.error(e);
        }
        return 0;
    }

    /**
     * 
     */
    public boolean delete(UID uid, Path path) {
        File file = getFile(uid);
        try {
            svnClient.delete(file);
            svnClient.commit(file, "yarep automated commit");
            return true;
        } catch (SVNException e) {
            // TODO
            log.error(e);
        }
        return false;
    }

    /**
     * @deprecated
     */
    public Writer getWriter(UID uid, Path path) {
        return null;
    }

    /**
     * @deprecated
     */
    public Reader getReader(UID uid, Path path) throws NoSuchNodeException {
        return null;
    }

    protected File getFile(UID uid) {
        return new File(svnWorkingDir.getAbsolutePath() + File.separator + uid.toString());
    }

}
