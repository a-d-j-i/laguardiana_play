package devices.glory.operation;

import java.text.SimpleDateFormat;
import java.util.Date;

/* 
 * You set the DATA by ASCII.
 * h: upper data, l: lower data
 * Update the time on DE.
 */
public class SetTime extends OperationWithAckResponse {

    final Date time;

    public SetTime(Date time) {
        super(0x51);
        this.time = time;
    }

    @Override
    public byte[] getCmdStr() {
        SimpleDateFormat df = new SimpleDateFormat();
        df.applyPattern("yyyyMMddhhmmss");
        return getCmdStrFromData(df.format(time).getBytes());
    }
}
