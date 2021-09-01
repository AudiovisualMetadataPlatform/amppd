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
import edu.indiana.dlib.amppd.util.TestHelper;


@Component
public class DataentityProcessor implements Processor {	
	@Autowired
    private TestHelper testHelper;
	
	private Unit unit;
	private Collection collection;
	private Item item;
	private Primaryfile primaryfile;
//	private String unitUrl = dataentityService.getDataentityUrl(unit);
//	private String collectionUrl = dataentityService.getDataentityUrl(collection);
//	private String itemUrl = dataentityService.getDataentityUrl(item);
//	private String primaryfileUrl = dataentityService.getDataentityUrl(primaryfile);		

    @Autowired
    public DataentityProcessor(TestHelper testHelper) {
        this.testHelper = testHelper;
    	unit = testHelper.ensureUnit("Test Unit");
    	collection = testHelper.ensureCollection("Test Unit", "Test Collection");
    	item = testHelper.ensureItem("Test Unit", "Test Collection", "Test Item");
    	primaryfile = testHelper.ensurePrimaryfile("Test Unit", "Test Collection", "Test Item", "Test Primaryfile");
    }
    
	public void execute(Object dataentity) {
		if (dataentity instanceof Unit) {
			// don't need to do anything;
		}
		else if (dataentity instanceof Collection) {
			((Collection)dataentity).setUnit(unit);
		}
		else if (dataentity instanceof Item) {
			((Item)dataentity).setCollection(collection);
		}
		else if (dataentity instanceof Primaryfile) {
			((Primaryfile)dataentity).setItem(item);
		}
		else if (dataentity instanceof CollectionSupplement) {
			((CollectionSupplement)dataentity).setCollection(collection);
		}
		else if (dataentity instanceof ItemSupplement) {
			((ItemSupplement)dataentity).setItem(item);
		}
		else if (dataentity instanceof PrimaryfileSupplement) {
			((PrimaryfileSupplement)dataentity).setPrimaryfile(primaryfile);
		}
	}
	
}
