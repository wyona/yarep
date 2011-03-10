package org.wyona.yarep.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;

import org.w3c.dom.Document;

import org.wyona.yarep.core.NodeType;
import org.wyona.yarep.core.Repository;

/**
 * This utility directly interacts with Yarep and returns your data objects for a given Yarep Path or write your data object to a given Yarep Path.
 * The whole mechanism is based on the standard JAXB API.
 * Restriction: data objects must be in a package where the jaxb.properties file is available!
 * @author Balz Schreier, Zwischengas AG, www.zwischengas.com
 */
public class YarepXMLBindingUtil {

    private static final Logger log = Logger.getLogger(YarepXMLBindingUtil.class);

    /**
     * The following requirements must be fulfilled so that marshalling of objects to XML is working:
     * - You need all your domain objects in a specific package
     * - In that package you need a jaxb.properties file containing the following line:
     *   javax.xml.bind.context.factory=org.eclipse.persistence.jaxb.JAXBContextFactory
     * - Alternatively you can specify a package-info.java so that namespace are used as you want it to.
     * @param repo Data repository
     * @param jaxbObject
     * @param yarepPath
     * @return
     */
    public static boolean writeJAXBDataObject(Repository repo, Object jaxbObject, String yarepPath) {
        boolean success = true;
        OutputStream os = null;
        try {
            JAXBContext context = JAXBContext.newInstance(jaxbObject.getClass());

            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

            org.wyona.yarep.core.Node node = null;
            if (repo.existsNode(yarepPath)) {
                node = repo.getNode(yarepPath);
            } else {
                node = repo.getRootNode().addNode(yarepPath, NodeType.RESOURCE);
            }
            os = node.getOutputStream();
            
            m.marshal(jaxbObject, node.getOutputStream());
            node.setMimeType("application/xml"); // important so that TikaParser kicks in.
            
        } catch (Exception e) {
            success = false;
            log.fatal("Could not write data object of class '"+jaxbObject.getClass()+"' into Yarep Node with path '"+yarepPath+"' !");
            log.fatal(e,e);
        } finally {
            try {
                os.close();
            } catch (Exception e) {
            }
        }
        return success; 
    }
    
    /**
     * Convert a data object into a DOM document (not that the data object must be annotated with the JAXB annotations)
     * @param jaxbObject
     * @return
     */
    public static Document getDocFromJAXBDataObject(Object jaxbObject) {
        Document result = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            result = db.newDocument();

            JAXBContext context = JAXBContext.newInstance(jaxbObject.getClass());
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

            m.marshal(jaxbObject, result);
            
        } catch (Exception e) {
            log.fatal("Could not convert data object of class '"+jaxbObject.getClass()+"' into a DOM document!");
            log.fatal(e,e);
        } 
        return result; 
    }

    /**
     * Convert a DOM document into a JAXB data object
     * @param jaxbObject
     * @return
     */
    public static <T>T getJAXBObjectFromDoc(Class<T> targetClass, Document doc) {
        T result = null;
        InputStream is = null;
        try {
            is = getInputStreamFromDoc(doc);
            // prepare unmarshalling for targetClass
            JAXBContext jc = JAXBContext.newInstance(targetClass);
            Unmarshaller u = jc.createUnmarshaller();
            result = (T)(u.unmarshal(is));
            
        } catch (Exception e) {
            log.fatal("Could not convert document into object of class '"+targetClass+"' !");
            log.fatal(e,e);
        } finally {
            try {
                is.close();
            } catch (Exception e) {
            }
        }
        return result; 
    }

    /**
     * Get your data object given the Yarep Path and the Repository. You also need to provide the Class.
     * @param targetClass You get an instance of this object after XML -> Object transformation
     * @param repository Data repository
     * @param yarepPath The Yarep Path to the XML
     * @return returns an instance of class targetClass
     */
    public static <T>T readJAXBDataObject(Class<T> targetClass, Repository repository, String yarepPath) {
        T result = null;
        InputStream is = null;
        try {
            // get the node
            // TODO
            org.wyona.yarep.core.Node node = repository.getNode(yarepPath);
            is = node.getInputStream();
            
            // prepare unmarshalling for targetClass
            JAXBContext jc = JAXBContext.newInstance(targetClass);
            
            Unmarshaller u = jc.createUnmarshaller();
            result = (T)(u.unmarshal(is));
            
        } catch (Throwable e) {
            log.fatal("Could not convert document at '"+yarepPath+"' into class '"+targetClass+"'!");
            log.fatal(e,e);
        } finally {
            try {
                is.close();
            } catch (Exception e) {
            }
        }
        return result; 
    }

    private static InputStream getInputStreamFromDoc(Document doc) {
        InputStream is = null;
        try {
            DOMSource source = new DOMSource(doc);  
            StringWriter xmlAsWriter = new StringWriter();  
            StreamResult result = new StreamResult(xmlAsWriter);  
            TransformerFactory.newInstance().newTransformer().transform(source, result);  
            is = new ByteArrayInputStream(xmlAsWriter.toString().getBytes("UTF-8"));         
        } catch (Exception e) {
        }
        return is;
    }

}
