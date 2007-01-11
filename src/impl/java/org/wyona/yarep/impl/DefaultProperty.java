package org.wyona.yarep.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Category;
import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.Property;
import org.wyona.yarep.core.PropertyType;
import org.wyona.yarep.core.RepositoryException;

public class DefaultProperty implements Property {
    private static Category log = Category.getInstance(DefaultProperty.class);
   
    protected String name;
    protected Node node;
    protected int type;
    
    protected DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    
    protected boolean booleanValue;
    protected double doubleValue;
    protected long longValue;
    protected Date dateValue;
    protected String stringValue;
    
    public DefaultProperty(String name, int type, Node node) throws RepositoryException {
        this.name = name;
        this.type = type;
        this.node = node;
    }
    
    /**
     * Gets the name of this property.
     * @return
     * @throws RepositoryException
     */
    public String getName() throws RepositoryException {
        return this.name;
    }
    
    /**
     * Gets the node to which this property belongs to.
     * @return
     * @throws RepositoryException
     */
    public Node getNode() throws RepositoryException {
        return this.node;
    }
    
    /**
     * Gets the type of this property.
     * @return
     * @throws RepositoryException
     */
    public int getType() throws RepositoryException {
        return this.type;
    }
    
    public String toString() {
        try {
            return "Property [name=" + this.name + ", type=" + this.type + ", value=" + getValueAsString() + "]";
        } catch (RepositoryException e) {
            log.error(e, e);
            return "Property [name=" + this.name + ", type=" + this.type + ", value=unknown]";
        }
    }
    
    /**
     * Sets the value by reading it from the given string and converting it to 
     * the correct type.
     * @param value
     * @throws RepositoryException
     */
    public void setValueFromString(String value) throws RepositoryException {
        switch (this.type) {
            case PropertyType.BOOLEAN: setValue(Boolean.valueOf(value).booleanValue()); break; 
            case PropertyType.DOUBLE: setValue(Double.valueOf(value).doubleValue()); break;
            case PropertyType.LONG: setValue(Long.valueOf(value).longValue()); break; 
            case PropertyType.DATE: 
                try {
                    setValue(dateFormat.parse(value));
                } catch (ParseException e) {
                    throw new RepositoryException(e.getMessage(), e);
                } break; 
            case PropertyType.STRING: setValue(value); break;
            default: log.warn("unsupported property type: " + this.type);
        }
    }
    
    /**
     * Gets the value of this property converted to a string.
     * @return
     * @throws RepositoryException
     */
    public String getValueAsString() throws RepositoryException {
        switch (this.type) {
            case PropertyType.BOOLEAN: return "" + getBoolean(); 
            case PropertyType.DOUBLE: return "" + getDouble(); 
            case PropertyType.LONG: return "" + getLong(); 
            case PropertyType.DATE: return dateFormat.format(getDate()); 
            case PropertyType.STRING: return getString();
            default: return "";
        }
    }
    
    
    /**
     * Gets the length of this property if its a binary property.
     * @return
     * @throws RepositoryException
     */
    public long getLength() throws RepositoryException {
        // TODO: not implemented yet
        log.warn("Not implemented yet.");
        return -1;
    }
    
    /**
     * Gets the value of this property as a boolean.
     * @return
     * @throws RepositoryException
     */
    public boolean getBoolean() throws RepositoryException {
        return this.booleanValue;
    }
    
    /**
     * Gets the value of this property as a date object.
     * @return
     * @throws RepositoryException
     */
    public Date getDate() throws RepositoryException {
        return this.dateValue;
    }
    
    /**
     * Gets the value of this property as a double.
     * @return
     * @throws RepositoryException
     */
    public double getDouble() throws RepositoryException {
        return this.doubleValue;
    }
    
    /**
     * Gets an input stream to read from this property.
     * @return
     * @throws RepositoryException
     */
    public InputStream getInputStream() throws RepositoryException {
        // TODO: not implemented yet
        log.warn("Not implemented yet.");
        return null;
    }
    
    /**
     * Gets an output stream to write to this property.
     * @return
     * @throws RepositoryException
     */
    public OutputStream getOutputStream() throws RepositoryException {
        // TODO: not implemented yet
        log.warn("Not implemented yet.");
        return null;
    }
    
    /**
     * Gets the value of this property as a long.
     * @return
     * @throws RepositoryException
     */
    public long getLong() throws RepositoryException {
        return this.longValue;
    }
    
    /**
     * Gets the value of this property as a string.
     * @return
     * @throws RepositoryException
     */
    public String getString() throws RepositoryException {
        return this.stringValue;
    }
    
    /**
     * Sets the value of this property as a boolean.
     * @param value
     * @throws RepositoryException
     */
    public void setValue(boolean value) throws RepositoryException {
        this.booleanValue = value;
    }
    
    /**
     * Sets the value of this property as a date.
     * @param value
     * @throws RepositoryException
     */
    public void setValue(Date value) throws RepositoryException {
        this.dateValue = value;
    }
    
    /**
     * Sets the value of this property as a double.
     * @param value
     * @throws RepositoryException
     */
    public void setValue(double value) throws RepositoryException {
        this.doubleValue = value;
    }
    
    //public void setValue(InputStream value) throws RepositoryException;
    
    /**
     * Sets the value of this property as a long.
     * @param value
     * @throws RepositoryException
     */
    public void setValue(long value) throws RepositoryException {
        this.longValue = value;
    }
    
    /**
     * Sets the value of this property as a string.
     * @param value
     * @throws RepositoryException
     */
    public void setValue(String value) throws RepositoryException {
        this.stringValue = value;
    }
    
}