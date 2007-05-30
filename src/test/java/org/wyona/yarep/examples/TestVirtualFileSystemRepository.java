package org.wyona.yarep.examples;

import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.Repository;
import org.wyona.yarep.core.RepositoryFactory;

import java.io.File;

import org.apache.log4j.Category;

/**
 *
 */
public class TestVirtualFileSystemRepository {

    private static Category log = Category.getInstance(TestVirtualFileSystemRepository.class);

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
            Node root = repo.getNode("/");
            System.out.println("Root node: " + root.isCollection());

            Node child = root.getNode("hello-world.txt");
            System.out.println("Child node: " + child.isCollection());
            System.out.println("Child node: " + child.isResource());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            System.err.println(e);
            return;
        }
    }
}
