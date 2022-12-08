package edu.indiana.dlib.amppd.repository;

import edu.indiana.dlib.amppd.model.MgmEvaluationTest;
import edu.indiana.dlib.amppd.web.MgmEvaluationSearchQuery;
import edu.indiana.dlib.amppd.web.MgmEvaluationTestResponse;
import edu.indiana.dlib.amppd.web.WorkflowResultSortRule;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Slf4j
public class MgmEvaluationTestRepositoryCustomImpl implements MgmEvaluationTestRepositoryCustom {
    @PersistenceContext
    EntityManager em;

    public MgmEvaluationTestResponse findByQuery(MgmEvaluationSearchQuery mesq){
        MgmEvaluationTestResponse response = new MgmEvaluationTestResponse();
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

        Predicate[] preds = new Predicate[0];
        if (!predicates.isEmpty()) {
            preds = predicates.toArray(preds);
            cq.where(preds);
        }
        return preds;
    }

    private List<Map> prepareRows(List<MgmEvaluationTest> rows){
        List<Map> datatable = new ArrayList<Map>();
        for(MgmEvaluationTest test: rows) {
            Map<String, String> row = new HashMap<String, String>()
            {
                {
                    put("id", test.getId().toString());
                    put("testDate", test.getDateSubmitted().toString());
                    put("OutputDate", test.getWorkflowResult().getDateCreated().toString());
                    put("submitter", test.getSubmitter());
                    put("unit", test.getWorkflowResult().getUnitName());
                    put("collection", test.getWorkflowResult().getCollectionName());
                    put("externalSource", test.getWorkflowResult().getExternalSource());
                    put("externalId", test.getWorkflowResult().getExternalId());
                    put("item", test.getWorkflowResult().getItemName());
                    put("primaryFile", test.getWorkflowResult().getPrimaryfileName());
                    put("workflow", test.getWorkflowResult().getWorkflowName());
                    put("step", test.getWorkflowResult().getWorkflowStep());
                    put("output", test.getWorkflowResult().getOutputName());
                    put("groundTruth", test.getGroundtruthSupplement().getName());
                    put("testOutput", "amp_evaluation");
                    put("workflowId", test.getWorkflowResult().getId().toString());
                }
            };
            datatable.add(row);
        }
        return datatable;

    }
}
