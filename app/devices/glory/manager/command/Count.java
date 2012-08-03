/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager.command;

import devices.glory.GloryStatus;
import devices.glory.manager.Manager.ThreadCommandApi;
import java.util.HashMap;
import java.util.Map;
import play.Logger;

/**
 *
 * @author adji
 */
public class Count extends ManagerCommandAbstract {

    public Count( ThreadCommandApi threadCommandApi, int[] bills ) {
        super( threadCommandApi );
        this.batchBills = bills;
    }

    static public class CountData extends CommandData {

        private Map< Integer, Integer> billData = new HashMap<Integer, Integer>();
        private boolean depositOk = false;
        private boolean storeDeposit = false;

        private Map< Integer, Integer> getBillData() {
            rlock();
            try {
                return billData;
            } finally {
                runlock();
            }
        }

        private void setBillData( Map<Integer, Integer> billData ) {
            wlock();
            try {
                this.billData = billData;
            } finally {
                wunlock();
            }
        }

        private boolean isDepositOk() {
            rlock();
            try {
                return depositOk;
            } finally {
                runlock();
            }
        }

        private void setDepositOk( boolean depositOk ) {
            wlock();
            try {
                this.depositOk = depositOk;
            } finally {
                wunlock();
            }
        }

        private boolean needToStoreDeposit() {
            rlock();
            try {
                return storeDeposit;
            } finally {
                runlock();
            }
        }

        private void storeDeposit( boolean storeDeposit ) {
            wlock();
            try {
                this.storeDeposit = storeDeposit;
            } finally {
                wunlock();
            }
        }
    }
    private int[] batchBills = new int[ 32 ];
    private CountData countData = new CountData();

    @Override
    public void execute() {
        gotoNeutral( true, false );
        if ( !sendGloryCommand( new devices.glory.command.SetDepositMode() ) ) {
            return;
        }
        if ( !sense() ) {
            return;
        }
        if ( gloryStatus.getD1Mode() != GloryStatus.D1Mode.deposit ) {
            threadCommandApi.setError( String.format( "cant set deposit mode d1 (%s) mode not neutral", gloryStatus.getD1Mode().name() ) );
            return;
        }
        while ( !mustCancel() ) {
            Logger.debug( "Counting" );
            if ( !sense() ) {
                return;
            }
            switch ( gloryStatus.getSr1Mode() ) {
                case storing_start_request:
                    if ( countData.needToStoreDeposit() ) {
                        sendGloryCommand( new devices.glory.command.StoringStart( 0 ) );
                        break;
                    } else {
                        if ( gloryStatus.isHopperBillPresent() ) {
                            resendBatch();
                        }
                    }
                // dont break;
                case counting:
                case waiting:
                    if ( !sendGCommand( new devices.glory.command.CountingDataRequest() ) ) {
                        return;
                    }
                    Map<Integer, Integer> bills = gloryStatus.getBills();
                    countData.setBillData( bills );
                    sleep();
                    break;
                case being_store:
                    sleep();
                    break;

                case counting_start_request:
                    resendBatch();
                    break;
                case abnormal_device:
                    threadCommandApi.setError( String.format( "Abnormal device, todo: get the flags" ) );
                    return;
                case storing_error:
                    threadCommandApi.setError( String.format( "Storing error, todo: get the flags" ) );
                    return;
                default:
                    threadCommandApi.setError( String.format( "invalid sr1 mode", gloryStatus.getSr1Mode().name() ) );
                    return;
            }
        }
        gotoNeutral( true, false );
    }

    public void storeDeposit( int sequenceNumber ) {
        countData.setDepositOk( false );
        countData.storeDeposit( true );
    }

    public Map<Integer, Integer> getBillData() {
        return countData.getBillData();
    }

    void resendBatch() {
        Map<Integer, Integer> bd = countData.getBillData();
        for ( Integer slot : bd.keySet() ) {
            Integer value = bd.get( slot );
            if ( slot >= batchBills.length ) {
                threadCommandApi.setError( String.format( "Invalid bill index %d", slot ) );
            } else {
                if ( value > batchBills[ slot] ) {
                    threadCommandApi.setError( String.format( "Invalid bill value %d %d", slot, value ) );
                }
                batchBills[ slot] -= value;
            }
        }
        sendGloryCommand( new devices.glory.command.BatchDataTransmition( batchBills ) );
    }
}
