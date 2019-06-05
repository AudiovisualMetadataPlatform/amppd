package edu.indiana.dlib.amppd.testData;

import java.text.SimpleDateFormat;
import java.util.HashMap;

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
import edu.indiana.dlib.amppd.model.JobMgmMode;
import edu.indiana.dlib.amppd.model.MgmMode;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;
import edu.indiana.dlib.amppd.model.RouteLink;
import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.model.Workflow;

//import edu.indiana.dlib.amppd.model.fixtures.TemplateLoader;

public class ModelTemplates implements TemplateLoader {

	@Override
	public void load() {
		// TODO Auto-generated method stub
	    Fixture.of(Collection.class).addTemplate("valid", new Rule() {{
	    	//add("id", random(Long.class, range(1L, 200L)));
	    	add("name", firstName());
			add("description", "Description for ${name}'s test case");		
			//add("createdDate", afterDate("2017-06-22", new SimpleDateFormat("yyyy-MM-dd"))); 
			//add("modifiedDate", afterDate("2017-06-22",new SimpleDateFormat("yyyy-MM-dd"))); 
			add("createdBy", "${name}");
			add("modifiedBy", "${name}");
				 
			/*
			 * add("items", has(2).of(Item.class, "valid")); add("supplements",
			 * has(2).of(CollectionSupplement.class, "valid")); add("unit",
			 * has(1).of(Unit.class, "valid"));
			 */
	    	}});
	    
		/*
		 * Fixture.of(CollectionSupplement.class).addTemplate("valid", new Rule() {{
		 * add("collection", has(1).of(Collection.class, "valid")); }});
		 */
	    
	    Fixture.of(Primaryfile.class).addTemplate("valid", new Rule() {{
			add("supplements", has(2).of(PrimaryfileSupplement.class, "valid"));
			add("jobs", has(2).of(Job.class, "valid"));
			add("item", has(1).of(Item.class, "valid"));
	    	}});
	    
	    Fixture.of(ItemSupplement.class).addTemplate("valid", new Rule() {{
	    	add("item", has(1).of(Item.class, "valid"));	
	    	}});
	    
	    Fixture.of(Bundle.class).addTemplate("valid", new Rule() {{
	    	add("items", has(2).of(Item.class, "valid"));	
	    	}});
	    
		/*
		 * Fixture.of(Item.class).addTemplate("valid", new Rule() {{
		 * 
		 * add("collection", has(1).of(Collection.class, "valid")); add("primaryfiles",
		 * has(2).of(Primaryfile.class, "valid")); add("supplements",
		 * has(2).of(ItemSupplement.class, "valid")); add("bundles",
		 * has(1).of(Bundle.class, "valid"));
		 * 
		 * }});
		 */
	    
	    Fixture.of(Dataentity.class).addTemplate("valid", new Rule() {{
	    	add("id", random(Long.class, range(1L, 200L)));
	    	add("name", "${firstName()} ${lastName()}");
			add("description", regex("[A-Z]{1}[A-Z a-z]{9,29}"));
			add("createdDate", afterDate("2017-06-22", new SimpleDateFormat("yyyy-MM-dd")));
			add("modifiedDate", afterDate("2017-06-22", new SimpleDateFormat("yyyy-MM-dd")));
			add("createdBy", "${firstName()} ${lastName()}");
			add("modifiedBy", "${firstName()} ${lastName()}");
	    	}});
	
	    Fixture.of(PrimaryfileSupplement.class).addTemplate("valid", new Rule() {{
	    	add("primaryfile", has(2).of(Primaryfile.class, "valid"));	
	    	}});
	    
		/*
		 * Fixture.of(Unit.class).addTemplate("valid", new Rule() {{ add("collections",
		 * has(2).of(Collection.class, "valid")); }});
		 */
	    
	    Fixture.of(Job.class).addTemplate("valid", new Rule() {{
	    	add("id", random(Long.class, range(1L, 200L)));
	    	add("submittedBy", "${firstName()} ${lastName()}");
	    	add("status", regex("[A-Z]{1}[A-Z a-z]{9,29}"));
			add("errorMessage", regex("[A-Z]{1}[A-Z a-z]{9,29}"));
			add("timeStarted", afterDate("2017-06-22", new SimpleDateFormat("yyyy-MM-dd")));
			add("timeEnded", afterDate("2017-06-22", new SimpleDateFormat("yyyy-MM-dd")));
			add("jobMgmModes", has(2).of(JobMgmMode.class, "valid"));
			add("primaryfile", has(1).of(Primaryfile.class, "valid"));
			add("workflow", has(1).of(Workflow.class, "valid"));
	    	}});
	    
	    Fixture.of(JobMgmMode.class).addTemplate("valid", new Rule() {{
	    	add("id", random(Long.class, range(1L, 200L)));
	    	add("params", has(1).of(HashMap.class, "valid"));
	    	add("percentage", random(Double.class, range(1L, 200L)));
	    	add("errorMessage", regex("[A-Z]{1}[A-Z a-z]{9,29}"));
	    	add("timeStarted", afterDate("2017-06-22", new SimpleDateFormat("yyyy-MM-dd")));
			add("timeEnded", afterDate("2017-06-22", new SimpleDateFormat("yyyy-MM-dd")));
			add("mgmModeIoMap", has(1).of(HashMap.class, "valid"));
			add("job", has(1).of(Job.class, "valid"));
			add("mgmMode", has(1).of(MgmMode.class, "valid"));
	    	}});
	    
	    Fixture.of(Workflow.class).addTemplate("valid", new Rule() {{
	    	add("startMgmMode", has(1).of(MgmMode.class, "valid"));	
	    	add("endMgmMode", has(1).of(MgmMode.class, "valid"));	
	    	add("routeLinks", has(2).of(RouteLink.class, "valid"));
	    	add("jobs", has(2).of(Job.class, "valid"));
	    	}});
	    
	    Fixture.of(RouteLink.class).addTemplate("valid", new Rule() {{
	    	add("id", random(Long.class, range(1L, 200L)));
	    	add("mgmModeIoMap", has(1).of(HashMap.class, "valid"));
	    	add("fromMgmMode", has(1).of(MgmMode.class, "valid"));	
	    	add("toMgmMode", has(2).of(RouteLink.class, "valid"));
	    	add("workflow", has(1).of(Workflow.class, "valid"));
	    	}});
	}

}
