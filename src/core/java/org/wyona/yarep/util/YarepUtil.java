package org.wyona.yarep.util;

import org.apache.log4j.Logger;
import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.Path;
import org.wyona.yarep.core.Repository;
import org.wyona.yarep.core.RepositoryException;
import org.wyona.yarep.core.RepositoryFactory;
import org.wyona.yarep.core.Revision;

import java.util.Date;

/**
 * Various yarep utility methods
 */
public class YarepUtil {

    private static Logger log = Logger.getLogger(YarepUtil.class);

    /**
     * Get revision of a specific node for a specific date (or just before)
     * (also see http://en.wikipedia.org/wiki/Point-in-time_recovery)
     *
     * @param node Yarep node for which a specific revision shall be found
     * @param pointInTime Date for which a revision shall be found, whereas the creation date of the revision is equals or older
     */
    public static Revision getRevision(Node node, Date pointInTime) throws RepositoryException {
        if (hasInterfaceImplemented(node, "Versionable", "1")) {
            try {
                return ((org.wyona.yarep.core.attributes.VersionableV1) node).getRevision(pointInTime);
            } catch(Exception e) {
                log.error(e, e);
                throw new RepositoryException(e.getMessage());
            }
        }

        log.warn("Use SLOW implementation!");

        String path = null;
        try {
            path = node.getPath();
            // INFO: Find the revision which was the current revision at (or before) the given date
            // IMPORTANT TODO: Improve this algorithm re performance/scalability
            Revision[] revisions = node.getRevisions();
            if (log.isDebugEnabled()) log.debug("Trying to find revision for node " + node.getPath() + " at time " + pointInTime);
            for (int i = revisions.length - 1; i >= 0; i--) {
                //if (log.isDebugEnabled()) log.debug("Checking revision: " + revisions[i].getName() + " " + revisions[i].getCreationDate());
                Date creationDate = revisions[i].getCreationDate();
                if (creationDate.before(pointInTime) || creationDate.equals(pointInTime)) {
                    if (log.isDebugEnabled()) log.debug("Revision found: " + revisions[i].getRevisionName());
                    return revisions[i];
                }
            }
            // TODO: what should happen in this case?
            log.warn("No revision found for node " + path + " and date " + pointInTime);
            return null;
        } catch (Exception e) {
            log.error(e, e);
            throw new RepositoryException("No revision found for node " + path + " and date " + pointInTime + ": " + e.getMessage(), e);
        }
    }

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

    /**
     * Copies the content of one repository into another repository.
     * Currently copies nodes and properties, but no revisions.
     * @param srcRepo repository to be copied
     * @param destRepo assumed to be empty
     * @throws RepositoryException
     */
    public static void copyRepository(Repository srcRepo, Repository destRepo) throws RepositoryException {
        Node srcRootNode = srcRepo.getRootNode(); 
        
        Node[] childNodes = srcRootNode.getNodes();
        for (int i = 0; i < childNodes.length; i++) {
            importNodeRec(childNodes[i], srcRepo, destRepo);
        }
    }
    
    /**
     * Imports nodes recursively
     * @param srcNode Source node
     * @param srcRepo Source repository
     * @param destRepo Destination repository
     * @throws RepositoryException
     */
    protected static void importNodeRec(Node srcNode, Repository srcRepo, Repository destRepo) throws RepositoryException {
        try {
            String destPath = srcNode.getPath();
            if (log.isInfoEnabled()) log.info("Importing node to "+destPath+"...");
            destRepo.importNode(destPath, srcNode.getPath(), srcRepo);
            // recursively import children
            Node[] childNodes = srcNode.getNodes();
            for (int i = 0; i < childNodes.length; i++) {
                importNodeRec(childNodes[i], srcRepo, destRepo);
            }
        } catch (Exception e) {
            //throw new RepositoryException(e.getMessage(), e);
            log.error("Could not import node: " + srcNode.getPath() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Creates the node named by this abstract pathname, including any necessary but nonexistent parent nodes (similar to java.io.File.mkdirs()).
     * @param repo Repository within the node shall be created
     * @param path Node path
     * @param nodeType Type of node (e.g. resource or collection)
     */
    public static Node addNodes(Repository repo, String path, int nodeType) throws RepositoryException {
        if (repo.existsNode(path)) {
            log.warn("Node '" + path + "' already exists, hence ignore creation, but return existing node instance.");
            return repo.getNode(path);
        } else {
            log.debug("Try to create node: " + path);
            org.wyona.commons.io.Path parentPath = new org.wyona.commons.io.Path(path).getParent();
            if (parentPath != null) {
                Node parentNode = null;
                if (repo.existsNode(parentPath.toString())) {
                    parentNode = repo.getNode(parentPath.toString());
                } else {
                    parentNode = addNodes(repo, parentPath.toString(), org.wyona.yarep.core.NodeType.COLLECTION);
                }
                log.debug("Parent node: " + parentNode.getPath());
                Node childNode = parentNode.addNode(new Path(path).getName().toString(), nodeType);
                log.info("Child node has been created: " + childNode.getPath());
                return childNode;
            } else {
                throw new RepositoryException("Root node does not have a parent!");
            }
        }
    }

    /**
     * Copy yarep node
     * @param repo Data repository
     * @param source Path of source node (to be copied)
     * @param destination Path of destination node (copy of orginial node)
     * @return Destination (copy of) node
     */
    public Node copyNode(Repository repo, String source, String destination) throws RepositoryException, java.io.IOException {
        log.warn("DEBUG: Copy node from '" + source + "' to '" + destination + "'.");
        if (!repo.existsNode(source)) throw new RepositoryException("No such source node: " + source);
        Node src = repo.getNode(source);
        Node dest = YarepUtil.addNodes(repo, destination, org.wyona.yarep.core.NodeType.RESOURCE);

        byte buffer[] = new byte[8192];
        int bytesRead;
        java.io.InputStream in = src.getInputStream();
        java.io.OutputStream out = dest.getOutputStream();
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        in.close();
        out.close();
        return null;
    }

    /**
     * Check if a class/object has an interface with a specific version implemented
     */
    static private boolean hasInterfaceImplemented(Object object, String attribute, String version) {
        boolean implemented = false;
        Class clazz = object.getClass();

        while (!clazz.getName().equals("java.lang.Object") && !implemented) {
            Class[] interfaces = clazz.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                if (interfaces[i].getName().equals("org.wyona.yarep.core.attributes." + attribute + "V" + version)) {
                    implemented = true;
                    break;
                }
                // TODO: Why does this not work?
                //if (interfaces[i].isInstance(iface)) implemented = true;
            }
            clazz = clazz.getSuperclass();
        }
        if (implemented) {
            if (log.isDebugEnabled()) log.debug(clazz.getName() + " does implement " + attribute + "V" + version + " interface!");
        } else {
            if (log.isDebugEnabled()) log.debug(clazz.getName() + " does NOT implement " + attribute + "V" + version + " interface!");
        }
        return implemented;
    }

    /**
     * (Re-)Index repository.
     * @param repo Repository to be (re-)indexed
     * @throws RepositoryException
     */
    public static void indexRepository(Repository repo) throws RepositoryException {
        log.warn("DEBUG: Start indexing repository: " + repo.getName());
        indexNodeRecursively(repo.getRootNode(), repo.getIndexer());
        log.warn("DEBUG: End indexing repository: " + repo.getName());
    }
    
    /**
     * Index nodes recursively
     * @param node Node to be indexed
     * @param indexer Indexer of repository
     * @throws RepositoryException
     */
    protected static void indexNodeRecursively(Node node, org.wyona.yarep.core.search.Indexer indexer) throws RepositoryException {
        try {
            // TODO: Make the ignore configurable. Actually the ignore should already be configurable by the repository (see for example src/test/repository/new-vfs-example/repository.xml)!
            if (node.getName().equals(".svn")) {
                log.warn("Ignore .svn directories.");
                return;
            }

            log.warn("DEBUG: Index node (inluding properties, but no revisions): " + node.getPath());
            indexer.index(node);

            org.wyona.yarep.core.Property[] properties = node.getProperties();
            if (properties != null) {
                for (org.wyona.yarep.core.Property property : properties) { 
                    indexer.index(node, property);
                }
            }

            // TODO: Index also revisions ...

            // INFO: Index children recursively
            Node[] childNodes = node.getNodes();
            for (int i = 0; i < childNodes.length; i++) {
                indexNodeRecursively(childNodes[i], indexer);
            }
        } catch (Exception e) {
            //throw new RepositoryException(e.getMessage(), e);
            log.error("Could not index node: " + node.getPath() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Check if a node is actually a revision
     */
    static public boolean isRevision(Node node) {
        boolean implemented = false;
        Class clazz = node.getClass();

        while (!clazz.getName().equals("java.lang.Object") && !implemented) {
            Class[] interfaces = clazz.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                if (interfaces[i].getName().equals("org.wyona.yarep.core.Revision")) {
                    implemented = true;
                    break;
                }
                // TODO: Why does this not work?
                //if (interfaces[i].isInstance(iface)) implemented = true;
            }
            clazz = clazz.getSuperclass();
        }
        if (implemented) {
            if (log.isDebugEnabled()) log.debug(node.getClass().getName() + " is a Revision!");
        } else {
            if (log.isDebugEnabled()) log.debug(node.getClass().getName() + " is NOT a Revision!");
        }
        return implemented;
    }
}
