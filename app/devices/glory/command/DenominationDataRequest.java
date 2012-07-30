package devices.glory.command;

import java.util.ArrayList;
import play.Logger;

/*
 * This command is return Denomination Data.
 */
public class DenominationDataRequest extends CommandWithDataResponse {

    public class Denomination {

        public int idx;
        public String currencyCode;
        public boolean newVal;
        public boolean oldVal;
        public Integer value;
    }
    private ArrayList<Denomination> denominationData = new ArrayList<Denomination>();

    public DenominationDataRequest() {
        super( ( byte ) 0x44, "DenominationDataRequest" );
    }

    @Override
    public CommandWithDataResponse setResult( byte[] dr ) {
        super.setResult( dr );
        if ( getError() != null ) {
            return this;
        }

        if ( data == null || data.length == 0 ) {
            return this;
        }
        for ( int i = 0; i < data.length; i += 10 ) {
            if ( i + 10 <= data.length ) {
                Denomination d = new Denomination();
                d.idx = i / 10;
                byte[] b = { data[i], data[i + 1], data[i + 2] };
                d.currencyCode = new String( b );
                d.newVal = ( data[ i + 3] != 0x30 );
                d.oldVal = ( data[ i + 4] != 0x30 );

                Double dd = ( Math.pow( 10, getDigit( data[i + 6] ) ) * ( getDigit( data[i + 7] ) * 100 + getDigit( data[i + 8] ) * 10 + getDigit( data[i + 9] ) * 1 ) );
                d.value = dd.intValue();
                if ( data[ i + 5] != 0x30 ) {
                    d.value = -d.value;
                }
                denominationData.add( d );
            }
        }
        //data.length;
        return this;
    }

    public ArrayList<Denomination> getDenominationData() {
        return denominationData;
    }
}
