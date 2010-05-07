package org.wyona.yarep.tests;

import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.NodeType;
import org.wyona.yarep.core.Repository;
import org.wyona.yarep.core.RepositoryFactory;
import org.wyona.yarep.core.Revision;

import org.apache.log4j.Logger;

import junit.framework.TestCase;

import java.util.Date;

/**
 * Test the 'virtual filesystem' repository implementation re creating and retrieving revisions.
 */
public class VirtualFilesystemRevisionsTest extends TestCase {

    private static Logger log = Logger.getLogger(VirtualFilesystemRevisionsTest.class);

    private Repository repo;

    private String NODE_NAME = "revision-test.txt";

    /**
     * Setup of repository configuration
     */
    public void setUp() throws Exception {
        RepositoryFactory repoFactory = new RepositoryFactory();
        repo = repoFactory.newRepository("new-vfs"); // INFO: Make sure that this repo is configured within 'src/test/java/yarep.properties'
        //repo = repoFactory.newRepository("vfs-example", new java.io.File("new-vfs-example/repository.xml"));
        log.info("Testing repository: " + repo.getName() + " (" + repo.getConfigFile() + ")");
    }

    /**
     * Test create a new revision
     */
    public void testCreateRevision() throws Exception {
        String path = "/" + NODE_NAME;
        if (repo.existsNode(path)) {
            repo.getNode(path).delete();
        }

        Node node = repo.getRootNode().addNode(NODE_NAME, NodeType.RESOURCE);
        node.checkout("bob");
        node.setMimeType("text/plain");
        java.io.PrintWriter pw = new java.io.PrintWriter(node.getOutputStream());
        pw.print("Hello revision test!");
        pw.close();
        Revision revision = node.checkin("My first revision");

        log.info("Revision has been created: " + revision.getRevisionName());
        assertTrue("Revision has been created: " + revision.getRevisionName(), revision != null);
    }

    /**
     * Test get revision by date (point in time)
     */
    public void testGetRevisionByDate() throws Exception {
        String path = "/" + NODE_NAME;
        //Date pointInTime = new Date(Long.parseLong("1000"));
        Date pointInTime = new Date();
        Revision revision = org.wyona.yarep.util.YarepUtil.getRevision(repo.getNode(path), pointInTime);
        log.info("Revision '" + revision.getRevisionName() + "' (" + new Date(Long.parseLong(revision.getRevisionName())) + ") has been found for point in time: " + pointInTime);
        assertTrue("Revision has been found: " + revision.getRevisionName(), revision != null);
    }

    /**
     * Test escaping/unescaping property names and values
     */
    public void testEscapeUnescapePropertyName() throws Exception {
        String path = "/" + NODE_NAME;

        String name1 = "prefix_name";
        String value1 = "value1" + System.getProperty("line.separator") + "value2";

        Node node = repo.getNode(path);
        node.setProperty(name1, value1);
        Node nodeCopy = repo.getNode(path);
        assertTrue(nodeCopy.getProperty(name1).getValueAsString().equals(value1));


        String name2 = "prefix:name";
        String value2 = "value1:value2";

        node = repo.getNode(path);
        node.setProperty(name2, value2);
        nodeCopy = repo.getNode(path);
        assertTrue(nodeCopy.getProperty(name2).getValueAsString().equals(value2));
    }

    /**
     * Test get revision by date (point in time)
     */
/*
    public void testGetRevisionByDate() throws Exception {
        String path = "/sitetree.xml";
        //Date pointInTime = new java.text.SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss/S").parse("2009/12/31T11:46:37/134");
        //Date pointInTime = new java.text.SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss/S").parse("2009/12/31T11:46:38/134");
        //Date pointInTime = new java.text.SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss/S").parse("2009/12/31T11:46:39/134");
        //Date pointInTime = new java.text.SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss/S").parse("2010/12/31T09:46:23/134");
        //Date pointInTime = new java.text.SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss/S").parse("2010/01/06T21:33:23/134");
        Date pointInTime = new java.text.SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss/S").parse("2010/01/07T21:33:23/134");
        Revision revision = org.wyona.yarep.util.YarepUtil.getRevision(repo.getNode(path), pointInTime);
        log.info("Revision '" + revision.getRevisionName() + "' (" + new Date(Long.parseLong(revision.getRevisionName())) + ") has been found for point in time: " + pointInTime);
        assertTrue("Revision has been found: " + revision.getRevisionName(), revision != null);
    }
*/

    /**
     * Test create many new revision
     */
/*
    public void testCreateRevision() throws Exception {
        String path = "/sitetree.xml";
        Node node = repo.getNode(path);

        for (int i = 0; i < 10000; i++) {
            node.checkout("bob");
            //node.setMimeType("application/xml");

            // TODO: The code below creates empty files, doesn't really matter for testing though
            byte[] buffer = new byte[1024];
            int bytesRead = 0;
            java.io.BufferedInputStream bis = new java.io.BufferedInputStream(node.getInputStream(), 160000);
            java.io.OutputStream out = node.getOutputStream();
            while((bytesRead = bis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            bis.close();
            Revision revision = node.checkin("new revision");
            log.info("Revision has been created: " + revision.getRevisionName());
        }

        assertTrue("Revisions have been created.", true);
    }
*/
}
