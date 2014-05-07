package devices.glory.operation;

import devices.glory.response.GloryDE50OperationResponse.Denomination;

/*
 * This command is return Denomination Data.
 */
public class DenominationDataRequest extends OperationdWithDataResponse {

    public DenominationDataRequest() {
        super((byte) 0x44, "DenominationDataRequest");
    }

    @Override
    public void setResponse(byte[] dr) {
        super.setResponse(dr);
        if (response.getError() != null) {
            return;
        }
        byte[] data = response.getData();
        if (data == null || data.length == 0) {
            return;
        }
        for (int i = 0; i < data.length; i += 10) {
            if (i + 10 <= data.length) {
                Denomination d = new Denomination();
                d.idx = i / 10;
                byte[] b = {data[i], data[i + 1], data[i + 2]};
                d.currencyCode = new String(b);
                d.newVal = (data[ i + 3] != 0x30);
                // TODO: Check for 0x4X
                d.denominationCode = 32 - new Integer(data[ i + 4] & 0x0F);

                Double dd = (Math.pow(10, getDecDigit(data[i + 6])) * (getDecDigit(data[i + 7]) * 100 + getDecDigit(data[i + 8]) * 10 + getDecDigit(data[i + 9]) * 1));
                d.value = dd.intValue();
                if (data[ i + 5] != 0x30) {
                    d.value = -d.value;
                }
                response.addToDenominationData(d);
            }
        }
        //data.length;
        return;
    }
}
