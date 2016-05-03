package devices.printer;

import com.sun.jna.Platform;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
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
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ImageView;
import javax.swing.text.html.StyleSheet;
import models.Configuration;
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

    static final double INCH = 72;
    static final double MM = INCH / 25.5;
    static final int DEFAULT_PAPER_WIDTH = 77;
    static final int DEFAULT_PAPER_LEN = 200;
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

    private static void printPageFormat(String msg, PageFormat pageFormat) {
        Logger.debug("%s %f %f imageable %f %f imageableXY %f %f paper %f %f pagerImageable %f %f [mm]",
                msg,
                pageFormat.getWidth() / MM, pageFormat.getHeight() / MM,
                pageFormat.getImageableWidth() / MM, pageFormat.getImageableHeight() / MM,
                pageFormat.getImageableX() / MM, pageFormat.getImageableY() / MM,
                pageFormat.getPaper().getWidth() / MM, pageFormat.getPaper().getHeight() / MM,
                pageFormat.getPaper().getImageableWidth() / MM, pageFormat.getPaper().getImageableHeight() / MM);

    }

    public void print(boolean isPrinterTest, String body, int desiredPaperWidth, int desiredPaperHeight) {
        if (!isPrinterTest) {
            if (service == null) {
                state.setError(new PrinterError(PrinterError.ERROR_CODE.PRINTER_NOT_FOUND, String.format("Printer %s not found", service == null ? "NULL" : service)));
                return;
            }
        }
        if (state.isError()) {
            state.sendEvent();
            return;
        }

        double paperHeight = Math.min(0, desiredPaperHeight * MM);
        double paperWidth = Math.min(0, desiredPaperWidth * MM);

        //Logger.debug("PRINT : %s", body);
        HTMLEditorKit kit = new HTMLEditorKit() {
            // TODO: Test this.
            @Override
            public StyleSheet getStyleSheet() {
                return super.getStyleSheet();
            }

            @Override
            public ViewFactory getViewFactory() {
                return new HTMLFactory() {
                    @Override
                    public View create(Element elem) {
                        View v = super.create(elem);
                        if ((v != null) && (v instanceof ImageView)) {
                            ((ImageView) v).setLoadsSynchronously(true);
                        }
                        return v;
                    }
                };
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
        } catch (BadLocationException ex) {
            state.setError(new PrinterError(PrinterError.ERROR_CODE.IO_EXCEPTION, "BadLocationException : " + ex.toString()));
        }

        final JEditorPane editor = new JEditorPane();
        editor.setEditorKit(kit);
        editor.setDocument(doc);
        editor.setEditable(false);

        Point po = editor.getLocation();
        Dimension d = editor.getSize();
        Logger.debug("EDITOR POSITION %f %f, EDITOR SIZE %f %f", po.getX(), po.getY(), d.getWidth(), d.getHeight());
        try {
            Paper pp = new Paper();
            pp.setSize(1, 1);
            pp.setImageableArea(0, 0, 1, 1);
            PageFormat pageFormat = new PageFormat();
            pageFormat.setPaper(pp);
            VirtualGraphics vg = new VirtualGraphics();
            editor.getPrintable(null, null).print(vg, pageFormat, 0);
            Logger.debug("VirtualGraphics RESULT : %f %f, desired %f %f [mm]",
                    vg.getWidthLimit() / MM, vg.getHeightLimit() / MM, paperWidth / MM, paperHeight / MM);
            // I have a huge ticket, adjust tu calculated size
            paperHeight = Math.max(paperHeight, vg.getHeightLimit());
            paperWidth = Math.max(paperWidth, vg.getWidthLimit());
        } catch (PrinterException ex) {
            Logger.debug("Exception %s", ex);
        }

        Paper prePaper = new Paper();
        prePaper.setSize(paperWidth, paperHeight);
        prePaper.setImageableArea(0, 0, paperWidth, paperHeight);
        PageFormat prePageFormat = new PageFormat();
        prePageFormat.setPaper(prePaper);
        printPageFormat("PRE VALIDATE", prePageFormat);
        PageFormat postPageFormat = new PageFormat();
        try {
            java.awt.print.PrinterJob pj = java.awt.print.PrinterJob.getPrinterJob();
            pj.setPrintService(this.service);
            postPageFormat = pj.validatePage(prePageFormat);
        } catch (PrinterException ex) {
            Logger.error(ex.toString());
        }
        printPageFormat("POS VALIDATE", postPageFormat);

        // Leave some fixed margin.
        double desiredX = postPageFormat.getImageableWidth();
        if (desiredPaperWidth > 0) {
            desiredX = Math.min(desiredPaperWidth * MM, postPageFormat.getImageableWidth());
        }
        double desiredY = Math.max(desiredPaperHeight * MM, postPageFormat.getImageableHeight());
        final double scalex = desiredX / prePageFormat.getImageableWidth() / 1.2;
        final double scaley = desiredY / prePageFormat.getImageableHeight() / 1.1;
        Logger.debug("CALCULATED SCALE %f %f, DESIRED %f %f", scalex, scaley, desiredX / MM, desiredY / MM);

        //        EditorPanePrinter pnl = new EditorPanePrinter(item, pp, new Insets(0, 0, 0, 0));
        if (!Configuration.isPrinterTest()) {
            try {
                Book book = new Book();
                //                book.append(pnl, pageFormat);
                book.append(new Printable() {
                    //Printable p = editor.getPrintable(null, null);

                    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                        if (pageIndex >= 1) {
                            return Printable.NO_SUCH_PAGE;
                        }

                        Graphics2D g2d = (Graphics2D) graphics;
                        g2d.scale(scalex, scaley);
                        g2d.translate((int) pageFormat.getImageableX(), (int) pageFormat.getImageableY());
                        editor.setLocation(0, 0);
                        editor.setSize((int) pageFormat.getImageableWidth(), (int) pageFormat.getImageableHeight());
                        editor.printAll(g2d);
                        return Printable.PAGE_EXISTS;
                        //return p.print(g2d, pageFormat, pageIndex);
                    }
                }, postPageFormat);

                Doc docc = new SimpleDoc(book, DocFlavor.SERVICE_FORMATTED.PAGEABLE, null);
                state.setState(PRINTER_STATE.PRINTER_PRINTING, "Printing");

                DocPrintJob printJob = this.service.createPrintJob();

                printJob.addPrintJobListener(new MyPrintJobListener());
                printJob.print(docc, null);
            } catch (PrintException ex) {
                state.setError(new PrinterError(PrinterError.ERROR_CODE.IO_EXCEPTION, "PrintException : " + ex.toString()));
            }
        } else {
            JFrame frame = new JFrame("Main print frame");
            //            pnl.setBackground(Color.black);
            //            frame.add(pnl);
            //to check the size: frame.setSize((int) paperWidth, (int) paperHeight);
            frame.setBackground(Color.black);
            frame.add(editor);

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
