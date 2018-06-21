package org.aion.gui.model;

import org.aion.api.IAionAPI;
import org.aion.api.impl.AionAPIImpl;
import org.aion.gui.events.EventBusRegistry;
import org.aion.gui.events.EventPublisher;
import org.aion.gui.model.dto.LightAppSettings;
import org.aion.log.AionLoggerFactory;
import org.slf4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Represents a connection to the kernel.
 */
public class KernelConnection {
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();

    private Future<?> connectionFuture;

    private Future<?> disconnectionFuture;

    private LightAppSettings lightAppSettings = getLightweightWalletSettings(ApiType.JAVA);

    private final IAionAPI api;

    private static final Logger LOG = AionLoggerFactory.getLogger(org.aion.log.LogEnum.GUI.name());

    /**
     * Constructor.  See also {@link #createDefaultConnection()}.
     *
     * @param aionApi API to connect to.
     */
    public KernelConnection(IAionAPI aionApi) {
        this.api = aionApi;

        EventBusRegistry.INSTANCE.getBus(EventPublisher.ACCOUNT_CHANGE_EVENT_ID).register(this);
        EventBusRegistry.INSTANCE.getBus(EventPublisher.SETTINGS_CHANGED_ID).register(this);
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
            synchronized (api) {
                api.connect(getConnectionString(), true);
            }
            EventPublisher.fireOperationFinished();
        });
    }

    public void disconnect() {
        connectionFuture.cancel(true);
        if(!api.isConnected()) {
            return;
        }

//        storeLightweightWalletSettings(lightAppSettings);

        disconnectionFuture = backgroundExecutor.submit(() -> {
            synchronized (api) {
                api.destroyApi().getObject();
            }
        });

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

    /**
     * Should only be used by AbstractAionApiClient
     */
    IAionAPI getApi() {
        return this.api;
    }

    public boolean isConnected() {
        synchronized (api) {
            return api.isConnected();
        }
    }

}
