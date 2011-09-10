

     YAREP - Yet Another Repository
     ==============================


     Prerequisites:
     --------------

     - JDK 1.6 or higher


     Building YAREP:
     ---------------

     Before running the examples, you need to build YAREP 
     and the examples by executing "ant build-examples" in
     or rather "./build.sh build-examples" in the current directory.


     Running the examples:
     ---------------------

     Run the examples by executing "ant run-examples" or rather
     "./build.sh run-examples"
     
     OPTIONAL: Run the hsqldb server: java -cp /home/USER/.m2/repository/hsqldb/hsqldb/1.8.0.7/hsqldb-1.8.0.7.jar org.hsqldb.Server


     Running the tests:
     ------------------

     For the SVN repository implementation one needs to configure the src, username and password within

       build/repository/svn-example/config/repository.xml

     of the storage.

     Run the tests by executing "ant test" or rather
     "./build.sh test"
     
     Run a particular test class:
     ./build.sh test -Dtest.class.name=org.wyona.yarep.tests.VirtualFilesystemRevisionsTest


     Configuration of repositories:
     ------------------------------

     The repositories can be specified within yarep.properties (e.g. build/classes/yarep.properties).
     Each repository is specified by a repository ID and a repository configuration file.


     Using YAREP within another application:
     ---------------------------------------

     A JAR file, build/lib/yarep-LCRxxx.jar can be created by
     executing "ant jar" or rather "./build.sh jar". Take a look at the examples how YAREP
     can be used within another application.


     How to copy a repository
     ------------------------

     copy content of repo 'foo' into repo 'bar':
     (NOTE: this will overwrite the content of repo 'bar')
     
     1) ./build.sh build-examples
     2) ./build.sh compile-tools
     3) ./build.sh copy-repository -Dcopy.src.repo.config=/home/yanel/foo-yarep-repository.xml -Dcopy.dest.repo.config=/home/yanel/bar-yarep-repository.xml


     How to (re-)index a repository
     ------------------------------

     (Re-)Index content of repo 'foo':
     
     1) ./build.sh build-examples
     2) ./build.sh compile-tools
     3) ./build.sh index-repository -Drepo.config=/home/yanel/foo-yarep-repository.xml


     Creating a release
     ------------------

     1) Update revision number (subversion.revision) within build.properties
     2) Set credentials (usernam and password) within local.build.properties
     3) Run ./build.sh svn-export
     4) Change directory: build/svn-export-trunk-rREVISION
     4.1) Update build.properties (revision number) and local.build.properties (credentials) accordingly
     5) Run ./build.sh deploy-jars
