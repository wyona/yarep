package org.wyona.yarep.core.impl.svn;

import java.util.ArrayList;
import java.util.Date;

import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;

/**
 * Used to collect revision information (numbers, dates, messages) of a given file. 
 */
public class RevisionLogEntryHandler implements ISVNLogEntryHandler {
    protected ArrayList revisions = new ArrayList();
    protected ArrayList messages = new ArrayList();
    protected ArrayList dates = new ArrayList();

    public void handleLogEntry(SVNLogEntry logEntry)  throws SVNException {
        long revision = logEntry.getRevision();
        String message = logEntry.getMessage();
        Date date = logEntry.getDate();
        
        revisions.add(new Long(revision));
        messages.add(message);
        dates.add(date);
    }
    
    public long[] getRevisions() {
        long[] revs = new long[revisions.size()];
        for (int i=0; i<revisions.size(); i++) {
            revs[i] = ((Long)revisions.get(i)).longValue();
        }
        return revs;
    }
    
    public String[] getMessages() {
        return (String[])messages.toArray(new String[messages.size()]);
    }

    public Date[] getDates() {
        return (Date[])dates.toArray(new Date[dates.size()]);
    }

}
