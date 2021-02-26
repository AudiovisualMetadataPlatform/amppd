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
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import edu.indiana.dlib.amppd.model.WorkflowResult;
import edu.indiana.dlib.amppd.web.GalaxyJobState;
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
	public WorkflowResultResponse findByQuery(WorkflowResultSearchQuery searchQuery) {		
        int count = getTotalCount(searchQuery);        
        List<WorkflowResult> rows = getWorkflowResultRows(searchQuery);       
        WorkflowResultFilterValues filters = getFilterValues();
        
        // Format the response
        WorkflowResultResponse response = new WorkflowResultResponse();
        response.setRows(rows);
        response.setTotalResults(count);
        // TODO we don't need to update filters with each query; we should update filters each time the WorkflowResult table gets updated
        response.setFilters(filters);
        return response;
    }

	private List<WorkflowResult> getWorkflowResultRows(WorkflowResultSearchQuery searchQuery){
		int firstResult = ((searchQuery.getPageNum() - 1) * searchQuery.getResultsPerPage());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<WorkflowResult> cq = cb.createQuery(WorkflowResult.class);
        Root<WorkflowResult> root = cq.from(WorkflowResult.class);

        // Setup predicates (where statements)
        List<Predicate> predicates = getPredicates(searchQuery, root, cb);
                
        if(!predicates.isEmpty()) {
        	Predicate[] preds = predicates.toArray(new Predicate[0]);
            cq.where(preds);
        }
        WorkflowResultSortRule sort = searchQuery.getSortRule();
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
        query.setMaxResults(searchQuery.getResultsPerPage());
        
        return query.getResultList();
	}
	
	private int getTotalCount(WorkflowResultSearchQuery searchQuery) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<WorkflowResult> root = countQuery.from(WorkflowResult.class);
        countQuery.select(cb.count(root));

        // Setup predicates (where statements)
        List<Predicate> predicates = getPredicates(searchQuery, root, cb);
        
        if(!predicates.isEmpty()) {
        	Predicate[] preds = predicates.toArray(new Predicate[0]);
            countQuery.where(preds);
        }
        
        Long count = em.createQuery(countQuery)
      		  .getSingleResult();
        
        return count.intValue();
	}
	
	private List<Predicate> getPredicates(WorkflowResultSearchQuery searchQuery, Root<WorkflowResult> root, CriteriaBuilder cb) {
		List<Predicate> predicates = new ArrayList<Predicate>();
		
        // Build the predicate for search terms
		if(searchQuery.getFilterBySearchTerm().length>0) {        	
        	In<String> inClause = cb.in(root.get("itemName"));
        	In<String> inClause2 = cb.in(root.get("primaryfileName"));
        	
        	for (String term : searchQuery.getFilterBySearchTerm()) {
        	    inClause.value(term);
        		inClause2.value(term);
        	}            

            // Combine the two predicates to get an "OR"
            Predicate sourcePredicate = cb.or(inClause2, inClause);
            predicates.add(sourcePredicate);
        }
		
        // Build the predicate for Date filter
		if(searchQuery.getFilterByDates().size()>0) { 
			Predicate fromDate = cb.greaterThanOrEqualTo(root.get(DATE_PROPERTY).as(java.util.Date.class),searchQuery.getFilterByDates().get(0)); 
			Predicate toDate = cb.lessThanOrEqualTo(root.get(DATE_PROPERTY).as(java.util.Date.class), searchQuery.getFilterByDates().get(1)); 
			Predicate datePredicate = cb.and(fromDate, toDate);
            predicates.add(datePredicate);
		}
        
        if(searchQuery.getFilterBySubmitters().length>0) {
            Path<String> path = root.get("submitter");
            Predicate predicate = path.in((Object[])searchQuery.getFilterBySubmitters());
            predicates.add(predicate);
        }
        
        if(searchQuery.getFilterByCollections().length>0) {
        	Path<String> path = root.get("collectionName");
            Predicate predicate = path.in((Object[])searchQuery.getFilterByCollections());
            predicates.add(predicate);
        }
        
        if(searchQuery.getFilterByItems().length>0) {
        	Path<String> path = root.get("itemName");
            Predicate predicate = path.in((Object[])searchQuery.getFilterByItems());
            predicates.add(predicate);
        }
        
        if(searchQuery.getFilterByFiles().length>0) {
        	Path<String> path = root.get("primaryfileName");
            Predicate predicate = path.in((Object[])searchQuery.getFilterByFiles());
            predicates.add(predicate);
        }
        
        if(searchQuery.getFilterByWorkflows().length>0) {
        	Path<String> path = root.get("workflowName");
            Predicate predicate = path.in((Object[])searchQuery.getFilterByWorkflows());
            predicates.add(predicate);
        }
        
        if(searchQuery.getFilterBySteps().length>0) {
        	Path<String> path = root.get("workflowStep");
            Predicate predicate = path.in((Object[])searchQuery.getFilterBySteps());
            predicates.add(predicate);
        }
        
        if(searchQuery.getFilterByStatuses().length>0) {
        	Path<String> path = root.get("status");
            Predicate predicate = path.in((Object[])searchQuery.getFilterByStatuses());
            predicates.add(predicate);
        }

        if(searchQuery.isFilterByRelevant()) {
        	Predicate predicate = cb.equal(root.get("relevant"), true);
            predicates.add(predicate);
        }

        if(searchQuery.isFilterByFinal()) {
        	Predicate predicate = cb.equal(root.get("isFinal"), true);
            predicates.add(predicate);
        }

        return predicates;
	}
	
	private WorkflowResultFilterValues getFilterValues() {
		WorkflowResultFilterValues filters = new WorkflowResultFilterValues();		
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        CriteriaQuery<Date> queryDate = cb.createQuery(Date.class);
        Root<WorkflowResult> root = query.from(WorkflowResult.class);
        Root<WorkflowResult> rootDateCriteria = queryDate.from(WorkflowResult.class);
        CriteriaQuery<GalaxyJobState> queryGjs = cb.createQuery(GalaxyJobState.class);
        Root<WorkflowResult> rootGjs = queryGjs.from(WorkflowResult.class);
        
        // We treat each filter independently, i.e. its possible value set is not dependent on current selected values in other filters;
        // rather, we populate each filter with distinct values existing in the current Workflow table.
        // Making filter value set context-dependent will result in deadlock queries. 

        List<Date> dateFilters = em.createQuery(queryDate.select(rootDateCriteria.get(DATE_PROPERTY).as(java.sql.Date.class))).getResultList();
        List<String> submitters = em.createQuery(query.select(root.get("submitter")).distinct(true)).getResultList();
        List<String> collections = em.createQuery(query.select(root.get("collectionName")).distinct(true)).getResultList();
        List<String> items = em.createQuery(query.select(root.get("itemName")).distinct(true)).getResultList();
        List<String> files = em.createQuery(query.select(root.get("primaryfileName")).distinct(true)).getResultList();
        List<String> workflows = em.createQuery(query.select(root.get("workflowName")).distinct(true)).getResultList();
        List<String> steps = em.createQuery(query.select(root.get("workflowStep")).distinct(true)).getResultList();
        List<GalaxyJobState> statuses = em.createQuery(queryGjs.select(rootGjs.get("status")).distinct(true)).getResultList();
        List<String> searchTerms = union(files, items);
        
        filters.setDateFilter(dateFilters);
        filters.setSubmitters(submitters);
        filters.setCollections(collections);
        filters.setItems(items);
        filters.setFiles(files);
        filters.setWorkflows(workflows);
        filters.setSteps(steps);
        filters.setStatuses(statuses);
        filters.setSearchTerms(searchTerms);
        
        return filters;
        
	}
	private <T> List<T> union(List<T> list1, List<T> list2) {
        Set<T> set = new HashSet<T>();
        set.addAll(list1);
        set.addAll(list2);
        return new ArrayList<T>(set);
    }
}
