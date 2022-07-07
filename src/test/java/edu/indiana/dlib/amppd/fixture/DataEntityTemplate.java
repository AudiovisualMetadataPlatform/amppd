package edu.indiana.dlib.amppd.fixture;

import br.com.six2six.fixturefactory.Fixture;
import br.com.six2six.fixturefactory.Rule;
import br.com.six2six.fixturefactory.loader.TemplateLoader;
import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.CollectionSupplement;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.ItemSupplement;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;
import edu.indiana.dlib.amppd.model.Unit;


//@Component
public class DataEntityTemplate implements TemplateLoader {
	public static String CATEGORY = "Face|Transcript|Program|Groundtruth|Other";
	public static String TASK_MANAGER = "Jira|Trello";
	public static String EXTERNAL_SOURCE = "MCO|DarkAvalon|NYPL";	// "^\\s*$|MCO|DarkAvalon|NYPL"
	
//	@Autowired
//    private DataentityService dataentityService;	
//	
//	@Autowired
//    private TestHelper testHelper;
	    
	@Override
	public void load() {
//		String[] supplementCategories = dataentityService.getSupplementCategories();
//		String[] taskManagers = dataentityService.getExternalSources();
//		String[] externalSources = dataentityService.getExternalSources();
//		
//		Unit unit = testHelper.ensureUnit("Test Unit");
//		Collection collection = testHelper.ensureCollection("Test Unit", "Test Collection");
//		Item item = testHelper.ensureItem("Test Unit", "Test Collection", "Test Item");
//		Primaryfile primaryfile = testHelper.ensurePrimaryfile("Test Unit", "Test Collection", "Test Item", "Test Primaryfile");
//		String unitUrl = dataentityService.getDataentityUrl(unit);
//		String collectionUrl = dataentityService.getDataentityUrl(collection);
//		String itemUrl = dataentityService.getDataentityUrl(item);
//		String primaryfileUrl = dataentityService.getDataentityUrl(primaryfile);		
		
		String nameRegex = "[a-zA-Z]{20}";
		String idRegex = "[0-9]{10}";
		
		Fixture.of(Unit.class).addTemplate("valid", new Rule() {{ 
			add("id", random(Long.class));
			add("name", regex("Test Unit - [0-9]{10}"));
			add("description", "Description for ${name}");	
		}});

		Fixture.of(Unit.class).addTemplate("invalid", new Rule() {{ 
			add("name", "");
		}});

		Fixture.of(Collection.class).addTemplate("valid", new Rule() {{
			add("name", regex("Test Collection - " + nameRegex));
			add("description", "Description for ${name}");	
			add("externalSource", regex(EXTERNAL_SOURCE));
			add("externalId", regex("external-" + idRegex));
			add("taskManager", regex(TASK_MANAGER));
//			add("externalSource", externalSources[rand.nextInt(externalSources.length)]);
//			add("taskManager", taskManagers[rand.nextInt(taskManagers.length)]);
//			add("unit", unitUrl);
		}}); 
			
		Fixture.of(Collection.class).addTemplate("invalid", new Rule() {{
			add("name", "");
			add("externalSource", "FakeExternalSource");
			add("taskManager", "FakeTaskManager");
		}}); 

		Fixture.of(Item.class).addTemplate("valid", new Rule() {{			
			add("name", regex("Test Item - " + nameRegex));
			add("description", "Description for ${name}");	
			add("externalSource", regex(EXTERNAL_SOURCE));
			add("externalId", regex("external-" + idRegex));
//			add("collection", collectionUrl);
		}});

		Fixture.of(Item.class).addTemplate("invalid", new Rule() {{			
			add("name", "");
			add("externalSource", "FakeExternalSource");
		}});

		Fixture.of(Primaryfile.class).addTemplate("valid", new Rule() {{
			add("name", regex("Test Primaryfile - " + nameRegex));
			add("description", "Description for ${name}");	
//			add("item", itemUrl);
//			add("originalFilename", firstName());
//			add("pathname", "C:/New Folder/${name}");
//			add("mediaInfo", "{}");
		}});

		Fixture.of(Primaryfile.class).addTemplate("invalid", new Rule() {{ 
			add("name", "");
		}});
		
		Fixture.of(CollectionSupplement.class).addTemplate("valid", new Rule() {{
			add("name", regex("Test CollectionSupplement - " + nameRegex));
			add("description", "Description for ${name}");	
			add("category", regex(CATEGORY));	
//			add("collection", collectionUrl);
//			add("originalFilename", firstName());
//			add("pathname", "C:/New Folder/${name}");
//			add("mediaInfo", "{}");
		}});

		Fixture.of(CollectionSupplement.class).addTemplate("invalid", new Rule() {{
			add("name", "");
		}}); 
		
		Fixture.of(ItemSupplement.class).addTemplate("valid", new Rule() {{
			add("name", regex("Test ItemSupplement - " + nameRegex));
			add("description", "Description for ${name}");	
			add("category", regex(CATEGORY));	
//			add("item", itemUrl);
//			add("originalFilename", firstName());
//			add("pathname", "C:/New Folder/${name}");
//			add("mediaInfo", "{}");
		}});

		Fixture.of(ItemSupplement.class).addTemplate("invalid", new Rule() {{
			add("name", "");
		}}); 
				
		Fixture.of(PrimaryfileSupplement.class).addTemplate("valid", new Rule() {{
			add("name", regex("Test PrimaryfileSupplement - " + nameRegex));
			add("description", "Description for ${name}");	
			add("category", regex(CATEGORY));	
//			add("primaryfile", primaryfileUrl);
//			add("originalFilename", firstName());
//			add("pathname", "C:/New Folder/${name}");
//			add("mediaInfo", "{}");
		}});
		
		Fixture.of(PrimaryfileSupplement.class).addTemplate("invalid", new Rule() {{
			add("name", "");
		}}); 				
	}

}
