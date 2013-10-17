/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions.states;

import static devices.glory.manager.ManagerInterface.MANAGER_STATE.JAM;
import devices.glory.manager.ManagerInterface.ManagerStatus;
import devices.ioboard.IoBoard;
import models.Configuration;
import models.ModelError;
import models.actions.UserAction;
import models.actions.UserAction.StateApi;
import play.Logger;

/**
 *
 * @author adji
 */
public class EnvelopeDepositReadyToStore extends EnvelopeDepositStart {

    private boolean delayedStore = false;

    public EnvelopeDepositReadyToStore(StateApi stateApi) {
        super(stateApi);
    }

    @Override
    public String name() {
        return "READY_TO_STORE";
    }

    @Override
    public String getMessage(UserAction userAction) {
        return "envelope_deposit.put_the_envelope_in_the_escrow";
    }

    @Override
    public void onGloryEvent(ManagerStatus m) {
        Logger.debug("%s glory event : %s", this.getClass().getSimpleName(), m.getState());
        switch (m.getState()) {
            case READY_TO_STORE:
                if (!Configuration.isIgnoreBag() && !stateApi.isIoBoardOk()) {
                    delayedStore = true;
                    stateApi.setState(new BagRemoved(stateApi, this));
                } else {
                    store();
                }
                break;
            case JAM: //The door is jamed.
                // Let it be!!!.
                stateApi.setState(new Jam(stateApi, this));
                //stateApi.setError(ModelError.ERROR_CODE.ESCROW_JAMED, "Escrow jamed");
                break;
            /*            case STORING:
             stateApi.setState(new StoringEnvelopeDeposit(stateApi));
             break;*/
            default:
                Logger.debug("EnvelopeDepositReadyToStore onGloryEvent invalid state %s %s", m.name(), name());
                break;
        }
    }

    @Override
    public void onIoBoardEvent(IoBoard.IoBoardStatus status) {
        Logger.error("ReadyToStoreEnvelopeDeposit onIoBoardEvent %s", status.toString());
        if (!Configuration.isIgnoreBag() && stateApi.isIoBoardOk()) {
            if (delayedStore) {
                Logger.error("ReadyToStoreEnvelopeDeposit DELAYED STORE!!!");
                store();
            }
        }
        super.onIoBoardEvent(status);
    }

    private void store() {
        if (Configuration.isIgnoreShutter()) {
            if (!stateApi.store()) {
                Logger.error("EnvelopeDepositReadyToStore can't store");
            }
            stateApi.setState(new EnvelopeDepositStoring(stateApi));
        } else {
            stateApi.openGate();
            stateApi.setState(new WaitForOpenGate(stateApi, new EnvelopeDepositStoring(stateApi)));
        }

    }
}
