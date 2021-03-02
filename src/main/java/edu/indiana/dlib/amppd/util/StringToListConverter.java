package edu.indiana.dlib.amppd.util;

import java.util.List;
import java.util.Map;

import org.springframework.core.convert.converter.Converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class StringToListConverter implements Converter<String, List<Map<String, String>>> {

    @Override
    public List<Map<String, String>> convert(String source) {
        try {
            return new ObjectMapper().readValue(source, new TypeReference<List<Map<String, String>>>() {});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
}
