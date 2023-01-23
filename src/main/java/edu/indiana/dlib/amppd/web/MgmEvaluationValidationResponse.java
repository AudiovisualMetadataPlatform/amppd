package edu.indiana.dlib.amppd.web;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MgmEvaluationValidationResponse {
    private boolean success;
    private Integer mgmEvaluationTestCount;
    private List<String> validationErrors;

    public MgmEvaluationValidationResponse() {
        validationErrors = new ArrayList<String>();
    }

    public void addError(String error) {
        validationErrors.add(error);
    }
    public void addErrors(List<String> errors) {
        if(validationErrors!=null && !errors.isEmpty()) {
            validationErrors.addAll(errors);
        }
    }

    public boolean hasErrors() {
        return !validationErrors.isEmpty();
    }

}
