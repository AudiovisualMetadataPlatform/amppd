package edu.indiana.dlib.amppd.repository;

import edu.indiana.dlib.amppd.model.MgmEvaluationTest;
import edu.indiana.dlib.amppd.web.*;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.*;

@Slf4j
public class MgmEvaluationTestRepositoryCustomImpl implements MgmEvaluationTestRepositoryCustom {
    @PersistenceContext
    EntityManager em;

    public static String DATE_PROPERTY = "dateCreated";

    public MgmEvaluationTestResponse findByQuery(MgmEvaluationSearchQuery mesq){
        MgmEvaluationTestResponse response = new MgmEvaluationTestResponse();
        MgmEvaluationTestFilters filters = getFilterValues(mesq);
        response.setFilters(filters);
        int count = getTotalCount(mesq);
        List<MgmEvaluationTest> rows = getMgmEvaluationTestRows(mesq);
        response.setTotalResults(count);
        response.setRows(prepareRows(rows));
        return response;
    }

    private List<MgmEvaluationTest> getMgmEvaluationTestRows(MgmEvaluationSearchQuery mesq){
        int firstResult = ((mesq.getPageNum() - 1) * mesq.getResultsPerPage());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MgmEvaluationTest> cq = cb.createQuery(MgmEvaluationTest.class);
        Root<MgmEvaluationTest> root = cq.from(MgmEvaluationTest.class);

        // add predicates (where statements)
        addPredicates(mesq, cb, cq, root);

        WorkflowResultSortRule sort = mesq.getSortRule();
        if(sort!=null && !sort.getColumnName().isEmpty()) {
            if(sort.isOrderByDescending()) {
                cq.orderBy(cb.desc(root.get(sort.getColumnName())));
            }
            else {
                cq.orderBy(cb.asc(root.get(sort.getColumnName())));
            }
        }

        // Get the actual rows
        TypedQuery<MgmEvaluationTest> query = em.createQuery(cq);
        log.trace("=======>>>>QUERY IS:"+query.unwrap(org.hibernate.Query.class).getQueryString()  );
        query.setFirstResult(firstResult);
        query.setMaxResults(mesq.getResultsPerPage());

        return query.getResultList();
    }

    private int getTotalCount(MgmEvaluationSearchQuery mesq) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<MgmEvaluationTest> root = cq.from(MgmEvaluationTest.class);
        cq.select(cb.count(root));

        // add predicates (where statements)
        addPredicates(mesq, cb, cq, root);

        Long count = em.createQuery(cq).getSingleResult();
        return count.intValue();
    }

    private Predicate getPartialSearchTermPredicate(String s, Root<MgmEvaluationTest> root, CriteriaBuilder cb) {
        String searchTerm = "%"+s.toLowerCase()+"%";
        Predicate collectionPredicate = cb.like(cb.lower(root.get("workflowResult").get("collectionName")), searchTerm);
        Predicate itemPredicate = cb.like(cb.lower(root.get("workflowResult").get("itemName")), searchTerm);
        Predicate fileName = cb.like(cb.lower(root.get("workflowResult").get("primaryfileName")), searchTerm);
        Predicate externalSource = cb.like(cb.lower(root.get("workflowResult").get("externalSource")), searchTerm);
        Predicate finalPredicate = cb.or(collectionPredicate, itemPredicate, fileName, externalSource);
        return finalPredicate;
    }

    private Predicate[] addPredicates(MgmEvaluationSearchQuery mesq, CriteriaBuilder cb, CriteriaQuery<?> cq, Root<MgmEvaluationTest> root) {
        List<Predicate> predicates = new ArrayList<Predicate>();
        if(mesq.getFilterByCategories().length>0) {
            Path<String> path = root.get("category");
            Predicate predicate = path.in((Object[])mesq.getFilterByCategories());
            predicates.add(predicate);
        }

        if(mesq.getFilterByMstTools().length>0) {
            Path<String> path = root.get("mst");
            Predicate predicate = path.in((Object[])mesq.getFilterByMstTools());
            predicates.add(predicate);
        }

        // Build the predicate for search terms
        if(mesq.getFilterBySearchTerms().length>0) {
            Predicate searchTermPredicate = null;
            for (String term : mesq.getFilterBySearchTerms()) {
                searchTermPredicate = getPartialSearchTermPredicate(term, root, cb);
                predicates.add(searchTermPredicate);
            }
        }

        if(mesq.getFilterByDates().length>0) {
            Predicate fromDate = cb.greaterThanOrEqualTo(root.get("workflowResult").get(DATE_PROPERTY).as(java.util.Date.class),mesq.getFilterByDates()[0]);
            Predicate toDate = cb.lessThanOrEqualTo(root.get("workflowResult").get(DATE_PROPERTY).as(java.util.Date.class), mesq.getFilterByDates()[1]);
            Predicate predicate = cb.and(fromDate, toDate);
            predicates.add(predicate);
        }

        if(mesq.getFilterBySubmitters().length>0) {
            Path<String> path = root.get("submitter");
            Predicate predicate = path.in((Object[])mesq.getFilterBySubmitters());
            predicates.add(predicate);
        }

        if(mesq.getFilterByUnits().length>0) {
            Path<String> path = root.get("workflowResult").get("unitId");
            Predicate predicate = path.in((Object[])mesq.getFilterByUnits());
            predicates.add(predicate);
        }

        if(mesq.getFilterByCollections().length>0) {
            Path<String> path = root.get("workflowResult").get("collectionId");
            Predicate predicate = path.in((Object[])mesq.getFilterByCollections());
            predicates.add(predicate);
        }

        if(mesq.getFilterByItems().length>0) {
            Path<String> path = root.get("workflowResult").get("itemId");
            Predicate predicate = path.in((Object[])mesq.getFilterByItems());
            predicates.add(predicate);
        }

        if(mesq.getFilterByFiles().length>0) {
            Path<String> path = root.get("workflowResult").get("primaryfileId");
            Predicate predicate = path.in((Object[])mesq.getFilterByFiles());
            predicates.add(predicate);
        }

        if(mesq.getFilterByExternalIds().length>0) {
            Path<String> path = root.get("workflowResult").get("externalId");
            Predicate predicate = path.in((Object[])mesq.getFilterByExternalIds());
            predicates.add(predicate);
        }

        if(mesq.getFilterByWorkflows().length>0) {
            Path<String> path = root.get("workflowResult").get("workflowName");
            Predicate predicate = path.in((Object[])mesq.getFilterByWorkflows());
            predicates.add(predicate);
        }

        if(mesq.getFilterBySteps().length>0) {
            Path<String> path = root.get("workflowResult").get("workflowStep");
            Predicate predicate = path.in((Object[])mesq.getFilterBySteps());
            predicates.add(predicate);
        }

        if(mesq.getFilterByOutputs().length>0) {
            Path<String> path = root.get("workflowResult").get("outputName");
            Predicate predicate = path.in((Object[])mesq.getFilterByOutputs());
            predicates.add(predicate);
        }

        if(mesq.getFilterByTypes().length>0) {
            Path<String> path = root.get("workflowResult").get("outputType");
            Predicate predicate = path.in((Object[])mesq.getFilterByTypes());
            predicates.add(predicate);
        }

        if(mesq.getFilterByStatuses().length>0) {
            Path<String> path = root.get("workflowResult").get("status");
            Predicate predicate = path.in((Object[])mesq.getFilterByStatuses());
            predicates.add(predicate);
        }

        if(mesq.isFilterByRelevant()) {
            Predicate predicate = cb.equal(root.get("workflowResult").get("relevant"), true);
            predicates.add(predicate);
        }

        if(mesq.isFilterByFinal()) {
            Predicate predicate = cb.equal(root.get("workflowResult").get("isFinal"), true);
            predicates.add(predicate);
        }

        Predicate[] preds = new Predicate[0];
        if (!predicates.isEmpty()) {
            preds = predicates.toArray(preds);
            cq.where(preds);
        }
        return preds;
    }

    private List<MgmEvaluationTestResult> prepareRows(List<MgmEvaluationTest> rows){
        ArrayList<MgmEvaluationTestResult> datatable = new ArrayList<MgmEvaluationTestResult>();
        for(MgmEvaluationTest test: rows) {
            MgmEvaluationTestResult row = new MgmEvaluationTestResult();
            row.setId(test.getId());
            row.setTestDate(test.getDateSubmitted());
            row.setOutputDate(test.getWorkflowResult().getDateCreated());
            row.setSubmitter(test.getSubmitter());
            row.setUnitName(test.getWorkflowResult().getUnitName());
            row.setCollectionName(test.getWorkflowResult().getCollectionName());
            row.setExternalSource(test.getWorkflowResult().getExternalSource());
            row.setExternalId(test.getWorkflowResult().getExternalId());
            row.setItemName(test.getWorkflowResult().getItemName());
            row.setPrimaryfileName(test.getWorkflowResult().getPrimaryfileName());
            row.setWorkflowName(test.getWorkflowResult().getWorkflowName());
            row.setWorkflowStep(test.getWorkflowResult().getWorkflowStep());
            row.setOutputName(test.getWorkflowResult().getOutputName());
            row.setOutputLabel(test.getWorkflowResult().getOutputLabel());
            row.setGroundTruth(test.getGroundtruthSupplement().getName());
            row.setOutputTest("amp_evaluation");
            row.setWorkflowId(test.getWorkflowResult().getId().toString());
            datatable.add(row);
        }
        return datatable;
    }

    private List<String> getFilterValues(String field, MgmEvaluationSearchQuery wrsq, CriteriaBuilder cb, boolean is_workflow_search) {
        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<MgmEvaluationTest> rt = cq.from(MgmEvaluationTest.class);
        String[] fbs = wrsq.getFilterByField(field);

        if (fbs.length > 0) {
            wrsq.setFilterByField(field, new String[0]);
            addPredicates(wrsq, cb, cq, rt);
            wrsq.setFilterByField(field, fbs);
        }
        else {
            addPredicates(wrsq, cb, cq, rt);
        }

        List<String> values;
        if (is_workflow_search == true){
            cq.orderBy(cb.asc(rt.get("workflowResult").get(field)));
            values = em.createQuery(cq.select(rt.get("workflowResult").get(field)).distinct(true)).getResultList();

        } else {
            cq.orderBy(cb.asc(rt.get(field)));
            values = em.createQuery(cq.select(rt.get(field)).distinct(true)).getResultList();
        }
        return values;
    }

    private MgmEvaluationTestFilters getFilterValues(MgmEvaluationSearchQuery wrsq) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // orderBy list for entity filters
        List<Order> orders = new ArrayList<Order>();

        // unit filter
        CriteriaQuery<WorkflowResultFilterUnit> cqUnit = cb.createQuery(WorkflowResultFilterUnit.class);
        Root<MgmEvaluationTest> rtUnit = cqUnit.from(MgmEvaluationTest.class);
        Long[] fbUnits = wrsq.getFilterByUnits();
        if (fbUnits.length > 0) {
            wrsq.setFilterByUnits(new Long[0]);
            addPredicates(wrsq, cb, cqUnit, rtUnit);
            wrsq.setFilterByUnits(fbUnits);
        } else {
            addPredicates(wrsq, cb, cqUnit, rtUnit);
        }
        orders.add(cb.asc(rtUnit.get("workflowResult").get("unitName")));
        cqUnit.orderBy(orders);

        List<WorkflowResultFilterUnit> units = em.createQuery(cqUnit.multiselect(
                        rtUnit.get("workflowResult").get("unitId"),
                        rtUnit.get("workflowResult").get("unitName")
                ).distinct(true)).getResultList();


        // collection filter
        CriteriaQuery<WorkflowResultFilterCollection> cqCollection = cb.createQuery(WorkflowResultFilterCollection.class);
        Root<MgmEvaluationTest> rtCollection = cqCollection.from(MgmEvaluationTest.class);
        Long[] fbCollections = wrsq.getFilterByCollections();
        if (fbCollections.length > 0) {
            wrsq.setFilterByCollections(new Long[0]);
            addPredicates(wrsq, cb, cqCollection, rtCollection);
            wrsq.setFilterByCollections(fbCollections);
        }
        else {
            addPredicates(wrsq, cb, cqCollection, rtCollection);
        }
        orders.add(cb.asc(rtCollection.get("workflowResult").get("collectionName")));
        cqCollection.orderBy(orders);
        List<WorkflowResultFilterCollection> collections = em.createQuery(cqCollection.multiselect(
                rtCollection.get("workflowResult").get("unitId"),
                rtCollection.get("workflowResult").get("unitName"),
                rtCollection.get("workflowResult").get("collectionId"),
                rtCollection.get("workflowResult").get("collectionName")
        ).distinct(true)).getResultList();

        // item filter
        CriteriaQuery<WorkflowResultFilterItem> cqItem = cb.createQuery(WorkflowResultFilterItem.class);
        Root<MgmEvaluationTest> rtItem = cqItem.from(MgmEvaluationTest.class);
        Long[] fbItems = wrsq.getFilterByItems();
        if (fbItems.length > 0) {
            wrsq.setFilterByItems(new Long[0]);
            addPredicates(wrsq, cb, cqItem, rtItem);
            wrsq.setFilterByItems(fbItems);
        }
        else {
            addPredicates(wrsq, cb, cqItem, rtItem);
        }
        orders.add(cb.asc(rtItem.get("workflowResult").get("itemName")));
        cqItem.orderBy(orders);
        List<WorkflowResultFilterItem> items = em.createQuery(cqItem.multiselect(
                rtItem.get("workflowResult").get("unitId"),
                rtItem.get("workflowResult").get("unitName"),
                rtItem.get("workflowResult").get("collectionId"),
                rtItem.get("workflowResult").get("collectionName"),
                rtItem.get("workflowResult").get("itemId"),
                rtItem.get("workflowResult").get("itemName"),
                rtItem.get("workflowResult").get("externalId"),
                rtItem.get("workflowResult").get("externalSource")
        ).distinct(true)).getResultList();

        // primaryfile filter
        CriteriaQuery<WorkflowResultFilterFile> cqFile = cb.createQuery(WorkflowResultFilterFile.class);
        Root<MgmEvaluationTest> rtFile = cqFile.from(MgmEvaluationTest.class);
        Long[] fbFiles = wrsq.getFilterByFiles();
        if (fbFiles.length > 0) {
            wrsq.setFilterByFiles(new Long[0]);
            addPredicates(wrsq, cb, cqFile, rtFile);
            wrsq.setFilterByFiles(fbFiles);
        }
        else {
            addPredicates(wrsq, cb, cqFile, rtFile);
        }
        orders.add(cb.asc(rtFile.get("workflowResult").get("primaryfileName")));
        cqFile.orderBy(orders);
        List<WorkflowResultFilterFile> files = em.createQuery(cqFile.multiselect(
                rtFile.get("workflowResult").get("unitId"),
                rtFile.get("workflowResult").get("unitName"),
                rtFile.get("workflowResult").get("collectionId"),
                rtFile.get("workflowResult").get("collectionName"),
                rtFile.get("workflowResult").get("itemId"),
                rtFile.get("workflowResult").get("itemName"),
                rtFile.get("workflowResult").get("externalId"),
                rtFile.get("workflowResult").get("externalSource"),
                rtFile.get("workflowResult").get("primaryfileId"),
                rtFile.get("workflowResult").get("primaryfileName")
        ).distinct(true)).getResultList();

        // create-date filter
        CriteriaQuery<Object[]> cqDate = cb.createQuery(Object[].class);
        Root<MgmEvaluationTest> rtDate = cqDate.from(MgmEvaluationTest.class);
        Date[] fbDates = wrsq.getFilterByDates();
        if (fbDates.length > 0) {
            wrsq.setFilterByDates(new Date[0]);
            addPredicates(wrsq, cb, cqDate, rtDate);
            wrsq.setFilterByDates(fbDates);
        }
        else {
            addPredicates(wrsq, cb, cqDate, rtDate);
        }
        // retrieve min/max dates based on current other filters, which is more useful than distinct dates,
        // as frontend can use the date range to allow selection for further filtering;
        // also, this can avoid returning many individual dates when the result row size is big,
        // as each result is likely to have different timestamps.
        Object[] minmaxdts = em.createQuery(cqDate.multiselect(
                cb.least(rtDate.get("workflowResult").<Date>get(DATE_PROPERTY)),
                cb.greatest(rtDate.get("workflowResult").<Date>get(DATE_PROPERTY))
        )).getSingleResult();
        List<Date> dates = new ArrayList<Date>();
        dates.add((Date)minmaxdts[0]);
        dates.add((Date)minmaxdts[1]);

        // job status filter
        CriteriaQuery<GalaxyJobState> cqStatus = cb.createQuery(GalaxyJobState.class);
        Root<MgmEvaluationTest> rtStatus = cqStatus.from(MgmEvaluationTest.class);
        GalaxyJobState[] fbStatuses = wrsq.getFilterByStatuses();
        if (fbStatuses.length > 0) {
            wrsq.setFilterByStatuses(new GalaxyJobState[0]);
            addPredicates(wrsq, cb, cqStatus, rtStatus);
            wrsq.setFilterByStatuses(fbStatuses);
        }
        else {
            addPredicates(wrsq, cb, cqStatus, rtStatus);
        }
        List<GalaxyJobState> statuses = em.createQuery(cqStatus.select(rtStatus.get("workflowResult").get("status")).distinct(true)).getResultList();

        // String type filters
        List<String> submitters = getFilterValues("submitter", wrsq, cb, false);
        List<String> externalIds = getFilterValues("externalId", wrsq, cb, true);
        List<String> workflows = getFilterValues("workflowName", wrsq, cb, true);
        List<String> steps = getFilterValues("workflowStep", wrsq, cb, true);
        List<String> outputs = getFilterValues("outputName", wrsq, cb, true);
        List<String> types = getFilterValues("outputType", wrsq, cb, true);

        // search terms
        List<String> searchTerms = unionTerms(collections, items, files);

        // create- test date filter
        CriteriaQuery<Object[]> cqTestDate = cb.createQuery(Object[].class);
        Root<MgmEvaluationTest> rtTestDate = cqTestDate.from(MgmEvaluationTest.class);
        Date[] fbTestDates = wrsq.getFilterByTestDates();
        if (fbTestDates.length > 0) {
            wrsq.setFilterByTestDates(new Date[0]);
            addPredicates(wrsq, cb, cqTestDate, rtTestDate);
            wrsq.setFilterByTestDates(fbTestDates);
        }
        else {
            addPredicates(wrsq, cb, cqTestDate, rtTestDate);
        }
        // retrieve min/max dates based on current other filters, which is more useful than distinct dates,
        // as frontend can use the date range to allow selection for further filtering;
        // also, this can avoid returning many individual dates when the result row size is big,
        // as each result is likely to have different timestamps.
        Object[] minmaxTestdts = em.createQuery(cqTestDate.multiselect(
                cb.least(rtTestDate.<Date>get("dateSubmitted")),
                cb.greatest(rtTestDate.<Date>get("dateSubmitted"))
        )).getSingleResult();
        List<Date> testDates = new ArrayList<Date>();
        testDates.add((Date)minmaxTestdts[0]);
        testDates.add((Date)minmaxTestdts[1]);

        MgmEvaluationTestFilters filters = new MgmEvaluationTestFilters();
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
        filters.setTypes(types);
        filters.setStatuses(statuses);
        filters.setSearchTerms(searchTerms);
        filters.setTestDateFilter(testDates);

        return filters;
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
