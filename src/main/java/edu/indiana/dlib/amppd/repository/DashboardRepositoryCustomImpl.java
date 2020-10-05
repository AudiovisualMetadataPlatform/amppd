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
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import edu.indiana.dlib.amppd.model.DashboardResult;
import edu.indiana.dlib.amppd.web.DashboardFilterValues;
import edu.indiana.dlib.amppd.web.DashboardResponse;
import edu.indiana.dlib.amppd.web.DashboardSearchQuery;
import edu.indiana.dlib.amppd.web.DashboardSortRule;

import lombok.extern.slf4j.Slf4j;


import edu.indiana.dlib.amppd.web.GalaxyJobState;

@Slf4j
public class DashboardRepositoryCustomImpl implements DashboardRepositoryCustom {
	@PersistenceContext
    EntityManager em;
	public DashboardResponse searchResults(DashboardSearchQuery searchQuery) {
		
        int count = getTotalCount(searchQuery);
        
        List<DashboardResult> rows = getDashboardRows(searchQuery);

        
        DashboardFilterValues filters = getFilterValues();

        
        
        // Format the response
        DashboardResponse response = new DashboardResponse();
        response.setRows(rows);
        response.setTotalResults(count);
        // TODO we don't need to update filters with each query; we should update filters each time the DashboardResult table gets updated
        response.setFilters(filters);
        return response;
    }

	private List<DashboardResult> getDashboardRows(DashboardSearchQuery searchQuery){
		int firstResult = ((searchQuery.getPageNum() - 1) * searchQuery.getResultsPerPage());
		
		
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<DashboardResult> cq = cb.createQuery(DashboardResult.class);
        Root<DashboardResult> root = cq.from(DashboardResult.class);

        // Setup predicates (where statements)
        List<Predicate> predicates = getPredicates(searchQuery, root, cb);
        

        if(!predicates.isEmpty()) {
        	Predicate[] preds = predicates.toArray(new Predicate[0]);
            cq.where(preds);
        }
        DashboardSortRule sort = searchQuery.getSortRule();
        if(sort!=null && !sort.getColumnName().isEmpty()) {
        	if(sort.isOrderByDescending()) {
                cq.orderBy(cb.desc(root.get(sort.getColumnName())));
        	}
        	else {
                cq.orderBy(cb.asc(root.get(sort.getColumnName())));
        	}
        }

        // Get the actual rows
        TypedQuery<DashboardResult> query = em.createQuery(cq);
        log.info("=======>>>>QUERY IS:"+query.unwrap(org.hibernate.Query.class).getQueryString()  );
        query.setFirstResult(firstResult);
        query.setMaxResults(searchQuery.getResultsPerPage());
        
        return query.getResultList();
	}
	
	private int getTotalCount(DashboardSearchQuery searchQuery) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<DashboardResult> root = countQuery.from(DashboardResult.class);
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
	
	private List<Predicate> getPredicates(DashboardSearchQuery searchQuery, Root<DashboardResult> root, CriteriaBuilder cb) {
		List<Predicate> predicates = new ArrayList<Predicate>();
		
		if(searchQuery.getFilterBySearchTerm().length>0) {        	
        	In<String> inClause = cb.in(root.get("sourceItem"));
        	In<String> inClause2 = cb.in(root.get("sourceFilename"));
        	
        	for (String term : searchQuery.getFilterBySearchTerm()) {
        	    inClause.value(term);
        		inClause2.value(term);
        	}            

            // Combine the two predicates to get an "OR"
            Predicate sourcePredicate = cb.or(inClause2, inClause);
            predicates.add(sourcePredicate);
        }
		
        if(searchQuery.getFilterBySubmitters().length>0) {
            Path<String> path = root.get("submitter");
            Predicate predicate = path.in((Object[])searchQuery.getFilterBySubmitters());
            predicates.add(predicate);
        }
        
        //Build the predicate for Date filter
		if(searchQuery.getFilterByDates().size()>0) { 
			Predicate fromDate = cb.greaterThanOrEqualTo(root.get("date").as(java.util.Date.class),searchQuery.getFilterByDates().get(0)); 
			Predicate toDate = cb.lessThanOrEqualTo(root.get("date").as(java.util.Date.class), searchQuery.getFilterByDates().get(1)); 
			Predicate datePredicate = cb.and(fromDate, toDate);
            predicates.add(datePredicate);
		}
        
        if(searchQuery.getFilterByWorkflows().length>0) {
        	Path<String> path = root.get("workflowName");
            Predicate predicate = path.in((Object[])searchQuery.getFilterByWorkflows());
            predicates.add(predicate);
        }
        
        if(searchQuery.getFilterByItems().length>0) {
        	Path<String> path = root.get("sourceItem");
            Predicate predicate = path.in((Object[])searchQuery.getFilterByItems());
            predicates.add(predicate);
        }
        
        if(searchQuery.getFilterByFiles().length>0) {
        	Path<String> path = root.get("sourceFilename");
            Predicate predicate = path.in((Object[])searchQuery.getFilterByFiles());
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

        if(searchQuery.isFilterByFinal()) {
        	Predicate predicate = cb.equal(root.get("isFinal"), true);
            predicates.add(predicate);
        }
        return predicates;
	}
	
	private DashboardFilterValues getFilterValues() {

		DashboardFilterValues filters = new DashboardFilterValues();		

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        CriteriaQuery<Date> queryDate = cb.createQuery(Date.class);
        Root<DashboardResult> root = query.from(DashboardResult.class);
        Root<DashboardResult> rootDateCriteria = queryDate.from(DashboardResult.class);
        CriteriaQuery<GalaxyJobState> queryGjs = cb.createQuery(GalaxyJobState.class);
        Root<DashboardResult> rootGjs = queryGjs.from(DashboardResult.class);
        
        // We treat each filter independently, i.e. its possible value set is not dependent on current selected values in other filters;
        // rather, we populate each filter with distinct values existing in the current dashboard table.
        // Making filter value set context-dependent will result in deadlock queries. 

        List<String> submitters = em.createQuery(query.select(root.get("submitter")).distinct(true)).getResultList();
        List<String> filenames = em.createQuery(query.select(root.get("sourceFilename")).distinct(true)).getResultList();
        List<String> items = em.createQuery(query.select(root.get("sourceItem")).distinct(true)).getResultList();
        List<Date> dateFilters = em.createQuery(queryDate.select(rootDateCriteria.get("date").as(java.sql.Date.class))).getResultList();
        List<String> workflows = em.createQuery(query.select(root.get("workflowName")).distinct(true)).getResultList();
        List<String> steps = em.createQuery(query.select(root.get("workflowStep")).distinct(true)).getResultList();
        List<GalaxyJobState> statuses = em.createQuery(queryGjs.select(rootGjs.get("status")).distinct(true)).getResultList();
        List<String> searchTerms= union(filenames, items);
        
        filters.setSearchTerms(searchTerms);
        filters.setSubmitters(submitters);
        filters.setDateFilter(dateFilters);
        filters.setWorkflows(workflows);
        filters.setSteps(steps);
        filters.setItems(items);
        filters.setFiles(filenames);
        filters.setStatuses(statuses);
        
        return filters;
        
	}
	private <T> List<T> union(List<T> list1, List<T> list2) {
        Set<T> set = new HashSet<T>();

        set.addAll(list1);
        set.addAll(list2);

        return new ArrayList<T>(set);
    }
}
