package org.wyona.yarep.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

public interface Property {
       
    /**
     * Gets the name of this property.
     * @return
     * @throws RepositoryException
     */
    public String getName() throws RepositoryException;
    
    /**
     * Gets the node to which this property belongs to.
     * @return
     * @throws RepositoryException
     */
    public Node getNode() throws RepositoryException;
    
    /**
     * Gets the type of this property.
     * @return
     * @throws RepositoryException
     */
    public int getType() throws RepositoryException;
    
    /**
     * Gets the length of this property if its a binary property.
     * @return
     * @throws RepositoryException
     */
    public long getLength() throws RepositoryException;
    
    /**
     * Gets the value of this property as a boolean.
     * @return
     * @throws RepositoryException
     */
    public boolean getBoolean() throws RepositoryException;
    
    /**
     * Gets the value of this property as a date object.
     * @return
     * @throws RepositoryException
     */
    public Date getDate() throws RepositoryException;
    
    /**
     * Gets the value of this property as a double.
     * @return
     * @throws RepositoryException
     */
    public double getDouble() throws RepositoryException;
    
    /**
     * Gets an input stream to read from this property.
     * @return
     * @throws RepositoryException
     */
    public InputStream getInputStream() throws RepositoryException;
    
    /**
     * Gets an output stream to write to this property.
     * @return
     * @throws RepositoryException
     */
    public OutputStream getOutputStream() throws RepositoryException;
    
    /**
     * Gets the value of this property as a long.
     * @return
     * @throws RepositoryException
     */
    public long getLong() throws RepositoryException;
    
    /**
     * Gets the value of this property as a string.
     * @return
     * @throws RepositoryException
     */
    public String getString() throws RepositoryException;
    
    /**
     * Sets the value of this property as a boolean.
     * @param value
     * @throws RepositoryException
     */
    public void setValue(boolean value) throws RepositoryException;
    
    /**
     * Sets the value of this property as a date.
     * @param value
     * @throws RepositoryException
     */
    public void setValue(Date value) throws RepositoryException;
    
    /**
     * Sets the value of this property as a double.
     * @param value
     * @throws RepositoryException
     */
    public void setValue(double value) throws RepositoryException;
    
    //public void setValue(InputStream value) throws RepositoryException;
    
    /**
     * Sets the value of this property as a long.
     * @param value
     * @throws RepositoryException
     */
    public void setValue(long value) throws RepositoryException;
    
    /**
     * Sets the value of this property as a string.
     * @param value
     * @throws RepositoryException
     */
    public void setValue(String value) throws RepositoryException;
    
}