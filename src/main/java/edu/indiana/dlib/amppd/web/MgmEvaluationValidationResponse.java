package edu.indiana.dlib.amppd.web;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class MgmEvaluationValidationResponse {
    private boolean success;	// true if all validation pass and tests ran in success
    private Integer testCount;	// count of successfully created tests
    private Integer resultCount;	// count of successfully run tests
    private List<String> validationErrors;	// validation or runtime errors for all tests

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
