package com.example.UDPFileTransfer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Packet implements Serializable {

    public static List<Packet> packets = new ArrayList<Packet>();

    private String seqNum;
    private String data;
    private int checksum;

    public Packet(String seqNum, String data, int checkSum) {
        this.seqNum = seqNum;
        this.data = data;
        this.checksum = checkSum;
    }

    public String getSeqNum() {
        return seqNum;
    }

    public String getData() {
        return data;
    }

    public int getChecksum() {
        return checksum;
    }

    public static List<Packet> getPackets() {return packets;}

}
