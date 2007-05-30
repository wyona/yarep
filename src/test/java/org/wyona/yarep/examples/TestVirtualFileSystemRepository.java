package org.wyona.yarep.examples;

import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.Repository;
import org.wyona.yarep.core.RepositoryFactory;

import java.io.File;

/**
 *
 */
public class TestVirtualFileSystemRepository {

    /**
     *
     */
    public static void main(String[] args) {

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
            repo = repoFactory.newRepository("vfs-example", new File("new-vfs-example/repository.xml"));
        } catch (Exception e) {
            System.err.println(e);
            return;
        }

        System.out.println(repoFactory);

        try {
            Node rootNode = repo.getNode("/");
            System.out.println("Root node: " + rootNode.isCollection());
        } catch (Exception e) {
            System.err.println(e);
            return;
        }
    }
}
