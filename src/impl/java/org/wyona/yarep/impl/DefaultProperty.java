package org.wyona.yarep.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.Property;
import org.wyona.yarep.core.PropertyType;
import org.wyona.yarep.core.RepositoryException;

public class DefaultProperty implements Property {
    private static Logger log = Logger.getLogger(DefaultProperty.class);
   
    protected String name;
    protected Node node;
    protected int type;

    protected DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    private DateFormat dateFormatInclMillis = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSSZ");
    
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
     * @see org.wyona.yarep.core.Property#getName()
     */
    public String getName() throws RepositoryException {
        return this.name;
    }
    
    /**
     * @see org.wyona.yarep.core.Property#getNode()
     */
    public Node getNode() throws RepositoryException {
        return this.node;
    }
    
    /**
     * @see org.wyona.yarep.core.Property#getType()
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
     * @see org.wyona.yarep.core.Property#setValueFromString(java.lang.String)
     */
    public void setValueFromString(String value) throws RepositoryException {
        switch (this.type) {
            case PropertyType.BOOLEAN: setValue(Boolean.valueOf(value).booleanValue()); break; 
            case PropertyType.DOUBLE: setValue(Double.valueOf(value).doubleValue()); break;
            case PropertyType.LONG: setValue(Long.valueOf(value).longValue()); break; 
            case PropertyType.DATE: 
                try {
                    setValue(parseDate(value));
                } catch (ParseException e) {
                    throw new RepositoryException(e.getMessage(), e);
                } break; 
            case PropertyType.STRING: setValue(value); break;
            default: log.warn("unsupported property type: " + this.type);
        }
    }
    
    /**
     * @see org.wyona.yarep.core.Property#getValueAsString()
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
    
    
    /*public long getLength() throws RepositoryException {
        // TODO: not implemented yet
        log.warn("Not implemented yet.");
        return -1;
    }*/
    
    /**
     * @see org.wyona.yarep.core.Property#getBoolean()
     */
    public boolean getBoolean() throws RepositoryException {
        return this.booleanValue;
    }
    
    /**
     * @see org.wyona.yarep.core.Property#getDate()
     */
    public Date getDate() throws RepositoryException {
        log.warn("DEBUG: Date: " + this.dateValue);
        return this.dateValue;
    }
    
    /**
     * @see org.wyona.yarep.core.Property#getDouble()
     */
    public double getDouble() throws RepositoryException {
        return this.doubleValue;
    }
    
    /*public InputStream getInputStream() throws RepositoryException {
        // TODO: not implemented yet
        log.warn("Not implemented yet.");
        return null;
    }*/
    
    /*public OutputStream getOutputStream() throws RepositoryException {
        // TODO: not implemented yet
        log.warn("Not implemented yet.");
        return null;
    }*/
    
    /* (non-Javadoc)
     * @see org.wyona.yarep.core.Property#getLong()
     */
    public long getLong() throws RepositoryException {
        return this.longValue;
    }
    
    /**
     * @see org.wyona.yarep.core.Property#getString()
     */
    public String getString() throws RepositoryException {
        return this.stringValue;
    }
    
    /**
     * @see org.wyona.yarep.core.Property#setValue(boolean)
     */
    public void setValue(boolean value) throws RepositoryException {
        this.booleanValue = value;
    }
    
    /**
     * @see org.wyona.yarep.core.Property#setValue(java.util.Date)
     */
    public void setValue(Date value) throws RepositoryException {
        log.warn("DEBUG: Date: " + value);
        this.dateValue = value;
    }
    
    /**
     * @see org.wyona.yarep.core.Property#setValue(double)
     */
    public void setValue(double value) throws RepositoryException {
        this.doubleValue = value;
    }
    
    //public void setValue(InputStream value) throws RepositoryException;
    
    /**
     * @see org.wyona.yarep.core.Property#setValue(long)
     */
    public void setValue(long value) throws RepositoryException {
        this.longValue = value;
    }
    
    /**
     * @see org.wyona.yarep.core.Property#setValue(java.lang.String)
     */
    public void setValue(String value) throws RepositoryException {
        this.stringValue = value;
    }

    /**
     * Parse date (with and without milliseconds)
     * @param value Date as string
     */
    private Date parseDate(String value) throws ParseException {
        try {
            return dateFormatInclMillis.parse(value);
        } catch (ParseException e) {
            try {
                log.warn("Date of node '" + node.getPath() + "' does not seem to contain milliseconds: " + value + " (probably old data ...)");
            } catch(Exception re) {
                log.error(re, re);
            }

            // INFO: Because of backwards compatibility we also need to be able to read dates without milliseconds!
            return dateFormat.parse(value);
        }
    }
}
