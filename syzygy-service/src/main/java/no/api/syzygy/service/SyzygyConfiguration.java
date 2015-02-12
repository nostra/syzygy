package no.api.syzygy.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import no.api.atomizer.header.dw.DropwizardAtomizerConfig;

import javax.validation.Valid;

/**
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SyzygyConfiguration extends Configuration {
    @JsonProperty(required = false)
    private String jsonLogPath;

    public String getJsonLogPath() {
        return jsonLogPath;
    }

    @JsonProperty
    @Valid
    private DropwizardAtomizerConfig atomizerHeaderConfig;

    public DropwizardAtomizerConfig getAtomizerHeaderConfig() {
        return atomizerHeaderConfig;
    }
}
