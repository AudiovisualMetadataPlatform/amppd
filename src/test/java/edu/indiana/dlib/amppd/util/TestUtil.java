package edu.indiana.dlib.amppd.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.CollectionSupplement;
import edu.indiana.dlib.amppd.model.Dataentity;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.ItemSupplement;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;
import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.service.DataentityService;

/**
 * Class containing static utility methods used by tests. 
 * @author yingfeng
 */
@Component
public class TestUtil {

	@Autowired
	private DataentityService dataentityService;	
	
	/**
	 * Return the standard media content type representation based on the given file extension, or null if the extension is not one of the common video/audio formats.
	 * @param extention the given file extension
	 * @return the corresponding content type
	 */
	public String getContentType(String extension) {
		if (StringUtils.isEmpty(extension)) {
			return null;
		}		
		String extlow =  extension.toLowerCase();
		String contentType = TestHelper.VIDEO_TYPES.contains(extlow) ? "video" : TestHelper.AUDIO_TYPES.contains(extlow) ? "audio" : null;
		return contentType == null ? null : contentType + "/" + extension;	
	}
	
	/**
	 * Converts the given valid Dataentity to proper JSON string for REST requests.
	 * @param dataentity the given Dataentity
	 * @return the corresponding JSON string
	 */
	public String toJson(Dataentity dataentity) throws Exception {		
		ObjectMapper mapper = new ObjectMapper();
		String json = "";
		
		/* TODO:
		 * ObjectMapper converts embedded reference object hierarchically instead of into its URI string;
		 * however, REST POST request requires the reference object to be represented by its URI string.
		 * Below is a workaround to fix the issue:
		 * 1) preserve the embedded object into a tmp variable
		 * 2) set the entity's embedded object to null, to avoid deep conversion into the hierarchy;
		 * 3) convert the entity to JSON via ObjectMapper, the corresponding JSON segment for embedded object is simply null; 
		 * 4) replace the null segment in above JSON string with the embedded object's URL;
		 * 5) set the entity's embedded object back to its original value in the tmp variable.
		 * Alternatively, we can just directly convert the original entity to JSON and replace the embedded object json segment
		 * with URL via regex; the downside is that the pattern would be very complicated, and deep conversion is a waste. 
		 */ 
		if (dataentity instanceof Unit) {
			// no embedded parent entity for unit, just convert it directly to JSON
			json = mapper.writeValueAsString(dataentity);
		}
		else if (dataentity instanceof Collection) {
			Collection collection = (Collection)dataentity;
			Unit unit = collection.getUnit();
			collection.setUnit(null);
			json = mapper.writeValueAsString(collection);
			json = StringUtils.replace(json, "\"unit\":null", "\"unit\":\"" + dataentityService.getDataentityUrl(unit) + "\"");
			collection.setUnit(unit);
		}
		else if (dataentity instanceof Item) {
			Item item = (Item)dataentity;
			Collection collection = item.getCollection();
			item.setCollection(null);
			json = mapper.writeValueAsString(item);
			json = StringUtils.replace(json, "\"collection\":null", "\"collection\":\"" + dataentityService.getDataentityUrl(collection) + "\"");
			item.setCollection(collection);
		}
		else if (dataentity instanceof Primaryfile) {
			Primaryfile primaryfile = (Primaryfile)dataentity;
			Item item = primaryfile.getItem();
			primaryfile.setItem(null);
			json = mapper.writeValueAsString(primaryfile);
			json = StringUtils.replace(json, "\"item\":null", "\"item\":\"" + dataentityService.getDataentityUrl(item) + "\"");
			primaryfile.setItem(item);
		}
		else if (dataentity instanceof CollectionSupplement) {
			CollectionSupplement collectionSupplement = (CollectionSupplement)dataentity;
			Collection collection = collectionSupplement.getCollection();
			collectionSupplement.setCollection(null);
			json = mapper.writeValueAsString(collectionSupplement);
			json = StringUtils.replace(json, "\"collection\":null", "\"collection\":\"" + dataentityService.getDataentityUrl(collection) + "\"");
			collectionSupplement.setCollection(collection);
		}
		else if (dataentity instanceof ItemSupplement) {
			ItemSupplement itemSupplement = (ItemSupplement)dataentity;
			Item item = itemSupplement.getItem();
			itemSupplement.setItem(null);
			json = mapper.writeValueAsString(itemSupplement);
			json = StringUtils.replace(json, "\"item\":null", "\"item\":\"" + dataentityService.getDataentityUrl(item) + "\"");
			itemSupplement.setItem(item);
		}
		else if (dataentity instanceof PrimaryfileSupplement) {
			PrimaryfileSupplement primaryfileSupplement = (PrimaryfileSupplement)dataentity;
			Primaryfile primaryfile = primaryfileSupplement.getPrimaryfile();
			primaryfileSupplement.setPrimaryfile(null);
			json = mapper.writeValueAsString(primaryfileSupplement);
			json = StringUtils.replace(json, "\"primaryfile\":null", "\"primaryfile\":\"" + dataentityService.getDataentityUrl(primaryfile) + "\"");
			primaryfileSupplement.setPrimaryfile(primaryfile);
		}

		return json;
	}
	
	
}
