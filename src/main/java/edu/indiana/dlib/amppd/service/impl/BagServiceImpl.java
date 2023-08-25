package edu.indiana.dlib.amppd.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.BagContent;
import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.CollectionBag;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.ItemBag;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.PrimaryfileBag;
import edu.indiana.dlib.amppd.model.WorkflowResult;
import edu.indiana.dlib.amppd.repository.CollectionRepository;
import edu.indiana.dlib.amppd.repository.ItemRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.service.BagService;
import edu.indiana.dlib.amppd.service.MediaService;
import edu.indiana.dlib.amppd.service.WorkflowResultService;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of BagService.
 * @author yingfeng
 */
@Service
@Slf4j
public class BagServiceImpl implements BagService {

	@Autowired
	private PrimaryfileRepository primaryfileRepository;
	
	@Autowired
	private ItemRepository itemRepository;
	
	@Autowired
	private CollectionRepository collectionRepository;
	
	@Autowired
	private MediaService mediaService;
		
	
	@Autowired
	private WorkflowResultService workflowResultService;
		
		
	/**
	 * @see edu.indiana.dlib.amppd.service.BagService.getPrimaryfileBag(Primaryfile)
	 */
	public PrimaryfileBag getPrimaryfileBag(Primaryfile primaryfile) {
		PrimaryfileBag pbag = new PrimaryfileBag();			
		
		Long primaryfileId = primaryfile.getId();
		pbag.setPrimaryfileId(primaryfileId);
		pbag.setPrimaryfileName(primaryfile.getName());
		List<BagContent> bcontents = new ArrayList<BagContent>();
		pbag.setBagContents(bcontents);		
		List<WorkflowResult> results = workflowResultService.getFinalWorkflowResults(primaryfileId);
		
		for (WorkflowResult result : results) {
			BagContent bcontent = new BagContent();
			bcontent.setResultId(result.getId());
			bcontent.setSubmitter(result.getSubmitter());
			bcontent.setDateCreated(result.getDateCreated());
			bcontent.setDateUpdated(result.getDateUpdated());
			bcontent.setWorkflowId(result.getWorkflowId());
			bcontent.setInvocationId(result.getInvocationId());
			bcontent.setStepId(result.getStepId());
			bcontent.setOutputId(result.getOutputId());	
			bcontent.setWorkflowName(result.getWorkflowName());
			bcontent.setWorkflowStep(result.getWorkflowStep()); 
			bcontent.setToolInfo(result.getToolInfo());	 
			bcontent.setOutputName(result.getOutputName());
			bcontent.setOutputType(result.getOutputType());			
			bcontent.setOutputUrl(mediaService.getWorkflowResultOutputUrl(result.getId()));	
			bcontents.add(bcontent);
		}
		
		log.info("Successfully retrieved PrimaryfileBag for primaryfileId " + primaryfileId + " with " + bcontents.size() + " BagContents.");
		return pbag;
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.BagService.getPrimaryfileBag(Long)
	 */
	public PrimaryfileBag getPrimaryfileBag(Long primaryfileId) {		
		Primaryfile primaryfile = primaryfileRepository.findById(primaryfileId).orElseThrow(() -> new StorageException("primaryfile <" + primaryfileId + "> does not exist!"));    		
		return getPrimaryfileBag(primaryfile);
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.BagService.getItemBag(Item)
	 */
	public ItemBag getItemBag(Item item) {
		ItemBag ibag = new ItemBag();			

		Long itemId = item.getId();
		ibag.setItemId(itemId);
		ibag.setItemName(item.getName());
		ibag.setExternalSource(item.getExternalSource());
		ibag.setExternalId(item.getExternalId());
		List<PrimaryfileBag> pbags = new ArrayList<PrimaryfileBag>();
		ibag.setPrimaryfileBags(pbags);		
		Set<Primaryfile> primaryfiles = item.getPrimaryfiles();
		
		for (Primaryfile primaryfile : primaryfiles) {
			PrimaryfileBag pbag = getPrimaryfileBag(primaryfile.getId());
			pbags.add(pbag);
		}
		
		log.info("Successfully retrieved ItemBag for itemId " + itemId + " with " + pbags.size() + " PrimaryfileBags.");
		return ibag;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.BagService.getItemBag(Long)
	 */
	public ItemBag getItemBag(Long itemId) {			
		Item item = itemRepository.findById(itemId).orElseThrow(() -> new StorageException("item <" + itemId + "> does not exist!"));    
		return getItemBag(item);
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.BagService.getItemBag(String, String)
	 */
	public ItemBag getItemBag(String externalSource, String externalId) {
		List<Item> items = itemRepository.findByExternalSourceAndExternalId(externalSource, externalId);    		
		if (items.size() > 1 ) {
			throw new StorageException("More than one item is found for external source-id " + externalSource + "-" + externalId);
		}
		
		Item item = items.get(0);
		return getItemBag(item);
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.BagService.getCollectionBag(Collection)
	 */
	public CollectionBag getCollectionBag(Collection collection) {
		CollectionBag cbag = new CollectionBag();			
		
		Long collectionId = collection.getId();
		cbag.setCollectionId(collectionId);
		cbag.setCollectionName(collection.getName());
		cbag.setUnitName(collection.getUnit().getName());
		List<ItemBag> ibags = new ArrayList<ItemBag>();
		cbag.setItemBags(ibags);		
		Set<Item> items = collection.getItems();
		
		for (Item item : items) {
			ItemBag ibag = getItemBag(item.getId());
			ibags.add(ibag);
		}
		
		log.info("Successfully retrieved CollectionBag for collectionId " + collectionId + " with " + ibags.size() + " ItemBags.");
		return cbag;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.BagService.getCollectionBag(Long)
	 */
	public CollectionBag getCollectionBag(Long collectionId) {
		Collection collection = collectionRepository.findById(collectionId).orElseThrow(() -> new StorageException("collection <" + collectionId + "> does not exist!"));    		
		return getCollectionBag(collection);
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.BagService.getCollectionBag(String, String)
	 */
	public CollectionBag getCollectionBag(String unitName, String collectionName) {
		List<Collection> collections = collectionRepository.findByUnitNameAndName(unitName, collectionName);    		
		if (collections.size() > 1 ) {
			throw new StorageException("More than one collection is found for unitName-collectionName " + unitName + "-" + collectionName);
		}
		
		Collection collection = collections.get(0);
		return  getCollectionBag(collection);
	}
	
	
}
