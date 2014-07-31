package devices.glory.response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author adji
 */
public class GloryDE50ResponseError extends GloryDE50Response implements GloryDE50ResponseInterface {

    private final String error;

    public GloryDE50ResponseError(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    @Override
    public String toString() {
        return "GloryDE50ResponseError " + "error=" + error;
    }

    @Override
    public GloryDE50ResponseDetailsInterface getRepr() {
        return new GloryDE50ResponseDetailsInterface() {

            public boolean isError() {
                return true;
            }

            public String getError() {
                return error;
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
