
   README YAREP COCOON BLOCK
   =========================

Requirements:
-------------
  Apache Ant 1.6.x


Getting Started:
----------------
  Build Yarep library resp. jar file first (see ../../../README.txt)

  IMPORTANT:
    Make sure to set the Yarep version ID of the library (build.xml and xpatch/jars/file-yarep.xconf)
    (will be done automatically in a future version)

  Run ant ("ant")

  IMPORTANT:
    Make sure that within local.build.properties resp. build.properties of Cocoon the samples
    are being enabled (#exclude.webapp.samples=true)

  (Re-)Build Cocoon

  (Re-)Start Cocoon

  Browse to http://127.0.0.1:8888/samples/blocks/yarep/
