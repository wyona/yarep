

     YAREP - Yet Another Repository
     ==============================


     Prerequisites:
     --------------

     - JDK 1.4.X or higher


     Building YAREP:
     ---------------

     Before running the examples, you need to build YAREP 
     and the examples by executing "ant build-examples" in
     resp. "./build.sh build-examples" in the current directory.


     Running the examples:
     ---------------------

     Run the examples by executing "ant run-examples" resp.
     "./build.sh run-examples"
     
     OPTIONAL: Run the hsqldb server: java -cp /home/USER/.m2/repository/hsqldb/hsqldb/1.8.0.7/hsqldb-1.8.0.7.jar org.hsqldb.Server


     Running the tests:
     ------------------

     For the SVN repository implementation one needs to configure the src, username and password within

       build/repository/svn-example/config/repository.xml

     of the storage.

     Run the tests by executing "ant test" resp.
     "./build.sh test"


     Configuration of repositories:
     ------------------------------

     The repositories can be specified within yarep.properties (e.g. build/classes/yarep.properties).
     Each repository is specified by a repository ID and a repository configuration file.


     Using YAREP within another application:
     ---------------------------------------

     A JAR file, build/lib/yarep-LCRxxx.jar can be created by
     executing "ant jar" resp. "./build.sh jar". Take a look at the examples how YAREP
     can be used within another application.
