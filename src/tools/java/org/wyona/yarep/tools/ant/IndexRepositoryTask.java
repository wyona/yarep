
package org.wyona.yarep.tools.ant;

import java.io.File;

import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.wyona.yarep.core.Repository;
import org.wyona.yarep.core.RepositoryException;
import org.wyona.yarep.core.RepositoryFactory;
import org.wyona.yarep.util.YarepUtil;

/**
 * Ant task in order to (re-)index a repository
 */
public class IndexRepositoryTask extends Task {
    private static Logger log = Logger.getLogger(IndexRepositoryTask.class);
    
    private Path repoConfigPath;

    /** (non-Javadoc)
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute() throws BuildException {
        try {
            log.info("Repository configuration path: " + getRepoConfigPath());
            
            RepositoryFactory repoFactory = new RepositoryFactory();
            
            //Repository repo = repoFactory.newRepository(getRepoID());
            Repository repo = repoFactory.newRepository("dummy-id", new File(getRepoConfigPath().toString()));
            if (repo == null) {
                throw new BuildException("Repository [" + getRepoConfigPath() + "] is null!");
            }
            
            log.info("Starting to index...");
            YarepUtil.indexRepository(repo);
            repo.close();
            log.info("Indexing done.");
        } catch (Exception e) {
            e.printStackTrace(System.err);
            throw new BuildException(e.getMessage(), e);
        }

    }
    
    public Path getRepoConfigPath() {
        return repoConfigPath;
    }

    public void setRepoConfigPath(Path repoConfigPath) {
        this.repoConfigPath = repoConfigPath;
    }
}
