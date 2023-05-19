package edu.indiana.dlib.amppd.fixture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.six2six.fixturefactory.processor.Processor;
import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.CollectionSupplement;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.ItemSupplement;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;
import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.model.UnitSupplement;
import edu.indiana.dlib.amppd.util.TestHelper;


/**
 * Processor for post-processing fixtures created by DataEntityTemplate. 
 * @author yingfeng
 */
@Component
public class DataentityProcessor implements Processor {	
	@Autowired
    private TestHelper testHelper;	
    
	@Override
	public void execute(Object dataentity) {
		// ensure and populate parent entity post fixture creation, as a workaround for the @Autowire issue in DataEntityTemplate
		if (dataentity instanceof Unit) {
			// don't need to do anything;
		}
		else if (dataentity instanceof Collection) {
			Unit unit = testHelper.ensureUnit("Test Unit");
			((Collection)dataentity).setUnit(unit);
		}
		else if (dataentity instanceof Item) {
			Collection collection = testHelper.ensureCollection("Test Unit", "Test Collection");
			((Item)dataentity).setCollection(collection);
		}
		else if (dataentity instanceof Primaryfile) {
			Item item = testHelper.ensureItem("Test Unit", "Test Collection", "Test Item");
			((Primaryfile)dataentity).setItem(item);
		}
		else if (dataentity instanceof UnitSupplement) {
			Unit unit = testHelper.ensureUnit("Test Unit");
			((UnitSupplement)dataentity).setUnit(unit);
		}
		else if (dataentity instanceof CollectionSupplement) {
			Collection collection = testHelper.ensureCollection("Test Unit", "Test Collection");
			((CollectionSupplement)dataentity).setCollection(collection);
		}
		else if (dataentity instanceof ItemSupplement) {
			Item item = testHelper.ensureItem("Test Unit", "Test Collection", "Test Item");
			((ItemSupplement)dataentity).setItem(item);
		}
		else if (dataentity instanceof PrimaryfileSupplement) {
			Primaryfile primaryfile = testHelper.ensurePrimaryfile("Test Unit", "Test Collection", "Test Item", "Test Primaryfile");
			((PrimaryfileSupplement)dataentity).setPrimaryfile(primaryfile);
		}
	}
	
}
