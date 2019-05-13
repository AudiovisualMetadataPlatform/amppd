package edu.indiana.dlib.amppd.model.factory;


import java.util.HashMap;
import java.util.Map.Entry;
import java.lang.reflect.*;


import edu.indiana.dlib.amppd.model.Dataentity;



public class ObjectFactory implements BaseObjectFactory{

	@Override
	public <O extends Dataentity> O createDataentityObject(HashMap<?,?> args, String classname)
	{
		O res = null;
		try 
		{
			Class<?> entityObj = Class.forName("edu.indiana.dlib.amppd.model."+classname);
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
			throw new RuntimeException("Unable to create Dataentity in ObjectFactory for "+classname, e);
		}
		
		return res;
	}
}
