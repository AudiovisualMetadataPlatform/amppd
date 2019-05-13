package edu.indiana.dlib.amppd.model.factory;


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

	@Override
	public <O extends Dataentity> O createDataentityObject(HashMap<?,?> args, String classname) throws ClassNotFoundException
	{
		O res = null;
		Class<?> entityObj = Class.forName("edu.indiana.dlib.amppd.model."+classname);
		try 
		{
			res = (O)entityObj.getConstructor().newInstance();			
			for(Entry<?, ?> entry : args.entrySet())
			{ 
				Field field = entityObj.getDeclaredField(entry.getKey().toString());
				if(entry.getKey().toString().equals(field.getName()))
				{
					field.setAccessible(true);
					field.set(res, entry.getValue());
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception occurred in getting the new instance"+e);
		}
		
		return res;
	}

}
