package devices.glory.operation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/*
 * get partial result and keep counting
 * Batch data for each bill type (MAX 32 denominations) is received,
 * and counting starts.
 * Add Batch Data (32(Number of bill types) x 3byte)
 * Upon SR1/counting start request (Q) from the device, TM transmits.
 * (For SR1, refer to 3-3-5. Details of Responses.)
 */
public class BatchDataTransmition extends OperationWithAckResponse {

    final int[] bills;

    public BatchDataTransmition(int[] bills) {
        super(0x32);
        this.bills = bills;
    }

    @Override
    public byte[] getCmdStr() {
        if (bills.length != 32) {
            throw new IllegalArgumentException("Need 32 integers describing the batch");
        }
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            for (int i : bills) {
                if (i < 0 || i > 999) {
                    throw new IllegalArgumentException(String.format("Error setting bill batch %d", i));
                }
                bo.write(String.format("%03d", i).getBytes());
            }
            bo.close();

            return getCmdStrFromData(bo.toByteArray());

        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

    }
}
