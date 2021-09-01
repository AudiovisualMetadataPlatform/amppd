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
import edu.indiana.dlib.amppd.service.DataentityService;
import edu.indiana.dlib.amppd.util.TestHelper;


@Component
public class DataentityProcessor implements Processor {
	
	@Autowired
    private DataentityService dataentityService;	
	
	@Autowired
    private TestHelper testHelper;
	
	private String[] taskManagers = dataentityService.getAllowedExternalSources();
	private String[] externalSources = dataentityService.getAllowedExternalSources();
	
	private Unit unit = testHelper.ensureUnit("Test Unit");
	private Collection collection = testHelper.ensureCollection("Test Unit", "Test Collection");
	private Item item = testHelper.ensureItem("Test Unit", "Test Collection", "Test Item");
	private Primaryfile primaryfile = testHelper.ensurePrimaryfile("Test Unit", "Test Collection", "Test Item", "Test Primaryfile");
	private String unitUrl = dataentityService.getDataentityUrl(unit);
	private String collectionUrl = dataentityService.getDataentityUrl(collection);
	private String itemUrl = dataentityService.getDataentityUrl(item);
	private String primaryfileUrl = dataentityService.getDataentityUrl(primaryfile);		

	public void execute(Object dataentity) {
		if (dataentity instanceof Unit) {
			desFound = unitRepository.findByName(dataentity.getName());
		}
		else if (dataentity instanceof Collection) {
			desFound = collectionRepository.findByUnitIdAndName(((Collection)dataentity).getUnit().getId(), dataentity.getName());
		}
		else if (dataentity instanceof Item) {
			desFound = itemRepository.findByCollectionIdAndName(((Item)dataentity).getCollection().getId(), dataentity.getName());
		}
		else if (dataentity instanceof Primaryfile) {
			desFound = primaryfileRepository.findByItemIdAndName(((Primaryfile)dataentity).getItem().getId(), dataentity.getName());
		}
		else if (dataentity instanceof CollectionSupplement) {
			desFound = collectionSupplementRepository.findByCollectionIdAndName(((CollectionSupplement)dataentity).getCollection().getId(), dataentity.getName());
		}
		else if (dataentity instanceof ItemSupplement) {
			desFound = itemSupplementRepository.findByItemIdAndName(((ItemSupplement)dataentity).getItem().getId(), dataentity.getName());
		}
		else if (dataentity instanceof PrimaryfileSupplement) {
			desFound = primaryfileSupplementRepository.findByPrimaryfileIdAndName(((PrimaryfileSupplement)dataentity).getPrimaryfile().getId(), dataentity.getName());
		}
	}
	
}
