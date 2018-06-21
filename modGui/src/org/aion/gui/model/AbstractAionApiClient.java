package org.aion.gui.model;

import org.aion.api.IAionAPI;
import org.aion.api.type.ApiMsg;

/**
 * Provides access to an {@link IAionAPI} instance in a thread-safe manner.
 *
 * Example implementation: {@link GeneralKernelInfoRetriever}.
 */
public abstract class AbstractAionApiClient {
    private final IAionAPI api;

    /**
     * Constructor
     *
     * @param kernelConnection connection containing the API instance to interact with
     */
    protected AbstractAionApiClient(KernelConnection kernelConnection) {
        this.api = kernelConnection.getApi();
    }

    @FunctionalInterface
    protected interface ApiFunction {
        ApiMsg call(IAionAPI api);
    }

    /**
     * Call the {@link IAionAPI} in a thread-safe manner.  Specifically, call the given function
     * within a synchronization block over the underlying API object.  Intention is for subclasses
     * to use this to execute critical sections that interact with the API.
     *
     * @param func a function that calls the Aion API
     * @return object returned by Aion API.
     */
    protected ApiMsg callApi(ApiFunction func) {
        synchronized (api) {
            return func.call(api);
        }
    }

    protected String logStringForErrorApiMsg(ApiMsg msg) {
        return String.format("Error in API call.  Code = %s.  Error = %s.",
                msg.getErrorCode(), msg.getErrString());
    }


}
