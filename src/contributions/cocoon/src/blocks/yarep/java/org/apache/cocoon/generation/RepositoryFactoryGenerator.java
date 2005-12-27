package org.apache.cocoon.generation;

import org.apache.avalon.framework.component.Component;

import org.apache.cocoon.generation.ComposerGenerator;

import org.apache.excalibur.xml.sax.SAXParser;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.wyona.yarep.core.RepositoryFactory;

/**
 * @author Michael Wechner
 */
public class RepositoryFactoryGenerator extends ComposerGenerator {

    // The URI of the namespace of this generator
    private String URI = ""; //"http://www.wyona.org/yarep/1.0";

    /**
     * Generate XML data.
     */
    public void generate() throws SAXException {

        SAXParser parser = null;
        try {
            RepositoryFactory rf = new RepositoryFactory();
            String[] repoIDs = rf.getRepositoryIDs();

            // Return XML
            this.contentHandler.startDocument();
            AttributesImpl attr = new AttributesImpl();
            attr.addAttribute("", "properties", "properties", "CDATA", "" + rf.CONFIGURATION_FILE);
            super.contentHandler.startElement(URI, "repositories", "repositories", attr);
            attr.clear();
            String data = rf.toString();
            super.contentHandler.characters(data.toCharArray(), 0, data.length());
            super.contentHandler.endElement(URI, "repositories", "repositories");
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
