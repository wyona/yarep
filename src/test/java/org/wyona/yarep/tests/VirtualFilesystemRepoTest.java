package org.wyona.yarep.tests;

import java.io.File;

import org.wyona.yarep.core.RepositoryFactory;

import org.apache.log4j.Logger;

/**
 * Test for the 'virtual filesystem' repository implementation.
 */
public class VirtualFilesystemRepoTest extends NodeRepoTest {

    private static Logger log = Logger.getLogger(VirtualFilesystemRepoTest.class);

    public void setUp() throws Exception {
        RepositoryFactory repoFactory = new RepositoryFactory();
        repo = repoFactory.newRepository("vfs-example", new File("new-vfs-example/repository.xml"));
        log.info("Testing repository: " + repo.getName());
    }
    
    protected String getCollectionTestPath() {
        return "/another-directory";
    }
    
    protected String getResourceTestName() {
        return "index.html";
    }
    
    protected String getResourceTestPath() {
        return getCollectionTestPath() + "/" + getResourceTestName();
    }
    
    protected String getRevisionTestPath() {
        return "/hello-world.txt";
    }

    
}
