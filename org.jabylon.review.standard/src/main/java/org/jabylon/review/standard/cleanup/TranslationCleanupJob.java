/**
 *
 */
package org.jabylon.review.standard.cleanup;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.emf.cdo.view.CDOView;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jabylon.cdo.server.ServerConstants;
import org.jabylon.properties.Project;
import org.jabylon.properties.ProjectLocale;
import org.jabylon.properties.ProjectVersion;
import org.jabylon.properties.Property;
import org.jabylon.properties.PropertyFile;
import org.jabylon.properties.PropertyFileDescriptor;
import org.jabylon.properties.Workspace;
import org.jabylon.resources.persistence.PropertyPersistenceService;
import org.jabylon.review.standard.ReviewActivator;
import org.jabylon.scheduler.JobContextUtil;
import org.jabylon.scheduler.JobExecution;

/**
 * @author Johannes Utzig (jutzig.dev@googlemail.com)
 *
 */
public class TranslationCleanupJob implements JobExecution {


    private static final Logger logger = LoggerFactory.getLogger(TranslationCleanupJob.class);

    /**
     *
     */
    public TranslationCleanupJob() {
    }

    /* (non-Javadoc)
     * @see org.jabylon.scheduler.JobExecution#run(java.util.Map)
     */
    @Override
    public void run(Map<String, Object> jobContext) throws Exception {
        logger.info("Starting translation cleanup job");
        CDOView view = JobContextUtil.openView(jobContext);
        try {
            Resource resource = view.getResource(ServerConstants.WORKSPACE_RESOURCE);
            Workspace workspace = (Workspace) resource.getContents().get(0);
            cleanup(workspace);
            logger.info("Translation cleanup job finished successfully");
        } catch (Exception e) {
            logger.error("Internal Error during translation cleanup",e);
        }
        finally {
            if(view!=null)
                view.close();
        }

    }

    private void cleanup(Workspace workspace) {
        EList<Project> projects = workspace.getChildren();
        for (Project project : projects) {
            cleanup(project);
        }

    }

    private void cleanup(Project project) {
        for (ProjectVersion version : project.getChildren()) {
            cleanup(version);
        }

    }

    private void cleanup(ProjectVersion version) {
        for (ProjectLocale locale : version.getChildren()) {
            cleanup(locale);
        }


    }

    private void cleanup(ProjectLocale locale) {
        for (PropertyFileDescriptor descriptor : locale.getDescriptors()) {
            cleanup(descriptor);
        }

    }

    private void cleanup(PropertyFileDescriptor descriptor) {
        PropertyFile masterProperties = descriptor.getMaster().loadProperties();
        Map<String, Property> map = masterProperties.asMap();
        PropertyFile properties = descriptor.loadProperties();
        Iterator<Property> iterator = properties.getProperties().iterator();
        boolean hadDeletes = false;
        while (iterator.hasNext()) {
            Property property = (Property) iterator.next();
            if(!map.containsKey(property.getKey()))
            {
                iterator.remove();
                logger.info("Removed unused translation {} in {}",property.getKey(), descriptor.fullPath());
                hadDeletes = true;
            }
        }
        if(hadDeletes)
        {
            PropertyPersistenceService propertyPersistenceService = ReviewActivator.getDefault().getPersistenceService();
            propertyPersistenceService.saveProperties(descriptor, properties);
        }
    }

    /* (non-Javadoc)
     * @see org.jabylon.scheduler.JobExecution#retryOnError()
     */
    @Override
    public boolean retryOnError() {
        return false;
    }

}