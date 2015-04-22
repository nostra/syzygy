package no.api.syzygy.service.view;

import io.dropwizard.views.View;
import no.api.syzygy.SyzygyPayload;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class IndexView extends View {

    private final List<SyzygyGuiPayload> payloadList;

    public IndexView(List<SyzygyPayload> payloadList) {
        super("index.ftl");
        this.payloadList = convertToGuiList( payloadList );
    }

    private List<SyzygyGuiPayload> convertToGuiList(List<SyzygyPayload> payloadList) {
        List<SyzygyGuiPayload> guiList = new ArrayList<>();
        for ( SyzygyPayload payload : payloadList ) {
            guiList.add(new SyzygyGuiPayload(payload));
        }
        return guiList;
    }

    public List<SyzygyGuiPayload> getPayloadList() {
        return payloadList;
    }
}
