package com.example.UDPFileTransfer;

import java.util.zip.CRC32;

public class UDPChecksum {

    public static int calculateChecksum(byte[] data) {
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        return (int) crc32.getValue();
    }

    public static boolean verifyChecksum(byte[] data, int receivedChecksum) {
        int calculatedChecksum = calculateChecksum(data);
        return receivedChecksum == calculatedChecksum;
    }
}
