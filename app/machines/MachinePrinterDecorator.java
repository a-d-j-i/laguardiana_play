package machines;

import devices.printer.Printer;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.print.PrintService;
import models.Configuration;
import models.db.LgSystemProperty;
import play.Logger;
import play.templates.Template;
import play.templates.TemplateLoader;

/**
 *
 * @author adji
 */
public class MachinePrinterDecorator {

    public static final int PRINTER_STATUS_POOL_TIMEOUT = 1000;
    private final AtomicBoolean mustStop = new AtomicBoolean(false);
    private final BlockingQueue<Runnable> jobq = new LinkedBlockingQueue<Runnable>();
    private final StatusThread statusThread;
    Printer printer = null;

    public MachinePrinterDecorator() {
        statusThread = new StatusThread();
    }

    private class StatusThread extends Thread {

        @Override
        public void run() {
            Logger.debug("Printer status thread started");
            while (!mustStop.get()) {
                Runnable job;
                try {
                    job = jobq.poll(PRINTER_STATUS_POOL_TIMEOUT, TimeUnit.MICROSECONDS);
                } catch (InterruptedException ex) {
                    continue;
                }

                if (job != null) {
                    try {
                        job.run();
                    } catch (Exception ex) {
                        Logger.error("Exception in printer job.run %s", ex.toString());
                        ex.printStackTrace();
                    }
                    continue;
                }
                if (printer != null) {
                    printer.refreshState();
                }
            }
            Logger.debug("Printer status thread done");
        }
    }

    public void start() {
        Logger.debug("Printer status thread start");
        if (!statusThread.isAlive()) {
            try {
                statusThread.start();
            } catch (IllegalThreadStateException e) {

            }
        }
        setCurrentPrinter(null);
    }

    @Override
    protected void finalize() throws Throwable {
        stop();
        super.finalize();
    }

    public void stop() {
        mustStop.set(true);
        statusThread.interrupt();
        try {
            statusThread.join(PRINTER_STATUS_POOL_TIMEOUT * 2);
        } catch (InterruptedException ex) {
            Logger.error("Error closing the printer status thread %s", ex.getMessage());
        }
    }

    @Override
    public String toString() {
        return "MachinePrinterDecorator{" + "mustStop=" + mustStop + ", statusThread=" + statusThread + ", printer=" + printer + ", jobq=" + jobq + '}';
    }

    public void setCurrentPrinter(String prt) {
        if (prt == null) {
            prt = Configuration.getSystemProperty("printer.port");
            if (prt == null) {
                Logger.error("Default printer must be configured");
                return;
            }
        }

        final PrintService p = (PrintService) Printer.PRINTERS.get(prt);
        if (p == null) {
            Logger.error("Wrong printer name %s", prt);
            return;
        }
        LgSystemProperty.setOrCreateProperty("printer.port", prt);
        boolean ret = jobq.offer(new Runnable() {
            public void run() {
                printer = new Printer(p);
            }
        });
        if (!ret) {
            Logger.error("Error setting current printer in the print job");
        }
    }

    public Collection<PrintService> getPrinters() {
        return Printer.PRINTERS.values();
    }

    public void print(final String templateName, final Map<String, Object> args, final int paperWidth, final int paperLen) {
        final boolean isPrinterTest = Configuration.isPrinterTest();
        Template template = TemplateLoader.load(templateName);
        if (template == null) {
            template = TemplateLoader.load(templateName + ".html");
        }
        if (template == null) {
            template = TemplateLoader.load(templateName + ".txt");
        }
        if (template == null) {
            Logger.error("invalid template %s", templateName);
            return;
        }
        args.put("currentDate", new Date());
        final String body = template.render(args);

        boolean ret = jobq.offer(new Runnable() {
            public void run() {
                if (printer != null) {
                    printer.print(isPrinterTest, body, paperWidth, paperLen);
                }
            }
        });
        if (!ret) {
            Logger.error("Error inserting the print job");
        }
    }

    public String getPrinterPort() {
        return Configuration.getSystemProperty("printer.port");
    }

    public String getPrinterState() {
        try {
            Future<String> ret = new FutureTask<String>(new Callable<String>() {
                public String call() {
                    if (printer != null) {
                        Printer.State st = printer.getState();
                        if (st != null) {
                            return st.toString();
                        } else {
                            return "State is null";
                        }
                    } else {
                        return "Invalid printer";
                    }
                }
            });
            if (!jobq.offer((Runnable) ret)) {
                return "Error submitting task";
            }
            return ret.get();
        } catch (InterruptedException ex) {
            return String.format("Exception in getPrinterState : %s", ex.toString());
        } catch (ExecutionException ex) {
            return String.format("Exception in getPrinterState : %s", ex.toString());
        }
    }

    public boolean needCheck() {
        if (Configuration.isPrinterTest()) {
            return false;
        }
        try {
            Future<Boolean> ret = new FutureTask<Boolean>(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    if (printer != null) {
                        Printer.State st = printer.getState();
                        if (st != null) {
                            return st.needCheck();
                        }
                    }
                    Logger.error("Error getting printer state needCheck");
                    return true;
                }
            });
            if (!jobq.offer((Runnable) ret)) {
                return false;
            }
            return ret.get();
        } catch (InterruptedException ex) {
            Logger.error("Exception in isReadyToPrint : %s", ex.toString());
        } catch (ExecutionException ex) {
            Logger.error("Exception in isReadyToPrint : %s", ex.toString());
        }
        return false;
    }

}
