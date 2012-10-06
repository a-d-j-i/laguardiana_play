/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices;

import java.awt.print.PrinterException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.Attribute;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.event.PrintJobEvent;
import javax.print.event.PrintJobListener;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import play.Logger;
import play.exceptions.TemplateNotFoundException;
import play.templates.Template;
import play.templates.TemplateLoader;

/**
 *
 * @author adji
 */
public class Printer {

    // TODO: Manage errors.
    class MyPrintListener implements PrintJobListener {

        public void printDataTransferCompleted(PrintJobEvent pje) {
        }

        public void printJobCompleted(PrintJobEvent pje) {
        }

        public void printJobFailed(PrintJobEvent pje) {
        }

        public void printJobCanceled(PrintJobEvent pje) {
        }

        public void printJobNoMoreEvents(PrintJobEvent pje) {
        }

        public void printJobRequiresAttention(PrintJobEvent pje) {
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

    public void print(String templateName, Map args) throws PrinterException, PrintException {
        Template template = null;
        try {
            template = TemplateLoader.load("Printer/" + templateName + ".html");
        } catch (TemplateNotFoundException e) {
        }
        if (template == null) {
            template = TemplateLoader.load("Printer/" + templateName + ".txt");
        }
        String body = template.render(args);
        Logger.debug("PRINT : %s", body);

        HTMLEditorKit kit = new HTMLEditorKit();
        HTMLDocument doc = (HTMLDocument) (kit.createDefaultDocument());
        Reader fin = new StringReader(body);
        try {
            kit.read(fin, doc, 0);
            fin.close();
        } catch (IOException ex) {
            Logger.error(ex.toString());
        } catch (BadLocationException ex) {
            Logger.error(ex.toString());
        }

        JEditorPane item = new JEditorPane();
        item.setEditorKit(kit);
        item.setDocument(doc);
        item.setEditable(false);


        PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
        // http://docs.oracle.com/javase/1.4.2/docs/api/javax/print/attribute/PrintRequestAttribute.html
        //attrs.add(new Copies(1));
        //attrs.add(Sides.ONE_SIDED);

        //attrs.add(OrientationRequested.PORTRAIT);
        //attrs.add(MediaSizeName.ISO_A4);
        //attrs.add(PrintQuality.DRAFT);
        //attrs.add(new PrinterResolution(600, 600, PrinterResolution.DPI));
        //attrs.add(new MediaPrintableArea(0, 0, 10, 10, MediaPrintableArea.MM));


        // Two "false" args mean "no print dialog" and "non-interactive" ( ie, batch - mode printing). 
        if (printers.containsKey(port)) {
            PrintService p = printers.get(port);
            DocPrintJob printJob = p.createPrintJob();
            HashDocAttributeSet attrc = new HashDocAttributeSet();
            //attrc.add(new MediaPrintableArea((float) 0, (float) 0, (float) 80, (float) 30, MediaPrintableArea.MM));
            attrc.add(OrientationRequested.PORTRAIT);
            attrc.add(MediaSize.findMedia((float) 80, (float) 30, MediaSize.MM));
            addMediaSizeByName(p, attrc, "80mm * 160mm");
            Doc docc = new SimpleDoc(item.getPrintable(null, null), DocFlavor.SERVICE_FORMATTED.PRINTABLE, attrc);
            //printJob.addPrintJobListener(new MyPrintListener());
            printJob.print(docc, null);


            /*            attrs.add(new MediaPrintableArea((float) 0, (float) 0, (float) 80, (float) 160, MediaPrintableArea.MM));
             attrs.add(OrientationRequested.PORTRAIT);
             addMediaSizeByName(p, attrs, "80mm * 160mm");
             */

            //Logger.debug("Printer : " + p.getName() + " " + p.toString());
            //item.print(null, null, false, p, attrs, false);
        } else {
            throw new PrinterException(String.format("Printer %s not found", port));


        }
    }
//    private void printDocTest1(DocFlavor format, String body) throws PrintException, PrinterException {
//        //format = DocFlavor.INPUT_STREAM.AUTOSENSE;
//        //ByteArrayInputStream b = new ByteArrayInputStream(body.getBytes());
//        //StringReader r = new StringReader(body);
//        Doc myDoc = new SimpleDoc(body, format, null);
//        PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
//        aset.add(new Copies(1));
//        aset.add(Sides.ONE_SIDED);
//        //aSet.add(new MediaSize(58, 120000, MediaSize.MM));
//        PrintServiceAttributeSet s = printService.getAttributes();
//        for (Attribute a : s.toArray()) {
//            Logger.debug("Default printer service attr: " + a.getName() + " " + a.toString());
//        }
//        DocPrintJob job = printService.createPrintJob();
//        job.print(myDoc, aset);
//    }

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
