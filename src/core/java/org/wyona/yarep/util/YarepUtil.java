package org.wyona.yarep.util;

import org.apache.log4j.Category;

import org.wyona.yarep.core.Path;
import org.wyona.yarep.core.Repository;
import org.wyona.yarep.core.RepositoryException;
import org.wyona.yarep.core.RepositoryFactory;

/**
 *
 */
public class YarepUtil {

    private static Category log = Category.getInstance(YarepUtil.class);

    /**
     *
     */
    public RepoPath getRepositoryPath(Path path, RepositoryFactory repoFactory) throws RepositoryException {
        Repository repo = null;

        // Determine possible Repository ID. If such a repo ID doesn't exist, then use ROOT repository
	String[] splittedPath = path.toString().split("/");
        if (splittedPath != null) {
            if (splittedPath.length < 2) {
	        log.debug("Length = " + splittedPath.length + ". Use ROOT repository.");
            } else {
                if (repoFactory.exists(splittedPath[1])) {
                    repo = repoFactory.newRepository(splittedPath[1]);
                    log.debug("New Repository: " + repo.getID() + " - " + repo.getName());

                    log.debug("Repo ID length: " + repo.getID().length());
                    path = new Path(path.toString().substring(repo.getID().length() + 1));
                    log.debug("New Path: " + path);
                    return new RepoPath(repo, path);
                } else {
                    log.debug("No such repository \"" + splittedPath[1] + "\". Use ROOT repository.");
                }
            }
        } else {
            log.debug("Path could not be split. Use ROOT repository.");
        }

        // First repository shall be ROOT repository
        repo = repoFactory.firstRepository();
        log.debug("ROOT Repository: " + repo.getID() + " - " + repo.getName());

        log.debug("Path (still original): " + path);
        return new RepoPath(repo, path);
    }
}
