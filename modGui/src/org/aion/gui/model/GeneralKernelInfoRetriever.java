package org.aion.gui.model;

import org.aion.api.type.ApiMsg;
import org.aion.log.AionLoggerFactory;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

/**
 * Provides general info about kernel.  Methods in here should probably eventually be
 * refactored into their own class, i.e. {@link org.aion.gui.model.dto.SyncInfoDTO2}.
 *
 * For now we'll keep them here out of laziness.
 */
public class GeneralKernelInfoRetriever extends AbstractAionApiClient {
    private static final Logger LOG = AionLoggerFactory.getLogger(org.aion.log.LogEnum.GUI.name());

    public GeneralKernelInfoRetriever(KernelConnection kernelConnection) {
        super(kernelConnection);
    }

    public Optional<Boolean> isMining() {
        ApiMsg resp = callApi(api -> api.getMine().isMining());
        if(resp.isError()) {
            logStringForErrorApiMsg(resp);
            return Optional.empty();
        }
        return Optional.ofNullable((Boolean)resp.getObject());
    }

    public Optional<Integer> getPeerCount() {
        final int size;

        ApiMsg resp = callApi(api -> api.getNet().getActiveNodes());
        if (resp.isError()) {
            logStringForErrorApiMsg(resp);
            return Optional.empty();
        }
        return Optional.ofNullable(((List) resp.getObject()).size());
    }
}