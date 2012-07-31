/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.glory.manager;

import devices.glory.GloryStatus;
import devices.glory.GloryStatus.D1Mode;
import devices.glory.GloryStatus.SR1Mode;
import devices.glory.command.CommandWithCountingDataResponse.Bill;
import devices.glory.command.GloryCommandAbstract;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author adji
 */
public class ManagerStatus {

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();
    private final GloryStatus gloryStatus = new GloryStatus();
    private String error = null;
    private ArrayList<Bill> billData = null;
    private ArrayList<Bill> depositedBillData = null;
    private boolean depositOk = false;

    public boolean setStatusOk( GloryCommandAbstract sendCommand ) {
        w.lock();
        try {
            return gloryStatus.setStatusOk( sendCommand );
        } finally {
            w.unlock();
        }
    }
    // TODO: Converto to something mode meaningfull.
    final static ArrayList<Bill> noBills = new ArrayList<Bill>();

    {
        Bill b;
        for ( int i = 0; i < 32; i++ ) {
            b = new Bill();
            b.idx = i;
            b.value = 0;
            noBills.add( b );
        }
    }

    public void setBillData( ArrayList<Bill> billData ) {
        w.lock();
        try {
            this.billData = billData;
        } finally {
            w.unlock();
        }
    }

    public void clearBillData() {
        setBillData( noBills );
    }

    public boolean setDepositedBillDataFromGlory() {
        w.lock();
        try {
            boolean haveBills = false;
            depositOk = false;
            ArrayList<Bill> ret = gloryStatus.getBills();
            if ( ret != null && !ret.isEmpty() ) {
                this.depositedBillData = ret;
                for ( Bill b : ret ) {
                    if ( b.value != 0 ) {
                        haveBills = true;
                        break;
                    }
                }
            }
            return haveBills;
        } finally {
            w.unlock();
        }
    }

    public void depositOk() {
        w.lock();
        try {
            depositOk = true;
        } finally {
            w.unlock();
        }
    }

    public boolean isDepositOk() {
        r.lock();
        try {
            return depositOk;
        } finally {
            r.unlock();
        }
    }

    public ArrayList<Bill> getDepositedBillData() {
        r.lock();
        try {
            return depositedBillData;
        } finally {
            r.unlock();
        }
    }

    public void setSetBillDataFromGlory() {
        w.lock();
        try {
            ArrayList<Bill> ret = gloryStatus.getBills();
            if ( ret != null && !ret.isEmpty() ) {
                this.billData = ret;
            }
        } finally {
            w.unlock();
        }
    }

    public ArrayList<Bill> getBillData() {
        r.lock();
        try {
            return billData;
        } finally {
            r.unlock();
        }
    }

    public boolean isHopperBillPresent() {
        r.lock();
        try {
            return gloryStatus.isHopperBillPresent();
        } finally {
            r.unlock();
        }
    }

    public SR1Mode getSr1Mode() {
        r.lock();
        try {
            return gloryStatus.getSr1Mode();
        } finally {
            r.unlock();
        }
    }

    public D1Mode getD1Mode() {
        r.lock();
        try {
            return gloryStatus.getD1Mode();
        } finally {
            r.unlock();
        }
    }

    public String getGloryError() {
        r.lock();
        try {
            return gloryStatus.getLastError();
        } finally {
            r.unlock();
        }
    }

    public String getError() {
        r.lock();
        try {
            return error;
        } finally {
            r.unlock();
        }
    }

    void setError( String error ) {
        w.lock();
        try {
            this.error = error;
        } finally {
            w.unlock();
        }
    }
}
