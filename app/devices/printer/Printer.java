package devices.printer;

import com.sun.jna.Platform;
import devices.printHelper.EditorPanePrinter;
import java.awt.Color;
import java.awt.Insets;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.PrintJobAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.PrinterIsAcceptingJobs;
import javax.print.attribute.standard.QueuedJobCount;
import javax.print.event.PrintJobAttributeEvent;
import javax.print.event.PrintJobAttributeListener;
import javax.print.event.PrintJobEvent;
import javax.print.event.PrintJobListener;
import javax.print.event.PrintServiceAttributeEvent;
import javax.print.event.PrintServiceAttributeListener;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import models.Configuration;
import play.Logger;
import play.Play;
import play.templates.Template;
import play.templates.TemplateLoader;

/**
 *
 * @author adji
 */
public class Printer extends Observable {

    class MyPrintJobAttributeListener implements PrintJobAttributeListener {

        public void attributeUpdate(PrintJobAttributeEvent pjae) {
            PrintJobAttributeSet attrs = pjae.getAttributes();
            if (attrs != null) {
                Attribute a = attrs.get(QueuedJobCount.class);
                if (a != null) {
                    QueuedJobCount q = (QueuedJobCount) a;
                    Logger.debug("MyPrintJobAttributeListener 0: %s %d", pjae.getPrintJob().getPrintService().getName(), q.getValue());
                }
            }
        }
    }

    class MyPrintServiceAttributeListener implements PrintServiceAttributeListener {

        public void attributeUpdate(PrintServiceAttributeEvent psae) {
            PrintServiceAttributeSet attrs = psae.getAttributes();
            if (attrs != null) {
                Attribute a = attrs.get(QueuedJobCount.class);
                if (a != null) {
                    QueuedJobCount q = (QueuedJobCount) a;
                    Logger.debug("MyPrintJobAttributeListener 0: %s %d", psae.getPrintService().getName(), q.getValue());
                }
            }
        }
    }

    class MyPrintJobListener implements PrintJobListener {

        public void printDataTransferCompleted(PrintJobEvent pje) {
            //Logger.debug("PRINTER : printDataTransferCompleted");
        }

        public void printJobCompleted(PrintJobEvent pje) {
            //Logger.debug("PRINTER : printJobCompleted");
        }

        public void printJobFailed(PrintJobEvent pje) {
            //Logger.debug("PRINTER : printJobFailed");
        }

        public void printJobCanceled(PrintJobEvent pje) {
            //Logger.debug("PRINTER : printJobCanceled");
        }

        public void printJobNoMoreEvents(PrintJobEvent pje) {
            if (isPrinterOk(pje.getPrintJob().getPrintService())) {
                Logger.debug("PRINTER : print DONE");
                state.setSTATE(PRINTER_STATE.PRINTER_READY, null);
            }
        }

        public void printJobRequiresAttention(PrintJobEvent pje) {
            //Logger.debug("PRINTER : printJobRequiresAttention");
        }
    }

    // read timeout in ms
    public enum PRINTER_STATE {

        PRINTER_READY,
        PRINTER_PRINTING,
        PRINTER_NOT_ACCEPTING_JOBS,
        PRINTER_WINSPOOL_STATUS;
    };

    // An immutable cloned version of PrinterState 
    public class PrinterStatus {

        private final PRINTER_STATE printerState;
        private final String stateDesc;
        private final PrinterError error;

        private PrinterStatus(State currentState) {
            this.printerState = currentState.printerState;
            this.stateDesc = currentState.stateDesc;
            this.error = currentState.error;
        }

        public PRINTER_STATE getPrinterState() {
            return printerState;
        }

        public PrinterError getError() {
            return error;
        }

        public String getStateDesc() {
            return stateDesc;
        }

        @Override
        public String toString() {
            return "PrinterStatus{" + "printerState=" + printerState + ", error=" + error + '}';
        }
    }
    // A singleton create to hold the state of the printer.

    private class State extends Observable {

        private PRINTER_STATE printerState = null;
        private String stateDesc;
        private PrinterError error = null;

        synchronized private void setSTATE(PRINTER_STATE state, String stateDesc) {
            //Logger.debug("Printer setState prev : printerSTate %s stateDesc %s", state, stateDesc);
            if (this.printerState != state) {
                this.printerState = state;
                setChanged();
            }
            if ((this.stateDesc == null && stateDesc != null)
                    || (this.stateDesc != null && !this.stateDesc.equals(stateDesc))) {
                this.stateDesc = stateDesc;
                setChanged();
            }
            if (hasChanged()) {
                Logger.debug("Printer setState : printerSTate %s stateDesc %s", this.printerState, this.stateDesc);
                notifyObservers(new PrinterStatus(this));
            }
        }

        synchronized private void setError(PrinterError error) {
            this.error = error;
            setChanged();
            notifyObservers(new PrinterStatus(this));
        }

        synchronized public void clearError() {
            // Don't overwrite the first error!!!.
            this.error = null;
            setChanged();
            notifyObservers(new PrinterStatus(this));
        }

        synchronized private PrinterError getError() {
            return error;
        }

        synchronized private PRINTER_STATE getPrinterState() {
            return printerState;
        }

        synchronized private String getStateDesc() {
            return stateDesc;
        }
    }
    public static final int PRINTER_STATUS_POOL_TIMEOUT = 1000;
    final private State state = new State();

    @Override
    public String toString() {
        return "Printer{" + "state=" + state + ", mustStop=" + mustStop + ", statusThread=" + statusThread + '}';
    }

    private class StatusThread extends Thread {

        @Override
        public void run() {
            Logger.debug("Printer status thread started");
            while (!mustStop.get()) {
                PrintService p = printers.get(port);
                if (p == null) {
                    Logger.error("Printer status thread invalid port");
                    break;
                }
                // pool the status of the printer every 2 secs.
                if (isPrinterOk(p)) {
                    state.setSTATE(PRINTER_STATE.PRINTER_READY, null);
                    //Logger.debug("printer status error");
                }
                try {
                    Thread.sleep(2 * 1000);
                } catch (InterruptedException ex) {
                }
            }
            Logger.debug("Printer status thread done");
        }
    }
    final double INCH = 72;
    final double MM = INCH / 25.5;
    final int DEFAULT_PAPER_WIDTH = 80;
    final int DEFAULT_PAPER_LEN = 200;
    private AtomicBoolean mustStop = new AtomicBoolean(false);
    private final StatusThread statusThread;
    public static final Map<String, PrintService> printers = new HashMap<String, PrintService>();
    private final String port;

    static {
        PrintService[] prnSvcs;
        prnSvcs = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService p : prnSvcs) {
            printers.put(p.getName(), p);
            //p.addPrintServiceAttributeListener(new MyPrintServiceAttributeListener());
        }
    }

    public Printer(String port) {
        this.port = port;
        statusThread = new StatusThread();
    }

    public PrinterStatus getStatus() {
        return new PrinterStatus(state);
    }

    public State getInternalState() {
        return state;
    }

    public void startStatusThread() {
        Logger.debug("Printer status thread start");
        statusThread.start();
    }

    public void close() {
        mustStop.set(true);
        try {
            statusThread.join(PRINTER_STATUS_POOL_TIMEOUT * 2);
        } catch (InterruptedException ex) {
            Logger.error("Error closing the printer status thread %s", ex.getMessage());
        }
    }

    public void clearError() {
        state.clearError();
    }

    public PrinterError getError() {
        return state.getError();
    }

    public void addObserver(Observer observer) {
        state.addObserver(observer);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.port != null ? this.port.hashCode() : 0);
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
        final Printer other = (Printer) obj;
        if ((this.port == null) ? (other.port != null) : !this.port.equals(other.port)) {
            return false;
        }
        return true;
    }

    public void print(String templateName, Map<String, Object> args, int paperWidth, int paperLen) {
        if (paperLen <= 0) {
            paperLen = DEFAULT_PAPER_LEN;
        }
        if (paperWidth <= 0) {
            paperWidth = DEFAULT_PAPER_WIDTH;
        }
        Template template = TemplateLoader.load(templateName);
        if (template == null) {
            template = TemplateLoader.load(templateName + ".html");
        }
        if (template == null) {
            template = TemplateLoader.load(templateName + ".txt");
        }
        if (template == null) {
            state.setError(new PrinterError(PrinterError.ERROR_CODE.TEMPLATE_NOT_FOUND, String.format("Template %s not found", templateName)));
        }
        args.put("currentDate", new Date());
        String body = template.render(args);
        //Logger.debug("PRINT : %s", body);

        HTMLEditorKit kit = new HTMLEditorKit();
        HTMLDocument doc = (HTMLDocument) (kit.createDefaultDocument());
        try {
            URI u;
            try {
                u = new URI(Play.applicationPath.toURI() + "/PrinterController");
            } catch (URISyntaxException ex) {
                u = Play.applicationPath.toURI();
            }
            //Logger.debug(u.toString());
            doc.setBase(u.toURL());
            Reader fin = new StringReader(body);
            kit.read(fin, doc, 0);
            fin.close();
        } catch (IOException ex) {
            state.setError(new PrinterError(PrinterError.ERROR_CODE.IO_EXCEPTION, "IOException : " + ex.toString()));
        } catch (BadLocationException ex) {
            state.setError(new PrinterError(PrinterError.ERROR_CODE.IO_EXCEPTION, "BadLocationException : " + ex.toString()));
        }


        JEditorPane item = new JEditorPane();
        item.setEditorKit(kit);
        item.setDocument(doc);
        item.setEditable(false);


        if (!Configuration.isPrinterTest()) {
            if (port == null || !printers.containsKey(port)) {
                state.setError(new PrinterError(PrinterError.ERROR_CODE.PRINTER_NOT_FOUND, String.format("Printer %s not found", port == null ? "NULL" : port)));
                return;
            }
        }
        PrintService p = printers.get(port);
        if (!isPrinterOk(p)) {
            return;
        }
        /*
         for (Attribute a : att.toArray()) {
         Logger.debug("attr : %s %s %s", a.getClass(), a.getName(), att.get(a.getClass()).toString());
         }*/
        Paper pp = new Paper();
        pp.setImageableArea(0, 0, paperWidth * MM, paperLen * MM);
        pp.setSize(paperWidth * MM, paperLen * MM);
        pp.setImageableArea(0, 0, pp.getWidth(), pp.getHeight());
        EditorPanePrinter pnl = new EditorPanePrinter(item, pp, new Insets(0, 0, 0, 0));

        if (!Configuration.isPrinterTest()) {
            try {
                DocPrintJob printJob = p.createPrintJob();

                printJob.addPrintJobListener(new MyPrintJobListener());
                //printJob.addPrintJobAttributeListener(new MyPrintJobAttributeListener(), null);

                PageFormat pageFormat = new PageFormat();
                pageFormat.setPaper(pp);

                Book book = new Book();
                book.append(pnl, pageFormat);

                Doc docc = new SimpleDoc(book, DocFlavor.SERVICE_FORMATTED.PAGEABLE, null);
                state.setSTATE(PRINTER_STATE.PRINTER_PRINTING, "Printing");
                printJob.print(docc, null);
//  pnl.print(p);
/*                PrinterJob pj = PrinterJob.getPrinterJob();
                 pj.setPageable(pnl);
                 pj.setPrintService(p);
                 pj.print();
                 */
            } catch (PrintException ex) {
                state.setError(new PrinterError(PrinterError.ERROR_CODE.IO_EXCEPTION, "PrintException : " + ex.toString()));
            }
        } else {
            JFrame frame = new JFrame("Main print frame");
            pnl.setBackground(Color.black);
            frame.add(pnl);
            //frame.getContentPane().add(item);
            frame.pack();
            frame.setVisible(true);
        }
    }

    private boolean isPrinterOk(PrintService p) {
        AttributeSet att = p.getAttributes();
        if (att.get(PrinterIsAcceptingJobs.class) != null) {
            if (!att.get(PrinterIsAcceptingJobs.class).equals(PrinterIsAcceptingJobs.ACCEPTING_JOBS)) {
                state.setSTATE(PRINTER_STATE.PRINTER_NOT_ACCEPTING_JOBS, "Printer not accepting jobs");
                return false;
            }
        }
        if (Platform.isWindows()) {
            WinSpool.PrinterStatus st = WinSpool.getPrinterStatus(p.getName());
            if (st != WinSpool.PrinterStatus.PRINTER_STATUS_READY) {
                state.setSTATE(PRINTER_STATE.PRINTER_WINSPOOL_STATUS, st.getDesc());
                return false;
            }
        }
        return true;
    }
}
