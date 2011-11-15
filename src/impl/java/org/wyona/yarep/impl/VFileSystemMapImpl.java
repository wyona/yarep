package org.wyona.yarep.impl;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.log4j.Category;
import org.wyona.commons.io.FileUtil;
import org.wyona.yarep.core.Map;
import org.wyona.yarep.core.Path;
import org.wyona.yarep.core.RepositoryException;
import org.wyona.yarep.core.UID;
import org.wyona.yarep.core.impl.vfs.SplitPathConfig;

/**
 *
 */
public class VFileSystemMapImpl implements Map {

    private static Category log = Category.getInstance(VFileSystemMapImpl.class);

    protected File pathsDir;
    protected Pattern[] ignorePatterns;
    protected ChildrenFilter childrenFilter = new ChildrenFilter();
    
    // Configuration parameters of the <splitpath ...> element
    private boolean splitPathEnabled = false;
    private SplitPathConfig splitPathConfig = new SplitPathConfig();


    /**
     *
     */
    public void readConfig(Configuration mapConfig, File repoConfigFile) throws RepositoryException {
        try {
            setPathsDir(new File(mapConfig.getAttribute("src")), repoConfigFile);

            if (mapConfig != null) {
                Configuration[] ignoreElements = mapConfig.getChildren("ignore");
                setIgnorePatterns(ignoreElements);
            }
            // Read the <splitpath> configuration
            log.debug("Reading Split Path Configuation");
            Configuration splitConfig = mapConfig.getChild("splitpath", false);
            if (splitConfig != null) {
                splitPathConfig.setSplitparts(Integer.parseInt(splitConfig.getAttribute("depth", "0")));
                splitPathConfig.setSplitlength(Integer.parseInt(splitConfig.getAttribute("length", "0")));
                splitPathConfig.setEscapeChar(splitConfig.getAttribute("escape", "-"));

                int numberOfIncludePaths = splitConfig.getChildren("include").length;
                int i = 0;
                if (numberOfIncludePaths > 0) {
                    String[] includepaths = new String[numberOfIncludePaths];
                    for (Configuration include : splitConfig.getChildren("include")) {
                        includepaths[i++] = include.getAttribute("path");
                    }
                    splitPathConfig.setIncludepaths(includepaths);
                }
                log.debug("Split Path Configuration DONE");
                splitPathEnabled = true;
            } 
            
        } catch(Exception e) {
            log.error(e);
            throw new RepositoryException("Could not read map configuration: " 
                    + repoConfigFile.getAbsolutePath() + e.getMessage(), e);
        }
    }

    /**
     * Set paths directory
     */
    public void setPathsDir(File src,  File repoConfigFile) throws RepositoryException {
        pathsDir = src;
        if (!pathsDir.isAbsolute()) {
            pathsDir = FileUtil.file(repoConfigFile.getParent(), pathsDir.toString());
        }
        log.info("Paths dir: " + pathsDir.toString());

        if (!pathsDir.exists()) {
            log.error("No such file or directory: " + pathsDir);
            throw new RepositoryException("No such file or directory: " + pathsDir);
        }
    }
    
    /**
     * Test if path should be ignored
     * @return true returns true if the path can be ignored
     */
    public boolean ignorePath(String path) {
        if (ignorePatterns != null) {
            for (int i=0; i<this.ignorePatterns.length; i++) {
                Matcher matcher = this.ignorePatterns[i].matcher(path); 
                if (matcher.matches()) {
                    log.debug(path + " matched ignore pattern " + ignorePatterns[i].pattern());
                    return true;
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug(path + " did not match any ignore patterns");
        }
        return false;
    }

    /**
     *
     */
    public boolean isResource(Path path) throws RepositoryException {
        File file = null;
        if (splitPathEnabled) {
            String maybeSplitedPath = SplitPathConfig.splitPathIfRequired(path.toString(), this.splitPathConfig);
            file = new File(pathsDir + maybeSplitedPath);
        }
        if (file == null || !file.exists()) {
            file = new File(pathsDir + path.toString());
        }
        return file.isFile();
    }

    /**
     *
     */
    public boolean exists(Path path) throws RepositoryException {
        File file = null;
        if (splitPathEnabled) {
            String maybeSplitedPath = SplitPathConfig.splitPathIfRequired(path.toString(), this.splitPathConfig);
            file = new File(pathsDir + maybeSplitedPath);
        }
        if (file == null || !file.exists()) {
            file = new File(pathsDir + path.toString());
        }
        boolean result = file.exists() && !ignorePath(file.getPath());
        log.debug("file.exists()="+result+": File: "+file.getPath());
        return result;
    }

    /**
     * Calling this method has no effect anymore because delete is done in the storage impl!
     */
    public boolean delete(Path path) throws RepositoryException {
        // don't do anything because if we delete the file here, the delete
        // in the storage will fail later
        //File file = new File(pathsDir + path.toString());
        //return file.delete();
        return true;
    }

    /**
     *
     */
    public boolean isCollection(Path path) throws RepositoryException {
        File file = null;
        if (splitPathEnabled) {
            String maybeSplitedPath = SplitPathConfig.splitPathIfRequired(path.toString(), this.splitPathConfig);
            file = new File(pathsDir + maybeSplitedPath);
        }
        if (file == null || !file.exists()) {
            file = new File(pathsDir + path.toString());
        }
        return file.isDirectory();
    }

    /**
     * Get children, the path of the children includes the path of the parent!
     */
    public Path[] getChildren(Path path) throws RepositoryException {
        // Note: path is always NOT splited, because the caller of this method does not know anything about it
        log.debug("path = "+path.toString());
        if (splitPathEnabled) {
            File startingDirectory = new File(pathsDir + path.toString());
            if (!startingDirectory.exists()) {
                log.warn("No such file or directory: " + startingDirectory);
                return new Path[0];
            }
            if (startingDirectory.isFile()) {
                log.warn("Can not get children form a file! : " + startingDirectory);
                return new Path[0];
            }
            
            List<File> allChildren = getAllFiles(startingDirectory);
            log.debug("Number of children: " + allChildren.size() + " (" + startingDirectory + ")");
            List<Path> validChildrenPaths = new ArrayList<Path>();
            String fileSepForRegEx = File.separator;
            if (fileSepForRegEx.equals("\\")) {
                fileSepForRegEx = "\\\\"; // this is a double backslash, used for the regex later
            }
            
            for (File child: allChildren) {
                String unsplitPath = child.getAbsolutePath().replaceAll(fileSepForRegEx, "/"); // whatever the file separator was, yarep uses "/"
                log.debug("startingDirectory= "+startingDirectory);
                log.debug("child = "+child.getAbsolutePath());
                if (unsplitPath.startsWith(startingDirectory.getAbsolutePath())) {
                    unsplitPath = unsplitPath.substring(startingDirectory.getAbsolutePath().length());
                    log.debug("1. path to unsplit = "+unsplitPath);
                    unsplitPath = SplitPathConfig.unsplitPathIfRequired(unsplitPath, splitPathConfig);
                    log.debug("2. unsplitPath = "+unsplitPath);
                    Path newPath = null;
                    if (!ignorePath(unsplitPath)) {
                        if (path.toString().endsWith(File.separator)) {
                            newPath = new Path(path + unsplitPath);
                            log.debug("3a. Added "+newPath.toString());
                        } else {
                            // NOTE: Do not use File.separator here, because it's the repository path and not the Operating System's File System path
                            newPath = new Path(path + "/" + unsplitPath);
                            log.debug("3b. Added "+newPath.toString());
                        }
                        validChildrenPaths.add(newPath);
                   } else {
                       log.debug("ignored: "+child.getAbsolutePath());
                   }
                 } else {
                    log.error("Something is wrong: children are not within parents!");
                }
            }
            Path[] childrenArray = validChildrenPaths.toArray(new Path[validChildrenPaths.size()]);
            return childrenArray;
            
        } else {
            File file = new File(pathsDir + path.toString());
            if (!file.exists()) {
                log.warn("No such file or directory: " + file);
                return new Path[0];
            }

            String[] filenames = file.list(this.childrenFilter);

        // NOTE: This situation should only occur if one is trying to get children for a file than a directory! One might want to consider to test first with isResource() or isCollection().
            if (filenames == null) {
                log.warn("No children: " + path + " (" + file + ")");
                return new Path[0];
            }

            log.debug("Number of children: " + filenames.length + " (" + file + ")");
            Path[] children = new Path[filenames.length];
            for (int i = 0;i < children.length; i++) {
                if (path.toString().endsWith(File.separator)) {
                    children[i] = new Path(path + filenames[i]);
                } else {
                    // NOTE: Do not use File.separator here, because it's the repository path and not the Operating System File System path
                    children[i] = new Path(path + "/" + filenames[i]);
                }
                log.debug("Child: " + children[i]);
            }
            return children;
            
        }
    }
    
    /**
     * 
     * @param dir
     * @return List of Files (directories are NOT in the list!)
     */
    private List<File> getAllFiles(File dir) {
        List<File> result = new ArrayList<File>();
        if (dir.isDirectory()) {
            File[] filesAndDirsArray = dir.listFiles();
            List<File> filesAndDirs = Arrays.asList(filesAndDirsArray);
            for (File file : filesAndDirs) {
                if (file.isFile()) {
                    result.add(file);
                } else {
                    List<File> deeperList = getAllFiles(file);
                    result.addAll(deeperList);
                }
            }
        }
        return result;
    }

    /**
     * Get UID
     */
    public synchronized UID getUID(Path path) throws RepositoryException {
        // TODO: Check if leading slash should be removed ...
        return new UID(path.toString());
    }

    /**
     * Create UID:
     */
    public synchronized UID create(Path path, int type) throws RepositoryException {
        log.debug("pathsDir = "+pathsDir.getAbsolutePath());
        log.debug("path = "+path);
        log.debug("path parent = "+path.getParent());
        // TODO: Check if leading slash should be removed ...
        File parent = new File(pathsDir + File.separator + path.getParent().toString()); // e.g. access-control/users
        if (!parent.exists()) {
            log.warn("Directory will be created: " + parent);
            parent.mkdirs();
        }
        if (type == org.wyona.yarep.core.NodeType.COLLECTION) {
            new File(parent, path.getName()).mkdir();
        } else {
            try {
                if (splitPathEnabled) {
                    // splitted e.g: ab/cd/4.xml
                    String maybeSplitedPath = SplitPathConfig.splitPathIfRequired(path.toString(), this.splitPathConfig);
                    log.debug("maybeSplited = "+maybeSplitedPath);
                    String newParent = pathsDir.getAbsolutePath();
                    if (maybeSplitedPath.contains("/")) {
                        // newparent for splitted above would be pathsDir/ab/cd
                        newParent = newParent + maybeSplitedPath.substring(0,maybeSplitedPath.lastIndexOf("/")+1);
                    }
                    String newFileName = new File(maybeSplitedPath).getName();
                    log.debug("new parent = "+newParent);
                    log.debug("new file name = "+newFileName);
                    File newFilePath = new File(newParent , newFileName);
                    new File(newParent).mkdirs();
                    log.debug("new parent exists: "+new File(newParent).exists());
                    boolean created = newFilePath.createNewFile();
                    log.debug("new file exists: "+newFilePath.exists());
                    log.debug("new file is directory: "+newFilePath.isDirectory());
                    if (!created)  {
                        log.debug("Maybe file has not been created: " + newFilePath.getAbsolutePath()); // On Mac OSX 10.6, the file gets created even in this case
                    }
                    
                } else {
                    if(!new File(parent, path.getName()).createNewFile()) log.debug("File has not been created: " + new File(parent, path.getName()));
                }

                
            } catch (Exception e) {
                log.error("Could not create new file!! Exception: "+e.getMessage(), e);
            }
        }
        
        return new UID(path.toString());
    }

    /**
     * An exception gets thrown if you call this method because symbolic links are not implemented for virtual file systems!
     */
    public void addSymbolicLink(Path path, UID uid) throws RepositoryException {
        throw new RepositoryException("Symbolic links not implemented for virtual file system!");
    }
    
    /**
     * Ignore all children which are matched by an ignore pattern (see repository configuration, e.g. src/test/repository/node-fs-example/repository.xml)
     */
    protected class ChildrenFilter implements FilenameFilter {
        public ChildrenFilter() {
        }
        
        /**
         * @param dir is ignored in this implementation
         */
        public boolean accept(File dir, String name) {
            
            if (VFileSystemMapImpl.this.ignorePath(name)) {
                return false;
            } else {
                return true;
            }
        }
    }

    /**
     * Set ignore patterns
     */
    public void setIgnorePatterns(Configuration[] ignoreElements) throws org.apache.avalon.framework.configuration.ConfigurationException {
        if (ignoreElements != null) {
            ignorePatterns = new Pattern[ignoreElements.length];
            for (int i=0; i<ignoreElements.length; i++) {
                String patternString = ignoreElements[i].getAttribute("pattern");
                ignorePatterns[i] = Pattern.compile(patternString);
                log.debug("adding ignore pattern: " + ignorePatterns[i].pattern());
            }
        } else {
            ignorePatterns = null; // see ignorePath(String)
        }
    }

    public SplitPathConfig getSplitPathConfig() {
        return splitPathConfig;
    }

    public void setSplitPathConfig(SplitPathConfig splitPathConfig) {
        this.splitPathConfig = splitPathConfig;
    }

    public boolean isSplitPathEnabled() {
        return splitPathEnabled;
    }

    public void setSplitPathEnabled(boolean splitPathEnabled) {
        this.splitPathEnabled = splitPathEnabled;
    }
}
