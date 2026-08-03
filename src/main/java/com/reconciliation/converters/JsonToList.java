package com.reconciliation.converters;

import com.reconciliation.beans.InternalTxn;
import com.reconciliation.beans.ProcessorTxn;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class JsonToList {

    private final ObjectMapper objectMapper;
    private final String filename;

    public JsonToList(@Value("${json.filename}") String filename, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.filename = filename;
    }

    public List<ProcessorTxn> convert() throws IOException {
        File f = new File(filename);
        return objectMapper.readValue(
                f,
                new TypeReference<List<ProcessorTxn>>() {}
        );

    }
}
