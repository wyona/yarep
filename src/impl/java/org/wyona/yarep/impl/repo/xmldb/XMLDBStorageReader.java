package org.wyona.yarep.impl.repo.xmldb;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

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
class XMLDBStorageReader extends Reader {
    private static Category mLog = Category.getInstance(XMLDBStorageReader.class);

    private boolean mStreamIsClosed = false;

    private StringReader mContentReader;
    private Collection   mCollection;
    private Resource     mResource;

    /**
     * XMLDBStorageReader constructor.
     *
     * Do not use this to read from a binary resource. Use a
     * XMLDBStorageInputStream instead. Any binary data will
     * be converted using UTF-8.
     *
     * @param aXMLDBStorage    the database instance to work upon
     * @param aCollectionPath  the path to the collection which contains the resource
     * @param aResourceName    the name of the resource to write
     */
    XMLDBStorageReader(XMLDBStorage aXMLDBStorage, String aCollectionPath, String aResourceName) throws RepositoryException {
        super();

        mLog.error("Collection path = \"" + aCollectionPath + "\", resource name = \"" + aResourceName + "\", Resource type = \".");

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
                mContentReader = new StringReader(new String((byte[]) mResource.getContent(), "UTF-8"));
            } else if (mResource.getResourceType().equals(XMLResource.RESOURCE_TYPE)) {
                mContentReader = new StringReader((String) mResource.getContent());
            }
        } catch (Exception exception) {
            throw new RepositoryException(exception.getMessage(), exception);
        }
    }

    /**
     * Closes the Reader for any further reading.
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

        mContentReader.close();

        /* Let the finalizer know that close was called, so he does not close the collection
         * again. Also, make for speedy memory reclaiming, because the collection, resource
         * and stream objects are guaranteed to not be used again in this class. */
        mCollection    = null;
        mResource      = null;
        mContentReader = null;
    }

    public void mark(int aReadLimit) throws IOException {
        mContentReader.mark(aReadLimit);
    }

    public boolean markSupported() {
        return mContentReader.markSupported();
    }

    public int read() throws IOException {
        if (mStreamIsClosed)
            throw new IOException("Stream was already closed.");

        return mContentReader.read();
    }

    public int read(char[] aCharacterArray) throws IOException, NullPointerException {
        if (mStreamIsClosed)
            throw new IOException("Stream was already closed.");

        return mContentReader.read(aCharacterArray, 0, aCharacterArray.length);
    }

    public int read(char[] aCharacterArray, int aOffset, int aLength) throws IOException, NullPointerException {
        if (mStreamIsClosed)
            throw new IOException("Stream was already closed.");

        return mContentReader.read(aCharacterArray, aOffset, aLength);
    }

    public boolean ready() throws IOException {
        if (mStreamIsClosed)
            throw new IOException("Stream was already closed.");

        return mContentReader.ready();
    }

    public void reset() throws IOException {
        if (mStreamIsClosed)
            throw new IOException("Stream was already closed.");

        mContentReader.reset();
    }

    public long skip(long aNumberOfBytes) throws IOException {
        if (mStreamIsClosed)
            throw new IOException("Stream was already closed.");

        return mContentReader.skip(aNumberOfBytes);
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
