package edu.indiana.dlib.amppd.data;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
import edu.indiana.dlib.amppd.service.DataentityService;
import edu.indiana.dlib.amppd.util.TestHelper;


@Component
public class ModelTemplates implements TemplateLoader {
	
	@Autowired
    private DataentityService dataentityService;	
	
	@Autowired
    private TestHelper testHelper;
	
	private Random rand = new Random();
	private Long id = 0L;
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

	@Override
	public void load() {
		id = rand.nextLong();
		Fixture.of(Unit.class).addTemplate("valid", new Rule() {{ 
//			add("id", rand.nextLong());
			add("name", "Test Unit ${id}");
			add("description", "Description for ${name}");	
		}});

		Fixture.of(Unit.class).addTemplate("invalid", new Rule() {{ 
			add("name", "");
		}});

		id = rand.nextLong();		
		Fixture.of(Collection.class).addTemplate("valid", new Rule() {{
//			add("id", rand.nextLong());
			add("name", "Test Collection ${id}");
			add("description", "Description for ${name}");	
			add("externalSource", externalSources[rand.nextInt(externalSources.length)]);
			add("externalId", "ext-" + rand.nextInt());
			add("taskManager", taskManagers[rand.nextInt(taskManagers.length)]);
			add("unit", unitUrl);
		}}); 
			
		Fixture.of(Collection.class).addTemplate("invalid", new Rule() {{
			add("name", "");
			add("externalSource", "Fake");
			add("taskManager", "Fake");
		}}); 

		id = rand.nextLong();
		Fixture.of(Item.class).addTemplate("valid", new Rule() {{			
//			add("id", rand.nextLong());
			add("name", "Test Item ${id}");
			add("description", "Description for ${name}");	
			add("externalSource", externalSources[rand.nextInt(externalSources.length)]);
			add("externalId", "ext-" + rand.nextInt());
			add("collection", collectionUrl);
		}});

		Fixture.of(Item.class).addTemplate("invalid", new Rule() {{			
			add("name", "");
			add("externalSource", "Fake");
		}});

		id = rand.nextLong();
		Fixture.of(Primaryfile.class).addTemplate("valid", new Rule() {{
//			add("id", rand.nextLong());
			add("name", "Test Primaryfile ${id}");
			add("description", "Description for ${name}");	
			add("item", itemUrl);
//			add("originalFilename", firstName());
//			add("pathname", "C:/New Folder/${name}");
//			add("mediaInfo", "{}");
		}});

		id = rand.nextLong();
		Fixture.of(Primaryfile.class).addTemplate("invalid", new Rule() {{ 
			add("name", "");
		}});
		
		id = rand.nextLong();
		Fixture.of(CollectionSupplement.class).addTemplate("valid", new Rule() {{
//			add("id", rand.nextLong());
			add("name", "Test CollectionSupplement ${id}");
			add("description", "Description for ${name}");	
			add("collection", collectionUrl);
//			add("originalFilename", firstName());
//			add("pathname", "C:/New Folder/${name}");
//			add("mediaInfo", "{}");
		}});

		Fixture.of(CollectionSupplement.class).addTemplate("invalid", new Rule() {{
			add("name", "");
		}}); 
		
		id = rand.nextLong();
		Fixture.of(ItemSupplement.class).addTemplate("valid", new Rule() {{
//			add("id", rand.nextLong());
			add("name", "Test ItemSupplement ${id}");
			add("description", "Description for ${name}");	
			add("item", itemUrl);
//			add("originalFilename", firstName());
//			add("pathname", "C:/New Folder/${name}");
//			add("mediaInfo", "{}");
		}});

		Fixture.of(ItemSupplement.class).addTemplate("invalid", new Rule() {{
			add("name", "");
		}}); 
				
		id = rand.nextLong();
		Fixture.of(PrimaryfileSupplement.class).addTemplate("valid", new Rule() {{
//			add("id", rand.nextLong());
			add("name", "PrimaryfileSupplement ${id}");
			add("description", "Description for ${name}");	
			add("primaryfile", primaryfileUrl);
//			add("originalFilename", firstName());
//			add("pathname", "C:/New Folder/${name}");
//			add("mediaInfo", "{}");
		}});
		
		Fixture.of(PrimaryfileSupplement.class).addTemplate("invalid", new Rule() {{
			add("name", "");
		}}); 				
	}

}
