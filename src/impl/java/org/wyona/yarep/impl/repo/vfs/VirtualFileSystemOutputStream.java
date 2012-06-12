package org.wyona.yarep.impl.repo.vfs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import java.lang.Long;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.AtomicMoveNotSupportedException;

import org.apache.log4j.Logger;
import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.search.Metadata;
import org.wyona.yarep.impl.AbstractNode;

/**
 * OutputStream which sets some properties (lastModified, size) to the node 
 * when the stream is closed. The output stream also implements a copy-on-write
 * mechanism, whereby the file is written to temporary location and only moved
 * to it's permanent position on close (if possible, atomically). 
 */
public class VirtualFileSystemOutputStream extends OutputStream {

    private static Logger log = Logger.getLogger(VirtualFileSystemOutputStream.class);

    protected Node node;
    protected OutputStream out;
    protected File file;

    protected boolean copyOnWrite;
    protected boolean isClosed;
    protected boolean isMoved;
    protected Path tempPath;
    protected Path destPath;

    // The number of times we will (at most) retry trying to create
    // a temporary file for copying by using a random tag.
    private static int MAX_RETRIES = 10;

    /**
     * Constructor for VFS output stream.
     * @param node  The underlying node.
     * @param file  The destination file to write to.
     */
    public VirtualFileSystemOutputStream(Node node, File file) throws Exception {
        this.node = node;
        this.file = file;
        isClosed = false;
        isMoved = false;

        // Check for copy-on-write
        VirtualFileSystemRepository vfsRepo = ((VirtualFileSystemNode) node).getRepository();
        copyOnWrite = vfsRepo.isCopyOnWriteEnabled();

        if(!copyOnWrite) {
            // Copy-on-write disabled
            // Use regular file output stream
            out = new FileOutputStream(file);
        } else {
            // Copy-on-write enabled:
            // Try to set up copy-on-write mechanism
            String name = file.getName();
            File parent = file.getParentFile();
            Random random = new Random();

            // Attempt to create a temporary file for writing
            File tempFile = null;
            int i = 0;
            boolean success = false;
            while(i < MAX_RETRIES && !success) {
                // We use random tags to avoid collisions
                // Re-try up to MAX_TRIES times if we fail
                String tag = Long.toHexString(random.nextLong());
                tempFile = new File(parent, name + ".tmp." + tag);
                success = tempFile.createNewFile();
                i = i + 1;
            }

            if(!success) {
                // Unable to create temporary file, abort
                log.error("Unable to create new temporary file.");
                log.error("Absolute path was: " + tempFile.getAbsolutePath());
                throw new Exception("Copy-on-write failed: Unable to create new temporary file.");
            }

            // Get path names, set output stream
            tempPath = tempFile.toPath();
            destPath = file.toPath();
            out = new FileOutputStream(tempFile);
        }
    }
    
    public void write(int b) throws IOException {
        out.write(b);
    }

    public void flush() throws IOException {
        out.flush();
    }

    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    public void write(byte[] b) throws IOException {
        out.write(b);
    }

    /**
     * Just in case - close stream on garbage collection.
     */
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    /**
     * Custom close implementation.
     * Automatically performs an atomic move if necessary and updates node
     * properties of the underlying node.
     */
    public void close() throws IOException {
        if(isClosed && isMoved) return;

        // Close stream first
        out.close();
        isClosed = true;

        // Perform atomic move (if copy-on-write is set up)
        // Otherwise we have nothing special to do here
        if(copyOnWrite && !isMoved) {
            try {
                // Attempt to perform ATOMIC_MOVE on filesystem
                Files.move(
                    tempPath, destPath,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
                isMoved = true;
            } catch(AtomicMoveNotSupportedException e) {
                // Filesystem does not support ATOMIC_MOVE argument
                // Try with a regular move instead, that should work
                log.error(e, e);
                Files.move(
                    tempPath, destPath,
                    StandardCopyOption.REPLACE_EXISTING);
                isMoved = true;
            } catch(Exception e) {
                // Unable to move the file - re-try one last time w/o args.
                log.error(e, e);
                Files.move(tempPath, destPath);
                isMoved = true;
            }
        } else {
            isMoved = true;
        }

        try {
            // Update node properties
            node.setProperty(AbstractNode.PROPERTY_LAST_MODIFIED, file.lastModified());

            // Check for auto-indexing
            VirtualFileSystemRepository vfsRepo = ((VirtualFileSystemNode) node).getRepository();
            
            if(vfsRepo.isAutoFulltextIndexingEnabled()) {
                log.debug("Auto fulltext indexing enabled, indexing after write!");
                vfsRepo.getIndexer().index(node);
            }

        } catch (Exception e) {
            log.error(e, e);
            throw new IOException(e.toString());
        }
    }
}
