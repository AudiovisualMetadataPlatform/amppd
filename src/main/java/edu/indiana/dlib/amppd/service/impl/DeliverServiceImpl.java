package edu.indiana.dlib.amppd.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import edu.indiana.dlib.amppd.config.AvalonPropertyConfig;
import edu.indiana.dlib.amppd.model.BagContent;
import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.ItemBag;
import edu.indiana.dlib.amppd.model.PrimaryfileBag;
import edu.indiana.dlib.amppd.model.dto.AvalonMediaObject;
import edu.indiana.dlib.amppd.model.dto.AvalonRelatedItems;
import edu.indiana.dlib.amppd.model.dto.AvalonRelatedItems.AvalonRelatedItemsFields;
import edu.indiana.dlib.amppd.service.BagService;
import edu.indiana.dlib.amppd.service.DeliverService;
import edu.indiana.dlib.amppd.service.MediaService;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of DeliverService.
 * @author yingfeng
 */
@Service
@Slf4j
public class DeliverServiceImpl implements DeliverService {
		
	// external source for Avalon
	public static final String AVALON = "MCO"; 
	
	// Avalon media objects URL path
	public static final String AVALON_MEDIA_OBJECTS = "media_objects"; 
	
	// Avalon API key header
	public static final String AVALON_API_KEY = "Avalon-Api-Key"; 
	
	@Autowired
	private AvalonPropertyConfig avalonPropertyConfig;
	
	@Autowired
	private BagService bagService;

	@Autowired
	private MediaService mediaService;
	
	
	/**
	 * @see edu.indiana.dlib.amppd.service.DeliverService.deliverAvalonItem(Item)
	 */
	public AvalonRelatedItems deliverAvalonItem(Item item) {
		Long itemId = item.getId();
		String collectionExternalId = null;
		Collection collection = item.getCollection();

		// verify that the item has a valid externalId for Avalon
		if (!AVALON.equalsIgnoreCase(item.getExternalSource()) || StringUtils.isEmpty(item.getExternalId())) {
			throw new RuntimeException("Item " + itemId + " has invalid external source or ID for Avalon");
		}
		
		// verify that the item's collection has a valid externalId for Avalon
		if (collection == null) {
			throw new RuntimeException("Collection for item " + itemId + " is null");
		}
		else {
			collectionExternalId = collection.getExternalId();    
			if (!AVALON.equalsIgnoreCase(collection.getExternalSource()) || StringUtils.isEmpty(collectionExternalId)) {
				throw new RuntimeException("Collection " + collection.getId() + " for item " + itemId + " has invalid external source or ID for Avalon");
			}
		}		

		ItemBag itemBag = bagService.getItemBag(item);		
		List<String> urls = new ArrayList<String>();		
		List<String> labels = new ArrayList<String>();	
		AvalonRelatedItems aris = new AvalonRelatedItems(collectionExternalId, new AvalonRelatedItemsFields(urls, labels));	
		
		// go through each PrimaryfileBag contained in the itemBag
		for (PrimaryfileBag pb : itemBag.getPrimaryfileBags()) {
			// go through each BagContent contained in the primaryfileBag
			for (BagContent bc : pb.getBagContents()) {
//				urls.add(bc.getOutputUrl());	
				// use symlink here instead of the outputUrl so that Avalon users don't need to login to AMP to access the output
				String url = mediaService.getWorkflowResultOutputSymlinkUrl(bc.getResultId());
				urls.add(url);
				labels.add("AMP " + bc.getOutputName() + " - " + pb.getPrimaryfileName() + " - " + bc.getDateCreated());
//				labels.add("AMP " + bc.getOutputType().toUpperCase() + " - " + pb.getPrimaryfileName() + " - " + bc.getDateCreated());
			}
		}
				
		// call Avalon API to link related items to the corresponding media object
		AvalonMediaObject amo = putAvalonRelatedItems(itemBag.getExternalId(), aris);
		if (amo != null) {
			log.info("Successfully delivered " + labels.size() + " final results for item " + itemId + " to Avalon media object " + amo.getId());
			return aris;
		}
		else {
			log.error("Failed to deliver " + labels.size() + " final results for item " + itemId + " to Avalon media object " + itemBag.getExternalId());			
			return null;
		}		
	}
		
	/**
	 * @see edu.indiana.dlib.amppd.service.DeliverService.deliverAvalonCollection(Collection)
	 */
	public List<AvalonRelatedItems> deliverAvalonCollection(Collection collection) {
		List<AvalonRelatedItems> ariss = new ArrayList<AvalonRelatedItems>();
		Long collectionId = collection.getId();
		String collectionExternalId = collection.getExternalId();  

		// verify that the collection has a valid externalId for Avalon		
		if (!DeliverServiceImpl.AVALON.equalsIgnoreCase(collection.getExternalSource()) || StringUtils.isEmpty(collectionExternalId)) {
			throw new RuntimeException("Collection " + collection.getId() + " has invalid external source or ID for Avalon");
		}

		// deliver final results for each item in the collection to Avalon
		for (Item item : collection.getItems()) {
			AvalonRelatedItems aris = deliverAvalonItem(item);
			if (aris != null) {
				ariss.add(aris);
			}
		}
		
		log.info("Successfully delivered final results for " + ariss.size() + " items in collection " + collectionId + " to Avalon collection " + collectionExternalId);
		int failed = collection.getItems().size() - ariss.size();
		if (failed > 0) {
			log.error("Failed to deliver final results for " + failed + " items in collection " + collectionId + " to Avalon collection " + collectionExternalId);
		}
		return ariss;
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.DeliverService.putAvalonRelatedItems(String, AvalonRelatedItems)
	 */
	public AvalonMediaObject putAvalonRelatedItems(String externalId, AvalonRelatedItems aris) {		
		RestTemplate restTemplate = new RestTemplate();
		
		// get API url
		String url = getAvalonMediaObjectUrl(externalId);
		
		// set up headers
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.set(AVALON_API_KEY, avalonPropertyConfig.getToken());
		
		// set up request with body and headers
		HttpEntity<AvalonRelatedItems> request = new HttpEntity<AvalonRelatedItems>(aris, headers);
		
		// send put request to update related items of the media object
		try {
			ResponseEntity<AvalonMediaObject> response = restTemplate.exchange(url, HttpMethod.PUT, request, AvalonMediaObject.class);
			HttpStatus status = response.getStatusCode();
			AvalonMediaObject amo = response.getBody();
			if (status.is2xxSuccessful() && amo != null && StringUtils.isNotEmpty(amo.getId())) {
				log.info("Successfully put Avalon related items into media object " + amo.getId());
				return amo;
			}
			else {
				log.error("Failed to put Avalon related items into media object " + externalId);
				log.error("Avalon API response code: " + status);
				log.error("Avalon API response body: " + amo);
				return null;				
			}
		}
		catch(RestClientException e) {
			log.error("Exception while putting Avalon related items into media object " + externalId, e);
			return null;
		}		
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.DeliverService.getAvalonMediaObjectUrl(String)
	 */
	public String getAvalonMediaObjectUrl(String externalId) {
		return avalonPropertyConfig.getUrl() + "/" + AVALON_MEDIA_OBJECTS + "/" + externalId + ".json";
	}
	
}
