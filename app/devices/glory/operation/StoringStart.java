package devices.glory.operation;

import java.io.IOException;

/*
 * This command starts storage.
 * Upon SR1/ Storing start request from the device, TM transmits.
 * (For SR1, refer to 3-3-5. Details of Responses.)
 * DATA:
 * Sequence number of storing.(000 to 999)
 * You should attach the storing process's sequential
 * number. Because DE-50 can not be synchronized with TM.
 */
public class StoringStart extends OperationWithAckResponse {

    public StoringStart( int sequenceNumber ) {
        super( ( byte ) 0x34, "Storing Start" );
        setCmdData( String.format( "%03d", sequenceNumber ).getBytes() );
    }
}
