/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager.command;

import devices.glory.GloryStatus;
import devices.glory.command.CommandWithCountingDataResponse.Bill;
import devices.glory.manager.ManagerThread;
import java.util.ArrayList;

/**
 *
 * @author adji
 */
public class StartCounting extends ManagerCommandAbstract {

    int[] bills = new int[ 32 ];
    boolean isBatch;

    public StartCounting( int[] bills ) {
        this.bills = bills;
        isBatch = false;
        for ( int i = 0; i < bills.length; i++ ) {
            if ( bills[i] != 0 ) {
                isBatch = true;
                break;
            }
        }
    }

    @Override
    public void execute( ManagerThread thread ) {
        thread.getStatus().clearBillData();
        gotoNeutral( thread );
        if ( !sendGloryCommand( thread, new devices.glory.command.SetDepositMode() ) ) {
            return;
        }
        if ( !sense( thread ) ) {
            return;
        }
        if ( thread.getStatus().getD1Mode() != GloryStatus.D1Mode.deposit ) {
            thread.setError( String.format( "cant set deposit mode d1 (%s) mode not neutral", thread.getStatus().getD1Mode().name() ) );
            return;
        }
        boolean keepRunning = true;
        while ( !thread.mustCancel() && keepRunning ) {

            if ( !sense( thread ) ) {
                return;
            }
            switch ( thread.getStatus().getSr1Mode() ) {
                case storing_start_request:
                    if ( thread.getStatus().isHopperBillPresent() ) {
                        resendBatch( thread );
                    }
                case counting:
                case waiting:
                    setBills( thread );
                    thread.sleep();
                    break;
                case counting_start_request:
                    resendBatch( thread );
                    break;
                case abnormal_device:
                    setAbnormalDevice( thread );
                    keepRunning = false;
                    break;
                default:
                    thread.setError( String.format( "invalid sr1 mode", thread.getStatus().getSr1Mode().name() ) );
                    break;
            }
        }
        thread.getStatus().clearBillData();
    }

    void resendBatch( ManagerThread thread ) {
        ArrayList<Bill> bd = thread.getStatus().getBillData();
        for ( Bill b : bd ) {
            if ( b.idx >= bills.length ) {
                thread.setError( String.format( "Invalid bill index %d", b.idx ) );
            } else {
                if ( b.value > bills[ b.idx] ) {
                    thread.setError( String.format( "Invalid bill value %d %d", b.idx, b.value ) );
                }
                bills[ b.idx] -= b.value;
            }
        }
        sendGloryCommand( thread, new devices.glory.command.BatchDataTransmition( bills ) );
    }
}
