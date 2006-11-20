package org.wyona.yarep.core.impl.svn;

import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.ISVNStatusHandler;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusType;

/**
 * Used to check if there are any conflicts or other problems in the working
 * copy of the repository. 
 */
public class ConsistencyStatusHandler implements ISVNStatusHandler {
    public ConsistencyStatusHandler() {
    }

    public void handleStatus(SVNStatus status) throws SVNException {
        SVNStatusType statusType = status.getContentsStatus();

        if (statusType == SVNStatusType.STATUS_CONFLICTED
                || statusType == SVNStatusType.STATUS_MISSING
                || statusType == SVNStatusType.STATUS_INCOMPLETE
                || statusType == SVNStatusType.STATUS_OBSTRUCTED) {
            throw new SVNException(SVNErrorMessage.create(SVNErrorCode.UNKNOWN, "Invalid status ["
                    + statusType + "] of file: " + status.getFile().getAbsolutePath()));
        }

    }

}
