package org.wyona.yarep.core.impl.vfs;

public class SplitPathConfig {
    private String[] includepaths = new String[0];
    private int splitparts;
    private int splitlength;
    private String escapeChar = "+";
    
    /**
     * Split Path Configuration: 
     * @param includepaths : if an includepath is configured, e.g. "/base/" then everything after "/base"/ gets splitted, except for the file extension!
     * @param splitparts max subdirectories from the baserpath: e.g. base/xx/xx/xx/xx/restofpath : splitparts = 4
     * @param splitlength the length of the additional subdirectories, e.g. base/xx/xx/xx/xx/restofpath : length=2
     */
    public SplitPathConfig(String[] includepaths, int splitparts, int splitlength) {
        this.includepaths = includepaths;
        this.splitparts = splitparts;
        this.splitlength = splitlength;
    }
    
    /**
     * Creates a config without any include paths
     */
    public SplitPathConfig() {
    }
    
    /**
     * Splits a String such that the result can be used as a repo path for a tree-like repo structure.
     * The original string can be shorter than splitparts * partlength
     * The file extension does not get split.
     * Slashes in the original string are escaped if they are in the range of the split area (splitparts * partlength):
     * The escape character is currently hardcoded and is "+": a slash gets replaced by "+-" and the plus itself by "++".
     *
     * Example for splitparts = 2 and partlength = 3:
     * hugoboss.xml --> hug/obo/ss.xml
     * hugo.xml --> hug/o.xml
     * hug.xml --> hug.xml
     * hugo/boss.xml --> gets stored as hug/o+-/boss.xml
     * hug/oboss.xml --> gets stored as hug/+-o/boss.xml
     * hugo+boss.xml --> gets stored as hug/o++/boss.xml
     * hugobos/s.xml --> gets stored as hug/obo/s/s.xml
     * hugobo/ss.xml --> (special case: actually hugobo could be split into hug/obo/ ... but as the next character in the original string is a "/", we do not allow this in the split string, because "//" can not get unsplit anymore.
     *                   So this becomes hug/obo/+-ss.xml
     * And a mixed example with the escape character:
     * hu/go+bo/ss.xml --> hu+/-go/++bo/ss.xml (Note that the first slash in the original string got replaced by +- and then the actual slash has been inserted!)
     *
     * If the strings length is shorter than parts * partslength, then as many
     * parts as possible are split, e.g.
     * in:  foobar, 2, 5
     * out: fooba/r
     * in:  lorem, 3, 10
     * out: lorem
     *
     * An example with "/" characters:
     * in:  /foobar/lorem/ipsum.txt, parts = 3, lenght = 3
     * out: /foo/bar/-lo/rem/ipsum.txt
     *
     * @param path usually the full yarep path of a node, e.g. "/users/chucknorris.xml" or if you have the data repository at "/data" and you add a node to this repository, the path is relative to this repository path, e.g. "/pictures/..." and not "/data/pictures/...
     * @return split path according to the configured rules
     */
    public static String splitPathIfRequired(String path, SplitPathConfig splitPathConfig) {
        // NOTE: uuid should be a full yarep path, so we can safely remove
        // the leading slash
        
        // check if the given path matches any of the include values
        // in the configuration
        boolean include = false;
        String base = null;
        for (String s : splitPathConfig.getIncludepaths()) {
            if (path.startsWith(s)) {
                include = true;
                base = s;
                break;
            }
        }

        // return the path unchanged if it doesn't match
        // any of the include values
        if (!include) {
            return path;
        }
        
        // remove the leading base string, will be added again later
        path = path.substring(base.length(), path.length());
        // we do not want to split the file ending (e.g. ".xml")
        String suffix = "";
        if (path.contains(".")) {
            suffix = path.substring(path.lastIndexOf("."));
            path = path.substring(0, path.lastIndexOf("."));
        }
        int splitparts = splitPathConfig.getSplitparts();
        int splitlength = splitPathConfig.getSplitlength();
        
        // replace "/" characters where needed
        if (path.length() <= splitparts * splitlength) {
            path = path.replaceAll("\\+", "++");
            path = path.replaceAll("/", "+-");
        } else {
            path = path.replaceAll("\\+", "++");
            path = String.format("%s%s",
                    path.substring(0, splitparts * splitlength).replaceAll("/", "+-"),
                    path.substring(splitparts * splitlength));
        }

        // now do the actual splitting
        StringBuffer splitPath = new StringBuffer(path);
        int slashIndex = splitlength;
        int numberOfSlashesInserted = 0;
        // slashindex < path length + number of already inserted slahes. by each inserted slash, the path gets one char bigger...
        while (slashIndex < (path.length() + numberOfSlashesInserted) && numberOfSlashesInserted < splitparts) {
            splitPath.insert(slashIndex, "/");
            slashIndex = slashIndex + splitlength + 1; // +1 because the inserted slash
            numberOfSlashesInserted++;
        }
        path = base + splitPath.toString();
        
//        ORIGINAL CODE FROM PREVIOUS SPLIT IMPL : I found this too complex and hard to understand.       
//        int len = path.length();
//        int pos = 0;
//        String out = "";
//
//        int partc = 0;
//        int w;
//        while (len > 0 && partc < splitparts) {
//            partc++;
//            if (len < splitlength) {
//                w = len;
//            } else {
//                w = splitlength;
//            }
//            out += path.substring(pos, pos + w);
//            pos += w;
//            len -= w;
//
//            if (len > 0) {
//                out += "/";
//            }
//        }
//
//        // append remainder
//        if (len > 0) {
//            out += path.substring(pos, pos + len);
//        }
//
//        // finally, add the leading zero again and return the new path
//        path = base + out;
        
        
        if (path.contains("//")) {
            path = path.replaceAll("//", "/+-");
        }
        // and we add the suffix again
        path = path + suffix;

        return path;
    }
    
    public static String unsplitPathIfRequired(String path, SplitPathConfig splitPathConfig) {
        boolean include = false;
        String base = "";
        for (String s : splitPathConfig.getIncludepaths()) {
            if (path.startsWith(s)) {
                include = true;
                base = s;
                break;
            }
        }

        if (!include) {
            return path;
        }
        // remove the leading base string, will be added again later
        path = path.substring(base.length(), path.length());
        int splitparts = splitPathConfig.getSplitparts();
        int splitlength = splitPathConfig.getSplitlength();

        // we know that each "/" must be removed and every "+" becomes a slash.
        
        // the area where we apply the logic is the original length (splitparts * splitlength) plus one char for each splitpart ("/")  
        int splitLength = (splitparts * splitlength)+splitparts+1;
        if (path.length()<splitLength) {
            splitLength = path.length();
        }
        // remove all slashes
        path = path.substring(0, splitLength).replaceAll("/", "")+path.substring(splitLength);
        
        // a simple replacement of (++ -> +) and (+- -> /) does not work because they must be replaced from left to right.
        StringBuffer convertedPath = new StringBuffer("");
        char current;
        char next;
        int i = 0;
        for (; i < path.length()-1; i++) {
            current = path.charAt(i);
            next = path.charAt(i+1);
            if (current == '+' && next == '+') {
                convertedPath.append("+");
                i++; // skip next char because we found a token!
            } else if (current == '+' && next == '-') {
                convertedPath.append("/");
                i++; // skip next char because we found a token!
            } else {
                convertedPath.append(current);
            }
        }
        if (i == path.length()-1) {
            convertedPath.append(path.charAt(i));
        }        
        
        path = base + convertedPath.toString();
        return path;
    }
    
    public static boolean isIncludePath(String path, SplitPathConfig splitPathConfig) {
        boolean isIncludePath = false;
        // currently the configuration of split include paths is like "/path/", therefore we have to add a slash if it is missing. otherwise it doesnot match
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        for (String s : splitPathConfig.getIncludepaths()) {
            if (path.startsWith(s)) {
                isIncludePath = true;
                break;
            }
        }
        return isIncludePath;
    }

    

    public String[] getIncludepaths() {
        return includepaths;
    }

    public void setIncludepaths(String[] includepaths) {
        this.includepaths = includepaths;
    }

    public int getSplitparts() {
        return splitparts;
    }

    public void setSplitparts(int splitparts) {
        this.splitparts = splitparts;
    }

    public int getSplitlength() {
        return splitlength;
    }

    public void setSplitlength(int splitlength) {
        this.splitlength = splitlength;
    }

    public String getEscapeChar() {
        return escapeChar;
    }

    public void setEscapeChar(String separator) {
        this.escapeChar = separator;
    }
    
}
