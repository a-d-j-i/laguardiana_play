package devices.glory.response;

import devices.glory.response.GloryDE50ResponseWithData.Denomination;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author adji
 */
public interface GloryDE50ResponseDetailsInterface {

    public boolean isError();

    public String getError();

    public String getSRMode();

    public String getD1Mode();

    public ArrayList< String> getSrBits();

    public ArrayList<String> getD2Bits();

    public ArrayList<String> getInfo();

    public String getData();

    public Map<Integer, Integer> getBills();

    public boolean haveData();

    public List<Denomination> getDenominationData();

    @Override
    public String toString();
}
