package org.wyona.yarep.tools.cmdl;

import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.Repository;
import org.wyona.yarep.core.RepositoryFactory;
import org.wyona.yarep.core.Revision;
import org.wyona.yarep.impl.search.lucene.LuceneSearcher;

import java.io.File;

import org.apache.log4j.Logger;

/**
 * Utility to clean index, such as for example delete documents from index which reference repository nodes, which do not exist anymore
 */
public class CleanIndex {

    private static Logger log = Logger.getLogger(CleanIndex.class);

    /**
     *
     */
    public static void main(String[] args) {

        if(args.length != 3) {
            System.out.println("Usage: <data respository configuration> <boolean delete flag> <number of nodes to be deleted>");
            return;
        }

        RepositoryFactory repoFactory;
        try {
            repoFactory = new RepositoryFactory();
        } catch (Exception e) {
            System.err.println(e);
            return;
        }

        System.out.println(repoFactory);
        System.out.println("\n\n");

        Repository repo;
        try {
            repo = repoFactory.newRepository("my-repo", new File(args[0]));
            LuceneSearcher luceneSearcher = (LuceneSearcher) repo.getSearcher();
            boolean delete = new Boolean(args[1]).booleanValue();
            int listSizeLimit = new Integer(args[2]).intValue();
            String[] missingNodePaths = luceneSearcher.getMissingNodes(delete, listSizeLimit);
            for (int i = 0; i < missingNodePaths.length; i++) {
                System.out.println("Missing node: " + missingNodePaths[i]);
            }
        } catch (Exception e) {
            System.err.println(e);
            return;
        }

        System.out.println(repoFactory);
    }
}
