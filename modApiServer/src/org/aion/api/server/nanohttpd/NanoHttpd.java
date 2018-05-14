/*
 * Copyright (c) 2017-2018 Aion foundation.
 *
 * This file is part of the aion network project.
 *
 * The aion network project is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or any later version.
 *
 * The aion network project is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with the aion network
 * project source files. If not, see <https://www.gnu.org/licenses/>.
 *
 * The aion network project leverages useful source code from other open source projects. We
 * greatly appreciate the effort that was invested in these projects and we thank the individual
 * contributors for their work. For provenance information and contributors. Please see
 * <https://github.com/aionnetwork/aion/wiki/Contributors>.
 *
 * Contributors to the aion source files in decreasing order of code volume:
 * Aion foundation.
 * <ether.camp> team through the ethereumJ library.
 * Ether.Camp Inc. (US) team through Ethereum Harmony.
 * John Tromp through the Equihash solver.
 * Samuel Neves through the BLAKE2 implementation.
 * Zcash project team. Bitcoinj team.
 */
package org.aion.api.server.nanohttpd;

import fi.iki.elonen.NanoHTTPD;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.aion.api.server.rpc.RpcProcessor;
import org.aion.log.AionLoggerFactory;
import org.aion.log.LogEnum;
import org.slf4j.Logger;

public class NanoHttpd extends NanoHTTPD {
    private static final Logger LOG = AionLoggerFactory.getLogger(LogEnum.API.name());

    private RpcProcessor rpcProcessor;
    private boolean corsEnabled;

    public NanoHttpd(String hostname, int port, boolean corsEnabled, List<String> enabledEndpoints)
            throws IOException {
        super(hostname, port);
        this.rpcProcessor = new RpcProcessor(enabledEndpoints);
        this.corsEnabled = corsEnabled;
    }

    protected Response addCORSHeaders(Response resp) {
        resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.addHeader("Access-Control-Allow-Headers", "origin,accept,content-type");
        resp.addHeader("Access-Control-Allow-Credentials", "true");
        resp.addHeader("Access-Control-Allow-Methods", "POST");
        resp.addHeader("Access-Control-Max-Age", "86400");
        return resp;
    }

    private Response respond(IHTTPSession session) {
        String requestBody = null;

        Map<String, String> body = new HashMap<String, String>(); // body need to grab key postData
        try {
            session.parseBody(body);
        } catch (Exception e) {
            LOG.debug("<rpc-server - no request body found>", e);
        }

        requestBody = body.getOrDefault("postData", null);

        return NanoHTTPD.newFixedLengthResponse(
                Response.Status.OK, "application/json", rpcProcessor.process(requestBody));
    }

    @Override
    public Response serve(IHTTPSession session) {
        // First let's handle CORS OPTION query
        Response r;
        if (corsEnabled && Method.OPTIONS.equals(session.getMethod())) {
            r = NanoHTTPD.newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, null, 0);
        } else {
            r = respond(session);
        }

        if (corsEnabled) {
            r = addCORSHeaders(r);
        }
        return r;
    }

    @Override
    public void stop() {
        super.stop();
        rpcProcessor.shutdown();
    }
}
