package org.aion.gui.model;

import org.aion.api.type.ApiMsg;
import org.aion.log.AionLoggerFactory;
import org.slf4j.Logger;

import java.util.Optional;

public class MiningStatusRetriever extends AbstractAionApiClient {
    private static final Logger LOG = AionLoggerFactory.getLogger(org.aion.log.LogEnum.GUI.name());

    public MiningStatusRetriever(KernelConnection kernelConnection) {
        super(kernelConnection);
    }

    public Optional<Boolean> isMining() {
        ApiMsg resp = callApi(api -> api.getMine().isMining());
        if(resp.isError()) {

            return Optional.empty();
        }
        return Optional.ofNullable((Boolean)resp.getObject());
    }
}