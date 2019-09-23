package edu.indiana.dlib.amppd;

import org.junit.BeforeClass;

/**
 * Base class for all AMPPD tests, for the purpose of setting up Galaxy bootstrap.
 * @author yingfeng
 *
 */
public abstract class AmppdTestsBase {
	
	/**
	 * This method will be called before any tests are run on any AMPPD class.
	 */
	@BeforeClass
	public static void initGalaxyBootStrap() {
		// TODO use logic similar to com.github.jmchilton.blend4j.galaxy.TestGalaxyInstance.bootStrapGalaxy() to bootstrap an instance of Galaxy
	}
	
}
