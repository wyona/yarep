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

        // if this path does not exist yet, it will be created automatically.
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
    
    public void testRevisions() throws Exception {

        // if this path does not exist yet, it will be created automatically.
        Path path = new Path("/test/foo/bar.txt");
        
        write(path, "revision test 1111");
        String[] revisions1 = repo.getRevisions(path);
        
        System.out.println("revisions after first edit: ");
        for (int i=0; i<revisions1.length; i++) {
            System.out.println("rev[" + i + "]: " + revisions1[i]);
        }
        
        write(path, "revision test 2222");
        String[] revisions2 = repo.getRevisions(path);
        
        System.out.println("revisions after second edit: ");
        for (int i=0; i<revisions2.length; i++) {
            System.out.println("rev[" + i + "]: " + revisions2[i]);
        }
        
        assertTrue("New revision has not been created.", revisions1.length + 1 == revisions2.length);
        
        assertEquals("New revision not found in history.", revisions1[0], revisions2[1]);
    }
    
    protected void write(Path path, String content) throws Exception {
        Writer writer = repo.getWriter(path);
        writer.write(content);
        writer.close();
    }

}
