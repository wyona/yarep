package org.wyona.yarep.impl.repo.xmldb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.wyona.yarep.core.RepositoryException;

import org.apache.log4j.Category;

import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.BinaryResource;
import org.xmldb.api.modules.XMLResource;

/**
 * @author Andreas Wuest
 *
 * This class is marked with default protection so that it can only be
 * instantiated from within this package.
 */
class XMLDBStorageOutputStream extends OutputStream {
    private static Category mLog = Category.getInstance(XMLDBStorageOutputStream.class);

    private boolean mStreamIsClosed = false;

    private ByteArrayOutputStream mContentStream;
    private Collection            mCollection;
    private Resource              mResource;

    /**
     * XMLDBStorageOutputStream constructor.
     *
     * Do not use this to write to an XML character resource. Use a
     * XMLDBStorageWriter instead. Any character data written will
     * be converted using UTF-8.
     *
     * @param aXMLDBStorage    the database instance to work upon
     * @param aCollectionPath  the path to the collection which contains the resource
     * @param aResourceName    the name of the resource to write
     * @param aResourceType    the resource type
     */
    XMLDBStorageOutputStream(XMLDBStorage aXMLDBStorage, String aCollectionPath, String aResourceName, String aResourceType) throws RepositoryException {
        super();

        mLog.error("Collection path = \"" + aCollectionPath + "\", resource name = \"" + aResourceName + "\", Resource type = \"" + aResourceType + "\".");

        /* The collection cannot be passed to us directly, because we need our own
         * private copy of the collection, since we have to close the collection when
         * we are done reading, and no further operations on that collection are allowed
         * after that. We can't realisticly expect the caller to give us our own private
         * collection copy to work on it at will. */
        if ((mCollection = aXMLDBStorage.getCollectionRelative(aCollectionPath)) == null)
            throw new RepositoryException("Parent collection \"" + aCollectionPath + "\" does not exist.");

        try {
            // check if the resource already exists
            mResource = mCollection.getResource(aResourceName);

            if (mResource != null) {
                /* Check if the resource types are compatible. If not, remove
                 * the existing resource, and create a new one. */
                if (!mResource.getResourceType().equals(aResourceType)) {
                    mCollection.removeResource(mResource);

                    mResource = mCollection.createResource(aResourceName, aResourceType);
                }
            } else {
                mResource = mCollection.createResource(aResourceName, aResourceType);
            }
        } catch (XMLDBException exception) {
            throw new RepositoryException(exception.getMessage(), exception);
        }

        mContentStream = new ByteArrayOutputStream();
    }

    /**
     * Closes the OutputStream for any further writing, and stores
     * the data in the database.
     *
     * @throws IOException
     */
    public void close() throws IOException {
        // contract: closing a previously-closed stream has no effect
        if (mStreamIsClosed)
            return;

        mStreamIsClosed = true;

        try {
            // set the content on the resource
            if (mResource.getResourceType().equals(BinaryResource.RESOURCE_TYPE)) {
                mResource.setContent(mContentStream.toByteArray());
            } else if (mResource.getResourceType().equals(XMLResource.RESOURCE_TYPE)) {
                mResource.setContent(mContentStream.toString("UTF-8"));
            }

            // store the resource in the database
            mCollection.storeResource(mResource);

            // close the collection and the stream
            mCollection.close();
        } catch (XMLDBException exception) {
            throw new IOException(exception.getMessage());
        }

        mContentStream.close();

        /* Let the finalizer know that close was called, so he does not close the collection
         * again. Also, make for speedy memory reclaiming, because the collection, resource
         * and stream objects are guaranteed to not be used again in this class. */
        mCollection    = null;
        mResource      = null;
        mContentStream = null;
    }

    public void flush() throws IOException {
        if (mStreamIsClosed)
            throw new IOException("Stream was already closed.");

        mContentStream.flush();
    }

    public void write(byte[] aByteArray) throws IOException {
        if (mStreamIsClosed)
            throw new IOException("Stream was already closed.");

        mContentStream.write(aByteArray);
    }

    public void write(byte[] aByteArray, int aOffset, int aLength) throws IOException {
        if (mStreamIsClosed)
            throw new IOException("Stream was already closed.");

        mContentStream.write(aByteArray, aOffset, aLength);
    }

    public void write(int aByte) throws IOException {
        if (mStreamIsClosed)
            throw new IOException("Stream was already closed.");

        mContentStream.write(aByte);
    }

    protected void finalize() throws Throwable {
        /* Close the collection, but don't persist the resource. The collection must
         * always be closed as per XML:DB specification, no matter what happened. On
         * the other hand, if the collection still exists, this means that our close()
         * method was never called, so the user did not want us to store the data. */
        if (mCollection != null) {
            mCollection.close();
        }

        super.finalize();
    }
}
