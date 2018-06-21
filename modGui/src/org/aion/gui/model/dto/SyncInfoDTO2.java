package org.aion.gui.model.dto;

import org.aion.api.IAionAPI;
import org.aion.api.type.ApiMsg;
import org.aion.api.type.SyncInfo;
import org.aion.gui.model.AbstractAionApiClient;
import org.aion.gui.model.KernelConnection;
import org.aion.log.AionLoggerFactory;
import org.slf4j.Logger;

import java.util.Optional;

public class SyncInfoDTO2 extends AbstractAionApiClient {
    private long chainBestBlkNumber;
    private long networkBestBlkNumber;

    private static final Logger LOG = AionLoggerFactory.getLogger(org.aion.log.LogEnum.GUI.name());


    /**
     * Constructor
     *
     * @param kernelConnection connection containing the API instance to interact with
     */
    public SyncInfoDTO2(KernelConnection kernelConnection) {
        super(kernelConnection);
    }

    public long getNetworkBestBlkNumber() {
        return networkBestBlkNumber;
    }

    public void setNetworkBestBlkNumber(long networkBestBlkNumber) {
        this.networkBestBlkNumber = networkBestBlkNumber;
    }

    public long getChainBestBlkNumber() {
        return chainBestBlkNumber;
    }

    public void setChainBestBlkNumber(long chainBestBlkNumber) {
        this.chainBestBlkNumber = chainBestBlkNumber;
    }


    public Void loadFromApi() {
        Long chainBest;
        long netBest;
        SyncInfo syncInfo;
        try {
            ApiMsg msg = callApi(api -> api.getNet().syncInfo());
            if(msg.isError()) {
                LOG.error(logStringForErrorApiMsg(msg));
                return null;
            }
            syncInfo = msg.getObject();
            chainBest = syncInfo.getChainBestBlock();
            netBest = syncInfo.getNetworkBestBlock();
        } catch (Exception e) {
            chainBest = getLatest(); // FIXME more intelligent null handling
            if(chainBest == null) {
                chainBest = 0l;
            }
            netBest = chainBest;
        }

        setChainBestBlkNumber(chainBest);
        setNetworkBestBlkNumber(netBest);

        return null;
    }

    private Long getLatest() {
        final Long latest;

        ApiMsg msg = callApi(api -> api.getChain().blockNumber());
        if(msg.isError()) {
            logStringForErrorApiMsg(msg);
            return null;
        }

        return msg.getObject();
    }
}
