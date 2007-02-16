package org.wyona.yarep.core;

import java.util.Date;

/**
 * A revision of a repository node.
 */
public interface Revision extends Node {
    /**
     * Gets the name which identifies this revision.
     * The revision name is set by the implementation and cannot be changed because
     * it uniquely identifies this revision among all other revisions of this node.
     * @return revision name
     * @throws RepositoryException
     */
    public String getRevisionName() throws RepositoryException;
    
    /**
     * Gets the tag of this revision.
     * Tags may be used by an application to mark certain revisions.
     * @return tag or null if there is no tag
     * @throws RepositoryException
     */
    public String getTag() throws RepositoryException;
    
    /**
     * Sets the tag of this revision.
     * Tags may be used by an application to mark certain revisions.
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
     * @return creation date
     * @throws RepositoryException
     */
    public Date getCreationDate() throws RepositoryException;
    
    /**
     * Gets a string representing the user who created this revision.
     * @return user id
     * @throws RepositoryException
     */
    public String getCreator() throws RepositoryException;
    
    /**
     * Gets the comment which was given when this revision was created.
     * @return comment
     * @throws RepositoryException
     */
    public String getComment() throws RepositoryException;


}