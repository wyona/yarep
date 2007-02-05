package org.wyona.yarep.impl.repo.xmldb;

import org.wyona.yarep.core.Path;
import org.wyona.yarep.core.RepositoryException;
import org.wyona.yarep.core.Storage;
import org.wyona.yarep.core.UID;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.log4j.Category;

import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.Service;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.CollectionManagementService;

/**
 * @author Andreas Wuest
 */
public class XMLDBStorage implements Storage {
    private static Category    mLog               = Category.getInstance(XMLDBStorage.class);
    private        Credentials mCredentials;
    private        String      mDatabaseURIPrefix;

    /**
     * Reads the repository configuration and initialises the database.
     *
     * @param aStorageConfig   the storage configuration
     * @param aRepoConfigFile  the storage configuration as a raw file
     */
    public void readConfig(Configuration aStorageConfig, File aRepoConfigFile) throws RepositoryException {
        Boolean       createPrefix;
        Configuration credentialsConfig;
        Database      database;
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

        // check if we received a storage configuration
        if (aStorageConfig == null)
            throw new RepositoryException("No storage configuration available.");

        try {
            // retrieve the database driver name (e.g. "org.apache.xindice.client.xmldb.DatabaseImpl") [mandatory]
            driverName        = aStorageConfig.getChild("driver").getValue("");
            mLog.error("Specified driver name = \"" + driverName + "\".");

            // retrieve the database home (e.g. "/var/db/") [optional]
            databaseHome      = aStorageConfig.getChild("db-home").getValue("");
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

            // set the database location
            if (databaseHome == null || (databaseHome != null && databaseHome.equals(""))) {
                // TODO: use the repo data path
                database.setProperty("db-home", "/Users/awuest/Documents/devel/src/wyona/realms/xmldb-test/yanel/data");
            } else {
                // use the specifed database home
                database.setProperty("db-home", databaseHome);
            }

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

                    // re-check if complete collection prefix exists now
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
    }

    /**
     *@deprecated
     */
    public Writer getWriter(UID aUID, Path aPath) {
        mLog.warn("Not implemented yet!");
        return null;
    }

    /**
     *
     */
    public OutputStream getOutputStream(UID aUID, Path aPath) throws RepositoryException {
        mLog.warn("Not implemented yet!");
        return null;
    }

    /**
     *@deprecated
     */
    public Reader getReader(UID aUID, Path aPath) {
        mLog.warn("Not implemented yet!");
        return null;
    }

    /**
     *
     */
    public InputStream getInputStream(UID aUID, Path aPath) throws RepositoryException {
        mLog.warn("Not implemented yet!");
        return null;
    }

    /**
     *
     */
    public long getLastModified(UID aUID, Path aPath) throws RepositoryException {
        mLog.warn("Not implemented yet!");
        return 0;
    }

    /**
     *
     */
    public long getSize(UID aUID, Path aPath) throws RepositoryException {
        mLog.warn("Not implemented yet!");
        return 0;
    }

    /**
     *
     */
    public boolean delete(UID aUID, Path aPath) throws RepositoryException {
        mLog.error("TODO: Not implemented yet!");
        return false;
    }

    /**
     *
     */
    public String[] getRevisions(UID aUID, Path aPath) throws RepositoryException {
        mLog.warn("Versioning not implemented yet");
        return null;
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
    private Collection getCollectionRelative(String aCollectionURI) throws RepositoryException {
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
