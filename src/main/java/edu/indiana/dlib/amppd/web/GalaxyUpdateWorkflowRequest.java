package edu.indiana.dlib.amppd.web;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import java.util.HashMap;

public class GalaxyUpdateWorkflowRequest {
    private String creator_name;
    private String workflow_name;
    private String annotation;

    public GalaxyUpdateWorkflowRequest(String creator, String workflow, String annotation){
        this.creator_name = creator;
        this.workflow_name = workflow;
        this.annotation = annotation;
    }

    public String params(){
        JSONObject request =new JSONObject();
        JSONObject workflow =new JSONObject();
        JSONArray creators =new JSONArray();
        HashMap creator = new HashMap();
        creator.put("name", this.creator_name);
        creator.put("class", "Person");
        creators.add(creator);
        workflow.put("steps", new HashMap());
        workflow.put("report", new HashMap());
        workflow.put("license", null);
        workflow.put("annotation", this.annotation);
        workflow.put("name", this.workflow_name);
        workflow.put("creator", creators);
        request.put("from_tool_form", true);
        request.put("workflow", workflow);
        return request.toJSONString();
    }
}
