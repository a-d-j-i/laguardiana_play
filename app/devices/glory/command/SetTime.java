package devices.glory.command;

import java.text.SimpleDateFormat;
import java.util.Date;

/* 
 * You set the DATA by ASCII.
 * h: upper data, l: lower data
 * Update the time on DE.
 */
public class SetTime extends OperationWithAckResponse {

    public SetTime( Date time ) {
        super( ( byte ) 0x51, "SetTime" );
        SimpleDateFormat df = new SimpleDateFormat();
        df.applyPattern("yyyyMMddhhmmss");
        setCmdData( df.format( time ).getBytes() );
    }
}
