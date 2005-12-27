package org.apache.cocoon.generation;

import org.apache.avalon.framework.component.Component;

import org.apache.cocoon.generation.ComposerGenerator;

import org.apache.excalibur.xml.sax.SAXParser;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.wyona.yarep.core.RepositoryFactory;

import java.io.File;

/**
 * @author Michael Wechner
 */
public class RepositoryFactoryGenerator extends ComposerGenerator {

    // The URI of the namespace of this generator
    private String URI = "";
    //private String URI = "http://www.wyona.org/yarep/1.0";

    /**
     * Generate XML data.
     */
    public void generate() throws SAXException {

        SAXParser parser = null;
        try {
            RepositoryFactory rf = new RepositoryFactory();
            String[] repoIDs = rf.getRepositoryIDs();

            // Start document
            this.contentHandler.startDocument();

            // Start root element
            AttributesImpl attr = new AttributesImpl();
            attr.addAttribute("", "file", "file", "CDATA", new File(rf.getPropertiesURL().getFile()).getAbsolutePath());
            super.contentHandler.startElement(URI, "repositories", "repositories", attr);
            //super.contentHandler.startElement(URI, "repositories", "yarep:repositories", attr);
            attr.clear();
            for (int i = 0; i < repoIDs.length; i++) {
                attr.addAttribute("", "id", "id", "CDATA", "" + repoIDs[i]);
                attr.addAttribute("", "file", "file", "CDATA", "" + rf.newRepository(repoIDs[i]).getConfigFile().getAbsolutePath());
                super.contentHandler.startElement(URI, "repository", "repository", attr);
                attr.clear();
                String data = rf.newRepository(repoIDs[i]).getName();
                super.contentHandler.characters(data.toCharArray(), 0, data.length());
                super.contentHandler.endElement(URI, "repository", "repository");
            }
            super.contentHandler.endElement(URI, "repositories", "repositories");
            //super.contentHandler.endElement(URI, "repositories", "yarep:repositories");

            this.contentHandler.endDocument();


/*
            byte[] sresponse = new byte[1024];
            InputSource input = new InputSource(new ByteArrayInputStream(sresponse));
            parser = (SAXParser) this.manager.lookup(SAXParser.ROLE);
            parser.parse(input, this.xmlConsumer);
*/
        } catch (Exception e) {
            this.contentHandler.startDocument();
            AttributesImpl attr = new AttributesImpl();
            super.contentHandler.startElement(URI, "cvsupdate", "cvsupdate", attr);
            String data = ".generate(): " + e;
            super.contentHandler.characters(data.toCharArray(), 0, data.length());
            super.contentHandler.endElement(URI, "cvsupdate", "cvsupdate");
            attr.clear();
            this.contentHandler.endDocument();

            getLogger().error(".generate():", e);
        } finally {
            this.manager.release((Component) parser);
        }
    }
}
