package edu.indiana.dlib.amppd.testData;

import java.util.HashMap;
import java.util.HashSet;

import br.com.six2six.fixturefactory.Fixture;
import br.com.six2six.fixturefactory.Rule;
import br.com.six2six.fixturefactory.loader.TemplateLoader;
import edu.indiana.dlib.amppd.model.Bundle;
import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.CollectionSupplement;
import edu.indiana.dlib.amppd.model.Dataentity;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.ItemSupplement;
import edu.indiana.dlib.amppd.model.Job;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;
import edu.indiana.dlib.amppd.model.Unit;

//import edu.indiana.dlib.amppd.model.fixtures.TemplateLoader;

public class ModelTemplates implements TemplateLoader {

	@Override
	public void load() {
		// TODO Auto-generated method stub
		
		Fixture.of(Dataentity.class).addTemplate("valid", new Rule() {{
			add("id", random(Long.class, range(1L, 200L)));
	    	add("name", firstName());
	    	add("description", "Description for ${name}'s test case");	
			add("createdDate", 20170101L); 
			add("modifiedDate", 20170201L); 
			add("createdBy", "${name}");
			add("modifiedBy", "${name}");
		}});
		
		    Fixture.of(Collection.class).addTemplate("valid", new Rule() {{
	    	add("externalIds", new HashMap<String, String>());
		    add("id", random(Long.class, range(1L, 200L)));
	    	add("name", firstName());
	    	add("description", "Description for ${name}'s test case");	
			add("createdDate", 20170101L); 
			add("modifiedDate", 20170201L); 
			add("createdBy", "${name}");
			add("modifiedBy", "${name}");
	    	add("items", new HashSet<Item>());
	    	add("supplements",new HashSet<CollectionSupplement>()); 
	    	}}); 
	    	
		
		Fixture.of(CollectionSupplement.class).addTemplate("valid", new Rule() {{
			add("id", random(Long.class, range(1L, 200L)));
			add("name", firstName());
	    	add("description", "Description for ${name}'s test case");	
			add("createdDate", 20170101L); 
			add("modifiedDate", 20170201L); 
			add("createdBy", "${name}");
			add("modifiedBy", "${name}");
			//add("collection", new Collection());
			add("originalFilename", firstName());
		  	add("pathname", "C:/New Folder/${name}");
		  	add("metainfo", regex("[A-Z]{1}[A-Z a-z]{9,29}"));
		  	add("externalIds", new HashMap<String, String>());
		  	}});
		 
		 
	    Fixture.of(Item.class).addTemplate("valid", new Rule() {{			
	    	add("id", random(Long.class, range(1L, 200L)));
	    	add("name", firstName());
	    	add("description", "Description for ${name}'s test case");	
			add("createdDate", 20170101L); 
			add("modifiedDate", 20170201L); 
			add("createdBy", "${name}");
			add("modifiedBy", "${name}");
	    	//add("collection", one(Collection.class, "valid"));
			add("primaryfiles", new HashSet<Primaryfile>()); 
			add("supplements", new HashSet<ItemSupplement>()); 
			add("externalIds", new HashMap<String, String>());
			}});
			
	    Fixture.of(Primaryfile.class).addTemplate("valid", new Rule() {{
	    	add("id", random(Long.class, range(1L, 200L)));
	    	add("name", firstName());
	    	add("description", "Description for ${name}'s test case");	
			add("createdDate", 20170101L); 
			add("modifiedDate", 20170201L); 
			add("createdBy", "${name}");
			add("modifiedBy", "${name}");
	    	add("supplements", new HashSet<PrimaryfileSupplement>());
			add("jobs", new HashSet<Job>());
			add("originalFilename", firstName());
			add("pathname", "C:/New Folder/${name}");
			add("metainfo", regex("[A-Z]{1}[A-Z a-z]{9,29}"));
			add("externalIds", new HashMap<String, String>());
			}});
	    
		
		  Fixture.of(ItemSupplement.class).addTemplate("valid", new Rule() {{
			//add("item", has(1).of(Item.class, "valid")); 
			add("id", random(Long.class, range(1L, 200L)));
			add("name", firstName());
	    	add("description", "Description for ${name}'s test case");	
			add("createdDate", 20170101L); 
			add("modifiedDate", 20170201L); 
			add("createdBy", "${name}");
			add("modifiedBy", "${name}");
			add("originalFilename", firstName());
		  	add("pathname", "C:/New Folder/${name}");
		  	add("metainfo", regex("[A-Z]{1}[A-Z a-z]{9,29}"));
		  	add("externalIds", new HashMap<String, String>());
			}});
		 
	    
	    Fixture.of(Bundle.class).addTemplate("valid", new Rule() {{
	    	//add("items", has(2).of(Item.class, "valid"));	
	    	add("id", random(Long.class, range(1L, 200L)));
			add("name", firstName());
	    	add("description", "Description for ${name}'s test case");	
			add("createdDate", 20170101L); 
			add("modifiedDate", 20170201L); 
			add("createdBy", "${name}");
			add("modifiedBy", "${name}");
	    	}});
	    
		
		  Fixture.of(PrimaryfileSupplement.class).addTemplate("valid", new Rule() {{
			  //add("primaryfile", new Primaryfile());
			add("id", random(Long.class, range(1L, 200L)));
			add("name", firstName());
	    	add("description", "Description for ${name}'s test case");	
			add("createdDate", 20170101L); 
			add("modifiedDate", 20170201L); 
			add("createdBy", "${name}");
			add("modifiedBy", "${name}");
			//add("collection", new Collection());
			add("originalFilename", firstName());
		  	add("pathname", "C:/New Folder/${name}");
		  	add("metainfo", regex("[A-Z]{1}[A-Z a-z]{9,29}"));
		  	add("externalIds", new HashMap<String, String>());
		  }});
		 
	  
	    Fixture.of(Unit.class).addTemplate("valid", new Rule() {{ 
	    	//add("collections", has(3).of(Collection.class, "valid")); 
	    	add("externalIds", new HashMap<String, String>());
	    	add("id", random(Long.class, range(1L, 200L)));
			add("name", firstName());
	    	add("description", "Description for ${name}'s test case");	
			add("createdDate", 20170101L); 
			add("modifiedDate", 20170201L); 
			add("createdBy", "${name}");
			add("modifiedBy", "${name}");
	    	}});
	
	    	}

}
