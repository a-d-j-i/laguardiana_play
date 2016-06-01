package devices.glory.command;

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
public class BatchDataTransmition extends CommandWithAckResponse {

    public BatchDataTransmition( int bills[] ) {
        super( ( byte ) 0x32, "Batch Data Transmission" );
        if ( bills.length != 32 ) {
            setError( "Need 32 integers describing the batch" );
            return;
        }
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            for( int i : bills ) {
                if ( i < 0 || i > 999 ) {
                    setError( String.format( "Error setting bill batch %d", i ) );
                    i = 0;
                }
                bo.write( String.format( "%03d", i ).getBytes() );
            }
            bo.close();

            setCmdData( bo.toByteArray() );

        } catch ( IOException e ) {
            setError( e.getMessage() );
        }

    }
}
