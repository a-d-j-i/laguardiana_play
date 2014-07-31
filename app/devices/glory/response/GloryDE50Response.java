package devices.glory.response;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * *
 * This class correspond really to the view, but it is here for historical
 * reasons.
 *
 * @author adji
 */
public class GloryDE50Response implements Serializable, GloryDE50ResponseInterface {

    @Override
    public String toString() {
        return "GloryDE50OperationResponseAck";
    }

    @Override
    public GloryDE50ResponseDetailsInterface getRepr() {
        return new GloryDE50ResponseDetailsInterface() {

            public boolean isError() {
                return false;
            }

            public String getError() {
                return null;
            }

            public String getSRMode() {
                return null;
            }

            public String getD1Mode() {
                return null;
            }

            public ArrayList<String> getSrBits() {
                return null;
            }

            public ArrayList<String> getD2Bits() {
                return null;
            }

            public ArrayList<String> getInfo() {
                return null;
            }

            public String getData() {
                return null;
            }

            public Map<Integer, Integer> getBills() {
                return null;
            }

            public boolean haveData() {
                return false;
            }

            public List<GloryDE50ResponseWithData.Denomination> getDenominationData() {
                return null;
            }
        };
    }

}
