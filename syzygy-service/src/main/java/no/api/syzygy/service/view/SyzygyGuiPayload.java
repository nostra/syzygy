package no.api.syzygy.service.view;

import no.api.syzygy.SyzygyPayload;

import java.util.List;

/**
 *
 */
public class SyzygyGuiPayload {

    private final SyzygyPayload payload;

    public SyzygyGuiPayload(SyzygyPayload payload) {
        this.payload = payload;
    }


    public String getName() {
        return payload.getName();
    }


    public String getValue() {
        return ""+payload.getValue();
    }


    public String getHits() {
        return ""+payload.getHits();
    }


    public List<String> getPath() {
        return payload.getPath();
    }


    public String getDoc() {
        return payload.getDoc();
    }

}
