package devices.printer;

import com.sun.jna.Platform;
import java.awt.Color;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
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
import javax.swing.text.html.StyleSheet;
import play.Logger;
import play.Play;

/**
 *
 * @author adji
 */
public class Printer {

    // read timeout in ms
    public enum PRINTER_STATE {

        PRINTER_READY,
        PRINTER_PRINTING,
        PRINTER_NOT_ACCEPTING_JOBS,
        PRINTER_SPOOL_PROBLEM;
    };

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
            /*            if (isPrinterOk(pje.getPrintJob().getPrintService())) {
             Logger.debug("PRINTER : print DONE");
             state.setState(PRINTER_STATE.PRINTER_READY, null);
             }*/
        }

        public void printJobRequiresAttention(PrintJobEvent pje) {
            //Logger.debug("PRINTER : printJobRequiresAttention");
        }
    }

    final double INCH = 72;
    final double MM = INCH / 25.5;
    final int DEFAULT_PAPER_WIDTH = 77;
    final int DEFAULT_PAPER_LEN = 200;
    public static final Map<String, PrintService> PRINTERS = new HashMap<String, PrintService>();
    PrintService service;

    static {
        PrintService[] prnSvcs;
        prnSvcs = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService p : prnSvcs) {
            PRINTERS.put(p.getName(), p);
            //p.addPrintServiceAttributeListener(new MyPrintServiceAttributeListener());
        }
    }

    public Printer(PrintService p) {
        this.service = p;
    }

    public void print(boolean isPrinterTest, String body, int paperWidth, int paperLen) {
        if (!isPrinterTest) {
            if (service == null) {
                state.setError(new PrinterError(PrinterError.ERROR_CODE.PRINTER_NOT_FOUND, String.format("Printer %s not found", service == null ? "NULL" : service)));
                return;
            }
        }

        if (paperLen <= 0) {
            paperLen = DEFAULT_PAPER_LEN;
        }
        if (paperWidth <= 0) {
            paperWidth = DEFAULT_PAPER_WIDTH;
        }
        //Logger.debug("PRINT : %s", body);

        HTMLEditorKit kit = new HTMLEditorKit() {
            // TODO: Test this.
            @Override
            public StyleSheet getStyleSheet() {
                return super.getStyleSheet();
            }
        };
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
            return;
        } catch (BadLocationException ex) {
            state.setError(new PrinterError(PrinterError.ERROR_CODE.IO_EXCEPTION, "BadLocationException : " + ex.toString()));
            return;
        }

        final JEditorPane item = new JEditorPane();
        item.setEditorKit(kit);
        item.setDocument(doc);
        item.setEditable(false);

        Paper pp = new Paper();
        pp.setImageableArea(0, 0, paperWidth * MM, Integer.MAX_VALUE);
        pp.setSize(paperWidth * MM, Integer.MAX_VALUE);
        pp.setImageableArea(0, 0, pp.getWidth(), pp.getHeight());
        PageFormat pageFormat = new PageFormat();
        pageFormat.setPaper(pp);

        try {
            VirtualGraphics vg = new VirtualGraphics();
            item.getPrintable(null, null).print(vg, pageFormat, 0);
            Logger.debug("VirtualGraphics RESULT : %d", vg.getHeightLimit());
            int desiredPaperLen = (int) (240 * vg.getHeightLimit() / 625);
            if (desiredPaperLen > paperLen) {
                paperLen = desiredPaperLen;
            }
        } catch (PrinterException ex) {
            Logger.debug("Exception %s", ex);
        }
        Logger.debug("Print paper len %d mm", paperLen);
        pp = new Paper();
        pp.setImageableArea(0, 0, paperWidth * MM, paperLen * MM);
        pp.setSize(paperWidth * MM, paperLen * MM + 10 * MM);
        pp.setImageableArea(0, 0, pp.getWidth(), pp.getHeight());
        //        EditorPanePrinter pnl = new EditorPanePrinter(item, pp, new Insets(0, 0, 0, 0));

        if (!isPrinterTest) {
            try {

                pageFormat = new PageFormat();
                pageFormat.setPaper(pp);

                Book book = new Book();
                //                book.append(pnl, pageFormat);
                book.append(item.getPrintable(null, null), pageFormat);

                Doc docc = new SimpleDoc(book, DocFlavor.SERVICE_FORMATTED.PAGEABLE, null);

                DocPrintJob printJob = service.createPrintJob();
                printJob.addPrintJobListener(new MyPrintJobListener());
                printJob.print(docc, null);
            } catch (PrintException ex) {
                state.setError(new PrinterError(PrinterError.ERROR_CODE.IO_EXCEPTION, "PrintException : " + ex.toString()));
                return;
            }
        } else {
            JFrame frame = new JFrame("Main print frame");
            //            pnl.setBackground(Color.black);
            //            frame.add(pnl);
            frame.setBackground(Color.black);
            frame.add(item);

            //frame.getContentPane().add(item);
            frame.pack();
            frame.setVisible(true);
        }
    }

    final public State state = new State();

    public void setState(PRINTER_STATE st, String str) {
        state.setState(st, str);
    }

    public State getState() {
        return state;
    }

    public void refreshState() {
        // pool the status of the printer every 2 secs.
        AttributeSet att = service.getAttributes();

        if (att != null && att.get(PrinterIsAcceptingJobs.class) != null) {
            if (!att.get(PrinterIsAcceptingJobs.class).equals(PrinterIsAcceptingJobs.ACCEPTING_JOBS)) {
                setState(PRINTER_STATE.PRINTER_NOT_ACCEPTING_JOBS, "Printer not accepting jobs");
                return;
            }
        }
        if (Platform.isWindows()) {
            WinSpool.refreshState(service.getName(), state);
        } else {
            LinuxSpool.refreshState(service.getName(), state);
        }
    }

    public class State extends Observable {

        private PRINTER_STATE printerState = null;
        private String stateDesc;
        private PrinterError error = null;

        public void setState(PRINTER_STATE state, String stateDesc) {
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
                notifyObservers(this);
            }
        }

        public void setError(PrinterError error) {
            this.error = error;
            setChanged();
            notifyObservers(this);
        }

        public void clearError() {
            // Don't overwrite the first error!!!.
            if (this.error != null) {
                this.error = null;
                setChanged();
                notifyObservers(this);
            }
        }
//
//         private PrinterError getError() {
//            return error;
//        }

        private boolean isError() {
            return error != null;
        }

        private PRINTER_STATE getPrinterState() {
            return printerState;
        }

        @Override
        public String toString() {
            return stateDesc;
        }

        public boolean needCheck() {
            if (printerState == null) {
                return false;
            }
            return (printerState != PRINTER_STATE.PRINTER_READY && printerState != PRINTER_STATE.PRINTER_PRINTING);
        }

        private void sendEvent() {
            setChanged();
            notifyObservers(this);
        }
    }

    @Override
    public String toString() {
        return "Printer{ service=" + service.toString() + ", state=" + state + '}';
    }

}
