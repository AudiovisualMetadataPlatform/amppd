package edu.indiana.dlib.amppd.fixture.unused;

import java.util.HashMap;

import edu.indiana.dlib.amppd.model.Dataentity;


public interface BaseObjectFactory {

	public <O extends Dataentity> O createDataentityObject(HashMap<?,?> args, String classname) throws Exception;

}
