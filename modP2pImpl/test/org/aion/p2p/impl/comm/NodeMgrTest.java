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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.aion.log.AionLoggerFactory;
import org.aion.log.LogEnum;
import org.aion.log.LogLevels;
import org.aion.p2p.INode;
import org.aion.p2p.impl1.P2pMgr;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;


/**
 * @author sridevi
 */
public class NodeMgrTest {

    private final int MAX_TEMP_NODES = 128;
    private final int MAX_ACTIVE_NODES = 128;

    private String nodeId1 = UUID.randomUUID().toString();
    private String nodeId2 = UUID.randomUUID().toString();
    private String ip1 = "127.0.0.1";
    private String ip2 = "192.168.0.11";
    private int port1 = 30304;
    private int port2 = 30305;
    private Logger LOGGER;

    private String[] nodes = new String[]{
        "p2p://" + nodeId1 + "@" + ip1 + ":" + port2,
        "p2p://" + nodeId2 + "@" + ip2 + ":" + port1,
    };

    @Mock
    private P2pMgr p2p;

    @Mock
    private Node node;

    @Mock
    private SocketChannel channel;

    private NodeMgr nMgr;

    Random r;

    @Before
    public void Setup() {
        Map<String, String> logMap = new HashMap<>();
        logMap.put(LogEnum.P2P.name(), LogLevels.TRACE.name());
        AionLoggerFactory.init(logMap);
        LOGGER = AionLoggerFactory.getLogger(LogEnum.P2P.name());

        MockitoAnnotations.initMocks(this);

        nMgr = new NodeMgr(p2p, MAX_ACTIVE_NODES, MAX_TEMP_NODES, LOGGER);
        r = new Random();
    }

    private void addActiveNode() {
        INode node = nMgr.allocNode(ip1, 0);

        byte[] rHash = new byte[32];
        r.nextBytes(rHash);
        node.updateStatus(r.nextLong(), rHash, BigInteger.valueOf(r.nextLong()));
        addNodetoOutbound(node, UUID.randomUUID());
        nMgr.movePeerToActive(node.getIdHash(), "outbound");
    }

    private void addNodetoOutbound(INode node, UUID _uuid) {
        node.setChannel(channel);
        try {
            node.setId(_uuid.toString().getBytes("UTF-8"));
            node.refreshTimestamp();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        nMgr.addOutboundNode(node);
        assertNotNull(nMgr.getOutboundNode(node.getIdHash()));
    }

    private void addNodetoInbound(INode node, UUID _uuid) {
        node.setChannel(channel);
        try {
            node.setId(_uuid.toString().getBytes("UTF-8"));
            node.refreshTimestamp();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        nMgr.addInboundNode(node);
        assertNotNull(nMgr.getInboundNode(channel.hashCode()));
    }

    @Test
    public void testTempNode() {
        nMgr.addTempNode(node);
        assertEquals(1, nMgr.tempNodesSize());

        nMgr.addTempNode(node);
        assertEquals(1, nMgr.tempNodesSize());

        String nl = "p2p://" + nodeId1 + "@" + ip1 + ":" + port1;

        INode node = Node.parseP2p(nl);

        nMgr.addTempNode(node);
        assertEquals(2, nMgr.tempNodesSize());
    }

    @Test
    public void testTempNodeMax_128() {

        String[] nodes_max = new String[130];
        int ip = 0;

        int port = 10000;
        for (int i = 0; i < 130; i++) {
            nodes_max[i] =
                "p2p://" + UUID.randomUUID().toString() + "@255.255.255." + ip++ + ":" + port++;
        }

        for (String nodeL : nodes_max) {
            INode node = Node.parseP2p(nodeL);
            assertNotNull(node);
            nMgr.addTempNode(node);
            nMgr.seedIpAdd(node.getIpStr());
        }
        assertEquals(128, nMgr.tempNodesSize());
    }

    @Test
    public void testTempNodesTake() {

        String[] nodes = new String[]{
            "p2p://" + nodeId1 + "@" + ip1 + ":" + port2,
            "p2p://" + nodeId2 + "@" + ip1 + ":" + port1,
        };

        NodeMgr mgr = new NodeMgr(p2p, MAX_ACTIVE_NODES, MAX_TEMP_NODES, LOGGER);

        for (String nodeL : nodes) {
            Node node = Node.parseP2p(nodeL);
            mgr.addTempNode(node);
            assert node != null;
            mgr.seedIpAdd(node.getIpStr());
        }
        assertEquals(2, mgr.tempNodesSize());

        INode node;
        while (mgr.tempNodesSize() != 0) {
            node = mgr.tempNodesTake();
            assertEquals(ip1, node.getIpStr());
            assertTrue(node.getIfFromBootList());
            assertTrue(mgr.isSeedIp(ip1));
        }

        assertEquals(0, mgr.tempNodesSize());
    }


    @Test
    public void testTempNodeMax_Any() {

        NodeMgr mgr = new NodeMgr(p2p, 512, 512, LOGGER);
        String[] nodes_max = new String[512];

        int ip = 0;
        int port = 10000;
        for (int i = 0; i < 256; i++) {
            nodes_max[i] =
                "p2p://" + UUID.randomUUID().toString() + "@255.255.255." + ip++ + ":" + port++;
        }

        ip = 0;
        port = 10000;
        for (int i = 256; i < 512; i++) {
            nodes_max[i] =
                "p2p://" + UUID.randomUUID().toString() + "@255.255.254." + ip++ + ":" + port++;

        }

        for (String nodeL : nodes_max) {
            Node node = Node.parseP2p(nodeL);
            if (node == null) {
                System.out.println("node is null");
            }
            mgr.addTempNode(node);
            assert node != null;
            mgr.seedIpAdd(node.getIpStr());

        }
        assertEquals(512, mgr.tempNodesSize());

    }

    @Test
    public void testAddInOutBoundNode() {

        INode node = nMgr.allocNode(ip1, 0);
        node.setChannel(channel);
        try {
            node.setId(nodeId1.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        nMgr.addInboundNode(node);
        INode iNode = nMgr.getInboundNode(channel.hashCode());
        assertEquals(ip1, iNode.getIpStr());

        nMgr.addOutboundNode(node);
        INode oNode = nMgr.getOutboundNode(node.getIdHash());
        assertEquals(ip1, oNode.getIpStr());
    }

    @Test
    public void testGetActiveNodesList() {

        NodeMgr nMgr = new NodeMgr(p2p, MAX_ACTIVE_NODES, MAX_TEMP_NODES, LOGGER);
        INode node = nMgr.allocNode(ip2, 0);

        node.setChannel(channel);
        try {
            node.setId(nodeId2.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        nMgr.addInboundNode(node);
        assertEquals(0, nMgr.activeNodesSize());

        nMgr.movePeerToActive(channel.hashCode(), "inbound");

        assertEquals(1, nMgr.activeNodesSize());

        List<INode> active = nMgr.getActiveNodesList();

        for (INode activeN : active) {
            assertEquals(ip2, activeN.getIpStr());
        }

    }

    @Test
    public void testDropActive() {
        INode node = nMgr.allocNode(ip2, 0);

        node.setChannel(channel);
        try {
            node.setId(nodeId2.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        nMgr.addInboundNode(node);
        assertEquals(0, nMgr.activeNodesSize());

        nMgr.movePeerToActive(channel.hashCode(), "inbound");
        assertEquals(1, nMgr.activeNodesSize());

        //will not drop
        nMgr.dropActive(node.getIdHash()-1, "close");
        assertEquals(1, nMgr.activeNodesSize());

        //will drop
        nMgr.dropActive(node.getIdHash(), "close");
        assertEquals(0, nMgr.activeNodesSize());
    }

    @Test
    public void testBan() {
        INode node = nMgr.allocNode(ip2, 0);

        node.setChannel(channel);
        try {
            node.setId(nodeId2.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        nMgr.addInboundNode(node);
        assertEquals(0, nMgr.activeNodesSize());

        nMgr.movePeerToActive(channel.hashCode(), "inbound");

        assertEquals(1, nMgr.activeNodesSize());

        assertTrue(node.getPeerMetric().notBan());
        nMgr.ban(node.getIdHash());
        assertFalse(node.getPeerMetric().notBan());

    }

    @Test
    public void testTimeoutInbound() {

        INode node = nMgr.allocNode(ip2, 0);
        addNodetoInbound(node, UUID.fromString(nodeId1));

        //Sleep for MAX_INBOUND_TIMEOUT
        try {
            Thread.sleep(10_001);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        nMgr.timeoutCheck();
        assertNull(nMgr.getInboundNode(channel.hashCode()));
    }


    @Test
    public void testTimeoutOutbound() {
        INode node = nMgr.allocNode(ip2, 0);
        addNodetoOutbound(node, UUID.fromString(nodeId1));

        //Sleep for MAX_OUTBOUND_TIMEOUT
        try {
            Thread.sleep(20_001);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        nMgr.timeoutCheck();
        assertNull(nMgr.getOutboundNode(node.getIdHash()));
    }

    @Test
    public void testAllocate() {
        INode node = nMgr.allocNode(ip2, 0);
        assertNotNull(node);
        assertFalse(node.getIfFromBootList());

        nMgr.seedIpAdd(ip2);
        node = nMgr.allocNode(ip2, 0);
        assertNotNull(node);
        assertTrue(node.getIfFromBootList());
    }

    @Test
    public void testGetOutBoundNode() {
        INode node = nMgr.allocNode(ip2, 0);
        addNodetoOutbound(node, UUID.fromString(nodeId1));

        //Sleep for MAX_OUTBOUND_TIMEOUT
        try {
            Thread.sleep(20_001);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        nMgr.timeoutCheck();
        assertNull(nMgr.getOutboundNode(node.getIdHash()));
    }

    @Test
    public void testMoveOutboundToActive() {
        INode node = nMgr.allocNode(ip2, 0);
        addNodetoOutbound(node, UUID.fromString(nodeId1));

        nMgr.movePeerToActive(node.getIdHash(), "outbound");
        assertNull(nMgr.getOutboundNode(node.getIdHash()));

        INode activeNode = nMgr.getActiveNode(node.getIdHash());
        assertNotNull(activeNode);
        assertEquals(node, activeNode);
    }

    @Test
    public void testMoveInboundToActive() {
        INode node = nMgr.allocNode(ip2, 0);
        addNodetoInbound(node, UUID.fromString(nodeId1));

        nMgr.movePeerToActive(node.getChannel().hashCode(), "inbound");
        assertNull(nMgr.getInboundNode(node.getChannel().hashCode()));

        INode activeNode = nMgr.getActiveNode(node.getIdHash());
        assertNotNull(activeNode);
        assertEquals(node, activeNode);
    }

    @Test
    public void testTimeoutActive() throws InterruptedException {
        INode node = nMgr.allocNode(ip2, 0);
        addNodetoInbound(node, UUID.fromString(nodeId1));

        nMgr.movePeerToActive(node.getChannel().hashCode(), "inbound");
        INode activeNode = nMgr.getActiveNode(node.getIdHash());
        assertNotNull(activeNode);
        assertEquals(node, activeNode);

        Thread.sleep(10_001);

        nMgr.timeoutCheck();
        assertNull(nMgr.getActiveNode(node.getIdHash()));
    }

    @Test
    public void testGetActiveNodesMap() {
        INode node = nMgr.allocNode(ip2, 0);
        addNodetoInbound(node, UUID.fromString(nodeId1));

        nMgr.movePeerToActive(node.getChannel().hashCode(), "inbound");

        Map activeMap = nMgr.getActiveNodesMap();
        assertNotNull(activeMap);
        assertEquals(1, activeMap.size());
    }

    @Test
    public void testNotAtOutBoundList() {
        INode node = nMgr.allocNode(ip2, 0);
        addNodetoOutbound(node, UUID.fromString(nodeId1));
        assertFalse(nMgr.notAtOutboundList(node.getIdHash()));

        node = nMgr.allocNode(ip1, 0);
        assertTrue(nMgr.notAtOutboundList(node.getIdHash()));
    }

    @Test
    public void testGetRandom() {
        assertNull(nMgr.getRandom());

        INode node = nMgr.allocNode(ip2, 0);
        addNodetoInbound(node, UUID.fromString(nodeId1));
        nMgr.movePeerToActive(node.getChannel().hashCode(), "inbound");

        INode nodeRandom = nMgr.getRandom();
        assertNotNull(node);
        assertEquals(node, nodeRandom);
    }

    @Test
    public void testMovePeerToActive() {
        INode node = nMgr.allocNode(ip2, 0);
        node.setChannel(channel);
        nMgr.movePeerToActive(node.getChannel().hashCode(), "inbound");
        assertTrue(nMgr.getActiveNodesMap().isEmpty());
    }

    @Test
    public void testMovePeerToActive2() {
        nMgr = new NodeMgr(p2p, 2, 2, LOGGER);
        INode node = nMgr.allocNode(ip2, 0);
        addNodetoOutbound(node, UUID.fromString(nodeId1));
        nMgr.movePeerToActive(node.getIdHash(), "outbound");

        node = nMgr.allocNode(ip1, 0);
        addNodetoOutbound(node, UUID.fromString(nodeId2));
        nMgr.movePeerToActive(node.getIdHash(), "outbound");

        assertEquals(2, nMgr.getActiveNodesMap().size());

        node = nMgr.allocNode(ip1, port1);
        addNodetoOutbound(node, UUID.randomUUID());
        nMgr.movePeerToActive(node.getIdHash(), "outbound");
        assertEquals(2, nMgr.getActiveNodesMap().size());
    }

    @Test
    public void testMovePeerToActive3() {
        INode node = nMgr.allocNode(ip2, 0);
        addNodetoOutbound(node, UUID.fromString(nodeId1));

        when(p2p.getSelfIdHash()).thenReturn(node.getIdHash());

        nMgr.movePeerToActive(node.getIdHash(), "outbound");
        assertTrue(nMgr.getActiveNodesMap().isEmpty());
    }

    @Test
    public void testShutDown() {
        INode node = nMgr.allocNode(ip2, 0);
        addNodetoOutbound(node, UUID.randomUUID());

        node = nMgr.allocNode(ip1, 0);
        addNodetoOutbound(node, UUID.randomUUID());

        nMgr.movePeerToActive(node.getIdHash(), "outbound");

        node = nMgr.allocNode("1.1.1.1", 0);
        addNodetoInbound(node, UUID.randomUUID());

        assertEquals(1, nMgr.activeNodesSize());

        nMgr.shutdown();
        assertTrue(nMgr.getActiveNodesMap().isEmpty());
    }

    @Test
    public void testDumpNodeInfo() {
        String dump = nMgr.dumpNodeInfo("testId", false);
        assertNotNull(dump);

        INode node = nMgr.allocNode(ip1, 0);
        addNodetoOutbound(node, UUID.randomUUID());
        nMgr.movePeerToActive(node.getIdHash(), "outbound");

        String dump2 = nMgr.dumpNodeInfo("testId", false);
        assertEquals(dump.length(), dump2.length());

        String dump3 = nMgr.dumpNodeInfo("testId", true);
        assertTrue(dump3.length() > dump2.length());
    }

    @Test
    public void testDumpNodeInfo2() {
        String dump = nMgr.dumpNodeInfo("testId", false);
        assertNotNull(dump);

        nMgr.seedIpAdd(ip1);
        INode node = nMgr.allocNode(ip1, 0);
        byte[] rHash = new byte[32];
        r.nextBytes(rHash);
        node.updateStatus(r.nextLong(), rHash, BigInteger.ONE);
        addNodetoOutbound(node, UUID.randomUUID());
        nMgr.movePeerToActive(node.getIdHash(), "outbound");

        String dump2 = nMgr.dumpNodeInfo("testId", false);
        assertTrue(dump2.length() > dump.length());

        String dump3 = nMgr.dumpNodeInfo("testId", true);
        assertEquals(dump3.length(), dump2.length());
    }

    @Test
    public void testDumpNodeInfo3() {
        String dump = nMgr.dumpNodeInfo("testId", false);
        assertNotNull(dump);
        addActiveNode();
        addActiveNode();
        addActiveNode();

        String dump2 = nMgr.dumpNodeInfo("testId", true);
        LOGGER.info(dump2);
        assertTrue(dump2.length() > dump.length());
    }

}
