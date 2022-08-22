package com.datastax.astra;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.datastax.astra.sdk.databases.domain.Database;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ParseOutput {
    
    @Test
    public void test() throws StreamReadException, DatabindException, IOException {
        ObjectMapper om = new ObjectMapper();
        om.readValue(new File("src/test/resources/output.json"), new TypeReference<List<Database>>(){});
    }

} 
