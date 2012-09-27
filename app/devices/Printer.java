/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.*;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import play.Logger;
import play.exceptions.TemplateNotFoundException;
import play.mvc.Scope.RenderArgs;
import play.templates.Template;
import play.templates.TemplateLoader;

/**
 *
 * @author adji
 */
public class Printer {

    public List<PrintService> printers = new ArrayList<PrintService>();
    PrintService printService = null;
    PrintRequestAttributeSet attrs = null;

    Printer(String port, PrintRequestAttributeSet attrs) throws IOException {
        PrintService[] prnSvcs;
        prnSvcs = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService p : prnSvcs) {
            printers.add(p);
            if (p.getName().equalsIgnoreCase(port)) {
                this.printService = p;
                this.attrs = attrs;
            }
        }
        if (printService == null) {
            throw new IOException(String.format("Printer not found %s", port));
        }
    }

    public void print(String templateName, RenderArgs renderArgs) throws Throwable {
        Map args = new HashMap();
        if (renderArgs != null) {
            args = renderArgs.data;
        }
        Template template = null;
        try {
            template = TemplateLoader.load("Printer/" + templateName + ".html");
        } catch (TemplateNotFoundException e) {
        }
        if (template == null) {
            template = TemplateLoader.load("Printer/" + templateName + ".txt");
        }
        String body = template.render(args);
        Logger.debug("Printer : " + printService.getName() + " " + printService.toString());
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

        item.print(null, null, false, printService, attrs, false);
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
}
