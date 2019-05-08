package edu.indiana.dlib.amppd.factory;


import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.lang.reflect.*;

import edu.indiana.dlib.amppd.model.Bundle;
import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.CollectionSupplement;
import edu.indiana.dlib.amppd.model.Dataentity;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.ItemSupplement;
import edu.indiana.dlib.amppd.model.Job;
import edu.indiana.dlib.amppd.model.JobMgmMode;
import edu.indiana.dlib.amppd.model.Mgm;
import edu.indiana.dlib.amppd.model.MgmMode;
import edu.indiana.dlib.amppd.model.MgmModeInput;
import edu.indiana.dlib.amppd.model.MgmModeOutput;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;
import edu.indiana.dlib.amppd.model.RouteLink;
import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.model.Workflow;


public class ObjectFactory implements BaseObjectFactory{

	//public <obj extends Dataentity> obj;
	//@SuppressWarnings("finally")
	@Override
	public <O extends Dataentity> O createDataEntityObject(HashMap<?,?> args, String classname) throws ClassNotFoundException
	{
		System.out.println("entered createDataEntityObject");
		O res = null;
		//TODO: clean type so that it does not contain any special character
		Class<?> entityObj = Class.forName("edu.indiana.dlib.amppd.model."+classname);
		System.out.println(entityObj.getCanonicalName());		
		try 
		{
			res = (O)entityObj.getConstructor().newInstance();			
			for(Entry<?, ?> entry : args.entrySet())
			{ 
				Field field = entityObj.getDeclaredField(entry.getKey().toString());
				//System.out.println("entered for loop "+field.getName()+" is the field name and "+entry.getKey()+" is the entry.key. and "+entry.getValue()+" is the value");
				if(entry.getKey().toString().equalsIgnoreCase(field.getName()))
				{
					//System.out.println("entered if");
					field.setAccessible(true);
					field.set(res, entry.getValue());
					//System.out.println("instantiated name for d:"+res.getUnit().getName());
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception occurred in getting the new instance"+e);
		}
		
		return res;
	}
	
	
	@Override
	public Object createModelObject(String type) {
		// TODO Auto-generated method stub
		Object obj;
		switch(type.toLowerCase())
		{
			case "bundle":
				obj = new Bundle();
				break;
				
			case "collection":
				
				obj = new Collection();
				break;
			
			case "collectionsupplement":
				obj = new CollectionSupplement();
				break;
				
			case "item":
				obj = new Item();
				break;
				
			case "itemsupplement":
				obj = new ItemSupplement();
				break;
			
			case "job":
				obj = new Job();
				break;
				
			case "jobmgmmode":
				obj = new JobMgmMode();
				break;
				
			case "mgm":
				obj = new Mgm();
				break;
				
			case "mgmmode":
				obj = new MgmMode();
				break;
				
			case "mgmmodeinput":
				obj = new MgmModeInput();
				break;
			
			case "mgmmodeoutput":
				obj = new MgmModeOutput();
				break;
				
		/*
		 * case "mgmmodeio": obj = new MgmModeIo(); break;
		 */
			case "primaryfile":
				obj = new Primaryfile();
				break;
				
			case "primaryfilesupplement":
				obj = new PrimaryfileSupplement();
				break;
				
			case "routelink":
				obj = new RouteLink();
				break;
				
		/*
		 * case "supplement": obj = new Supplement(); break;
		 */
				
			case "unit":
				obj = new Unit();
				break;
				
			case "workflow":
				obj = new Workflow();
				break;
			default:
				throw new IllegalArgumentException("Could not identify Object type");
		
		}
		return obj;
	}

	
	
}
