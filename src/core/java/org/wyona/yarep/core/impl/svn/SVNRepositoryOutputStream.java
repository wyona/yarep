package org.wyona.yarep.core.impl.svn;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.apache.log4j.Category;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNStatusType;

/**
 * 
 */
public class SVNRepositoryOutputStream extends OutputStream {

    private static Category log = Category.getInstance(SVNRepositoryOutputStream.class);

    private FileOutputStream out;

    private SVNClient svnClient;

    private File file;

    /**
     * 
     */
    public SVNRepositoryOutputStream(File file, SVNClient svnClient) throws IOException {
        this.svnClient = svnClient;
        try {
            this.file = file;

            File parent = new File(file.getParent());
            if (!parent.exists()) {
                log.warn("Directory will be created: " + parent);
                // parent.mkdirs();
                svnClient.addDirectory(parent); // parent dirs will be created and added (svn add)
            }

            log.debug(file.toString());
            out = new FileOutputStream(file);
        } catch (Exception e) {
            log.error(e);
            throw new IOException(e.getMessage());
        }
    }

    /**
     * 
     */
    public void write(int b) throws IOException {
        out.write(b);
    }

    /**
     * 
     */
    public void close() throws IOException {
        out.close();

        try {
            SVNStatusType status = svnClient.getStatus(file);

            if (status == SVNStatusType.STATUS_UNVERSIONED) {
                log.debug("adding new file: " + file.getAbsolutePath());
                svnClient.addFile(file);
                ArrayList pathsList = getPathsToCommit(file);
                File[] paths = new File[pathsList.size()];
                paths = (File[]) pathsList.toArray(paths);
                svnClient.commit(paths, "yarep automated commit");
            } else if (status == SVNStatusType.STATUS_MODIFIED) {
                log.debug("checking in modified file: " + file.getAbsolutePath());
                svnClient.commit(file, "yarep automated commit");
            } else {
                throw new IOException("Invalid status of " + file.getAbsolutePath() + ": " 
                        + status);
            }
        } catch (SVNException e) {
            log.error(e);
            throw new IOException("could not commit file " + file.getAbsolutePath());
        }
    }

    protected ArrayList getPathsToCommit(File file) throws SVNException {
        SVNStatusType status = svnClient.getStatus(file);
        if (status == SVNStatusType.STATUS_ADDED) {
            File parent = file.getParentFile();
            ArrayList list = getPathsToCommit(parent);
            list.add(file);
            return list;
        }
        return new ArrayList();

    }
}
