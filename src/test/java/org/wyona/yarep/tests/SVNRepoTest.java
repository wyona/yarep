package org.wyona.yarep.tests;

import org.wyona.yarep.core.Path;
import org.wyona.yarep.core.Repository;
import org.wyona.yarep.core.RepositoryFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;
import java.io.Writer;

import junit.framework.TestCase;

/**
 * Test for the SVN Storage implementation.
 */
public class SVNRepoTest extends TestCase {

    protected Repository repo;
    
    public void setUp() throws Exception {
        RepositoryFactory repoFactory = new RepositoryFactory();
        repo = repoFactory.newRepository("svn-example", new File(
                "svn-example/config/repository.xml"));
    }
    
    public void testWriteRead() throws Exception {

        // this path may or may not exist in the repository.
        // if if does not exist, it will be created.
        Path path = new Path("/test/foo/bar.txt");

        // Write content to repository
        System.out.println("\nWriting content to repository " + repo.getName());
        Writer writer = repo.getWriter(path);
        String testContent = "Hello World! " + System.currentTimeMillis();
        writer.write(testContent);
        writer.close();
        
        assertTrue("Path does not exist, although it should have been created: " + path, repo.exists(path));

        // Read content from repository
        System.out.println("\nReading content from repository " + repo.getName());
        Reader reader = repo.getReader(path);
        BufferedReader br = new BufferedReader(reader);
        String line = br.readLine();
        br.close();
        reader.close();
        
        assertEquals(line, testContent);
        
    }    
}
