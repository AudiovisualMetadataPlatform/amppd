package edu.indiana.dlib.amppd.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.indiana.dlib.amppd.config.AmppdPropertyConfig;
import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.CollectionSupplement;
import edu.indiana.dlib.amppd.model.Dataentity;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.ItemSupplement;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;
import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.service.DataentityService;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of DataentityService.
 * @author yingfeng
 */
@Service
@Slf4j
public class DataentityServiceImpl implements DataentityService {
	
	public static String EXTERNAL_SOURCES = "externalSources";
	public static String TASK_MANAGERS = "taskManagers";

	@Autowired
	private AmppdPropertyConfig amppdPropertyConfig;
	
	@Value("#{'${amppd.externalSources}'.split(',')}")
	private List<String> externalSources;
	
	@Value("#{'${amppd.taskManagers}'.split(',')}")
	private List<String> taskManagers;
	
	/**
	 * @see edu.indiana.dlib.amppd.service.DataentityService.getExternalSources()
	 */
	public List<String> getExternalSources() {
		return externalSources;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.DataentityService.getTaskManagers()
	 */
	public List<String> getTaskManagers() {
		return taskManagers;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.DataentityService.getDataentityUrl(Dataentity)
	 */
	@Override
	public String getDataentityUrl(Dataentity dataentity) {
		String url = "";
		String destr = "";
		
		if (dataentity == null) {
			return url;
		}		
		else if (dataentity instanceof Unit) {
			destr = "units";
		}
		else if (dataentity instanceof Collection) {
			destr = "collections";
		}
		else if (dataentity instanceof Item) {
			destr = "items";
		}
		else if (dataentity instanceof Primaryfile) {
			destr = "primaryfiles";
		}
		else if (dataentity instanceof CollectionSupplement) {
			destr = "collectionSupplements";
		}
		else if (dataentity instanceof ItemSupplement) {
			destr = "itemSupplements";
		}
		else if (dataentity instanceof PrimaryfileSupplement) {
			destr = "primaryfileSupplements";
		}

		url = amppdPropertyConfig.getUrl() + "/" + destr + "/" + dataentity.getId();
		return url;
	}
	
}
