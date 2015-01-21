package devices.ioboard.response;

import devices.device.DeviceResponseInterface;

/**
 *
 * @author adji
 */
public class IoboardStatusResponse implements DeviceResponseInterface {

    final private Boolean sensorA;
    final private Boolean sensorB;
    final private Boolean sensorC;
    final private Boolean sensorLock;
    final private Integer A;
    final private Integer B;
    final private Integer C;
    final private Integer D;
    final private Integer BAG_STATUS;
    final private Integer BAG_SENSOR;

    public IoboardStatusResponse(Boolean sensorA, Boolean sensorB, Boolean sensorC, Boolean sensorLock, Integer A, Integer B, Integer C, Integer D, Integer BAG_STATUS, Integer BAG_SENSOR) {
        this.sensorA = sensorA;
        this.sensorB = sensorB;
        this.sensorC = sensorC;
        this.sensorLock = sensorLock;
        this.A = A;
        this.B = B;
        this.C = C;
        this.D = D;
        this.BAG_STATUS = BAG_STATUS;
        this.BAG_SENSOR = BAG_SENSOR;
    }

    public Boolean getSensorA() {
        return sensorA;
    }

    public Boolean getSensorB() {
        return sensorB;
    }

    public Boolean getSensorC() {
        return sensorC;
    }

    public Boolean getSensorLock() {
        return sensorLock;
    }

    public Integer getA() {
        return A;
    }

    public Integer getB() {
        return B;
    }

    public Integer getC() {
        return C;
    }

    public Integer getD() {
        return D;
    }

    public Integer getBAG_STATUS() {
        return BAG_STATUS;
    }

    public Integer getBAG_SENSOR() {
        return BAG_SENSOR;
    }

    @Override
    public String toString() {
        return "IoboardStatusResponse{" + "sensorA=" + sensorA + ", sensorB=" + sensorB + ", sensorC=" + sensorC + ", sensorLock=" + sensorLock + ", A=" + A + ", B=" + B + ", C=" + C + ", D=" + D + ", BAG_STATUS=" + BAG_STATUS + ", BAG_SENSOR=" + BAG_SENSOR + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (this.sensorA != null ? this.sensorA.hashCode() : 0);
        hash = 31 * hash + (this.sensorB != null ? this.sensorB.hashCode() : 0);
        hash = 31 * hash + (this.sensorC != null ? this.sensorC.hashCode() : 0);
        hash = 31 * hash + (this.sensorLock != null ? this.sensorLock.hashCode() : 0);
        hash = 31 * hash + (this.A != null ? this.A.hashCode() : 0);
        hash = 31 * hash + (this.B != null ? this.B.hashCode() : 0);
        hash = 31 * hash + (this.C != null ? this.C.hashCode() : 0);
        hash = 31 * hash + (this.D != null ? this.D.hashCode() : 0);
        hash = 31 * hash + (this.BAG_STATUS != null ? this.BAG_STATUS.hashCode() : 0);
        hash = 31 * hash + (this.BAG_SENSOR != null ? this.BAG_SENSOR.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IoboardStatusResponse other = (IoboardStatusResponse) obj;
        if (this.sensorA != other.sensorA && (this.sensorA == null || !this.sensorA.equals(other.sensorA))) {
            return false;
        }
        if (this.sensorB != other.sensorB && (this.sensorB == null || !this.sensorB.equals(other.sensorB))) {
            return false;
        }
        if (this.sensorC != other.sensorC && (this.sensorC == null || !this.sensorC.equals(other.sensorC))) {
            return false;
        }
        if (this.sensorLock != other.sensorLock && (this.sensorLock == null || !this.sensorLock.equals(other.sensorLock))) {
            return false;
        }
        if (this.A != other.A && (this.A == null || !this.A.equals(other.A))) {
            return false;
        }
        if (this.B != other.B && (this.B == null || !this.B.equals(other.B))) {
            return false;
        }
        if (this.C != other.C && (this.C == null || !this.C.equals(other.C))) {
            return false;
        }
        if (this.D != other.D && (this.D == null || !this.D.equals(other.D))) {
            return false;
        }
        if (this.BAG_STATUS != other.BAG_STATUS && (this.BAG_STATUS == null || !this.BAG_STATUS.equals(other.BAG_STATUS))) {
            return false;
        }
        if (this.BAG_SENSOR != other.BAG_SENSOR && (this.BAG_SENSOR == null || !this.BAG_SENSOR.equals(other.BAG_SENSOR))) {
            return false;
        }
        return true;
    }

}
