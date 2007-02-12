package org.wyona.yarep.impl.repo.xmldb;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.wyona.yarep.core.Path;
import org.wyona.yarep.core.RepositoryException;
import org.wyona.yarep.core.Storage;
import org.wyona.yarep.core.UID;

import org.wyona.commons.io.FileUtil;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;

import org.apache.log4j.Category;

import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.Service;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.BinaryResource;
import org.xmldb.api.modules.XMLResource;
import org.xmldb.api.modules.CollectionManagementService;
import org.xmldb.api.modules.XPathQueryService;

/**
 * @author Andreas Wuest
 */
public class XMLDBStorage implements Storage {
    private static Category mLog = Category.getInstance(XMLDBStorage.class);

    private Credentials mCredentials;
    private String      mDatabaseURIPrefix;

    /**
     * XMLDBStorage constructor.
     */
    public XMLDBStorage() {}

    /**
     * XMLDBStorage constructor.
     *
     * @param aID              the repository ID
     * @param aRepoConfigFile  the repository configuration file
     */
    public XMLDBStorage(String aID, File aRepoConfigFile) throws RepositoryException {
        Configuration storageConfig;

        try {
            storageConfig = (new DefaultConfigurationBuilder()).buildFromFile(aRepoConfigFile).getChild("storage", false);
        } catch (Exception exception) {
            mLog.error(exception);
            throw new RepositoryException(exception.getMessage(), exception);
        }

        readConfig(storageConfig, aRepoConfigFile);
    }

    /**
     * Reads the repository configuration and initialises the database.
     *
     * @param  aStorageConfig   the storage configuration
     * @param  aRepoConfigFile  the storage configuration as a raw file
     * @throws RepositoryException
     */
    public void readConfig(Configuration aStorageConfig, File aRepoConfigFile) throws RepositoryException {
        boolean       createPrefix;
        Configuration repositoryConfig;
        Configuration credentialsConfig;
        Database      database;
        File          databaseHomeDir;
        Service       collectionService;
        String        driverName;
        String        databaseHome;
        String        rootCollection;
        String        pathPrefix;
        String        databaseAddress;
        String        databaseName;
        String        databaseURIPrefix;

        /* TODO: replace most mLog.error() invocations by mLog.debug().
         * Unfortunately, mLog.debug() produces no output, even if activated
         * in the log4.properties. */

        // check if we received a storage configuration and a repo config file
        if (aStorageConfig == null || aRepoConfigFile == null)
            throw new RepositoryException("No storage/repository configuration available.");

        try {
            // retrieve the database driver name (e.g. "org.apache.xindice.client.xmldb.DatabaseImpl") [mandatory]
            driverName        = aStorageConfig.getChild("driver").getValue("");
            mLog.error("Specified driver name = \"" + driverName + "\".");

            // retrieve the database home (e.g. "../data") [optional]
            databaseHome      = aStorageConfig.getChild("db-home").getValue(null);
            mLog.error("Specified database home = \"" + databaseHome + "\".");

            // retrieve the root collection name (e.g. "db") [mandatory]
            rootCollection    = aStorageConfig.getChild("root").getValue("");
            mLog.error("Specified root collection = \"" + rootCollection + "\".");

            // retrieve the path prefix (e.g. "some/sample/collection") [optional]
            pathPrefix        = aStorageConfig.getChild("prefix").getValue("");
            createPrefix      = aStorageConfig.getChild("prefix").getAttributeAsBoolean("createIfNotExists", false);
            mLog.error("Specified collection prefix = \"" + pathPrefix + "\" (create if not exists: \"" + createPrefix + "\").");

            // retrieve the name of the database host (e.g. "myhost.domain.com:8080") [optional]
            databaseAddress   = aStorageConfig.getChild("address").getValue("");
            mLog.error("Specified database address = \"" + databaseAddress + "\".");

            // retrieve credentials [optional]
            credentialsConfig = aStorageConfig.getChild("credentials", false);

            if (credentialsConfig != null) {
                mCredentials = new Credentials(credentialsConfig.getChild("username").getValue(""),
                                               credentialsConfig.getChild("password").getValue(""));
                mLog.error("Specified credentials read.");
            }
        } catch (Exception exception) {
            mLog.error(exception);
            throw new RepositoryException(exception.getMessage(), exception);
        }

        // check if the driver name was specified
        if (driverName.equals(""))
            throw new RepositoryException("Database driver not specified.");

        // check if the root collection was specified
        if (rootCollection.equals(""))
            throw new RepositoryException("Database root collection not specified.");

        // register the database with the database manager
        try {
            database = (Database) Class.forName(driverName).newInstance();

            // determine the database location
            if (databaseHome != null) {
                // resolve the database home relative to the repo config file directory
                databaseHomeDir = new File(databaseHome);

                if (!databaseHomeDir.isAbsolute()) {
                    databaseHomeDir = FileUtil.file(aRepoConfigFile.getParent(), databaseHomeDir.toString());
                }

                mLog.error("Resolved database home directory = \"" + databaseHomeDir + "\"");

                database.setProperty("db-home", databaseHomeDir.toString());
            }

            // set the database location
            DatabaseManager.registerDatabase(database);

            databaseName = database.getName();
        } catch (Exception exception) {
            mLog.error(exception);
            throw new RepositoryException(exception.getMessage(), exception);
        }

        // construct the database URI prefix up to (and inluding) the root collection
        databaseURIPrefix = "xmldb:" + databaseName + "://" + databaseAddress  + "/" + rootCollection + "/";

            // construct the complete database URI prefix including a potential path prefix
            if (pathPrefix.equals("")) {
                mDatabaseURIPrefix = databaseURIPrefix;
            } else {
                mDatabaseURIPrefix = databaseURIPrefix + "/" + pathPrefix + "/";
            }

        mLog.error("Collection base path = \"" + databaseURIPrefix + "\".");
        mLog.error("Complete collection base path = \"" + mDatabaseURIPrefix + "\".");

        // test drive our new database instance
        try {
            database.acceptsURI(mDatabaseURIPrefix);
        } catch (XMLDBException exception) {
            mLog.error(exception);

            if (exception.errorCode == org.xmldb.api.base.ErrorCodes.INVALID_URI) {
                throw new RepositoryException("The database does not accept the URI prefix \"" + mDatabaseURIPrefix + "\" as valid. Please make sure that the database host address (\"" + databaseAddress + "\") is correct. Original message: " + exception.getMessage(), exception);
            } else {
                throw new RepositoryException(exception.getMessage(), exception);
            }
        } catch (Exception exception) {
            mLog.error(exception);
            throw new RepositoryException(exception.getMessage(), exception);
        }

        try {
            // check if the specified root collection exists
            if (getCollection(databaseURIPrefix) == null)
                throw new RepositoryException("Specified root collection (\"" + rootCollection + "\") does not exist.");

            // check if the complete collection prefix exists
            if (getCollectionRelative(null) == null) {
                if (createPrefix) {
                    // create the prefix collection
                    try {
                        collectionService = getCollection(databaseURIPrefix).getService("CollectionManagementService", "1.0");

                        ((CollectionManagementService) collectionService).createCollection(pathPrefix);

                        // re-check if complete collection prefix exists now, we don't want to take any chances here
                        if (getCollectionRelative(null) == null)
                            throw new RepositoryException("Specified collection prefix (\"" + pathPrefix + "\") does not exist.");
                    } catch (Exception exception) {
                        mLog.error(exception);
                        throw new RepositoryException("Failed to create prefix collection (\"" + pathPrefix + "\"). Original message: " + exception.getMessage(), exception);
                    }

                    mLog.error("Created new collection \"" + pathPrefix + "\".");
                } else {
                    // the prefix collection does not exist
                    throw new RepositoryException("Specified collection prefix (\"" + pathPrefix + "\") does not exist.");
                }
            }
        } catch (Exception exception) {
            // something went wrong after registering the database, we have to deregister it now
            try {
                DatabaseManager.deregisterDatabase(database);
            } catch (Exception databaseException) {
                mLog.error(databaseException);
                throw new RepositoryException(databaseException.getMessage(), databaseException);
            }

            /* Rethrow exception. We have to construct a new exception here, because the type system
             * doesn't know that only RepositoryExceptions can get to this point (we catch all other
             * exceptions above already), and would therefore complain. */
            throw new RepositoryException(exception.getMessage(), exception);
        }
    }

    /**
     * Returns a Writer to store character data. Creates an XML resource in the database if one of
     * the same name and path does not already exist, otherwise overwrites an already existing
     * resource. If the already existing resource is of a binary type, the resource is removed,
     * and a new XML resource is created.
     *
     * Call close() on the returned Writer to actually store the data in the database.
     *
     * Do not use this to write binary data, use getOutputStream instead. This method will
     * create a XML character resource. Ignore the deprecated status of this method in the
     * super class.
     *
     * @param  aUID    the UID (not used in this implementation)
     * @param  aPath   the path including the resource name of the resource to write to
     * @return Writer  returns a Writer instance
     */
    public Writer getWriter(UID aUID, Path aPath) {
        org.wyona.commons.io.Path parentPath;

        mLog.error("UID = \"" + aUID + "\", path = \"" + aPath + "\".");

        // obviously, writing means creation of a new resource, so we have to get the parent collection
        parentPath = aPath.getParent();
        mLog.error("Path to the parent collection = \"" + parentPath.toString() + "\".");

        /* For whatever reasons, the Storage interface does not declare this method to throw a
         * RepositoryException, therefore we have to catch it here. */
        try {
            return (new XMLDBStorageWriter(this, parentPath.toString(), null, XMLResource.RESOURCE_TYPE));
        } catch (Exception exception) {
            return null;
        }
    }

    /**
     * Returns an OutputStream to store character data. Creates a binary resource in the database
     * if one of the same name and path does not already exist, otherwise overwrites an already
     * existing resource. If the already existing resource is of an XML type, the resource is removed,
     * and a new binary resource is created.
     *
     * Call close() on the returned OutputStream to actually store the data in the database.
     *
     * Do not use this to write character data, use getWriter instead. This method will
     * create a binary resource.
     *
     * @param  aUID          the UID (not used in this implementation)
     * @param  aPath         the path including the resource name of the resource to write to
     * @return OutputStream  returns an OutputStream instance
     * @throws RepositoryException
     */
    public OutputStream getOutputStream(UID aUID, Path aPath) throws RepositoryException {
        org.wyona.commons.io.Path parentPath;

        mLog.error("UID = \"" + aUID + "\", path = \"" + aPath + "\".");

        // obviously, writing means creation of a new resource, so we have to get the parent collection
        parentPath = aPath.getParent();
        mLog.error("Path to the parent collection = \"" + parentPath.toString() + "\".");

        return (new XMLDBStorageOutputStream(this, parentPath.toString(), null, BinaryResource.RESOURCE_TYPE));
    }

    /**
     * Returns a Reader to read character data.
     *
     * Call close() on the returned Reader when you are done reading.
     *
     * Do not use this to read binary data, use getInputStream instead. If you read
     * binary data with this Reader, all data will be converted to characters using
     * UTF-8 encoding. Ignore the deprecated status of this method in the super class.
     *
     * @param  aUID    the UID (not used in this implementation)
     * @param  aPath   the path including the resource name of the resource to read from
     * @return Reader  returns a Reader instance
     */
    public Reader getReader(UID aUID, Path aPath) {
        org.wyona.commons.io.Path parentPath;

        mLog.error("UID = \"" + aUID + "\", path = \"" + aPath + "\".");

        // get the parent collection
        parentPath = aPath.getParent();
        mLog.error("Path to the parent collection = \"" + parentPath.toString() + "\".");

        /* For whatever reasons, the Storage interface does not declare this method to throw a
         * RepositoryException, therefore we have to catch it here. */
        try {
            return (new XMLDBStorageReader(this, parentPath.toString(), aPath.getName()));
        } catch (Exception exception) {
            return null;
        }
    }

    /**
     * Returns an InputStream to read binary data.
     *
     * Call close() on the returned InputStream when you are done reading.
     *
     * Do not use this to read character data, use getReader instead. If you read
     * XML character data with this InputStream, all data will be converted to bytes
     * using UTF-8 encoding. Ignore the deprecated status of this method in the super
     * class.
     *
     * @param  aUID         the UID (not used in this implementation)
     * @param  aPath        the path including the resource name of the resource to read from
     * @return InputStream  returns an InputStream instance
     * @throws RepositoryException
     */
    public InputStream getInputStream(UID aUID, Path aPath) throws RepositoryException {
        org.wyona.commons.io.Path parentPath;

        mLog.error("UID = \"" + aUID + "\", path = \"" + aPath + "\".");

        // get the parent collection
        parentPath = aPath.getParent();
        mLog.error("Path to the parent collection = \"" + parentPath.toString() + "\".");

        return (new XMLDBStorageInputStream(this, parentPath.toString(), aPath.getName()));
    }

    /**
     * This repository does not support modification dates.
     */
    public long getLastModified(UID aUID, Path aPath) throws RepositoryException {
        mLog.error("UID = \"" + aUID + "\", path = \"" + aPath + "\".");
        mLog.warn("This repository does not support modification dates.");
        return 0;
    }

    /**
     * Returns the size of a resource.
     *
     * @param  aUID   the UID (not used in this implementation)
     * @param  aPath  the path including the resource name of the resource to get the size
     * @return long   returns the size of the resource
     * @throws RepositoryException  throws a RepositoryException if the resource does not exist,
     *                              or another exception occurs
     */
    public long getSize(UID aUID, Path aPath) throws RepositoryException {
        Collection                collection;
        org.wyona.commons.io.Path parentPath;
        Resource                  resource;

        mLog.error("UID = \"" + aUID + "\", path = \"" + aPath + "\".");

         // get the parent collection
        parentPath = aPath.getParent();
        mLog.error("Path to the parent collection = \"" + parentPath.toString() + "\".");

        if ((collection = getCollectionRelative(parentPath.toString())) == null)
            throw new RepositoryException("Requested resource \"" + aPath + "\" does not exist.");

        try {
            resource = collection.getResource(aPath.getName());
        } catch (XMLDBException exception) {
            throw new RepositoryException(exception.getMessage(), exception);
        }

        if (resource == null)
            throw new RepositoryException("Requested resource \"" + aPath.getName() + "\" does not exist.");

        try {
            if (resource.getResourceType().equals(BinaryResource.RESOURCE_TYPE)) {
                return ((byte[]) resource.getContent()).length;
            } else if (resource.getResourceType().equals(XMLResource.RESOURCE_TYPE)) {
                return ((String) resource.getContent()).length();
            }
        } catch (Exception exception) {
            throw new RepositoryException(exception.getMessage(), exception);
        }

        return 0;
    }

    /**
     * Removes a resource.
     *
     * Throws a RepositoryException if the resource to remove does not exist.
     *
     * @param  aUID     the UID (not used in this implementation)
     * @param  aPath    the path including the resource name of the resource to remove
     * @return boolean  returns true (TODO: what is this for??)
     * @throws RepositoryException  throws a RepositoryException if the resource does not exist,
     *                              or another exception occurs
     */
    public boolean delete(UID aUID, Path aPath) throws RepositoryException {
        Collection                collection;
        org.wyona.commons.io.Path parentPath;
        Resource                  resource;

        mLog.error("UID = \"" + aUID + "\", path = \"" + aPath + "\".");

        // get the parent collection
        parentPath = aPath.getParent();
        mLog.error("Path to the parent collection = \"" + parentPath.toString() + "\".");

        if ((collection = getCollectionRelative(parentPath.toString())) == null)
            throw new RepositoryException("Requested resource \"" + aPath + "\" does not exist.");

        try {
            resource = collection.getResource(aPath.getName());
        } catch (XMLDBException exception) {
            throw new RepositoryException(exception.getMessage(), exception);
        }

        if (resource == null)
            throw new RepositoryException("Requested resource \"" + aPath.getName() + "\" does not exist.");

        try {
            collection.removeResource(resource);
        } catch (Exception exception) {
            throw new RepositoryException(exception.getMessage(), exception);
        }

        return true;
    }

    /**
     * This repository does not support versioning.
     */
    public String[] getRevisions(UID aUID, Path aPath) throws RepositoryException {
        mLog.error("UID = \"" + aUID + "\", path = \"" + aPath + "\".");
        mLog.warn("This repository does not support versioning.");
        return null;
    }

    /**
     * Executes a query.
     *
     * Note that this method is not part of the interface, and the repository therefore
     * has to be explicitly casted to org.wyona.yarep.impl.repo.xmldb.XMLDBStorage.
     *
     * @param aPath                  the path to the collection against whose subtree the query should be evaluated, or
     *                               null, if the root collection should be assumed
     * @param aNamespaceMap          a mapping of namespace prefixes (key, as a String) to namespaces (value, as a String)
     *                               as used in the aQuery parameter
     * @param aQuery                 the XPath query (note that the namespace prefixes used have to be declared in the
     *                               aNamespaceMap mapping
     * @return java.util.Collection  returns a collection of org.xmldb.api.base.Resource's against which the query matched
     * @throws RepositoryException   if an error occurred retrieving a collection or executing the query
     */
    public java.util.Collection executeQuery(Path aPath, Map aNamespaceMap, String aQuery) throws RepositoryException {
        ArrayList         queryResult;
        Collection        collection;
        Iterator          namespaceSetIter;
        ResourceIterator  resultSetIter;
        Map.Entry         mapEntry;
        ResourceSet       resultSet;
        XPathQueryService queryService;

        mLog.error("Path = \"" + aPath + "\", query = \"" + aQuery + "\".");

        collection = getCollectionRelative((aPath != null ? aPath.toString() : null));

        try {
            // get the XPathQueryService
            queryService = (XPathQueryService) collection.getService("XPathQueryService", "1.0");

            // populate namespace map
            namespaceSetIter = aNamespaceMap.entrySet().iterator();

            while (namespaceSetIter.hasNext()) {
                mapEntry = (Map.Entry) namespaceSetIter.next();
                queryService.setNamespace((String) mapEntry.getKey(), (String) mapEntry.getValue());
            }

            // run query
            resultSet = queryService.query(aQuery);

            // transform result set to collection in order to return it
            queryResult   = new ArrayList();
            resultSetIter = resultSet.getIterator();

            while (resultSetIter.hasMoreResources()) {
                // add resource names
                queryResult.add(resultSetIter.nextResource().getId());
            }
        } catch (Exception exception) {
            mLog.error(exception);
            throw new RepositoryException(exception.getMessage(), exception);
        }

        return queryResult;
    }

    /**
     * Retrieves the collection for the specified collection URI, relative to
     * the global collection prefix path.
     *
     * @param  aCollectionURI       the relative URI of the collection to retrieve, or null to
     *                              retrieve the global prefix collection
     * @return                      a collection instance for the requested collection or
     *                              null if the collection could not be found
     * @throws RepositoryException  if an error occurred retrieving the collection (e.g.
     *                              permission was denied)
     */
    Collection getCollectionRelative(String aCollectionURI) throws RepositoryException {
        return getCollection(constructCollectionURI(aCollectionURI));
    }

    /**
     * Retrieves the collection for the specified collection URI.
     *
     * @param  aCollectionURI       the xmldb URI of the collection to retrieve
     * @return                      a collection instance for the requested collection or
     *                              null if the collection could not be found
     * @throws RepositoryException  if an error occurred retrieving the collection (e.g.
     *                              permission was denied)
     */
    private Collection getCollection(String aCollectionURI) throws RepositoryException {
        try {
            if (mCredentials != null) {
                return DatabaseManager.getCollection(aCollectionURI, mCredentials.getUsername(), mCredentials.getPassword());
            } else {
                return DatabaseManager.getCollection(aCollectionURI);
            }
        } catch (Exception exception) {
            mLog.error(exception);
            throw new RepositoryException(exception.getMessage(), exception);
        }
    }

    private String constructCollectionURI(String aCollectionURI) {
        return mDatabaseURIPrefix + "/" + (aCollectionURI != null ? aCollectionURI : "");
    }


    private class Credentials {
        private final String mUsername;
        private final String mPassword;

        public Credentials(String aUsername, String aPassword) {
            mUsername = aUsername;
            mPassword = aPassword;
        }

        public String getUsername() {
            return mUsername;
        }

        public String getPassword() {
            return mPassword;
        }
    }
}
