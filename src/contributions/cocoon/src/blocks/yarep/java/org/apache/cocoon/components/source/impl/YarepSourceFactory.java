package org.apache.cocoon.components.source.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.Request;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.SourceResolver;
//import org.apache.lenya.cms.publication.DocumentIdentityMap;
//import org.apache.lenya.cms.publication.PageEnvelope;
//import org.apache.lenya.cms.publication.PageEnvelopeException;
//import org.apache.lenya.cms.publication.PageEnvelopeFactory;
//import org.apache.lenya.cms.publication.Publication;
/*
import org.apache.lenya.transaction.IdentityMap;
import org.apache.lenya.transaction.UnitOfWork;
*/

import org.wyona.yarep.core.RepositoryFactory;

/**
 * A factory for the "yarep" scheme (virtual protocol), which is used to resolve any src="yarep:..."
 * attributes in sitemaps. This implementation constructs the path to the source document from the
 * page envelope and delegates any further resolving to the "context" source resolver of Cocoon.
 */
public class YarepSourceFactory extends AbstractLogEnabled implements SourceFactory, ThreadSafe,
        Contextualizable, Serviceable, Configurable {

    protected static final String SCHEME = "yarep";
    protected RepositoryFactory repoFactory;

    /** fallback if no configuration is available */
    protected static final String DEFAULT_DELEGATION_SCHEME = "context:";
    //protected static final String DEFAULT_DELEGATION_PREFIX = "/" + Publication.PUBLICATION_PREFIX_URI;

    private Context context;
    private ServiceManager manager;
    private String delegationScheme;
    //private String delegationPrefix;

    /**
     * Used for resolving the object model.
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context _context) throws ContextException {
        this.context = _context;
    }

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager _manager) throws ServiceException {
        this.manager = _manager;
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration configuration) throws ConfigurationException {
        this.delegationScheme = configuration.getAttribute("scheme", DEFAULT_DELEGATION_SCHEME);
        //this.delegationPrefix = configuration.getAttribute("prefix", DEFAULT_DELEGATION_PREFIX);

        try {
            repoFactory = new RepositoryFactory();
            getLogger().info("Initialize Repository Factory: " + repoFactory.toString());
        } catch(Exception e) {
            getLogger().error(e.getMessage(), e);
        }
    }

    /**
     * @see org.apache.excalibur.source.SourceFactory#getSource(java.lang.String, java.util.Map)
     */
    public Source getSource(String src, Map parameters) throws MalformedURLException, IOException {

        SourceResolver sourceResolver = null;

        try {
            sourceResolver = (SourceResolver) this.manager
                    .lookup(org.apache.excalibur.source.SourceResolver.ROLE);

            String path = src.substring(SCHEME.length() + 1);
            getLogger().debug("src = " + src);

            if (!path.startsWith("//")) {

                Map objectModel = ContextHelper.getObjectModel(this.context);
            }

            while (path.startsWith("/")) {
                path = path.substring(1);
            }

            //IdentityMap map = null;

            Request request = ContextHelper.getRequest(this.context);

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Creating repository source for URI [" + src + "]");
            }

            getLogger().debug("Creating repository source for URI [" + src + "]");

            try {
                return new YarepSource(src, repoFactory);
            } catch (Exception e) {
                getLogger().error(e.getMessage(), e);
                throw new IOException(e.getMessage());
            }

            //return sourceResolver.resolveURI(path);

        } catch (final ServiceException e) {
            throw new SourceException(e.getMessage(), e);
        } finally {
            this.manager.release(sourceResolver);
        }
    }

    /**
     * Does nothing because the delegated factory does this.
     * @see org.apache.excalibur.source.SourceFactory#release(org.apache.excalibur.source.Source)
     */
    public void release(Source source) {
        // do nothing
    }
}
