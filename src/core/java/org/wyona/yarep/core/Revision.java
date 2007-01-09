package org.wyona.yarep.core;

import java.util.Date;

public interface Revision extends Node {
    /**
     * Gets the name which identifies this revision.
     * @return
     * @throws RepositoryException
     */
    public String getRevisionName() throws RepositoryException;
    
    /**
     * Gets the label of this revision.
     * @return
     * @throws RepositoryException
     */
    public String getLabel() throws RepositoryException;
    
    /**
     * Sets the label of this revision.
     * @param label
     * @throws RepositoryException
     */
    public void setLabel(String label) throws RepositoryException;
    
    /**
     * Gets the date when this revision was created.
     * @return
     * @throws RepositoryException
     */
    public Date getCreationDate() throws RepositoryException;
}