/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices;

import java.awt.print.PrinterException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.PrintRequestAttributeSet;
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

    public Map<String, PrintService> printers = new HashMap<String, PrintService>();
    PrintRequestAttributeSet attrs = null;
    String port = null;

    Printer(String port, PrintRequestAttributeSet attrs) {
        this.port = port;
        this.attrs = attrs;
        PrintService[] prnSvcs;
        prnSvcs = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService p : prnSvcs) {
            printers.put(p.getName(), p);
        }
    }

    public void print(String templateName, RenderArgs renderArgs) throws PrinterException {
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

        if (printers.containsKey(port)) {
            PrintService p = printers.get(port);
            Logger.debug("Printer : " + p.getName() + " " + p.toString());
            item.print(null, null, false, p, attrs, false);
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
}
