/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager.command;

import devices.glory.GloryStatus;
import devices.glory.command.GloryCommandAbstract;
import devices.glory.manager.ManagerThread;
import play.Logger;

/**
 *
 * @author adji
 */
abstract public class ManagerCommandAbstract {

    abstract public void execute( ManagerThread thread );

    static void gotoNeutral( ManagerThread thread ) {
        if ( !sense( thread ) ) {
            return;
        }
        switch ( thread.getStatus().getSr1Mode() ) {
            case abnormal_device:
                switch ( thread.getStatus().getD1Mode() ) {
                    case deposit:
                        if ( !sendGloryCommand( thread, new devices.glory.command.RemoteCancel() ) ) {
                            return;
                        }
                    // dont break;
                    case neutral:
                        if ( !sendGloryCommand( thread, new devices.glory.command.SetErrorRecoveryMode() ) ) {
                            return;
                        }
                        break;
                    case normal_error_recovery_mode:
                        break;
                    default:
                        thread.setError( String.format( "Abnoremal device Invalid D1 mode %s", thread.getStatus().getD1Mode().name() ) );
                        return;
                }
                resetDevice( thread );
                break;
            default:
                break;
        }

        switch ( thread.getStatus().getD1Mode() ) {
            case normal_error_recovery_mode:
            case storing_error_recovery_mode:
            case deposit:
                switch ( thread.getStatus().getSr1Mode() ) {
                    case storing_start_request:
                        if ( !sendGloryCommand( thread, new devices.glory.command.OpenEscrow() ) ) {
                            break;
                        }
                    // dont break
                    case escrow_close_request:
                    case being_restoration:
                        WaitForEmptyEscrow( thread );
                        break;
                    case counting_start_request:
                        thread.setError( "Remove bills from hoper" );
                        return;
                    default:
                        break;
                }
                sendGloryCommand( thread, new devices.glory.command.RemoteCancel() );
                break;
            case manual:
            case initial:
                if ( !sendGloryCommand( thread, new devices.glory.command.RemoteCancel() ) ) {
                    return;
                }
                if ( thread.getStatus().getD1Mode() != GloryStatus.D1Mode.neutral ) {
                    thread.setError( String.format( "cant set neutral mode d1 (%s) mode not neutral", thread.getStatus().getD1Mode().name() ) );
                }
                break;
            case neutral:
                break;
            default:
                thread.setError( String.format( "Invalid D1 mode %s", thread.getStatus().getD1Mode().name() ) );
                break;
        }
    }

    static void WaitForEmptyEscrow( ManagerThread thread ) {
        boolean keepRunning = true;
        while ( !thread.mustStop() && keepRunning ) {
            if ( !sense( thread ) ) {
                return;
            }
            switch ( thread.getStatus().getSr1Mode() ) {
                case being_restoration:
                    thread.sleep();
                    break;
                case escrow_close_request:
                    if ( !sendGloryCommand( thread, new devices.glory.command.CloseEscrow() ) ) {
                        return;
                    }
                    break;
                case escrow_open:
                case escrow_close:
                    thread.sleep();
                    break;
                case waiting:
                    keepRunning = false;
                    break;
                case abnormal_device:
                    setAbnormalDevice( thread );
                    keepRunning = false;
                    break;
                default:
                    thread.setError( String.format( "WaitForEmptyEscrow invalid sr1 mode %s", thread.getStatus().getSr1Mode().name() ) );
                    break;
            }
        }
    }

    static boolean sendGloryCommand( ManagerThread thread, GloryCommandAbstract cmd ) {
        if ( cmd != null ) {
            if ( !thread.sendGCommand( cmd ) ) {
                return false;
            }
        }
        return sense( thread );
    }

    static boolean sense( ManagerThread thread ) {
        if ( !thread.sendGCommand( new devices.glory.command.Sense() ) ) {
            return false;
        }
        Logger.debug( String.format( "D1Mode %s SR1 Mode : %s", thread.getStatus().getD1Mode().name(), thread.getStatus().getSr1Mode().name() ) );
        return true;
    }

    static void resetDevice( ManagerThread thread ) {
        if ( !sendGloryCommand( thread, new devices.glory.command.ResetDevice() ) ) {
            return;
        }
        boolean keepRunning = true;
        while ( !thread.mustCancel() && keepRunning ) {
            if ( !sense( thread ) ) {
                return;
            }
            switch ( thread.getStatus().getSr1Mode() ) {
                case being_reset:
                case being_restoration:
                    thread.sleep();
                    break;
                default:
                    keepRunning = false;
                    break;
            }
        }
    }

    static void setAbnormalDevice( ManagerThread thread ) {
        thread.setError( String.format( "Abnormal device, todo: get the flags" ) );
    }

    static void setBills( ManagerThread thread ) {
        sendGloryCommand( thread, new devices.glory.command.CountingDataRequest() );
        thread.getStatus().setSetBillDataFromGlory();
    }
}