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
package org.aion.mcf.config;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

public final class CfgNetP2p {

    CfgNetP2p() {
        this.ip = "127.0.0.1";
        this.port = 30303;
        this.discover = false;
        this.showStatus = false;
        this.showLog = false;
        this.maxTempNodes = 128;
        this.maxActiveNodes = 128;
        this.errorTolerance = 50;
        this.clusterNodeMode = false;
        this.syncOnlyMode = false;
    }

    private String ip;

    private int port;

    private boolean discover;

    private boolean showStatus;

    private boolean showLog;

    private boolean clusterNodeMode;

    private boolean syncOnlyMode;

    private int maxTempNodes;

    private int maxActiveNodes;

    private int errorTolerance;

    public void fromXML(final XMLStreamReader sr) throws XMLStreamException {
        loop:
        while (sr.hasNext()) {
            int eventType = sr.next();
            switch (eventType) {
                case XMLStreamReader.START_ELEMENT:
                    String elelmentName = sr.getLocalName().toLowerCase();
                    switch (elelmentName) {
                        case "ip":
                            this.ip = Cfg.readValue(sr);
                            break;
                        case "port":
                            this.port = Integer.parseInt(Cfg.readValue(sr));
                            break;
                        case "discover":
                            this.discover = Boolean.parseBoolean(Cfg.readValue(sr));
                            break;
                        case "show-status":
                            this.showStatus = Boolean.parseBoolean(Cfg.readValue(sr));
                            break;
                        case "show-log":
                            this.showLog = Boolean.parseBoolean(Cfg.readValue(sr));
                            break;
                        case "cluster-node-mode":
                            this.clusterNodeMode = Boolean.parseBoolean(Cfg.readValue(sr));
                            break;
                        case "sync-only-mode":
                            this.syncOnlyMode = Boolean.parseBoolean(Cfg.readValue(sr));
                            break;
                        case "max-temp-nodes":
                            this.maxTempNodes = Integer.parseInt(Cfg.readValue(sr));
                            break;
                        case "max-active-nodes":
                            this.maxActiveNodes = Integer.parseInt(Cfg.readValue(sr));
                            break;
                        case "err-tolerance":
                            this.errorTolerance = Integer.parseInt(Cfg.readValue(sr));
                            break;
                        default:
                            // Cfg.skipElement(sr);
                            break;
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    break loop;
            }
        }
    }

    String toXML() {
        final XMLOutputFactory output = XMLOutputFactory.newInstance();
        XMLStreamWriter xmlWriter;
        String xml;
        try {
            Writer strWriter = new StringWriter();
            xmlWriter = output.createXMLStreamWriter(strWriter);
            xmlWriter.writeCharacters("\r\n\t\t");
            xmlWriter.writeStartElement("p2p");

            xmlWriter.writeCharacters("\r\n\t\t\t");
            xmlWriter.writeStartElement("ip");
            xmlWriter.writeCharacters(this.getIp());
            xmlWriter.writeEndElement();

            xmlWriter.writeCharacters("\r\n\t\t\t");
            xmlWriter.writeStartElement("port");
            xmlWriter.writeCharacters(this.getPort() + "");
            xmlWriter.writeEndElement();

            xmlWriter.writeCharacters("\r\n\t\t\t");
            xmlWriter.writeStartElement("discover");
            xmlWriter.writeCharacters(this.discover + "");
            xmlWriter.writeEndElement();

            xmlWriter.writeCharacters("\r\n\t\t\t");
            xmlWriter.writeStartElement("show-status");
            xmlWriter.writeCharacters(this.showStatus + "");
            xmlWriter.writeEndElement();

            xmlWriter.writeCharacters("\r\n\t\t\t");
            xmlWriter.writeStartElement("max-temp-nodes");
            xmlWriter.writeCharacters(this.maxTempNodes + "");
            xmlWriter.writeEndElement();

            xmlWriter.writeCharacters("\r\n\t\t\t");
            xmlWriter.writeStartElement("max-active-nodes");
            xmlWriter.writeCharacters(this.maxActiveNodes + "");
            xmlWriter.writeEndElement();

            xmlWriter.writeCharacters("\r\n\t\t");
            xmlWriter.writeEndElement();
            xml = strWriter.toString();
            strWriter.flush();
            strWriter.close();
            xmlWriter.flush();
            xmlWriter.close();
            return xml;
        } catch (IOException | XMLStreamException e) {
            return "";
        }
    }

    public void setIp(final String _ip) {
        this.ip = _ip;
    }

    public void setPort(final int _port) {
        this.port = _port;
    }

    public String getIp() {
        return this.ip;
    }

    public int getPort() {
        return this.port;
    }

    public boolean getDiscover() {
        return this.discover;
    }

    public boolean getShowStatus() {
        return this.showStatus;
    }

    public boolean getShowLog() {
        return this.showLog;
    }

    public int getMaxTempNodes() {
        return maxTempNodes;
    }

    public int getMaxActiveNodes() {
        return maxActiveNodes;
    }

    public int getErrorTolerance() {
        return errorTolerance;
    }

    public boolean inClusterNodeMode() {
        return clusterNodeMode;
    }

    public boolean inSyncOnlyMode() {
        return syncOnlyMode;
    }
}
