package com.reconciliation.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.reconciliation.beans.InternalTxn;
import com.reconciliation.beans.ProcessorTxn;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class CsvToJson {
    private final CsvMapper csvMapper = CsvMapper.builder()
                            .addModule(new JavaTimeModule())
                            .build();
    private final CsvSchema schema = CsvSchema.emptySchema().withHeader();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String csvFileName;

    public CsvToJson(@Value("${csv.filename}") String csvFileName) {
        this.csvFileName = csvFileName;
    }

    public List<InternalTxn> convertCsvToJson() throws IOException {
        File f = new File(csvFileName);
        List<InternalTxn> values = csvMapper.readerFor(InternalTxn.class)
                                    .with(schema)
                                    .<InternalTxn>readValues(f).readAll();

        //opportunity to log
        return values;
    }
}

