package edu.indiana.dlib.amppd.web;

import edu.indiana.dlib.amppd.model.MgmEvaluationTest;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MgmEvaluationValidationResponse {
    private boolean success;
    private MgmEvaluationTest mgmEvaluationTest;
    private List<String> validationErrors;
    private List<String> processingErrors;

    public MgmEvaluationValidationResponse() {
        validationErrors = new ArrayList<String>();
        processingErrors = new ArrayList<String>();
    }

    public void addError(String error) {
        validationErrors.add(error);
    }
    public void addErrors(List<String> errors) {
        if(validationErrors!=null && !errors.isEmpty()) {
            validationErrors.addAll(errors);
        }
    }

    public void addProcessingError(String error) {
        processingErrors.add(error);
    }
    public void addProcessingErrors(List<String> errors) {
        if(processingErrors!=null && !errors.isEmpty()) {
            processingErrors.addAll(errors);
        }
    }

    public boolean hasErrors() {
        return !validationErrors.isEmpty();
    }

    public boolean hasProcessingErrors() {
        return !processingErrors.isEmpty();
    }
}
