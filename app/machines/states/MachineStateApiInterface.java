package machines.states;

import models.lov.Currency;

/**
 *
 * @author adji
 */
public interface MachineStateApiInterface {

    public boolean setCurrentState(MachineStateInterface prevState);

    public boolean count(Currency currency);

    public boolean cancel();

    public boolean withdraw();

}
