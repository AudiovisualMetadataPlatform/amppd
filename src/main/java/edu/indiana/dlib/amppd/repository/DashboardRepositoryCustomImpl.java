package edu.indiana.dlib.amppd.repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import edu.indiana.dlib.amppd.model.DashboardResult;
import edu.indiana.dlib.amppd.web.DashboardFilterValues;
import edu.indiana.dlib.amppd.web.DashboardResponse;
import edu.indiana.dlib.amppd.web.DashboardSearchQuery;
import edu.indiana.dlib.amppd.web.DashboardSortRule;

public class DashboardRepositoryCustomImpl implements DashboardRepositoryCustom {
	@PersistenceContext
    EntityManager em;
	public DashboardResponse searchResults(DashboardSearchQuery searchQuery) {
		
        int count = getTotalCount(searchQuery);
        
        List<DashboardResult> rows = getDashboardRows(searchQuery);
        
        DashboardFilterValues filters = getFilterValues(searchQuery);
        
        
        // Format the response
        DashboardResponse response = new DashboardResponse();
        response.setRows(rows);
        response.setTotalResults(count);
        response.setFilters(filters);
        return response;
    }

	private List<DashboardResult> getDashboardRows(DashboardSearchQuery searchQuery){
		int firstResult = ((searchQuery.getPageNum() - 1) * searchQuery.getResultsPerPage()) + 1;
		
		
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
            Expression<String> sourceItem = root.get("submitter");
            Predicate submitterPred = sourceItem.in(searchQuery.getFilterBySubmitters());
            predicates.add(submitterPred);
        }
        return predicates;
	}
	private DashboardFilterValues getFilterValues(DashboardSearchQuery searchQuery) {

		DashboardFilterValues filters = new DashboardFilterValues();
		

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<DashboardResult> root = query.from(DashboardResult.class);
        

        // Setup predicates (where statements)
        List<Predicate> predicates = getPredicates(searchQuery, root, cb);
        

        if(!predicates.isEmpty()) {
        	Predicate[] preds = predicates.toArray(new Predicate[0]);
        	query.where(preds);
        }
        
        List<String> submitters = em.createQuery(query.select(root.get("submitter")).distinct(true)).getResultList();
        List<String> filenames = em.createQuery(query.select(root.get("sourceFilename")).distinct(true)).getResultList();
        List<String> items = em.createQuery(query.select(root.get("sourceItem")).distinct(true)).getResultList();

        List<String> searchTerms= union(filenames, items);
        
        filters.setSearchTerms(searchTerms);
        filters.setSubmitters(submitters);
        
        return filters;
        
	}
	private <T> List<T> union(List<T> list1, List<T> list2) {
        Set<T> set = new HashSet<T>();

        set.addAll(list1);
        set.addAll(list2);

        return new ArrayList<T>(set);
    }
}
