XMLDBStorage
============

The XMLDBStorage is a repository based on an XML:DB compatible
back-end (http://xmldb-org.sourceforge.net/). There are several
existing XML:DB implementations, e.g. Xindice
(http://xml.apache.org/xindice/), eXist
(http://exist.sourceforge.net/), or Tamino
(http://developer.softwareag.com/tamino/xmldb/Default.htm).

In order to use this repository with a web-app, your web-app has to
include the XML:DB API as well as a suitable implementation.

Usage with Yanel
----------------

For use with Yanel, you have to always add <dependency
groupId="xindice" artifactId="xmldb" version="1.0"/>. The database
implementation then has to be added with e.g. <dependency
groupId="xindice" artifactId="xindice" version="1.1b4"/>.

Repository Configuration
------------------------

A sample repository configuration could look like this:

<?xml version="1.0"?>
<repository>
  <name>XML-DB Sample Data Repository</name>

  <paths src="../data-paths" fallback="true"/>

  <storage class="org.wyona.yarep.impl.repo.xmldb.XMLDBStorage">
    <driver>org.apache.xindice.client.xmldb.embed.DatabaseImpl</driver>
    <address></address>
    <db-home></db-home>
    <root>db</root>
    <prefix createIfNotExists="true">sample-repo</prefix>
    <credentials>
      <username>sample</username>
      <password>sample</password>
    </credentials>
  </storage>
</repository>

driver: [mandatory]
  The name of the database driver you want to
  use. This element is mandatory.

address: [optional]
  The address of the machine the database lives on
  (e.g. db.example.com:9080).

  This element can be empty or missing (e.g. for embedded
  databases). If no <address> is specified, the empty string is used.

db-home: [optional]
  The database location on your disk.

  This element can be empty or missing (e.g. for embedded
  databases). If no <db-home> is specified, the database home is
  assumed to be the location as specified by the <paths> "src"
  attribute.

root: [mandatory]
  The database root collection. This element is
  mandatory.

prefix: [optional]
  A collection prefix path to be used with all
  database URIs.

  If the "createIfNotExists" attribute is set to "true", then a
  collection with the name of the prefix is created if it does not
  exist yet. If this attribute is missing or false, and the prefix
  does not exist, the repository instantiation will fail.

  If the <prefix> is missing, the database path will be the path up to
  and including the root collection.

credentials: [optional]
  You can specify database credentials by
  creating child elements named <username> and <password>. The values
  have to be text child nodes of those elements.

  If the <credentials> element is missing, then the database is
  accessed without using access control. If the <credentials> element
  is present, but one or both of the <username> and <password>
  children are missing, the database is nevertheless accessed using
  access control, but missing values are represented by the empty
  string ("").


How Database URIs are Constructed
---------------------------------

Database URIs are constructed as follows:

  xmldb: + database-name + :// + address + / + root + / + prefix

Note that "database-name" is a property of the driver in use, and is
retrieved by querying the instantiated database.
