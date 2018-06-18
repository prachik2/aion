package org.aion.gui.controller;

import org.aion.api.IAionAPI;
import org.aion.api.impl.AionAPIImpl;
import org.aion.api.type.SyncInfo;
import org.aion.gui.model.SyncInfoDTO;
import org.aion.gui.model.LightAppSettings;
import org.aion.gui.model.ApiType;
import org.aion.gui.events.EventBusFactory;
import org.aion.gui.events.EventPublisher;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Provides an interface to the Aion API.  This is
 */
public class KernelConnection {

    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();

    private Future<?> connectionFuture;

    private LightAppSettings lightAppSettings = getLightweightWalletSettings(ApiType.JAVA);

    private final ReentrantLock lock = new ReentrantLock();

    private final IAionAPI api;

    /**
     * Constructor.  See also {@link #createDefaultConnection()}.
     *
     * @param aionApi API to connect to.
     */
    public KernelConnection(IAionAPI aionApi) {
        this.api = aionApi;

        EventBusFactory.getBus(EventPublisher.ACCOUNT_CHANGE_EVENT_ID).register(this);
        EventBusFactory.getBus(EventPublisher.SETTINGS_CHANGED_ID).register(this);
    }

    /**
     * Convenient static builder method to construct this class with underlying API instance
     * {@link AionAPIImpl#inst()}.
     *
     * This is equivalent to <tt>new KernelConnection(org.aion.api.impl.IAionAPI.inst())</tt>.
     *
     * @return a kernel connection connected to {@link AionAPIImpl#inst()}.
     */
    public static KernelConnection createDefaultConnection() {
        return new KernelConnection(AionAPIImpl.inst());
    }

    public void connect() {
        if (connectionFuture != null) {
            connectionFuture.cancel(true);
        }
        connectionFuture = backgroundExecutor.submit(() -> {
            api.connect(getConnectionString(), true);
            EventPublisher.fireOperationFinished();
        });
    }

    public void disconnect() {
//        storeLightweightWalletSettings(lightAppSettings);
        lock();
        try {
            api.destroyApi().getObject();
        } finally {
            unLock();
        }
    }

    protected final void lock(){
        lock.lock();
    }

    protected final void unLock() {
        lock.unlock();
    }

    private String getConnectionString() {
        final String protocol = lightAppSettings.getProtocol();
        final String ip = lightAppSettings.getAddress();
        final String port = lightAppSettings.getPort();
        return protocol + "://" + ip + ":" + port;
    }

    protected final LightAppSettings getLightweightWalletSettings(final ApiType type){
        return new LightAppSettings("127.0.0.1", "8547", "tcp", ApiType.JAVA);
    }

    public LightAppSettings getSettings() {
        return lightAppSettings;
    }

    public SyncInfoDTO getSyncInfo() {
        long chainBest;
        long netBest;
        SyncInfo syncInfo;
        try {
            lock();
            try {
                syncInfo = api.getNet().syncInfo().getObject();
            } finally {
                unLock();
            }
            chainBest = syncInfo.getChainBestBlock();
            netBest = syncInfo.getNetworkBestBlock();
        } catch (Exception e) {
            chainBest = getLatest();
            netBest = chainBest;
        }
        SyncInfoDTO syncInfoDTO = new SyncInfoDTO();
        syncInfoDTO.setChainBestBlkNumber(chainBest);
        syncInfoDTO.setNetworkBestBlkNumber(netBest);
        return syncInfoDTO;
    }

    private Long getLatest() {
        final Long latest;
        lock();
        try {
            if (api.isConnected()) {
                latest = api.getChain().blockNumber().getObject();
            } else {
                latest = 0L;
            }
        } finally {
            unLock();
        }
        return latest;
    }

    public boolean getConnectionStatusByConnectedPeers() {
        final boolean connected;
        lock();
        try {
            connected = api.isConnected();
        } finally {
            unLock();
        }
        return connected;
    }

    public int getPeerCount() {
        final int size;
        lock();
        try {
            if (api.isConnected()) {
                size = ((List) api.getNet().getActiveNodes().getObject()).size();
            } else {
                size = 0;
            }
        } finally {
            unLock();
        }
        return size;
    }

}
