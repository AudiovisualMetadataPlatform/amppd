package edu.indiana.dlib.amppd.factory;

import java.util.HashMap;

import edu.indiana.dlib.amppd.model.Dataentity;

public interface BaseObjectFactory {
	
	public abstract Object createModelObject(String type);
	public <O extends Dataentity> O createDataEntityObject(HashMap<?,?> args, String classname) throws ClassNotFoundException;

}
