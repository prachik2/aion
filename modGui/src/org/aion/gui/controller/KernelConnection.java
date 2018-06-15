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

public class KernelConnection {

    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();

    private Future<?> connectionFuture;

    private LightAppSettings lightAppSettings = getLightweightWalletSettings(ApiType.JAVA);

    private final static IAionAPI API = AionAPIImpl.inst();

    private final ReentrantLock lock = new ReentrantLock();

    public KernelConnection() {
        //connect();
//        loadLocallySavedAccounts();
//        backgroundExecutor.submit(() -> processNewTransactions(0, addressToAccount.keySet()));
        EventBusFactory.getBus(EventPublisher.ACCOUNT_CHANGE_EVENT_ID).register(this);
        EventBusFactory.getBus(EventPublisher.SETTINGS_CHANGED_ID).register(this);
    }

    private static KernelConnection INST;

    public static KernelConnection getInstance() {
        /*
         * FIXME Really not a big fan of making this Singleton but the code
         * I copy-pasta'd from aion_ui calls out to the connection through static singleton calls;
         * don't want to write the plumbing right now for D.I. right now, may refactor after
         */
        if(INST == null) {
            INST = new KernelConnection();
        }
        return INST;
    }

    public void connect() {
        if (connectionFuture != null) {
            connectionFuture.cancel(true);
        }
        connectionFuture = backgroundExecutor.submit(() -> {
            API.connect(getConnectionString(), true);
            EventPublisher.fireOperationFinished();
        });
    }

    public void disconnect() {
//        storeLightweightWalletSettings(lightAppSettings);
        lock();
        try {
            API.destroyApi().getObject();
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
                syncInfo = API.getNet().syncInfo().getObject();
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
            if (API.isConnected()) {
                latest = API.getChain().blockNumber().getObject();
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
            connected = API.isConnected();
        } finally {
            unLock();
        }
        return connected;
    }

    public int getPeerCount() {
        final int size;
        lock();
        try {
            if (API.isConnected()) {
                size = ((List) API.getNet().getActiveNodes().getObject()).size();
            } else {
                size = 0;
            }
        } finally {
            unLock();
        }
        return size;
    }

}
