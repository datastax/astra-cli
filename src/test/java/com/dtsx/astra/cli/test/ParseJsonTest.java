package com.dtsx.astra.cli.test;

import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.utils.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ParseJsonTest {

    @Test
    public void shouldMarshallDbList() throws IOException {
        TypeReference<List<Database>> RESPONSE_DATABASES = new TypeReference<List<Database>>(){};
        String jsonContent = new String(Files.readAllBytes(Paths.get("src/test/resources/data.json")));
        List<Database> db = JsonUtils.unmarshallType(jsonContent, RESPONSE_DATABASES);


    }
}
