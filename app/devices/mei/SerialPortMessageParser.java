/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.mei;

import devices.serial.SerialPortAdapterInterface;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import play.Logger;

/**
 *
 * @author adji
 */
public class SerialPortMessageParser {

    Queue<MeiEbdsAcceptorMsg> messageQueue = new ConcurrentLinkedQueue<MeiEbdsAcceptorMsg>();

    // this must be a kind of circular buffer in the future.
    final byte[] buffer = new byte[256];
    int idx = 0;
    final SerialPortAdapterInterface serialPort;

    public SerialPortMessageParser(SerialPortAdapterInterface serialPort) {
        this.serialPort = serialPort;
    }

    public void invalidateBuffer() {
        int stx;
        for (stx = 0; stx < idx && buffer[stx] != 0x02; stx++);
        if (stx >= idx) {
            idx = 0;
            return;
        }
        int i;
        for (i = stx; i < idx; i++) {
            buffer[ i - stx] = buffer[ i];
        }
        idx = i;
        validateBuffer();
        return;
    }

    public void validateBuffer() {
        if (buffer[ 0] != 0x02) {
            invalidateBuffer();
        }
        if (idx < 4) {
            return;
        }
        int length = buffer[ 2];
        if (idx < length) {
            return;
        }
        // at least two bytes.
        if (buffer[length - 2] != 0x3) {
            invalidateBuffer();
            return;
        }
        // calculate the checksum
        int checksum = 0;
        for (int i = 0; i < length - 2; i++) {
            checksum = checksum ^ buffer[ i];
        }
        if (buffer[length - 1] != checksum) {
            invalidateBuffer();
            return;
        }
        //buffer valid
        messageQueue.add(new MeiEbdsAcceptorMsg(buffer));
        int i = length;
        for (; i < idx; i++) {
            buffer[ i - length] = buffer[ i];
        }
        idx -= length;
        validateBuffer();
    }

    public void onSerialPortEvent(SerialPortAdapterInterface s, byte data) {
        if (s != serialPort) {
            Logger.error("Serial port event from port %s don't match my port %s", s, serialPort);
            return;
        }
        if (idx >= 256) {
            Logger.error("buffer overrun");
            idx = 0;
            return;
        }
        buffer[ idx++] = data;
        validateBuffer();
    }
}
