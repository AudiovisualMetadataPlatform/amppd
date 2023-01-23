package edu.indiana.dlib.amppd.web;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.List;

@Data
public class MgmEvaluationParameterObj {
    private Long id;
    private String name;
    private String shortName;
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<String> value;
}
