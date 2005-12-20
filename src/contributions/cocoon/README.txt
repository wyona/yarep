
   README YAREP COCOON BLOCK
   =========================

Requirements:
-------------
  Apache Ant 1.6.x


Getting Started:
----------------
  Build Yarep library resp. jar file first (see ../../../README.txt)

  Copy build.properties to local.build.properties and configure the Cocoon source directory within local.build.properties

  IMPORTANT:
    Make sure to set the Yarep version of the library within xpatch/jars/file-yarep.xconf
    (will be done automatically in a future version)

  Run ant ("ant")

  IMPORTANT:
    Make sure that within local.build.properties resp. build.properties of Cocoon the samples
    are being enabled (#exclude.webapp.samples=true)

  (Re-)Build Cocoon

  (Re-)Start Cocoon

  Browse to http://127.0.0.1:8888/samples/blocks/yarep/
