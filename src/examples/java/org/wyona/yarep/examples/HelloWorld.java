package org.wyona.yarep.examples;

import org.wyona.yarep.core.Path;
import org.wyona.yarep.core.Repository;
import org.wyona.yarep.core.RepositoryFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 *
 */
public class HelloWorld {

    /**
     *
     */
    public static void main(String[] args) {

        RepositoryFactory repFactory = new RepositoryFactory();
        System.out.println(repFactory);

        Repository repoA = new RepositoryFactory().newRepository("example1");
        Repository repoC = new RepositoryFactory().newRepository("hugo");
        Repository repoB = new RepositoryFactory().newRepository(new File("example2/repository-config.xml"));

        // Write content to repository
        System.out.println("\nWrite content to repository ...");
        Writer writerA = repoA.getWriter(new Path("/hello/world.txt"));
        Writer writerB = repoB.getWriter(new Path("/hello/world.txt"));
        try {
            writerA.write("Hello World!\n...");
            writerA.close();
        } catch (Exception e) {
            System.err.println(e);
        }

        // Read content from repository
        System.out.println("\nRead content from repository ...");
        try {
            Reader readerA = repoA.getReader(new Path("/hello/world.txt"));
            BufferedReader br = new BufferedReader(readerA);
            String line = br.readLine();
            StringWriter strWriter = new StringWriter();
            while (line != null) {
                strWriter.write(line);
                System.out.println(line);
                line = br.readLine();
            }
            System.out.println(strWriter.toString());
            strWriter.close();
            readerA.close();
        } catch (Exception e) {
            System.err.println(e);
        }

        // List children
        System.out.println("\nList children ...");
        try {
            Path[] children = repoA.getChildren(new Path("/hello"));
            for (int i = 0; i < children.length; i++) {
                System.out.println(children[i]);
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
