package edu.indiana.dlib.amppd.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.ExposureConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.CollectionSupplement;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.ItemSupplement;
import edu.indiana.dlib.amppd.model.MgmCategory;
import edu.indiana.dlib.amppd.model.MgmScoringParameter;
import edu.indiana.dlib.amppd.model.MgmScoringTool;
import edu.indiana.dlib.amppd.model.MgmTool;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;
import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.model.UnitSupplement;

/**
 * Customized RepositoryRestConfigurer for AMP repositories.
 */
@Configuration
public class RestConfig implements RepositoryRestConfigurer {
	
    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration restConfig, CorsRegistry cors) {
        ExposureConfiguration config = restConfig.getExposureConfiguration();
        
        /* For all exposed repositories:
         * CUD should be disabled on association
         */
        
        /* For Dataentity subclasses:
         * GET on collection is not needed, as listing of the top level entity Unit is implemented in controller, 
         * while all other lower level entities can be obtained from its parent's detail view.
         * Meanwhile, Creation of Assets is also implemented in controller.
         */     
        
        config.forDomainType(Unit.class)
    		.withAssociationExposure((metadata, httpMethods) -> httpMethods
    			.disable(HttpMethod.POST)
    			.disable(HttpMethod.PUT)
    			.disable(HttpMethod.PATCH)
    			.disable(HttpMethod.DELETE));      
        
        config.forDomainType(Collection.class)
    		.withCollectionExposure((metadata, httpMethods) -> httpMethods
    			.disable(HttpMethod.GET))   
			.withAssociationExposure((metadata, httpMethods) -> httpMethods
    			.disable(HttpMethod.POST)
    			.disable(HttpMethod.PUT)
    			.disable(HttpMethod.PATCH)
    			.disable(HttpMethod.DELETE));         
        
		config.forDomainType(Item.class)
    		.withCollectionExposure((metadata, httpMethods) -> httpMethods
    			.disable(HttpMethod.GET))   
			.withAssociationExposure((metadata, httpMethods) -> httpMethods
	    		.disable(HttpMethod.POST)
	    		.disable(HttpMethod.PUT)
	    		.disable(HttpMethod.PATCH)
	    		.disable(HttpMethod.DELETE));         
             
        config.forDomainType(Primaryfile.class)
    		.withCollectionExposure((metadata, httpMethods) -> httpMethods
    			.disable(HttpMethod.GET)
                .disable(HttpMethod.POST))   
			.withAssociationExposure((metadata, httpMethods) -> httpMethods
	    		.disable(HttpMethod.POST)
	    		.disable(HttpMethod.PUT)
	    		.disable(HttpMethod.PATCH)
	    		.disable(HttpMethod.DELETE));         
       
        config.forDomainType(UnitSupplement.class)
    		.withCollectionExposure((metadata, httpMethods) -> httpMethods
    			.disable(HttpMethod.GET)
                .disable(HttpMethod.POST))   
			.withAssociationExposure((metadata, httpMethods) -> httpMethods
	    		.disable(HttpMethod.POST)
	    		.disable(HttpMethod.PUT)
	    		.disable(HttpMethod.PATCH)
	    		.disable(HttpMethod.DELETE));         
  
        
        config.forDomainType(CollectionSupplement.class)
    		.withCollectionExposure((metadata, httpMethods) -> httpMethods
    			.disable(HttpMethod.GET)
                .disable(HttpMethod.POST))   
			.withAssociationExposure((metadata, httpMethods) -> httpMethods
	    		.disable(HttpMethod.POST)
	    		.disable(HttpMethod.PUT)
	    		.disable(HttpMethod.PATCH)
	    		.disable(HttpMethod.DELETE));         
          
        config.forDomainType(ItemSupplement.class)
    		.withCollectionExposure((metadata, httpMethods) -> httpMethods
    			.disable(HttpMethod.GET)
                .disable(HttpMethod.POST))   
			.withAssociationExposure((metadata, httpMethods) -> httpMethods
	    		.disable(HttpMethod.POST)
	    		.disable(HttpMethod.PUT)
	    		.disable(HttpMethod.PATCH)
	    		.disable(HttpMethod.DELETE));              
        
        config.forDomainType(PrimaryfileSupplement.class)
    		.withCollectionExposure((metadata, httpMethods) -> httpMethods
    			.disable(HttpMethod.GET)
                .disable(HttpMethod.POST))   
			.withAssociationExposure((metadata, httpMethods) -> httpMethods
	    		.disable(HttpMethod.POST)
	    		.disable(HttpMethod.PUT)
	    		.disable(HttpMethod.PATCH)
	    		.disable(HttpMethod.DELETE));         
   
        /* For MGM related classes:
         * GET on collection is needed only for retrieving the top level instances - MgmCategory,
         * while all other lower level MGM instances can be obtained from its parent's detail view.
         * Meanwhile, CUD APIs are not exposed.
         */
        
        config.forDomainType(MgmCategory.class)
        	.withCollectionExposure((metadata, httpMethods) -> httpMethods
            	.disable(HttpMethod.POST))
        	.withItemExposure((metadata, httpMethods) -> httpMethods
        		.disable(HttpMethod.PUT)
        		.disable(HttpMethod.PATCH)
        		.disable(HttpMethod.DELETE))   
			.withAssociationExposure((metadata, httpMethods) -> httpMethods
		    	.disable(HttpMethod.POST)
		    	.disable(HttpMethod.PUT)
		    	.disable(HttpMethod.PATCH)
		    	.disable(HttpMethod.DELETE));         
        
        config.forDomainType(MgmScoringParameter.class)
        	.withCollectionExposure((metadata, httpMethods) -> httpMethods
                .disable(HttpMethod.GET)
            	.disable(HttpMethod.POST))
        	.withItemExposure((metadata, httpMethods) -> httpMethods
        		.disable(HttpMethod.PUT)
        		.disable(HttpMethod.PATCH)
        		.disable(HttpMethod.DELETE))   
			.withAssociationExposure((metadata, httpMethods) -> httpMethods
		    	.disable(HttpMethod.POST)
		    	.disable(HttpMethod.PUT)
		    	.disable(HttpMethod.PATCH)
		    	.disable(HttpMethod.DELETE));         
        
        config.forDomainType(MgmScoringTool.class)
    		.withCollectionExposure((metadata, httpMethods) -> httpMethods
                .disable(HttpMethod.GET)
            	.disable(HttpMethod.POST))
        	.withItemExposure((metadata, httpMethods) -> httpMethods
        		.disable(HttpMethod.PUT)
        		.disable(HttpMethod.PATCH)
        		.disable(HttpMethod.DELETE))   
			.withAssociationExposure((metadata, httpMethods) -> httpMethods
		    	.disable(HttpMethod.POST)
		    	.disable(HttpMethod.PUT)
		    	.disable(HttpMethod.PATCH)
		    	.disable(HttpMethod.DELETE));         

        config.forDomainType(MgmTool.class)
        	.withCollectionExposure((metadata, httpMethods) -> httpMethods
                .disable(HttpMethod.GET)
            	.disable(HttpMethod.POST))
        	.withItemExposure((metadata, httpMethods) -> httpMethods
        		.disable(HttpMethod.PUT)
        		.disable(HttpMethod.PATCH)
        		.disable(HttpMethod.DELETE))   
			.withAssociationExposure((metadata, httpMethods) -> httpMethods
		    	.disable(HttpMethod.POST)
		    	.disable(HttpMethod.PUT)
		    	.disable(HttpMethod.PATCH)
		    	.disable(HttpMethod.DELETE));         
    }
    
}