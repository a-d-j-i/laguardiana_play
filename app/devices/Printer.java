/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices;

import devices.printHelper.EditorPanePrinter;
import java.awt.Color;
import java.awt.Insets;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.Attribute;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.event.PrintJobEvent;
import javax.print.event.PrintJobListener;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import models.Configuration;
import play.Logger;
import play.Play;
import play.templates.Template;
import play.templates.TemplateLoader;

/**
 *
 * @author adji
 */
public class Printer {

    final double INCH = 72;
    final double MM = INCH / 25.5;
    final int PAGE_WIDTH = 80;
    final int DEFAULT_PAGE_LEN = 200;
    // TODO: Manage errors.

    class MyPrintListener implements PrintJobListener {

        public void printDataTransferCompleted(PrintJobEvent pje) {
            Logger.debug("printDataTransferCompleted");
        }

        public void printJobCompleted(PrintJobEvent pje) {
            Logger.debug("printJobCompleted");
        }

        public void printJobFailed(PrintJobEvent pje) {
            Logger.debug("printJobFailed");
        }

        public void printJobCanceled(PrintJobEvent pje) {
            Logger.debug("printJobCanceled");
        }

        public void printJobNoMoreEvents(PrintJobEvent pje) {
            Logger.debug("printJobNoMoreEvents");
        }

        public void printJobRequiresAttention(PrintJobEvent pje) {
            Logger.debug("printJobRequiresAttention");
        }
    }
    public Map<String, PrintService> printers = new HashMap<String, PrintService>();
    String port = null;

    Printer(String port) {
        this.port = port;
        PrintService[] prnSvcs;
        prnSvcs = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService p : prnSvcs) {
            printers.put(p.getName(), p);
        }
    }

    public void print(String templateName, Map<String, Object> args, int paperLen) {
        try {
            printInt(templateName, args, paperLen);
        } catch (Exception ex) {
            Logger.debug("Error printing : " + ex.getMessage());
        }
    }

    public void printInt(String templateName, Map<String, Object> args, int paperLen) throws PrinterException, PrintException {
        if (paperLen <= 0) {
            paperLen = DEFAULT_PAGE_LEN;
        }
        Template template = TemplateLoader.load(templateName);
        if (template == null) {
            template = TemplateLoader.load(templateName + ".html");
        }
        if (template == null) {
            template = TemplateLoader.load(templateName + ".txt");
        }
        if (template == null) {
            throw new PrinterException(String.format("Template %s not found", templateName));
        }
        args.put("currentDate", new Date());
        String body = template.render(args);
        //Logger.debug("PRINT : %s", body);

        HTMLEditorKit kit = new HTMLEditorKit();
        //HTMLEditorKit kit = new LargeHTMLEditorKit();
/*
        try {
            StyleSheet styles = new StyleSheet();
            FileInputStream is = new FileInputStream(new File(Play.applicationPath, "public/stylesheets/printer.css"));
            Reader r = new BufferedReader(new InputStreamReader(is, "ISO-8859-1"));
            styles.loadRules(r, null);
            r.close();
            kit.setStyleSheet(styles);
        } catch (Throwable e) {
            Logger.error("error loading stylesheet %s", e.toString());
        }
*/
        //Logger.debug("Stylesheet %s", kit.getStyleSheet());
        
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
            throw new PrinterException("IOException : " + ex.toString());
        } catch (BadLocationException ex) {
            throw new PrinterException("BadLocationException : " + ex.toString());
        }


        JEditorPane item = new JEditorPane();
        item.setEditorKit(kit);
//        item.getDocument().putProperty("ZOOM_FACTOR", new Double(1.5));
        item.setDocument(doc);
        item.setEditable(false);


        //PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
        // http://docs.oracle.com/javase/1.4.2/docs/api/javax/print/attribute/PrintRequestAttribute.html
        //attrs.add(new Copies(1));
        //attrs.add(Sides.ONE_SIDED);

        //attrs.add(OrientationRequested.PORTRAIT);
        //attrs.add(MediaSizeName.ISO_A4);
        //attrs.add(PrintQuality.DRAFT);
        //attrs.add(new PrinterResolution(600, 600, PrinterResolution.DPI));
        //attrs.add(new MediaPrintableArea(0, 0, 10, 10, MediaPrintableArea.MM));

        /*        HashDocAttributeSet attrc = new HashDocAttributeSet();
         //attrc.add(new MediaPrintableArea((float) 0, (float) 0, (float) PAGE_WIDTH, (float) paperLen, MediaPrintableArea.MM));
         attrc.add(OrientationRequested.PORTRAIT);
         MediaSizeName m = MediaSize.findMedia((float) PAGE_WIDTH, (float) paperLen, MediaSize.MM);
         if (m != null) {
         Logger.debug("MEDIA SIZE NAME %s", m);
         attrc.add(m);
         } else {
         Logger.error("MEDIA SIZE NAME IS NULL");
         return;
         }
         */
        // Two "false" args mean "no print dialog" and "non-interactive" ( ie, batch - mode printing). 
        if (port == null || !printers.containsKey(port)) {
            throw new PrinterException(String.format("Printer %s not found", port == null ? "NULL" : port));
        }
        PrintService p = printers.get(port);

        Paper pp = new Paper();
        pp.setImageableArea(0, 0, PAGE_WIDTH * MM, paperLen * MM);
        pp.setSize(PAGE_WIDTH * MM, paperLen * MM);
        EditorPanePrinter pnl = new EditorPanePrinter(item, pp, new Insets(0, 0, 0, 0));

        if (!Configuration.isPrinterTest()) {
            pnl.print(p);
        } else {
            JFrame frame = new JFrame("Main print frame");
            pnl.setBackground(Color.black);
            frame.add(pnl);
            frame.pack();
            frame.setVisible(true);
        }


        /*
         DocPrintJob printJob = p.createPrintJob();
         //attrc.add(MediaSize.findMedia((float) 80, (float) 160, MediaSize.MM));
         //addMediaSizeByName(p, attrc, "80mm * 160mm");
         Doc docc = new SimpleDoc(item.getPrintable(null, null), DocFlavor.SERVICE_FORMATTED.PRINTABLE, attrc);
         printJob.addPrintJobListener(new MyPrintListener());
         if (!Configuration.isPrinterTest()) {
         printJob.print(docc, null);
         } else {
         JFrame frame = new JFrame("Main print frame");
         frame.getContentPane().add(item);
         frame.setSize((int) (4.1 * PAGE_WIDTH), (int) (4.1 * paperLen));
         frame.pack();
         frame.setVisible(true);
         }
         */
        /*            attrs.add(new MediaPrintableArea((float) 0, (float) 0, (float) 80, (float) 160, MediaPrintableArea.MM));
         attrs.add(OrientationRequested.PORTRAIT);
         addMediaSizeByName(p, attrs, "80mm * 160mm");
         */
        //Logger.debug("Printer : " + p.getName() + " " + p.toString());
        //item.print(null, null, false, p, attrs, false);
    }
// Hack to add something from sun.print.

    void addMediaSizeByName(PrintService p, PrintRequestAttributeSet attrs, String name) {
        Object o = p.getSupportedAttributeValues(javax.print.attribute.standard.Media.class, null, null);
        if (o
                == null) {
            return;
        }

        if (o.getClass()
                .isArray()) {
            for (int k = 0; k < Array.getLength(o); k++) {
                Object o2 = Array.get(o, k);
                if (o2 != null) {
                    //Logger.debug(" O2 %s %s", o2.getClass(), o2);
                    if (o2.toString().equalsIgnoreCase(name)) {
                        attrs.add((javax.print.attribute.standard.Media) o2);
                        break;
                    }
                }
            }
        } else {
            Logger.debug(" O %s %s", o.getClass(), o);
        }
    }

    void addMediaSizeByName(PrintService p, DocAttributeSet attrs, String name) {
        Object o = p.getSupportedAttributeValues(javax.print.attribute.standard.Media.class, null, null);
        if (o
                == null) {
            return;
        }

        if (o.getClass()
                .isArray()) {
            for (int k = 0; k < Array.getLength(o); k++) {
                Object o2 = Array.get(o, k);
                if (o2 != null) {
                    //Logger.debug(" O2 %s %s", o2.getClass(), o2);
                    if (o2.toString().equalsIgnoreCase(name)) {
                        attrs.add((javax.print.attribute.standard.Media) o2);
                        break;
                    }
                }
            }
        } else {
            Logger.debug(" O %s %s", o.getClass(), o);
        }
    }

    public void printAttributes() {
        if (printers.containsKey(port)) {
            PrintService p = printers.get(port);

            PrintServiceAttributeSet pts = p.getAttributes();
            for (Attribute a : pts.toArray()) {
                Logger.debug(" Attribute %s %s %s %s", a.getClass(), a.getName(), a.getCategory(), a);
            }

            Class[] cats = p.getSupportedAttributeCategories();
            for (int j = 0; j < cats.length; j++) {
                if (cats[j] == null) {
                    continue;
                }
                Logger.debug("Category %s %s", cats[ j].getClass(), cats[j]);
                Attribute attr = (Attribute) p.getDefaultAttributeValue(cats[j]);
                if (attr == null) {
                    continue;
                }
                Logger.debug(" Attr %s %s", attr.getName(), attr.toString());
                Object o = p.getSupportedAttributeValues(attr.getCategory(), null, null);
                if (o == null) {
                    continue;
                }
                Logger.debug(" O %s %s", o.getClass(), o);
                if (o.getClass().isArray()) {
                    for (int k = 0; k < Array.getLength(o); k++) {
                        Object o2 = Array.get(o, k);
                        if (o2 != null) {
                            Logger.debug(" Possible values %s %s", o2.getClass().toString(), o2);

                            if (o2.getClass() != null && o2.getClass().toString() != null && o2.getClass().toString().equalsIgnoreCase("sun.print.CustomMediaSizeName")) {
                                /*if (o2.toString().equalsIgnoreCase("custom")) {
                                 Logger.debug("-------------> Possible values %s %s", o2.getClass().toString(), o2);
                                 //attrs.add((MediaSize) o2);
                                 }*/
                            }
                        }
                    }
                }
            }
        }
    }
}
