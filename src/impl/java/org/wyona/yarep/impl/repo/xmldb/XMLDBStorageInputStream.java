package org.wyona.yarep.impl.repo.xmldb;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

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
class XMLDBStorageInputStream extends InputStream {
    private static Category mLog = Category.getInstance(XMLDBStorageInputStream.class);

    private boolean mStreamIsClosed = false;

    private ByteArrayInputStream mContentStream;
    private Collection           mCollection;
    private Resource             mResource;

    /**
     * XMLDBStorageInputStream constructor.
     *
     * Do not use this to read from an XML character resource. Use a
     * XMLDBStorageReader instead. Any character data read will
     * be converted using UTF-8.
     *
     * @param aXMLDBStorage    the database instance to work upon
     * @param aCollectionPath  the path to the collection which contains the resource
     * @param aResourceName    the name of the resource to write
     */
    XMLDBStorageInputStream(XMLDBStorage aXMLDBStorage, String aCollectionPath, String aResourceName) throws RepositoryException {
        super();

        mLog.error("Collection path = \"" + aCollectionPath + "\", resource name = \"" + aResourceName + "\".");

        /* The collection cannot be passed to us directly, because we need our own
         * private copy of the collection, since we have to close the collection when
         * we are done reading, and no further operations on that collection are allowed
         * after that. We can't realisticly expect the caller to give us our own private
         * collection copy to work on it at will. */
        if ((mCollection = aXMLDBStorage.getCollectionRelative(aCollectionPath)) == null)
            throw new RepositoryException("Parent collection \"" + aCollectionPath + "\" does not exist.");

        try {
            mResource = mCollection.getResource(aResourceName);
        } catch (XMLDBException exception) {
            throw new RepositoryException(exception.getMessage(), exception);
        }

        if (mResource == null)
            throw new RepositoryException("Requested resource \"" + aResourceName + "\" does not exist.");

        try {
            if (mResource.getResourceType().equals(BinaryResource.RESOURCE_TYPE)) {
                mContentStream = new ByteArrayInputStream((byte[]) mResource.getContent());
            } else if (mResource.getResourceType().equals(XMLResource.RESOURCE_TYPE)) {
                mContentStream = new ByteArrayInputStream(((String) mResource.getContent()).getBytes("UTF-8"));
            }
        } catch (Exception exception) {
            throw new RepositoryException(exception.getMessage(), exception);
        }
    }

    public int available() throws IOException {
        if (mStreamIsClosed)
            throw new IOException("Stream was already closed.");

        return mContentStream.available();
    }

    /**
     * Closes the OutputStream for any further reading.
     *
     * @throws IOException
     */
    public void close() throws IOException {
        // contract: closing a previously-closed stream has no effect
        if (mStreamIsClosed)
            return;

        mStreamIsClosed = true;

        try {
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

    public void mark(int aReadLimit) {
        mContentStream.mark(aReadLimit);
    }

    public boolean markSupported() {
        return mContentStream.markSupported();
    }

    public int read() throws IOException {
        if (mStreamIsClosed)
            throw new IOException("Stream was already closed.");

        return mContentStream.read();
    }

    public int read(byte[] aByteArray) throws IOException, NullPointerException {
        if (mStreamIsClosed)
            throw new IOException("Stream was already closed.");

        return mContentStream.read(aByteArray, 0, aByteArray.length);
    }

    public int read(byte[] aByteArray, int aOffset, int aLength) throws IOException, NullPointerException {
        if (mStreamIsClosed)
            throw new IOException("Stream was already closed.");

        return mContentStream.read(aByteArray, aOffset, aLength);
    }

    public void reset() throws IOException {
        if (mStreamIsClosed)
            throw new IOException("Stream was already closed.");

        mContentStream.reset();
    }

    public long skip(long aNumberOfBytes) throws IOException {
        if (mStreamIsClosed)
            throw new IOException("Stream was already closed.");

        return mContentStream.skip(aNumberOfBytes);
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
