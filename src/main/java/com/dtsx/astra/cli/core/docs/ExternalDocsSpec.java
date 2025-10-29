package com.dtsx.astra.cli.core.docs;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record ExternalDocsSpec(
    @JsonProperty(required = true) List<String> collapsibleOptionGroups,
    @JsonProperty(required = true) List<String> hideCommands,
    @JsonProperty(required = true) Map<String, List<String>> seeAlsoLinks
) {}
