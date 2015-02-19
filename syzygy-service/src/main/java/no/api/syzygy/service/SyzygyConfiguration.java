package no.api.syzygy.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import no.api.atomizer.header.dw.DropwizardAtomizerConfig;

import javax.validation.Valid;

/**
 *
@JsonIgnoreProperties(ignoreUnknown = true)
 */
public class SyzygyConfiguration extends Configuration {
    @JsonProperty(required = false)
    private String jsonLogPath;

    @JsonProperty(required = false)
    private String etcdPrefix;

    @JsonProperty(required = true)
    @Valid
    private String etcdUrl;

    @JsonProperty
    @Valid
    private DropwizardAtomizerConfig atomizerHeaderConfig;

    public String getJsonLogPath() {
        return jsonLogPath;
    }

    public String getEtcdUrl() {
        return etcdUrl;
    }

    public DropwizardAtomizerConfig getAtomizerHeaderConfig() {
        return atomizerHeaderConfig;
    }

    public String getEtcdPrefix() {
        return etcdPrefix;
    }
}
