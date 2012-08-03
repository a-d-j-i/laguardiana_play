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
    
    private CountData countData;
    
    public Count( ThreadCommandApi threadCommandApi, Map<Integer, Integer> desiredQuantity ) {
        super( threadCommandApi );
        countData = new CountData( desiredQuantity );
        
    }
    
    static public class CountData extends CommandData {
        
        public CountData( Map<Integer, Integer> desiredQuantity ) {
            boolean isb = false;
            for ( Integer k : desiredQuantity.keySet() ) {
                Integer v = desiredQuantity.get( k );
                this.desiredQuantity.put( k, v );
                if ( v != 0 ) {
                    isb = true;
                }
            }
            this.isBatch = isb;
        }
        private Map< Integer, Integer> currentQuantity = new HashMap<Integer, Integer>();
        private boolean storeDeposit = false;
        private final Map<Integer, Integer> desiredQuantity = new HashMap<Integer, Integer>();
        private final boolean isBatch;
        
        private Map< Integer, Integer> getCurrentQuantity() {
            rlock();
            try {
                return currentQuantity;
            } finally {
                runlock();
            }
        }
        
        private void setCurrentQuantity( Map<Integer, Integer> billData ) {
            wlock();
            try {
                this.currentQuantity = billData;
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
    
    @Override
    public void execute() {
        boolean batchEnd = false;
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
        threadCommandApi.setSuccess( "Put the bills on the hoper" );
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
                        if ( countData.isBatch && batchEnd ) {
                            sleep();
                            break;
                        }
                        if ( gloryStatus.isHopperBillPresent() ) {
                            if ( batchCountStart() ) { // batch end
                                batchEnd = true;
                            }
                        }
                    }
                // dont break;
                case counting:
                case waiting:
                    if ( !sendGCommand( new devices.glory.command.CountingDataRequest() ) ) {
                        return;
                    }
                    Map<Integer, Integer> bills = gloryStatus.getBills();
                    countData.setCurrentQuantity( bills );
                    sleep();
                    break;
                case being_store:
                    sleep();
                    break;
                
                case counting_start_request:
                    if ( countData.isBatch && batchEnd ) {
                        threadCommandApi.setSuccess( "Counting Done" );
                        return;
                    }
                    if ( batchCountStart() ) { // batch end
                        batchEnd = true;
                    }
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
        countData.storeDeposit( true );
    }
    
    public Map<Integer, Integer> getCurrentQuantity() {
        return countData.getCurrentQuantity();
    }
    
    public Map<Integer, Integer> getDesiredQuantity() {
        return countData.desiredQuantity;
    }
    
    boolean batchCountStart() {
        boolean batchEnd = true;
        int[] bills = new int[ 32 ];
        
        if ( countData.isBatch ) {
            Logger.debug( "ISBATCH" );
            if ( !sendGCommand( new devices.glory.command.CountingDataRequest() ) ) {
                return false;
            }
            Map<Integer, Integer> currentQuantity = gloryStatus.getBills();
            for ( Integer slot : currentQuantity.keySet() ) {
                int desired = 0;
                if ( countData.desiredQuantity.get( slot ) != null ) {
                    desired = countData.desiredQuantity.get( slot ).intValue();
                }
                if ( currentQuantity == null || slot >= 32 ) {
                    threadCommandApi.setError( String.format( "Invalid bill index %d", slot ) );
                } else {
                    int value = currentQuantity.get( slot );
                    if ( value > desired ) {
                        threadCommandApi.setError( String.format( "Invalid bill value %d %d %d", slot, value, desired ) );
                        return true;
                    }
                    bills[ slot] = desired - value;
                    Logger.debug( "slot %d batch billls : %d desired %d value %d", slot, bills[ slot], desired, value );
                }
                if ( bills[ slot] != 0 ) {
                    batchEnd = false;
                }
            }
        }
        if ( !countData.isBatch || !batchEnd ) {
            sendGloryCommand( new devices.glory.command.BatchDataTransmition( bills ) );
        }
        return batchEnd;
    }
}
