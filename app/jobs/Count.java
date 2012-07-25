package jobs;

import models.CounterStatus;
import play.jobs.Job;

public class Count extends Job<String> {

    CounterStatus status;

    // TODO: Pass bill types
    public Count( CounterStatus status ) {
        this.status = status;
    }

    @Override
    public String doJobWithResult() throws Exception {
        if ( status.getCurrentState() != CounterStatus.CurrentState.COUNT ) {
            return "Invalid Status";
        }

        if ( !status.gotoNeutral() ) {
            return status.getError();
        }
        if ( !status.sendGloryCommand( new devices.glory.command.SetDepositMode() ) ) {
            return status.getError();
        }
        int[] bills = new int[ 32 ];
        while ( !status.isCancel() ) {
            status.Sense();
            if ( status.getStatus().isHopperBillPresent() ) {
                if ( !status.sendGloryCommand( new devices.glory.command.BatchDataTransmition( bills ) ) ) {
                    return status.getError();
                }
            }
        }
        status.setCurrentState( CounterStatus.CurrentState.NONE );
        return null;
    }
}
