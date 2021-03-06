/*
 * Copyright (c) 2017-2018 Aion foundation.
 *
 *     This file is part of the aion network project.
 *
 *     The aion network project is free software: you can redistribute it
 *     and/or modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation, either version 3 of
 *     the License, or any later version.
 *
 *     The aion network project is distributed in the hope that it will
 *     be useful, but WITHOUT ANY WARRANTY; without even the implied
 *     warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *     See the GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with the aion network project source files.
 *     If not, see <https://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Aion foundation.
 */
package org.aion.p2p.impl.comm;

import static org.aion.p2p.impl1.P2pMgr.p2pLOG;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import org.aion.p2p.INode;
import org.aion.p2p.INodeMgr;
import org.aion.p2p.IP2pMgr;

public class NodeMgr implements INodeMgr {

    private final static int TIMEOUT_INBOUND_NODES = 10000;

    private static final int TIMEOUT_OUTBOUND_NODES = 20000;

    private final int maxActiveNodes;

    private final int maxTempNodes;

    private static final Random random = new SecureRandom();

    private final Set<String> seedIps = new HashSet<>();

    private final IP2pMgr p2pMgr;

    private final BlockingQueue<INode> tempNodes = new LinkedBlockingQueue<>();
    private final Map<Integer, INode> outboundNodes = new ConcurrentHashMap<>();
    private final Map<Integer, INode> inboundNodes = new ConcurrentHashMap<>();
    private final Map<Integer, INode> activeNodes = new ConcurrentHashMap<>();

    public NodeMgr(IP2pMgr _p2pMgr, int _maxActiveNodes, int _maxTempNodes) {
        this.maxActiveNodes = _maxActiveNodes;
        this.maxTempNodes = _maxTempNodes;
        this.p2pMgr = _p2pMgr;
    }

    public Map<Integer, INode> getOutboundNodes() {
        return outboundNodes;
    }

    private final static char[] hexArray = "0123456789abcdef".toCharArray();

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * @param selfShortId String
     */
    @Override
    public String dumpNodeInfo(String selfShortId) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append(String.format(
            "======================================================================== p2p-status-%6s =========================================================================\n",
            selfShortId));
        sb.append(String.format(
            "temp[%3d] inbound[%3d] outbound[%3d] active[%3d]                                         s - seed node, td - total difficulty, # - block number, bv - binary version\n",
            tempNodesSize(), inboundNodes.size(), outboundNodes.size(), activeNodes.size()));
        List<INode> sorted = new ArrayList<>(activeNodes.values());
        if (sorted.size() > 0) {
            sb.append("\n          s"); // id & seed
            sb.append("               td");
            sb.append("          #");
            sb.append("                                                             hash");
            sb.append("              ip");
            sb.append("  port");
            sb.append("     conn");
            sb.append("              bv");
            sb.append("           ci\n");
            sb.append(
                "--------------------------------------------------------------------------------------------------------------------------------------------------------------------\n");
            sorted.sort((n1, n2) -> {
                int tdCompare = n2.getTotalDifficulty().compareTo(n1.getTotalDifficulty());
                if (tdCompare == 0) {
                    Long n2Bn = n2.getBestBlockNumber();
                    Long n1Bn = n1.getBestBlockNumber();
                    return n2Bn.compareTo(n1Bn);
                } else {
                    return tdCompare;
                }
            });
            for (INode n : sorted) {
                try {
                    sb.append(String.format("id:%6s %c %16s %10d %64s %15s %5d %8s %15s %12s\n",
                        n.getIdShort(),
                        n.getIfFromBootList() ? 'y' : ' ', n.getTotalDifficulty().toString(10),
                        n.getBestBlockNumber(),
                        n.getBestBlockHash() == null ? "" : bytesToHex(n.getBestBlockHash()),
                        n.getIpStr(),
                        n.getPort(),
                        n.getConnection(),
                        n.getBinaryVersion(),
                        n.getChannel().hashCode())
                    );
                } catch (Exception ex) {
                    ex.printStackTrace();
                    p2pLOG.error("NodeMgr dumpNodeInfo exception {}", ex.getMessage());
                }
            }
        }
        return sb.toString();
    }

    /**
     * @param _ip String
     */
    @Override
    public void seedIpAdd(String _ip) {
        this.seedIps.add(_ip);
    }

    @Override
    public boolean isSeedIp(String _ip) {
        return this.seedIps.contains(_ip);
    }

    /**
     * @param _n Node
     */
    @Override
    public synchronized void addTempNode(final INode _n) {
        if (tempNodes.size() < maxTempNodes) {
            tempNodes.add(_n);
        }
    }

    @Override
    public void addInboundNode(final INode _n) {
        inboundNodes.put(_n.getChannel().hashCode(), _n);
    }

    @Override
    public void addOutboundNode(final INode _n) {
        outboundNodes.put(_n.getIdHash(), _n);
    }

    @Override
    public INode tempNodesTake() throws InterruptedException {
        return tempNodes.take();
    }

    @Override
    public int tempNodesSize() {
        return tempNodes.size();
    }

    @Override
    public int activeNodesSize() {
        return activeNodes.size();
    }

    @Override
    public boolean notActiveNode(int k) {
        return !activeNodes.containsKey(k);
    }

    @Override
    public INode getActiveNode(int k) {
        return activeNodes.get(k);
    }

    @Override
    public INode getInboundNode(int k) {
        return inboundNodes.get(k);
    }

    @Override
    public INode getOutboundNode(int k) {
        return outboundNodes.get(k);
    }

    @Override
    public INode allocNode(String ip, int p0) {
        INode n = new Node(ip, p0);
        if (seedIps.contains(ip)) {
            n.setFromBootList(true);
        }
        return n;
    }

    @Override
    public List<INode> getActiveNodesList() {
        return new ArrayList<>(activeNodes.values());
    }

    @Override
    public HashMap getActiveNodesMap() {
        synchronized (activeNodes) {
            return new HashMap<>(activeNodes);
        }
    }

    @Override
    public void timeoutCheck() {
        timeoutInbound();
        timeoutOutBound();
        timeoutActive();
    }

    private void timeoutOutBound() {
        Iterator<Integer> outboundIt = getOutboundNodes().keySet().iterator();
        while (outboundIt.hasNext()) {
            int outBound = outboundIt.next();
            INode node = getOutboundNodes().get(outBound);
            if (System.currentTimeMillis() - node.getTimestamp()
                > TIMEOUT_OUTBOUND_NODES) {
                p2pMgr.closeSocket(
                    node.getChannel(),
                    "outbound-timeout node=" + node.getIdShort() + " ip=" + node.getIpStr());
                outboundIt.remove();
            }
        }
    }

    @Override
    public INode getRandom() {
        if (!activeNodes.isEmpty()) {
            Object[] keysArr = activeNodes.keySet().toArray();
            try {
                return this.getActiveNode((Integer) keysArr[random.nextInt(keysArr.length)]);
            } catch (IllegalArgumentException e) {
                System.out.println("<p2p get-random-exception>");
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * @param _ip String
     * @return boolean
     * @warning not thread safe helper function to check a specific ip a node associated with is is
     * allowed to add to active list
     */
    private boolean activeIpAllow(String _ip) {
        return true;
        // enable this in case
//        if(multiActiveAllowIps.contains(_ip))
//            return true;
//        else {
//            Set<String> ips = activeNodes.values().stream()
//                    .map((n)-> n.getIpStr())
//                    .collect(Collectors.toSet());
//            return !ips.contains(_ip);
//        }
    }

    /**
     * @param _channelHashCode int
     */
    // Attention: move node from container need sync to avoid node not belong to
    // any container during transit.
    public synchronized void moveInboundToActive(int _channelHashCode) {
        INode node = inboundNodes.remove(_channelHashCode);
        if (node != null) {

            if (activeNodes.size() >= maxActiveNodes) {
                p2pMgr.closeSocket(node.getChannel(), "inbound -> active, active full");
                return;
            }

            if (node.getIdHash() == p2pMgr.getSelfIdHash()) {
                p2pMgr.closeSocket(node.getChannel(), "inbound -> active, self-connected");
                return;
            }

            node.setConnection("inbound");
            node.setFromBootList(seedIps.contains(node.getIpStr()));
            INode previous = activeNodes.putIfAbsent(node.getIdHash(), node);
            if (previous != null) {
                p2pMgr.closeSocket(node.getChannel(),
                    "inbound -> active, node " + previous.getIdShort() + " exits");
            } else if (!activeIpAllow(node.getIpStr())) {
                p2pMgr.closeSocket(node.getChannel(),
                    "inbound -> active, ip " + node.getIpStr() + " exits");
            } else {
                if (p2pLOG.isDebugEnabled()) {
                    p2pLOG.debug("inbound -> active node-id={} ip={}", node.getIdShort(), node
                        .getIpStr());
                }
            }
        }
    }

    /**
     * @param _nodeIdHash int
     * @param _shortId String
     */
    // Attention: move node from container need sync to avoid node not belong to
    // any container during transit.
    public synchronized void moveOutboundToActive(int _nodeIdHash, String _shortId) {
        INode node = outboundNodes.remove(_nodeIdHash);
        if (node != null) {

            if (activeNodes.size() >= maxActiveNodes) {
                p2pMgr.closeSocket(node.getChannel(), "outbound -> active, active full");
                return;
            }

            if (node.getIdHash() == p2pMgr.getSelfIdHash()) {
                p2pMgr.closeSocket(node.getChannel(), "outbound -> active, self-connected");
                return;
            }

            node.setConnection("outbound");
            INode previous = activeNodes.putIfAbsent(_nodeIdHash, node);
            if (previous != null) {
                p2pMgr.closeSocket(node.getChannel(),
                    "outbound -> active, node " + previous.getIdShort() + " exits");
            } else {
                if (p2pLOG.isDebugEnabled()) {
                    p2pLOG.debug("outbound -> active node-id={} ip={}", _shortId, node.getIpStr());
                }
            }
        }
    }

    private void timeoutInbound() {
        Iterator<Integer> inboundIt = inboundNodes.keySet().iterator();
        while (inboundIt.hasNext()) {
            int key = inboundIt.next();
            INode node = inboundNodes.get(key);
            if (System.currentTimeMillis() - node.getTimestamp() > TIMEOUT_INBOUND_NODES) {
                p2pMgr.closeSocket(node.getChannel(), "inbound-timeout ip=" + node.getIpStr());
                inboundIt.remove();
            }
        }
    }

    private void timeoutActive() {
        long now = System.currentTimeMillis();

        OptionalDouble average = activeNodes.values().stream()
            .mapToLong(n -> now - n.getTimestamp()).average();
        double timeout = average.orElse(4000) * 5;
        timeout = Math.max(10000, Math.min(timeout, 60000));
        if (p2pLOG.isDebugEnabled()) {
            p2pLOG.debug("average-delay={}ms", (long)average.orElse(0));
        }

        Iterator<Integer> activeIt = activeNodes.keySet().iterator();
        while (activeIt.hasNext()) {
            int key = activeIt.next();
            INode node = getActiveNode(key);

            if (now - node.getTimestamp() > timeout) {
                p2pMgr.closeSocket(node.getChannel(),
                    "active-timeout node=" + node.getIdShort() + " ip=" + node.getIpStr());
                activeIt.remove();
            }

            if (!node.getChannel().isConnected()) {
                p2pMgr.closeSocket(node.getChannel(),
                    "channel-already-closed node=" + node.getIdShort() + " ip=" + node.getIpStr());
                activeIt.remove();
            }
        }
    }

    public void dropActive(int nodeIdHash, String _reason) {
        INode node = activeNodes.remove(nodeIdHash);
        if (node == null) {
            return;
        }
        p2pMgr.closeSocket(node.getChannel(), _reason);
    }

    @Override
    public void shutdown() {
        try {

            synchronized (outboundNodes) {
                outboundNodes.forEach((k, n) -> p2pMgr.closeSocket(n.getChannel(),
                    "p2p-shutdown outbound node=" + n.getIdShort() + " ip=" + n.getIpStr()));
                outboundNodes.clear();
            }

            synchronized (inboundNodes) {
                inboundNodes.forEach((k, n) -> p2pMgr
                    .closeSocket(n.getChannel(), "p2p-shutdown inbound ip=" + n.getIpStr()));
                inboundNodes.clear();
            }

            synchronized (activeNodes) {
                activeNodes.forEach((k, n) -> p2pMgr.closeSocket(n.getChannel(),
                    "p2p-shutdown active node=" + n.getIdShort() + " ip=" + n.getIpStr()));
                activeNodes.clear();
            }

        } catch (Exception e) {
            e.printStackTrace();
            p2pLOG.error("p2p-shutdown exception {}", e.getMessage());
        }
    }

    @Override
    public void ban(int _nodeIdHash) {
        INode node = activeNodes.get(_nodeIdHash);
        if (node != null) {
            node.getPeerMetric().ban();
        }
    }
}
