package org.wyona.yarep.examples;

import org.wyona.yarep.core.Path;
import org.wyona.yarep.core.Repository;
import org.wyona.yarep.core.RepositoryException;
import org.wyona.yarep.core.RepositoryFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.log4j.Category;

/**
 *
 */
public class HelloWorld {

    private static Category log = Category.getInstance(HelloWorld.class);

    /**
     *
     */
    public static void main(String[] args) {

        RepositoryFactory repoFactory;
        try {
            repoFactory = new RepositoryFactory();
            //repoFactory = new RepositoryFactory("my-yarep.properties");
        } catch (Exception e) {
            System.err.println(e);
            return;
        }

        System.out.println(repoFactory);

        Repository repoA;
        Repository repoC;
        Repository repoB;
        Repository repoD;
        Repository repoJCR;
        try {
            repoA = repoFactory.newRepository("example1");
            repoC = repoFactory.newRepository("hugo");
            repoJCR = repoFactory.newRepository("jcr");
    
            // Add more repositories to repository factory
            repoB = repoFactory.newRepository("vanya", new File("orm-example/repository-config.xml"));
            repoD = repoFactory.newRepository("vfs-example", new File("vfs-example/repository.xml"));
        } catch (Exception e) {
            System.err.println(e);
            return;
        }

        System.out.println(repoFactory);




        // Test YarepUtil ...
        Path path;
        try {
            path = new Path("/example2/hello.txt");
            org.wyona.yarep.util.RepoPath rp = new org.wyona.yarep.util.YarepUtil().getRepositoryPath(path, repoFactory);
            System.out.println("YarepUtil: " + rp.getRepo() + " " + rp.getPath());
            path = new Path("/pele/hello/");
            rp = new org.wyona.yarep.util.YarepUtil().getRepositoryPath(path, repoFactory);
            System.out.println("YarepUtil: " + rp.getRepo() + " " + rp.getPath());
        } catch (RepositoryException e) {
            System.err.println(e);
            return;
        }

        Path worldPath = new Path("/hello/world.txt");

        // Write content to repository
        try {
            System.out.println("\nWrite content to repository " + repoA.getName() + " (repoA) ...");
            Writer writerA = repoA.getWriter(worldPath);
            writerA.write("Hello World!\n...");
            writerA.close();

            repoA.addSymbolicLink(worldPath, new Path("/hello-world-link.txt"));

            System.out.println("\nWrite content to repository " + repoJCR.getName() + " (repoJCR) ...");
            repoJCR.getNode("/").setProperty("my-message", "Hello Hugo!");
            System.out.println(repoJCR.getNode("/").getProperty("my-message"));
            Writer writerJCR = repoJCR.getWriter(new Path("/"));
            //Writer writerJCR = repoJCR.getWriter(worldPath);
            writerJCR.write("Hello JCR!\n...");
            writerJCR.close();
            Reader readerJCR = repoJCR.getReader(new Path("/"));
            BufferedReader brJCR = new BufferedReader(readerJCR);
            System.out.println("Very first line of JCR node content: " + brJCR.readLine());
            brJCR.close();

            System.out.println("\nWrite content to repository " + repoB.getName() + " (repoB) ...");
            Writer writerB = repoB.getWriter(worldPath);

// TODO: See TODO.txt re VFS implementation
/*
            System.out.println("\nWrite content to repository " + repoD.getName() + "...");
            Writer writerD = repoD.getWriter(new Path("/hello/vfs-example.txt"));
            writerD.write("Hello VFS example!\n...");
            writerD.close();
*/
        } catch (Exception e) {
            log.error(e, e);
        }

        try {
            repoJCR.close();
        } catch (Exception e) {
            System.err.println(e);
        }

        // DEBUG ...
	//if(true) return;



        // Read content from repository
        System.out.println("\nRead content from repository " + repoA.getName() + " (repoA) ...");
        try {
            Reader readerA = repoA.getReader(worldPath);
            BufferedReader br = new BufferedReader(readerA);
            String line = br.readLine();
            StringWriter strWriter = new StringWriter();
            while (line != null) {
                strWriter.write(line + "\n");
                //System.out.println(line);
                line = br.readLine();
            }
            System.out.println(strWriter.toString());
            strWriter.close();
            br.close();
            readerA.close();

            Path vfsSamplePath = new Path("/hello/vfs-example.txt");
            System.out.println("\nRead content from repository " + repoD.getName() + " (repoD, path: " + vfsSamplePath + ") ...");
            Reader readerD = repoD.getReader(vfsSamplePath);
            br = new BufferedReader(readerD);
            System.out.println("Very first line: " + br.readLine());
            readerD.close();
            // Read directory!
            readerD = repoD.getReader(new Path(vfsSamplePath.getParent().toString()));
            br = new BufferedReader(readerD);
            System.out.println("Very first line: " + br.readLine());
            readerD.close();
            readerD = repoD.getReader(new Path("/"));
            br = new BufferedReader(readerD);
            System.out.println("Very first line: " + br.readLine());
            readerD.close();

            System.out.println("\nUSECASE: Read content from node without a UID \"/no/uid/example.txt\" from repository \"" + repoA.getName() + " (" + repoA.getID() + ")\" ...");
            readerA = repoA.getReader(new Path("/no/uid/example.txt"));
            br = new BufferedReader(readerA);
            System.out.println("Very first line: " + br.readLine());
            readerA.close();
        } catch (Exception e) {
            System.err.println(e);
        }

        try {

            // List children
            System.out.println("\nUSECASE: List children of path \"/hello\" from repository \"" + repoA.getName() + " (" + repoA.getID() + ")\" ...");
            Path helloPath = new Path("/hello");

            Path[] children = repoA.getChildren(helloPath);
            for (int i = 0; i < children.length; i++) {
                System.out.println("Child: " + children[i]);
            }

            // Delete collection with deprecated method
            System.out.println("\nUSECASE: Try to delete \"" + helloPath + "\" from repository \"" + repoA.getName() + " (" + repoA.getID() + ")\" with deprecated method Repository.delete(Path, boolean) ...");
            if (repoA.delete(helloPath, false)) {
                System.out.println("Node '" + helloPath + "' has been deleted.");
            } else {
                System.err.println("Node '" + helloPath + "' could not be deleted!");
            }

            // Delete resource
            System.out.println("\nUSECASE: Try to delete \"" + worldPath + "\" from repository \"" + repoA.getName() + " (" + repoA.getID() + ")\" with deprecated method Repository.delete(Path) ...");
            if (repoA.delete(worldPath)) {
                System.out.println("Node '" + worldPath + "' has been deleted.");
            } else {
                System.err.println("Node '" + worldPath + "' could not be deleted!");
            }

            // Delete collection
            System.out.println("\nUSECASE: Try to delete \"" + helloPath + "\" from repository \"" + repoA.getName() + " (" + repoA.getID() + ")\" ...");
            org.wyona.yarep.core.Node helloNode = repoA.getNode(helloPath.toString());
            try {
                helloNode.delete();
            } catch(Exception e) {
                System.err.println(e);
            }
            if (!repoA.existsNode(helloPath.toString())) {
                System.out.println("Node '" + helloPath + "' has been deleted.");
            } else {
                System.err.println("Node '" + helloPath + "' could not be deleted!");
            }

        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
