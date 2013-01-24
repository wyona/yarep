package org.wyona.yarep.impl.repo.vfs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;

import org.wyona.yarep.core.NoSuchRevisionException;
import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.NodeStateException;
import org.wyona.yarep.core.NodeType;
import org.wyona.yarep.core.Path;
import org.wyona.yarep.core.Property;
import org.wyona.yarep.core.PropertyType;
import org.wyona.yarep.core.RepositoryException;
import org.wyona.yarep.core.Revision;
import org.wyona.yarep.core.UID;
import org.wyona.yarep.core.attributes.VersionableV1;
import org.wyona.yarep.impl.AbstractNode;
import org.wyona.yarep.impl.DefaultProperty;

/**
 * Utility class to index and search revisions of a node by date
 */
public class DateIndexerSearcher {
    private static Logger log = Logger.getLogger(DateIndexerSearcher.class);

    private String TIME_ZONE_ID = "UTC";

    //private static final String DATE_INDEX_BASE_DIR = "index_date"; // DEPRECATED
    private static final String DATE_INDEX_BASE_DIR = "index_date_utc"; // INFO: Introduced new directory, because the directory structure is now using for setting/getting UTC (instead the local time), because this will make sure that timezone changes or winter/summer does not cause problems anymore (Also see http://www.odi.ch/prog/design/datetime.php)

    private static final String DATE_INDEX_ID_FILENAME = "id.txt";

    private File metaDir;
    private Node node;

    /**
     * @param node Node for revisions shall be indexed by date
     * @param metaDir Meta directory of this node
     */
    public DateIndexerSearcher(Node node, File metaDir) {
        this.node = node;
        this.metaDir = metaDir;
    }

    /**
     * Check if date index already exists
     */
    public boolean indexExists() {
        File dateIndexBaseDir = new File(this.metaDir, DATE_INDEX_BASE_DIR);
        return dateIndexBaseDir.isDirectory();
    }

    /**
     * Get (next) revision older than a specific date
     * @param date Date which is used as reference
     */
    public Revision getRevisionOlderThan(Date date) throws Exception {
        //log.debug("Get revision older than: " + format(date));
        Date olderThanDate = new Date(date.getTime() - 1);

        Revision revision = getRevision(olderThanDate);
        if (revision != null && date.getTime() > revision.getCreationDate().getTime()) {
            return revision;
        } else {
            log.warn("There seems to be NO revision older than: " + format(date));
            return null;
        }
    }

    /**
     * Get most recent revision
     * @return Most recent (head) revision, and if no such revision exists, then return null
     */
    public Revision getMostRecentRevision() {
        try {
            File dateIndexBaseDir = new File(this.metaDir, DATE_INDEX_BASE_DIR);
            String[] years = sortAlphabeticallyAscending(dateIndexBaseDir.list());
            if (years != null && years.length > 0) {
                //log.debug("Year: " + years[years.length - 1]); // INFO: Descend, e.g. 2012, 2011, 2010, 2009, ...
                return getRevisionFromIndexFile(getYoungestRevisionOfYear(new File(dateIndexBaseDir, years[years.length - 1])));
                //return node.getRevision(getRevisionName(getYoungestRevisionOfYear(new File(dateIndexBaseDir, years[years.length - 1]))));
            }
            log.warn("No year and hence no revision: " + dateIndexBaseDir);
            return null;
        } catch(Exception e) {
            log.error(e, e);
            return null;
        }
    }

    /**
     * Get oldest revision
     * @return Oldest revision, and if no such revision exists, then return null
     */
    public Revision getOldestRevision() {
        try {
            // TODO: Find oldest year, set date to one year below oldest year and then use getRevisionOlderThan(Date date)
            log.warn("Implementation not finished yet!");
            File dateIndexBaseDir = new File(this.metaDir, DATE_INDEX_BASE_DIR);
            String[] years = sortAlphabeticallyAscending(dateIndexBaseDir.list());
            if (years != null && years.length > 0) {
                //log.debug("Year: " + years[years.length - 1]); // INFO: Descend, e.g. 2012, 2011, 2010, 2009, ...
                return getRevisionFromIndexFile(getYoungestRevisionOfYear(new File(dateIndexBaseDir, years[years.length - 1])));
                //return node.getRevision(getRevisionName(getYoungestRevisionOfYear(new File(dateIndexBaseDir, years[years.length - 1]))));
            }
            log.warn("No year and hence no revision: " + dateIndexBaseDir);
            return null;
        } catch(Exception e) {
            log.error(e, e);
            return null;
        }
    }

    /**
     * Get revision (based on date index) with a creation date which is equal or just the next older revision than the specified date, e.g. specified date=2011.03.17T17:23:57:09:690, then a revision which has either exactly this creation date or which is the next older revision, e.g. 2011.03.17T17:23:57:09:698
     * @param date Date for which a revision shall be found
     */
    public Revision getRevision(Date date) throws Exception {
        //log.debug("Get revision for date: " + format(date));
        File dateIndexBaseDir = new File(this.metaDir, DATE_INDEX_BASE_DIR);
        log.debug("Use vfs-repo specific implementation: " + node.getPath() + ", " + date);
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(java.util.TimeZone.getTimeZone(TIME_ZONE_ID));
        cal.setTime(date);

        String path = getRevisionByYear(dateIndexBaseDir, cal);
        if (path == null) {
            log.debug("No index file found for date: " + date);
            return null;
        }
        if (path != null && !new File(path).isFile()) {
            log.warn("No such index file: " + path);
            return null;
        }
        String revisionName = getRevisionName(path);
        try {
            return node.getRevision(revisionName);
        } catch(NoSuchRevisionException e) {
            if (new File(path).isFile()) {
                log.warn("It seems that the index is out of sync, because an index file exists '" + path + "', but no such revision: " + revisionName);
            }
            throw e;
        }
        //return getRevisionByYear(dateIndexBaseDir, cal);
    }

    /**
     * Get revision by year, whereas the algorithm assumes that the order which File.list() is generating is ascending: 2007, 2008, 2009, 2010, ...
     * @param dateIndexBaseDir Directory where date index is located
     * @param cal Point in time for which a revision shall be found
     */
    private String getRevisionByYear(File dateIndexBaseDir, Calendar cal) throws Exception {
/* DEBUG
        String[] unsortedYears = {"2018", "1989", "2010" ,"2009", "2017"};
        String[] years = sortAlphabeticallyAscending(unsortedYears);
*/

        String[] years = sortAlphabeticallyAscending(dateIndexBaseDir.list());
        for (int i = years.length - 1; i >= 0; i--) { // INFO: Descend, e.g. 2012, 2011, 2010, 2009, ...
            log.debug("Year: " + years[i]);
            try {
                int year = new Integer(years[i]).intValue();

                if (year < cal.get(Calendar.YEAR)) {
                    log.debug("Year '" + year + "' which matched is smaller, hence get youngest revision for this year.");
                    return getYoungestRevisionOfYear(new File(dateIndexBaseDir, years[i]));
                } else if (year == cal.get(Calendar.YEAR)) {
                    log.debug("Year '" + year + "' which matched is equals, hence start comparing within this particular year.");
                    String path = getRevisionByMonth(new File(dateIndexBaseDir, years[i]), cal);
                    if (getRevisionFromIndexFile(path) != null) {
                        return path;
                    } else {
                        log.debug("Try next year lower ...");
                    }
                } else {
                    log.debug("Try next year lower ...");
                }
            } catch(NumberFormatException e) {
                log.warn("Does not seem to be a year '" + years[i] + "' and hence will be ignored.");
            }
        }
        return null;
    }

    /**
     * Get youngest (most recent) revision of year, whereas the algorithm assumes that the order of months is ascending 01, 02, ..., 12
     * @param yearDir Directory of year containing months
     */
    private String getYoungestRevisionOfYear(File yearDir) throws Exception {
        String[] months = sortAlphabeticallyAscending(yearDir.list());
        for (int k = months.length - 1; k >= 0; k--) {
            try {
                int month = Integer.parseInt(months[k]);
                //int month = new Integer(months[k]).intValue();
                if (1 <= month && month <= 12) {
                    log.debug("Youngest month '" + month + "' of year '" + yearDir + "' found");
                    return getYoungestRevisionOfMonth(new File(yearDir, months[k]));
                } else {
                    log.warn("Does not seem to be a month '" + month + "' and hence will be ignored.");
                }
            } catch(NumberFormatException e) {
                log.warn("Does not seem to be a month '" + months[k] + "' and hence will be ignored.");
            }
        }
        log.warn("No youngest month found within year '" + yearDir + "'");
        return null;
    }

    /**
     * Get youngest revision of month, whereas the algorithm assumes that the order of days is ascending 01, 02, ..., 31
     * @param monthDir Directory of month containing days
     */
    private String getYoungestRevisionOfMonth(File monthDir) throws Exception {
        String[] days = sortAlphabeticallyAscending(monthDir.list());
        for (int k = days.length - 1; k >= 0; k--) {
            try {
                int day = Integer.parseInt(days[k]);
                if (1 <= day && day <= 31) {
                    log.debug("Youngest day '" + day + "' of month '" + monthDir + "' found");
                    return getYoungestRevisionOfDay(new File(monthDir, days[k]));
                } else {
                    log.warn("Does not seem to be a day '" + day + "' and hence will be ignored.");
                }
            } catch(NumberFormatException e) {
                log.warn("Does not seem to be a day '" + days[k] + "' and hence will be ignored.");
            }
        }
        log.warn("No youngest day found within month '" + monthDir + "'");
        return null;
    }

    /**
     * Get youngest revision of day, whereas the algorithm assumes that the order of hours is ascending 00, 01, ..., 23
     * @param dayDir Directory of day containing hours
     */
    private String getYoungestRevisionOfDay(File dayDir) throws Exception {
        String[] hours = sortAlphabeticallyAscending(dayDir.list());
        for (int k = hours.length - 1; k >= 0; k--) {
            try {
                int hour = Integer.parseInt(hours[k]);
                if (0 <= hour && hour <= 23) {
                    log.debug("Youngest hour '" + hour + "' of day '" + dayDir + "' found");
                    return getYoungestRevisionOfHour(new File(dayDir, hours[k]));
                } else {
                    log.warn("Does not seem to be a hour '" + hour + "' and hence will be ignored.");
                }
            } catch(NumberFormatException e) {
                log.warn("Does not seem to be a hour '" + hours[k] + "' and hence will be ignored.");
            }
        }
        log.warn("No youngest hour found within day '" + dayDir + "'");
        return null;
    }

    /**
     * Get youngest revision of hour, whereas the algorithm assumes that the order of minutes is ascending 00, 01, ..., 59
     * @param hourDir Directory of hour containing minutes
     */
    private String getYoungestRevisionOfHour(File hourDir) throws Exception {
        String[] minutes = sortAlphabeticallyAscending(hourDir.list());
        for (int k = minutes.length - 1; k >= 0; k--) {
            try {
                int minute = Integer.parseInt(minutes[k]);
                if (0 <= minute && minute <= 59) {
                    log.debug("Youngest minute '" + minute + "' of hour '" + hourDir + "' found");
                    return getYoungestRevisionOfMinute(new File(hourDir, minutes[k]));
                } else {
                    log.warn("Does not seem to be a minute '" + minute + "' and hence will be ignored.");
                }
            } catch(NumberFormatException e) {
                log.warn("Does not seem to be a minute '" + minutes[k] + "' and hence will be ignored.");
            }
        }
        log.warn("No youngest hour found within hour '" + hourDir + "'");
        return null;
    }

    /**
     * Get youngest revision of minute, whereas the algorithm assumes that the order of seconds is ascending 00, 01, ..., 59
     * @param minuteDir Directory of minute containing seconds
     */
    private String getYoungestRevisionOfMinute(File minuteDir) throws Exception {
        String[] seconds = sortAlphabeticallyAscending(minuteDir.list());
        for (int k = seconds.length - 1; k >= 0; k--) {
            try {
                int second = Integer.parseInt(seconds[k]);
                if (0 <= second && second <= 59) {
                    log.debug("Youngest second '" + second + "' of minute '" + minuteDir + "' found");
                    return getYoungestRevisionOfSecond(new File(minuteDir, seconds[k]));
                } else {
                    log.warn("Does not seem to be a second '" + second + "' and hence will be ignored.");
                }
            } catch(NumberFormatException e) {
                log.warn("Does not seem to be a second '" + seconds[k] + "' and hence will be ignored.");
            }
        }
        log.warn("No youngest second found within minute '" + minuteDir + "'");
        return null;
    }

    /**
     * Get youngest revision of second, whereas the algorithm assumes that the order of milliseconds is ascending 0, 1, ..., 999
     * @param secondDir Directory of second containing milliseconds 
     */
    private String getYoungestRevisionOfSecond(File secondDir) throws Exception {
        String[] millis = sortAlphabeticallyAscending(secondDir.list());
        for (int k = millis.length - 1; k >= 0; k--) {
            try {
                int milli = Integer.parseInt(millis[k]);
                if (0 <= milli && milli <= 999) {
                    log.debug("Youngest millisecond '" + milli + "' of second '" + secondDir + "' found");

                    String path = secondDir.getAbsolutePath() + File.separator + millis[k] + File.separator + DATE_INDEX_ID_FILENAME;
                    log.debug("ID File: " + path);

                    if (new File(path).isFile()) {
                        return path;
                    } else {
                        log.warn("No such index file: " + path + " (Probably has been deleted by accident, please delete the millisec directory to clean this up)");
                        return null;
                    }
                } else {
                    log.warn("Does not seem to be a millisecond '" + milli + "' and hence will be ignored.");
                }
            } catch(NumberFormatException e) {
                log.warn("Does not seem to be a millisecond '" + millis[k] + "' and hence will be ignored.");
            }
        }
        log.warn("No youngest millisecond found within second '" + secondDir + "'");
        return null;
    }

    /**
     * Get revision by month
     */
    private String getRevisionByMonth(File yearDir, Calendar cal) throws Exception {
        String[] months = sortAlphabeticallyAscending(yearDir.list()); // IMPORTANT: Make sure the order is ascending: 1, 2, ..., 12
        for (int k = months.length - 1; k >= 0; k--) {
            if(log.isDebugEnabled()) log.debug("Month: " + months[k] + " (" + cal + ")");
            try {
                int month = new Integer(months[k]).intValue();

                if (month < cal.get(Calendar.MONTH) + 1) {
                    log.debug("Month '" + month + "' which matched is smaller, hence get youngest revision for this month.");
                    return getYoungestRevisionOfMonth(new File(yearDir, months[k]));
                } else if (month == cal.get(Calendar.MONTH) + 1) {
                    log.debug("Month '" + month + "' which matched is equals, hence start comparing within this particular month.");
                    String path = getRevisionByDay(new File(yearDir, months[k]), cal);
                    if (getRevisionFromIndexFile(path) != null) {
                        return path;
                    } else {
                        log.debug("Try next month lower ...");
                    }
                } else {
                    log.debug("Try next month lower ...");
                }
            } catch(NumberFormatException e) {
                log.warn("Does not seem to be a month: " + months[k]);
            }
        }
        return null;
    }

    /**
     * Get revision by day
     */
    private String getRevisionByDay(File monthDir, Calendar cal) throws Exception {
        String[] days = sortAlphabeticallyAscending(monthDir.list()); // IMPORTANT: Make sure the order is ascending: 1, 2, ..., 31
        for (int k = days.length - 1; k >= 0; k--) {
            log.debug("Day: " + days[k]);
            try {
                int day = new Integer(days[k]).intValue();

                if (day < cal.get(Calendar.DAY_OF_MONTH)) {
                    log.debug("Day '" + day + "' which matched is smaller, hence get youngest revision for this day.");
                    return getYoungestRevisionOfDay(new File(monthDir, days[k]));
                } else if (day == cal.get(Calendar.DAY_OF_MONTH)) {
                    log.debug("Day '" + day + "' which matched is equals, hence start comparing within this particular day.");
                    String path = getRevisionByHour(new File(monthDir, days[k]), cal);
                    if (getRevisionFromIndexFile(path) != null) {
                        return path;
                    } else {
                        log.debug("Try next day lower ...");
                    }
                } else {
                    log.debug("Try next day lower ...");
                }
            } catch(NumberFormatException e) {
                log.warn("Does not seem to be a day: " + days[k]);
            }
        }
        return null;
    }

    /**
     * Get revision by hour 
     */
    private String getRevisionByHour(File dayDir, Calendar cal) throws Exception {
        //log.debug("Try to find revision for UTC hour: " + cal.get(Calendar.HOUR_OF_DAY));
        String[] hours = sortAlphabeticallyAscending(dayDir.list()); // IMPORTANT: Make sure the order is ascending: 1, 2, 3, ...
        for (int k = hours.length - 1; k >= 0; k--) {
            log.debug("Hour: " + hours[k]);
            try {
                int hour = Integer.parseInt(hours[k]);

                if (hour < cal.get(Calendar.HOUR_OF_DAY)) {
                    log.debug("Hour '" + hour + "' which matched is smaller, hence get youngest revision for this hour.");
                    return getYoungestRevisionOfHour(new File(dayDir, hours[k]));
                } else if (hour == cal.get(Calendar.HOUR_OF_DAY)) {
                    log.debug("Hour '" + hour + "' which matched is equals, hence start comparing within this particular hour.");
                    String path = getRevisionByMinute(new File(dayDir, hours[k]), cal);
                    if (getRevisionFromIndexFile(path) != null) {
                        return path;
                    } else {
                        log.debug("Try next hour lower ...");
                    }
                } else {
                    log.debug("Try next hour lower ...");
                }
            } catch(NumberFormatException e) {
                log.warn("Does not seem to be a hour: " + hours[k]);
            }
        }
        return null;
    }

    /**
     * Get revision by minute 
     */
    private String getRevisionByMinute(File hourDir, Calendar cal) throws Exception {
        String[] minutes = sortAlphabeticallyAscending(hourDir.list()); // IMPORTANT: Make sure the order is ascending: 1, 2, 3, ...
        for (int k = minutes.length - 1; k >= 0; k--) {
            log.debug("Minute: " + minutes[k]);
            try {
                int minute = Integer.parseInt(minutes[k]);

                if (minute < cal.get(Calendar.MINUTE)) {
                    log.debug("Minute '" + minute + "' which matched is smaller, hence get youngest revision for this minute.");
                    return getYoungestRevisionOfMinute(new File(hourDir, minutes[k]));
                } else if (minute == cal.get(Calendar.MINUTE)) {
                    log.debug("Minute '" + minute + "' which matched is equals, hence start comparing within this particular minute.");
                    String path = getRevisionBySecond(new File(hourDir, minutes[k]), cal);
                    try {
                        if (getRevisionFromIndexFile(path) != null) {
                            return path;
                        } else {
                            log.debug("Try next minute lower ...");
                        }
                    } catch(IndexOutOfSyncException e) {
                        // TODO: We should rather just check one millisec lower, because we might also ignore other revisions, which are fine
                        log.warn("There seems to be an index file '" + e.getPath() + "' which is out of sync, whereas we will ignore it and continue with next minute lower...");
                    }
                } else {
                    log.debug("Try next minute lower ...");
                }
            } catch(NumberFormatException e) {
                log.warn("Does not seem to be a minute: " + minutes[k]);
            }
        }
        return null;
    }

    /**
     * Get revision by second
     */
    private String getRevisionBySecond(File minuteDir, Calendar cal) throws Exception {
        String[] seconds = sortAlphabeticallyAscending(minuteDir.list()); // IMPORTANT: Make sure the order is ascending: 0, 1, 2, 3, ..., 60
        for (int k = seconds.length - 1; k >= 0; k--) {
            log.debug("Second: " + seconds[k]);
            try {
                int second = Integer.parseInt(seconds[k]);

                if (second < cal.get(Calendar.SECOND)) {
                    log.debug("Second '" + second + "' which matched is smaller, hence get youngest revision for this second.");
                    return getYoungestRevisionOfSecond(new File(minuteDir, seconds[k]));
                } else if (second == cal.get(Calendar.SECOND)) {
                    log.debug("Second '" + second + "' which matched is equals, hence start comparing within this particular second.");
                    String path = getRevisionByMillisecond(new File(minuteDir, seconds[k]), cal);
                    if (getRevisionFromIndexFile(path) != null) {
                        return path;
                    } else {
                        log.debug("Try next second lower ...");
                    }
                } else {
                    log.debug("Try next second lower ...");
                }
            } catch(NumberFormatException e) {
                log.warn("Does not seem to be a second: " + seconds[k]);
            }
        }
        return null;
    }

    /**
     * Get revision by millisecond
     * @param secondDir TODO
     * @param cal TODO
     */
    private String getRevisionByMillisecond(File secondDir, Calendar cal) throws Exception {
        String[] millis = sortAlphabeticallyAscending(secondDir.list()); // IMPORTANT: Make sure the order is ascending: 0, 1, 2, 3, ..., 999
        for (int k = millis.length - 1; k >= 0; k--) {
            log.debug("Millisecond: " + millis[k]);
            try {
                int milli = new Integer(millis[k]).intValue();
                if (milli <= cal.get(Calendar.MILLISECOND) ) {
                    log.debug("Millisecond matched: " + milli);

                    String path = secondDir.getAbsolutePath() + File.separator + millis[k] + File.separator + DATE_INDEX_ID_FILENAME;
                    log.debug("ID File: " + path);

                    if (new File(path).isFile()) {
                        return path;
                    } else {
                        log.warn("No such index file: " + path + " (Probably has been deleted by accident, please delete the millisec directory to clean this up)");
                        return null;
                    }
                }
            } catch(NumberFormatException e) {
                log.warn("Does not seem to be a millisecond: " + millis[k]);
            }
        }
        return null;
    }

    /**
     * Delete revision from date index
     * @param revisionName Name of revision to be deleted
     */
    public void deleteRevision(String revisionName) throws Exception {
        if (!indexExists()) {
            log.warn("No index yet, hence cannot delete revision '" + revisionName + "' from index.");
            return;
        }

        File dateDirF = getRevisionDateDir(revisionName);
        File revisionIdFile = new File(dateDirF, DATE_INDEX_ID_FILENAME);
        if (revisionIdFile.isFile()) {
            String indexedRevisionName = getRevisionName(revisionIdFile.getAbsolutePath());
            if (indexedRevisionName.equals(revisionName)) {
                log.warn("Delete revision date/ID file: " + revisionIdFile);
                revisionIdFile.delete(); // TODO: What about deleting milliseconds directory, etc.?!
                deleteEmptyDirectories(revisionIdFile.getParentFile());
            } else {
                log.warn("Revision name '" + revisionName + "' is not equals revision name inside index file: " + revisionIdFile);
            }
        } else {
           log.warn("No such revision date file: " + revisionIdFile);
        }
    }

    /**
     * Add revision to date index
     */
    public void addRevision(String revisionName) throws Exception {
        log.debug("Add revision '" + revisionName + "' to date index ...");
        if (!indexExists()) {
            buildDateIndex();
        }

        File dateDirF = getRevisionDateDir(revisionName);
        if (!dateDirF.isDirectory()) {
            dateDirF.mkdirs();
            File revisionIdFile = new File(dateDirF, DATE_INDEX_ID_FILENAME);
            PrintWriter pw = new PrintWriter(new FileOutputStream(revisionIdFile));
            pw.print(revisionName);
            pw.close();
        } else {
           log.debug("Revision '" + revisionName + "' already exists within date index!");
        }
    }

    /**
     * Build date index in order to retrieve revisions more quickly based on creation date
     */
    public void buildDateIndex() throws Exception {
        File dateIndexBaseDir = new File(this.metaDir, DATE_INDEX_BASE_DIR);
        log.debug("Build date index: " + dateIndexBaseDir);

        if (!dateIndexBaseDir.isDirectory()) {
            dateIndexBaseDir.mkdirs();
        }

        Revision[] revisions = node.getRevisions();
        for (int i = revisions.length - 1; i >= 0; i--) {
            addRevision(revisions[i].getRevisionName());
        }
    }

    /**
     * Sort alphabetically ascending
     */
    private String[] sortAlphabeticallyAscending(String[] array) {
/*
        String unsorted = "";
        for (int i = array.length - 1; i >= 0; i--) {
            unsorted = unsorted + " " + array[i];
        }
        log.warn("DEBUG: Array unsorted: " + unsorted);
*/

        java.util.Arrays.sort(array);

/*
        String sorted = "";
        for (int i = array.length - 1; i >= 0; i--) {
            sorted = sorted + " " + array[i];
        }
        log.warn("DEBUG: Array sorted: " + sorted);
*/

        return array;
    }

    /**
     * Format date
     */
    private String format(Date date) {
        return new java.text.SimpleDateFormat("yyyy.MM.dd'T'HH:mm:ss/SZ").format(date);
    }

    /**
     * Get revision date directory, e.g. '/Users/michaelwechner/my-realm/data-repo/yarep-meta/foo/bar.html.yarep/index_date_utc/2011/11/04/09/28/51/526'
     * @param revisionName Name of revision
     */
    private File getRevisionDateDir(String revisionName) throws Exception {
        Date creationDate = node.getRevision(revisionName).getCreationDate(); // WARN: Older creation dates might not have milliseconds and hence are not corresponding exactly with the revision name, hence in order to build the date index correctly one needs to use the creation date
        //Date creationDate = new Date(Long.parseLong(revisionName)); // INFO: The name of a revision is based on System.currentTimeMillis() (see createRevision(String))
        log.debug("Creation date: " + creationDate);

        java.text.DateFormat df = new java.text.SimpleDateFormat("yyyy/MM/dd/HH/mm/ss/S");
        df.setTimeZone(java.util.TimeZone.getTimeZone(TIME_ZONE_ID)); // INFO: Write index in UTC
        String dateDirS = df.format(creationDate);
        log.debug("Date directory of revision '" + revisionName + "': " + dateDirS);
        File dateIndexBaseDir = new File(this.metaDir, DATE_INDEX_BASE_DIR);
        File dateDirF = new File(dateIndexBaseDir, dateDirS);
        return dateDirF;
    }

    /**
     * Get revision name from index file
     * @param path Path of revision name index file
     */
    private String getRevisionName(String path) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(new File(path)));
        String revisionName = br.readLine();
        br.close();
        return revisionName;
    }

    /**
     * Get revision from index file
     * @param path Absolute path of index file
     */
    private Revision getRevisionFromIndexFile(String path) throws Exception, IndexOutOfSyncException {
        if (path != null) {
        if (new File(path).isFile()) {
            String revisionName = getRevisionName(path);
            if (revisionName != null) {
                try {
                    return node.getRevision(revisionName);
                } catch (NoSuchRevisionException e) {
                    log.warn("No revision for revision name '" + revisionName + "' of index file: " + path);
                    throw new IndexOutOfSyncException(path);
                }
            } else {
                log.warn("Index file '" + path + "' does not seem to contain a revision name!");
                return null;
            }
        } else {
            log.warn("No such index file: " + path);
            return null;
        }
        } else {
            //log.debug("No path.");
            return null;
        }
    }

    /**
     * Delete empty directories recursively upwards
     * @param dir Directory which will be deleted if it is empty
     */
    private void deleteEmptyDirectories(File dir) {
        if (dir.isDirectory()) {
            if (isEmpty(dir)) {
                File parentDir = dir.getParentFile();
                dir.delete();
                deleteEmptyDirectories(parentDir);
            }
        } else {
            log.warn("No such directory: " + dir.getAbsolutePath());
        }
    }

    /**
     * Check whether a directory is empty
     * @param dir Directory to be checked
     * @return true if directory is empty and false otherwise
     */
    private boolean isEmpty(File dir) {
        String[] filesAndDirs = dir.list();
        if (filesAndDirs !=  null && filesAndDirs.length > 0) {
            log.warn("DEBUG: Directory '" + dir.getAbsolutePath() + "' is NOT empty.");
            return false;
        }
        log.warn("DEBUG: Directory '" + dir.getAbsolutePath() + "' is empty.");
        return true;
    }
}

/**
 *
 */
class IndexOutOfSyncException extends Exception {

    private String path;

    /**
     * @param path Path of index file which is out of sync
     */
    public IndexOutOfSyncException(String path) {
        this.path = path;
    }

    /**
     * Get path of index file which is out of sync
     */
    public String getPath() {
        return path;
    }
}
