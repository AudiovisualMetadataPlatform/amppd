package edu.indiana.dlib.amppd.repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import edu.indiana.dlib.amppd.model.WorkflowResult;
import edu.indiana.dlib.amppd.web.GalaxyJobState;
import edu.indiana.dlib.amppd.web.WorkflowResultFilterCollection;
import edu.indiana.dlib.amppd.web.WorkflowResultFilterFile;
import edu.indiana.dlib.amppd.web.WorkflowResultFilterItem;
import edu.indiana.dlib.amppd.web.WorkflowResultFilterUnit;
import edu.indiana.dlib.amppd.web.WorkflowResultFilterValues;
import edu.indiana.dlib.amppd.web.WorkflowResultResponse;
import edu.indiana.dlib.amppd.web.WorkflowResultSearchQuery;
import edu.indiana.dlib.amppd.web.WorkflowResultSortRule;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WorkflowResultRepositoryCustomImpl implements WorkflowResultRepositoryCustom {
	public static String DATE_PROPERTY = "dateCreated";
	
	@PersistenceContext
    EntityManager em;
	
	public WorkflowResultResponse findByQuery(WorkflowResultSearchQuery wrsq) {		
        int count = getTotalCount(wrsq);        
        List<WorkflowResult> rows = getWorkflowResultRows(wrsq);       
        WorkflowResultFilterValues filters = getFilterValues(wrsq);
        
        // Format the response
        WorkflowResultResponse response = new WorkflowResultResponse();
        response.setRows(rows);
        response.setTotalResults(count);
        response.setFilters(filters);
        
        // TODO: 
        // To reduce query overhead, instead of updating all value sets for all filters with each query, we can 
        // add separate API to return value set for a particular filter, so frontend can call the API upon accessing a filter.

        return response;
    }

	private List<WorkflowResult> getWorkflowResultRows(WorkflowResultSearchQuery wrsq){
		int firstResult = ((wrsq.getPageNum() - 1) * wrsq.getResultsPerPage());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<WorkflowResult> cq = cb.createQuery(WorkflowResult.class);
        Root<WorkflowResult> root = cq.from(WorkflowResult.class);

        // add predicates (where statements)
        addPredicates(wrsq, cb, cq, root);
        
        WorkflowResultSortRule sort = wrsq.getSortRule();
        if(sort!=null && !sort.getColumnName().isEmpty()) {
        	if(sort.getColumnName().equals("outputName")) {
    			List<Order> orderList = new ArrayList<Order>();
        		if(sort.isOrderByDescending()) {        			
        			orderList.add(cb.desc(root.get("outputName")));
        			orderList.add(cb.asc(root.get("workflowStep")));
            		orderList.add(cb.asc(root.get("workflowName")));
        			orderList.add(cb.desc(root.get(DATE_PROPERTY)));
        		}
            	else {
            		orderList.add(cb.asc(root.get("outputName")));
            		orderList.add(cb.asc(root.get("workflowStep")));
            		orderList.add(cb.asc(root.get("workflowName")));
            		orderList.add(cb.desc(root.get(DATE_PROPERTY)));
            	}
        		cq.orderBy(orderList);
        	}
        	else if(sort.isOrderByDescending()) {
                cq.orderBy(cb.desc(root.get(sort.getColumnName())));
        	}
        	else {
                cq.orderBy(cb.asc(root.get(sort.getColumnName())));
        	}
        }

        // Get the actual rows
        TypedQuery<WorkflowResult> query = em.createQuery(cq);
        log.trace("=======>>>>QUERY IS:"+query.unwrap(org.hibernate.Query.class).getQueryString()  );
        query.setFirstResult(firstResult);
        query.setMaxResults(wrsq.getResultsPerPage());
        
        return query.getResultList();
	}
	
	private int getTotalCount(WorkflowResultSearchQuery wrsq) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<WorkflowResult> root = cq.from(WorkflowResult.class);
        cq.select(cb.count(root));

        // add predicates (where statements)
        addPredicates(wrsq, cb, cq, root);
        
        Long count = em.createQuery(cq).getSingleResult();        
        return count.intValue();
	}
	
	private WorkflowResultFilterValues getFilterValues(WorkflowResultSearchQuery wrsq) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        
        // Note: Filters are context-dependent, 
        // i.e. each filter's possible value set is dependent on other currently selected filters.
        // Thus, when querying possible value set for a particular filter, 
        // we need to exclude all of its own current search values from the query predicates.
        // TODO: Avoid calling addPredicates for each filter, If there is an easy way to remove predicates for that filter. 

        // unit filtter
        CriteriaQuery<WorkflowResultFilterUnit> cqUnit = cb.createQuery(WorkflowResultFilterUnit.class);
        Root<WorkflowResult> rtUnit = cqUnit.from(WorkflowResult.class);
        Long[] fbUnits = wrsq.getFilterByUnits();
        if (fbUnits.length > 0) {
            wrsq.setFilterByUnits(new Long[0]);
            addPredicates(wrsq, cb, cqUnit, rtUnit); 
        	wrsq.setFilterByUnits(fbUnits);
        }
        else {
        	addPredicates(wrsq, cb, cqUnit, rtUnit); 
        }
        List<WorkflowResultFilterUnit> units = em.createQuery(cqUnit.multiselect(
        		rtUnit.get("unitId"),
        		rtUnit.get("unitName")
        		).distinct(true)).getResultList();
        
        // collection filtter
        CriteriaQuery<WorkflowResultFilterCollection> cqCollection = cb.createQuery(WorkflowResultFilterCollection.class);
        Root<WorkflowResult> rtCollection = cqCollection.from(WorkflowResult.class);
        Long[] fbCollections = wrsq.getFilterByCollections();
        if (fbCollections.length > 0) {
            wrsq.setFilterByCollections(new Long[0]);
            addPredicates(wrsq, cb, cqCollection, rtCollection); 
        	wrsq.setFilterByCollections(fbCollections);
        }
        else {
        	addPredicates(wrsq, cb, cqCollection, rtCollection); 
        }
        List<WorkflowResultFilterCollection> collections = em.createQuery(cqCollection.multiselect(
        		rtCollection.get("unitId"),
        		rtCollection.get("unitName"),
        		rtCollection.get("collectionId"),
        		rtCollection.get("collectionName")
        		).distinct(true)).getResultList();        

        // item filtter
        CriteriaQuery<WorkflowResultFilterItem> cqItem = cb.createQuery(WorkflowResultFilterItem.class);
        Root<WorkflowResult> rtItem = cqItem.from(WorkflowResult.class);
        Long[] fbItems = wrsq.getFilterByItems();
        if (fbItems.length > 0) {
            wrsq.setFilterByItems(new Long[0]);
            addPredicates(wrsq, cb, cqItem, rtItem); 
        	wrsq.setFilterByItems(fbItems);
        }
        else {
        	addPredicates(wrsq, cb, cqItem, rtItem); 
        }
        List<WorkflowResultFilterItem> items = em.createQuery(cqItem.multiselect(
        		rtItem.get("unitId"),
        		rtItem.get("unitName"),
        		rtItem.get("collectionId"),
        		rtItem.get("collectionName"),
        		rtItem.get("itemId"),
        		rtItem.get("itemName"),
        		rtItem.get("externalId"),
        		rtItem.get("externalSource")
        		).distinct(true)).getResultList();
        
        // primaryfile filtter
        CriteriaQuery<WorkflowResultFilterFile> cqFile = cb.createQuery(WorkflowResultFilterFile.class);
        Root<WorkflowResult> rtFile = cqFile.from(WorkflowResult.class);
        Long[] fbFiles = wrsq.getFilterByFiles();
        if (fbFiles.length > 0) {
            wrsq.setFilterByFiles(new Long[0]);
            addPredicates(wrsq, cb, cqFile, rtFile); 
        	wrsq.setFilterByFiles(fbFiles);
        }
        else {
        	addPredicates(wrsq, cb, cqFile, rtFile); 
        }
        List<WorkflowResultFilterFile> files = em.createQuery(cqFile.multiselect(
        		rtFile.get("unitId"),
        		rtFile.get("unitName"),
        		rtFile.get("collectionId"),
        		rtFile.get("collectionName"),
        		rtFile.get("itemId"),
        		rtFile.get("itemName"),
        		rtFile.get("externalId"),
        		rtFile.get("externalSource"),
        		rtFile.get("primaryfileId"),
        		rtFile.get("primaryfileName")
        		).distinct(true)).getResultList();
        
        // create-date filter
        CriteriaQuery<Date> cqDate = cb.createQuery(Date.class);
        Root<WorkflowResult> rtDate = cqDate.from(WorkflowResult.class);
        List <Date> fbDates = wrsq.getFilterByDates();
        if (!fbDates.isEmpty()) {
            wrsq.setFilterByDates(new ArrayList<Date>());
            addPredicates(wrsq, cb, cqDate, rtDate); 
        	wrsq.setFilterByDates(fbDates);
        }
        else {
        	addPredicates(wrsq, cb, cqDate, rtDate); 
        }
        List<Date> dates = em.createQuery(cqDate.select(rtDate.get(DATE_PROPERTY))).getResultList();
        // TODO: 
        // It would be more useful to return the min/max dates.
        // so the frontend can use the date range to allow selection. 
    	
        // job status filter
        CriteriaQuery<GalaxyJobState> cqStatus = cb.createQuery(GalaxyJobState.class);
        Root<WorkflowResult> rtStatus = cqStatus.from(WorkflowResult.class);
        GalaxyJobState[] fbStatuses = wrsq.getFilterByStatuses();
        if (fbStatuses.length > 0) {
            wrsq.setFilterByStatuses(new GalaxyJobState[0]);
            addPredicates(wrsq, cb, cqStatus, rtStatus); 
        	wrsq.setFilterByStatuses(fbStatuses);
        }
        else {
        	addPredicates(wrsq, cb, cqStatus, rtStatus); 
        }
        List<GalaxyJobState> statuses = em.createQuery(cqStatus.select(rtStatus.get("status")).distinct(true)).getResultList();

        // String type filters
        List<String> submitters = getFilterValues("submitter", wrsq, cb);
        List<String> externalIds = getFilterValues("externalId", wrsq, cb); 
        List<String> workflows = getFilterValues("workflowName", wrsq, cb);
        List<String> steps = getFilterValues("workflowStep", wrsq, cb);
        List<String> outputs = getFilterValues("outputName", wrsq, cb);
        
        // search terms
        List<String> searchTerms = unionTerms(collections, items, files);

		WorkflowResultFilterValues filters = new WorkflowResultFilterValues();		
        filters.setDateFilter(dates);
        filters.setSubmitters(submitters);
        filters.setUnits(units);
        filters.setCollections(collections);
        filters.setItems(items);
        filters.setFiles(files);
        filters.setExternalIds(externalIds);
        filters.setWorkflows(workflows);
        filters.setSteps(steps);
        filters.setOutputs(outputs);
        filters.setStatuses(statuses);
        filters.setSearchTerms(searchTerms);        
        return filters;        
	}
	
	/**
	 * Get the allowed filter value set for the specified field of String type.
	 */
	private List<String> getFilterValues(String field, WorkflowResultSearchQuery wrsq, CriteriaBuilder cb) {
        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<WorkflowResult> rt = cq.from(WorkflowResult.class);
        String[] fbs = wrsq.getFilterBy(field);
        
        if (fbs.length > 0) {
            wrsq.setFilterBy(field, new String[0]);
            addPredicates(wrsq, cb, cq, rt); 
        	wrsq.setFilterBy(field, fbs);
        }
        else {
        	addPredicates(wrsq, cb, cq, rt); 
        }
        
        List<String> values = em.createQuery(cq.select(rt.get(field)).distinct(true)).getResultList();
        return values;		
	}
	
	/**
	 * Add predicates for the specified CriteriaQuery based on the specified wrsq.
	 */
	private Predicate[] addPredicates(WorkflowResultSearchQuery wrsq, CriteriaBuilder cb, CriteriaQuery<?> cq, Root<WorkflowResult> root) {
		List<Predicate> predicates = new ArrayList<Predicate>();
		
        // Build the predicate for search terms
		if(wrsq.getFilterBySearchTerms().length>0) {        	
            Predicate searchTermPredicate = null;
            for (String term : wrsq.getFilterBySearchTerms()) {
                searchTermPredicate = getPartialSearchTermPredicate(term, root, cb);
                predicates.add(searchTermPredicate);
            }
        }
		
		if(wrsq.getFilterByDates().size()>0) { 
			Predicate fromDate = cb.greaterThanOrEqualTo(root.get(DATE_PROPERTY).as(java.util.Date.class),wrsq.getFilterByDates().get(0)); 
			Predicate toDate = cb.lessThanOrEqualTo(root.get(DATE_PROPERTY).as(java.util.Date.class), wrsq.getFilterByDates().get(1)); 
			Predicate datePredicate = cb.and(fromDate, toDate);
            predicates.add(datePredicate);
		}
        
        if(wrsq.getFilterBySubmitters().length>0) {
            Path<String> path = root.get("submitter");
            Predicate predicate = path.in((Object[])wrsq.getFilterBySubmitters());
            predicates.add(predicate);
        }
        
        if(wrsq.getFilterByUnits().length>0) {
            Path<String> path = root.get("unitId");
            Predicate predicate = path.in((Object[])wrsq.getFilterByUnits());
            predicates.add(predicate);
        }
        
        if(wrsq.getFilterByCollections().length>0) {
        	Path<String> path = root.get("collectionId");
            Predicate predicate = path.in((Object[])wrsq.getFilterByCollections());
            predicates.add(predicate);
        }

        if(wrsq.getFilterByItems().length>0) {
        	Path<String> path = root.get("itemId");
            Predicate predicate = path.in((Object[])wrsq.getFilterByItems());
            predicates.add(predicate);
        }
        
        if(wrsq.getFilterByFiles().length>0) {
        	Path<String> path = root.get("primaryfileId");
            Predicate predicate = path.in((Object[])wrsq.getFilterByFiles());
            predicates.add(predicate);
        }
        
        if(wrsq.getFilterByExternalIds().length>0) {
        	Path<String> path = root.get("externalId");
        	Predicate predicate = path.in((Object[])wrsq.getFilterByExternalIds());
        	predicates.add(predicate);
        }
        
        if(wrsq.getFilterByWorkflows().length>0) {
        	Path<String> path = root.get("workflowName");
            Predicate predicate = path.in((Object[])wrsq.getFilterByWorkflows());
            predicates.add(predicate);
        }
        
        if(wrsq.getFilterBySteps().length>0) {
        	Path<String> path = root.get("workflowStep");
            Predicate predicate = path.in((Object[])wrsq.getFilterBySteps());
            predicates.add(predicate);
        }
        
        if(wrsq.getFilterByOutputs().length>0) {
        	Path<String> path = root.get("outputName");
            Predicate predicate = path.in((Object[])wrsq.getFilterByOutputs());
            predicates.add(predicate);
        }
        
        if(wrsq.getFilterByStatuses().length>0) {
        	Path<String> path = root.get("status");
            Predicate predicate = path.in((Object[])wrsq.getFilterByStatuses());
            predicates.add(predicate);
        }

        if(wrsq.isFilterByRelevant()) {
        	Predicate predicate = cb.equal(root.get("relevant"), true);
            predicates.add(predicate);
        }

        if(wrsq.isFilterByFinal()) {
        	Predicate predicate = cb.equal(root.get("isFinal"), true);
            predicates.add(predicate);
        }

        Predicate[] preds = new Predicate[0];
        if (!predicates.isEmpty()) {
        	preds = predicates.toArray(preds);
        	cq.where(preds);
        }
    	return preds;
	}
		
    private Predicate getPartialSearchTermPredicate(String s, Root<WorkflowResult> root, CriteriaBuilder cb) {
        String searchTerm = "%"+s.toLowerCase()+"%";
        Predicate collectionPredicate = cb.like(cb.lower(root.get("collectionName")), searchTerm);
        Predicate itemPredicate = cb.like(cb.lower(root.get("itemName")), searchTerm);
        Predicate fileName = cb.like(cb.lower(root.get("primaryfileName")), searchTerm);
        Predicate externalSource = cb.like(cb.lower(root.get("externalSource")), searchTerm);
        Predicate finalPredicate = cb.or(collectionPredicate, itemPredicate, fileName, externalSource);
        return finalPredicate;
    }
	
	private List<String> unionTerms(List<WorkflowResultFilterCollection> collections, List<WorkflowResultFilterItem> items, List<WorkflowResultFilterFile> files) {
        Set<String> terms = new HashSet<String>();
        for (WorkflowResultFilterCollection collection : collections) {
        	terms.add(collection.getCollectionName());
        }
        for (WorkflowResultFilterItem item : items) {
        	terms.add(item.getItemName());
        }
        for (WorkflowResultFilterFile file : files) {
        	terms.add(file.getPrimaryfileName());
        }
        return new ArrayList<String>(terms);
    }
}
