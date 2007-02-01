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
     * Gets the tag of this revision.
     * @return
     * @throws RepositoryException
     */
    public String getTag() throws RepositoryException;
    
    /**
     * Sets the tag of this revision.
     * @param tag
     * @throws RepositoryException
     */
    public void setTag(String tag) throws RepositoryException;
    
    /**
     * Indicates whether this revision has a tag or not.
     * @return true if this revision has a tag, false otherwise.
     * @throws RepositoryException
     */
    public boolean hasTag() throws RepositoryException;

    /**
     * Gets the date when this revision was created.
     * @return
     * @throws RepositoryException
     */
    public Date getCreationDate() throws RepositoryException;
    
    /**
     * Gets a string representing the user who created this revision.
     * @return
     * @throws RepositoryException
     */
    public String getCreator() throws RepositoryException;
    
    /**
     * Gets the comment which was given when this revision was created.
     * @return
     * @throws RepositoryException
     */
    public String getComment() throws RepositoryException;


}