
package org.wyona.yarep.tools.ant;

import java.io.File;

import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.wyona.yarep.core.Repository;
import org.wyona.yarep.core.RepositoryException;
import org.wyona.yarep.core.RepositoryFactory;
import org.wyona.yarep.impl.repo.jcr.JCRRepository;
import org.wyona.yarep.util.YarepUtil;


public class CopyRepositoryTask extends Task {
    private static Logger log = Logger.getLogger(JCRRepository.class);
    
    private Path srcRepoConfigPath;
    private Path destRepoConfigPath;
    private String srcRepoID;
    private String destRepoID;

    /** (non-Javadoc)
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute() throws BuildException {
        try {
            log.info("src repo id: " + getSrcRepoID());
            log.info("src repo config path: " + getSrcRepoConfigPath());
            log.info("dest repo id: " + getDestRepoID());
            log.info("dest repo config path: " + getDestRepoConfigPath());
            
            RepositoryFactory repoFactory = new RepositoryFactory();
            
            String[] ids = repoFactory.getRepositoryIDs();
            if (ids.length == 0) {
                throw new BuildException("no repositories found!");
            }
            for (int i = 0; i < ids.length; i++) {
                log.info("repository: " + ids[i]);
            }
            //File srcRepoConfigFile = new File(getSrcRepoConfigPath().toString());
            Repository srcRepo = repoFactory.newRepository(getSrcRepoID());
            //Repository srcRepo = repoFactory.newRepository(getSrcRepoID(), srcRepoConfigFile);
            if (srcRepo == null) {
                throw new BuildException("src repo [" + getSrcRepoID() + "] is null!");
            }
            
            //File destRepoConfigFile = new File(getDestRepoConfigPath().toString());
            Repository destRepo = repoFactory.newRepository(getDestRepoID());
            //Repository destRepo = repoFactory.newRepository(getDestRepoID(), destRepoConfigFile);
            if (destRepo == null) {
                throw new BuildException("dest repo [" + getDestRepoID() + "] is null!");
            }
            
            
            log.info("starting to copy...");
            YarepUtil.copyRepository(srcRepo, destRepo);
            destRepo.close();
            srcRepo.close();
            log.info("done.");
            
        } catch (Exception e) {
            e.printStackTrace(System.err);
            throw new BuildException(e.getMessage(), e);
        }

    }
    
    public Path getDestRepoConfigPath() {
        return destRepoConfigPath;
    }

    public void setDestRepoConfigPath(Path destRepoConfigPath) {
        this.destRepoConfigPath = destRepoConfigPath;
    }

    public String getDestRepoID() {
        return destRepoID;
    }

    public void setDestRepoID(String destRepoID) {
        this.destRepoID = destRepoID;
    }

    public Path getSrcRepoConfigPath() {
        return srcRepoConfigPath;
    }

    public void setSrcRepoConfigPath(Path srcRepoConfigPath) {
        this.srcRepoConfigPath = srcRepoConfigPath;
    }

    public String getSrcRepoID() {
        return srcRepoID;
    }

    public void setSrcRepoID(String srcRepoID) {
        this.srcRepoID = srcRepoID;
    }

}
