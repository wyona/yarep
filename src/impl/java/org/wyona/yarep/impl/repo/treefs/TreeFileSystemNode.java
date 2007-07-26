package org.wyona.yarep.impl.repo.treefs;

import java.io.File;

import org.apache.log4j.Category;
import org.wyona.yarep.core.RepositoryException;
import org.wyona.yarep.impl.repo.fs.FileSystemNode;

/**
 * This class represents a repository node.
 * A repository node may be either a collection ("directory") or a resource ("file").
 */
public class TreeFileSystemNode extends FileSystemNode {
    private static Category log = Category.getInstance(TreeFileSystemNode.class);

    /**
     * Constructor
     * @throws RepositoryException
     */
    public TreeFileSystemNode(TreeFileSystemRepository repository, String path, String uuid) throws RepositoryException {
        super(repository, path, uuid);
    }
    
    /**
     * Constructor
     * @throws RepositoryException
     */
    protected TreeFileSystemNode(TreeFileSystemRepository repository, String path, String uuid, boolean doInit) throws RepositoryException {
        super(repository, path, uuid);
    }
    
    protected File determineContentFile(String uuid) {
        return new File(this.contentDir, splitUUID(uuid));
    }
    
    protected File determineMetaDir(String uuid) {
        return new File(this.contentDir, splitUUID(uuid) + META_DIR_SUFFIX);
    }

    
    protected String splitUUID(String uuid) {
        int splitInterval = ((TreeFileSystemRepository)getRepository()).getSplitInterval();
        int maxSplits = ((TreeFileSystemRepository)getRepository()).getMaxSplits();
        //System.out.println("uuid: " + uuid);
        //System.out.println("splitInterval: " + splitInterval);
        //System.out.println("maxSplits: " + maxSplits);
        String splitId = "";
        int slashIndex = uuid.indexOf("/");
        String part1 = uuid;
        String part2 = "";
        if (slashIndex > -1) {
            part1 = uuid.substring(0, slashIndex);
            part2 = uuid.substring(slashIndex + 1);
        }
        
        for (int i = 0; i < maxSplits && part1.length() > splitInterval; i++) {
            if (splitId.length() > 0) {
                splitId = splitId + "/";
            }
            splitId = splitId + part1.substring(0, splitInterval);
            part1 = part1.substring(splitInterval);
            //System.out.println("splitLevel: " + splitLevel);
            //System.out.println("part1: " + part1);
        }
        if (part1.length() > 0) {
            splitId = splitId + "/" + part1;
        }
        if (part2.length() > 0) {
            splitId = splitId + "/" + part2;
        }
        //System.out.println("split uuid: " + splitId);
        return splitId;
    }
    
}
