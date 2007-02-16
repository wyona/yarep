package org.wyona.yarep.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

/**
 * A property stores a value which belongs to a node. 
 * Such a value has one of the following types:
 * <ul>
 * <li>string</li>
 * <li>boolean</li>
 * <li>date</li>
 * <li>long</li>
 * <li>double</li>
 * <li>binary (binary properties are not supported atm)</li>
 */
public interface Property {
       
    /**
     * Gets the name of this property.
     * @return name
     * @throws RepositoryException
     */
    public String getName() throws RepositoryException;
    
    /**
     * Gets the node to which this property belongs to.
     * @return node
     * @throws RepositoryException
     */
    public Node getNode() throws RepositoryException;
    
    /**
     * Gets the type of this property.
     * @return type
     * @throws RepositoryException
     * @see org.wyona.yarep.core.PropertyType
     */
    public int getType() throws RepositoryException;
    
    /**
     * Sets the value by reading it from the given string and converting it to 
     * the correct type.
     * @param value
     * @throws RepositoryException
     */
    public void setValueFromString(String value) throws RepositoryException;
    
    /**
     * Gets the value of this property converted to a string.
     * @return string
     * @throws RepositoryException
     */
    public String getValueAsString() throws RepositoryException;

    /*
     * Gets the length of this property if its a binary property.
     * @return
     * @throws RepositoryException
     */
    //public long getLength() throws RepositoryException;
    
    /**
     * Gets the value of this property as a boolean.
     * @return true of false, or undefined value if this property is not a boolean
     * @throws RepositoryException
     */
    public boolean getBoolean() throws RepositoryException;
    
    /**
     * Gets the value of this property as a date object.
     * @return date, or undefined value if this property is not a date
     * @throws RepositoryException
     */
    public Date getDate() throws RepositoryException;
    
    /**
     * Gets the value of this property as a double.
     * @return double, or undefined value if this property is not a double
     * @throws RepositoryException
     */
    public double getDouble() throws RepositoryException;
    
    /*
     * Gets an input stream to read from this property.
     * @return
     * @throws RepositoryException
     */
    //public InputStream getInputStream() throws RepositoryException;
    
    /*
     * Gets an output stream to write to this property.
     * @return
     * @throws RepositoryException
     */
    //public OutputStream getOutputStream() throws RepositoryException;
    
    /**
     * Gets the value of this property as a long.
     * @return long, or undefined value if this property is not a long.
     * @throws RepositoryException
     */
    public long getLong() throws RepositoryException;
    
    /**
     * Gets the value of this property as a string.
     * @return string, or undefined value if this property is not a string.
     * @throws RepositoryException
     * @see #getValueAsString()
     */
    public String getString() throws RepositoryException;
    
    /**
     * Sets the value of this property as a boolean.
     * Undefined effect is this property is not a boolean.
     * @param value
     * @throws RepositoryException
     */
    public void setValue(boolean value) throws RepositoryException;
    
    /**
     * Sets the value of this property as a date.
     * Undefined effect is this property is not a date.
     * @param value
     * @throws RepositoryException
     */
    public void setValue(Date value) throws RepositoryException;
    
    /**
     * Sets the value of this property as a double.
     * Undefined effect is this property is not a double.
     * @param value
     * @throws RepositoryException
     */
    public void setValue(double value) throws RepositoryException;
    
    //public void setValue(InputStream value) throws RepositoryException;
    
    /**
     * Sets the value of this property as a long.
     * Undefined effect is this property is not a long.
     * @param value
     * @throws RepositoryException
     */
    public void setValue(long value) throws RepositoryException;
    
    /**
     * Sets the value of this property as a string.
     * Undefined effect is this property is not a string.
     * @param value
     * @throws RepositoryException
     * @see #setValueFromString(String)
     */
    public void setValue(String value) throws RepositoryException;
    
}