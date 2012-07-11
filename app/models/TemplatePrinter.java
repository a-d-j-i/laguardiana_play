package models;

import java.awt.Dimension;
import java.awt.print.PrinterException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import play.Logger;

public class TemplatePrinter {

    /**
     * Default print service.
     */
    static PrintService printService = PrintServiceLookup.lookupDefaultPrintService();
    //PrintService[] prnSvcs = PrintServiceLookup.lookupPrintServices(null, null);

    public static void printTemplate( String htmlText ) {

        HTMLEditorKit kit = new HTMLEditorKit();
        HTMLDocument doc = ( HTMLDocument ) ( kit.createDefaultDocument() );
        Reader fin = new StringReader( htmlText );
        try {
            kit.read( fin, doc, 0 );
            fin.close();
        } catch ( IOException ex ) {
            Logger.error( ex.toString() );
        } catch ( BadLocationException ex ) {
            Logger.error( ex.toString() );
        }

        JEditorPane item = new JEditorPane();
        item.setEditorKit( kit );
        item.setDocument( doc );
        //item.setPreferredSize( new Dimension( 800, 600 ) );
        item.setEditable( false );

        try {
            item.print( // Two "false" args mean "no print dialog" and 
                    // "non-interactive" ( ie, batch - mode printing). 
                    null, null, false,
                    printService, null, false );
        } catch ( PrinterException ex ) {
            Logger.error( ex.toString() );
        }

    }
}