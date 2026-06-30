package com.dtsx.astra.cli.unit.core.models;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.unit.BaseParseableTest;
import lombok.val;
import net.jqwik.api.Example;
import net.jqwik.api.Group;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class DbRefTest extends BaseParseableTest.WithTrimAndBasicValidation {
    private static final UUID SAMPLE_ID = UUID.fromString("822b0fff-6a73-4322-a8ec-09832b075287");

    public DbRefTest() {
        super("Database name/id", DbRef::parse);
    }

    @Group
    public class from_name {
        @Example
        public void parses_plain_name_as_name_ref() {
            val result = DbRef.parse("my-database");
            assertThat(result.getRight().isName()).isTrue();
            assertThat(result.getRight().toString()).isEqualTo("my-database");
        }
    }

    @Group
    public class from_uuid {
        @Example
        public void parses_uuid_as_id_ref() {
            val result = DbRef.parse(SAMPLE_ID.toString());
            assertThat(result.getRight().isId()).isTrue();
            assertThat(result.getRight().toString()).isEqualTo(SAMPLE_ID.toString());
        }
    }

    @Group
    public class from_endpoint_url {
        @Example
        public void parses_prod_api_endpoint() {
            val url = "https://" + SAMPLE_ID + "-us-east1.apps.astra.datastax.com";
            val result = DbRef.parse(url);
            assertThat(result.getRight().isId()).isTrue();
            assertThat(result.getRight().toString()).isEqualTo(SAMPLE_ID.toString());
        }

        @Example
        public void parses_dev_api_endpoint() {
            val url = "https://" + SAMPLE_ID + "-ap-south-1.apps.astra-dev.datastax.com";
            val result = DbRef.parse(url);
            assertThat(result.getRight().isId()).isTrue();
            assertThat(result.getRight().toString()).isEqualTo(SAMPLE_ID.toString());
        }

        @Example
        public void parses_endpoint_with_path() {
            val url = "https://" + SAMPLE_ID + "-us-east1.apps.astra.datastax.com/api/json/v1";
            val result = DbRef.parse(url);
            assertThat(result.getRight().isId()).isTrue();
            assertThat(result.getRight().toString()).isEqualTo(SAMPLE_ID.toString());
        }

        @Example
        public void parses_swagger_endpoint() {
            val url = "https://" + SAMPLE_ID + "-ap-south-1.apps.astra-dev.datastax.com/api/json/swagger-ui";
            val result = DbRef.parse(url);
            assertThat(result.getRight().isId()).isTrue();
            assertThat(result.getRight().toString()).isEqualTo(SAMPLE_ID.toString());
        }

        @Example
        public void falls_back_to_name_for_non_astra_url() {
            val url = "https://some-other-host.example.com/path";
            val result = DbRef.parse(url);
            assertThat(result.getRight().isName()).isTrue();
        }

        @Example
        public void falls_back_to_name_when_url_has_no_uuid_prefix() {
            val url = "https://not-a-uuid-at-all.apps.astra.datastax.com";
            val result = DbRef.parse(url);
            assertThat(result.getRight().isName()).isTrue();
        }
    }
}
