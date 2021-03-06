/**
 * (C) Copyright 2013 Jabylon (http://www.jabylon.org) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
/**
 *
 */
package org.jabylon.review.standard.internal;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.jabylon.common.review.ReviewParticipant;
import org.jabylon.common.review.TerminologyProvider;
import org.jabylon.properties.PropertiesFactory;
import org.jabylon.properties.Property;
import org.jabylon.properties.PropertyFileDescriptor;
import org.jabylon.properties.Review;
import org.jabylon.properties.ReviewState;
import org.jabylon.properties.Severity;

/**
 * @author Johannes Utzig (jutzig.dev@googlemail.com)
 *
 */
@Component
@Service(value=ReviewParticipant.class)
public class TerminologyCheck extends AdapterImpl implements ReviewParticipant {

    private static final String TERMINOLOGY_DELIMITER = " \t\n\r\f.,;:(){}\"'<>?-";

    @Reference(cardinality=ReferenceCardinality.MANDATORY_UNARY,policy=ReferencePolicy.DYNAMIC)
    private TerminologyProvider terminologyProvider;

    /**
     *
     */
    public TerminologyCheck() {
    }

    /* (non-Javadoc)
     * @see org.jabylon.ui.review.ReviewParticipant#review(org.jabylon.properties.PropertyFileDescriptor, org.jabylon.properties.Property, org.jabylon.properties.Property)
     */
    @Override
    public Review review(PropertyFileDescriptor descriptor, Property master, Property slave) {
        Locale variant = descriptor.getVariant();
        if(variant==null)
            return null;
        Map<String, Property> terminology = terminologyProvider.getTerminology(variant);
        return analyze(master, slave, terminology);

    }


    private Review analyze(Property master, Property slave, Map<String, Property> terminology) {

        if(master==null || slave==null || terminology.isEmpty())
            return null;
        String masterValue = master.getValue();
        String slaveValue = slave.getValue();
        if(masterValue==null || slaveValue==null)
            return null;

        Map<String,String> mustFinds = new HashMap<String,String>();
        StringTokenizer tokenizer = new StringTokenizer(masterValue, TERMINOLOGY_DELIMITER);
        while(tokenizer.hasMoreTokens())
        {
            String token = tokenizer.nextToken();
            Property property = terminology.get(token);
            if(property!=null)
                mustFinds.put(property.getValue(),property.getKey());
        }
        if(!mustFinds.isEmpty())
        {
            tokenizer = new StringTokenizer(slaveValue, TERMINOLOGY_DELIMITER);
            while(tokenizer.hasMoreTokens())
            {
                String token = tokenizer.nextToken();
                mustFinds.remove(token);
            }
        }
        if(!mustFinds.isEmpty())
        {
            Entry<String, String> next = mustFinds.entrySet().iterator().next();
            Review review = PropertiesFactory.eINSTANCE.createReview();
            review.setCreated(System.currentTimeMillis());
            review.setState(ReviewState.OPEN);
            review.setSeverity(Severity.ERROR);
			review.setReviewType(getReviewType());
            String message = "Template language contained the term ''{0}'' but the terminology translation ''{1}'' is missing";
            message = MessageFormat.format(message, next.getValue(),next.getKey());
            review.setMessage(message);
            return review;
        }

        return null;

    }
    
	@Override
	public String getReviewType() {
		return "Terminology";
	}

    public void bindTerminologyProvider(TerminologyProvider provider) {
        this.terminologyProvider = provider;
    }

    public void unbindTerminologyProvider(TerminologyProvider provider) {
        if(terminologyProvider==provider)
        	terminologyProvider = null;
    }

    @Override
    public String getID() {
        return "TerminologyCheck";
    }

    @Override
    public String getDescription() {
        return "%terminology.check.description";
    }

    @Override
    public String getName() {
        return "%terminology.check.name";
    }
}
