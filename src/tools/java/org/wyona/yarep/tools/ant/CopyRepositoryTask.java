
package org.wyona.yarep.tools.ant;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.wyona.yarep.core.Repository;
import org.wyona.yarep.core.RepositoryException;
import org.wyona.yarep.core.RepositoryFactory;
import org.wyona.yarep.util.YarepUtil;


public class CopyRepositoryTask extends Task {
    private Path srcRepoConfigPath;
    private Path destRepoConfigPath;
    private String srcRepoID;
    private String destRepoID;

    /** (non-Javadoc)
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute() throws BuildException {
        try {
            System.out.println("src repo id: " + getSrcRepoID());
            System.out.println("src repo config path: " + getSrcRepoConfigPath());
            System.out.println("dest repo id: " + getDestRepoID());
            System.out.println("dest repo config path: " + getDestRepoConfigPath());
            
            RepositoryFactory repoFactory = new RepositoryFactory();
            
            //File srcRepoConfigFile = new File(getSrcRepoConfigPath().toString());
            Repository srcRepo = repoFactory.newRepository(getSrcRepoID());
            //Repository srcRepo = repoFactory.newRepository(getSrcRepoID(), srcRepoConfigFile);
            
            //File destRepoConfigFile = new File(getDestRepoConfigPath().toString());
            Repository destRepo = repoFactory.newRepository(getDestRepoID());
            //Repository destRepo = repoFactory.newRepository(getDestRepoID(), destRepoConfigFile);
            
            System.out.println("starting to copy...");
            YarepUtil.copyRepository(srcRepo, destRepo);
            System.out.println("done.");
            
        } catch (RepositoryException e) {
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
