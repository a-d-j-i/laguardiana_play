package models;

import java.awt.print.PrinterJob;
import java.io.*;
import java.util.logging.Level;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaSize;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import play.Logger;

public class TemplatePrinter {

    /**
     * Default print service.
     */
    //static PrintService printService = PrintServiceLookup.lookupDefaultPrintService();
    static PrintService[] prnSvcs = PrintServiceLookup.lookupPrintServices( null, null );

    public static void printTemplate1( String htmlText ) {

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

        //try {
        for ( PrintService p : prnSvcs ) {
            if ( p.getName().equalsIgnoreCase( "san" ) ) {
                /*
                 * item.print( // Two "false" args mean "no print dialog" and //
                 * "non-interactive" ( ie, batch - mode printing). null, null,
                 * false, p, null, false );
                 */
            }
        }
        //} catch ( PrinterException ex ) {
        //    Logger.error( ex.toString() );
        //}
    }

    public static void printTemplate2( String htmlText ) {

        PrintService[] services = PrintServiceLookup.lookupPrintServices( null, null );

        for ( PrintService p : services ) {
            Logger.error( "Printer : " + p.getName() + " " + p.toString() );
            if ( p.getName().equalsIgnoreCase( "san" ) ) {
                PrintServiceAttributeSet s = p.getAttributes();
                for ( DocFlavor df : p.getSupportedDocFlavors() ) {
                    Logger.error( df.toString() );
                }
                for ( Attribute a : s.toArray() ) {
                    Logger.error( a.getName() + " " + a.toString() );
                }
            }
        }
    }

    public static void printTemplate( String htmlText ) {

        DocFlavor myFormat = DocFlavor.INPUT_STREAM.TEXT_HTML_UTF_8;
        StringReader r = new StringReader( htmlText );
        ByteArrayInputStream b = new ByteArrayInputStream( htmlText.getBytes() );

        Doc myDoc = new SimpleDoc( b, myFormat, null );
        PrintRequestAttributeSet aSet = new HashPrintRequestAttributeSet();
        aSet.add( new Copies( 1 ) );
        //aSet.add( new MediaSize( 58, 120000,  ) );
        PrintService[] services = PrintServiceLookup.lookupPrintServices( myFormat, aSet );

        for ( PrintService p : services ) {
            Logger.error( "Printer : " + p.getName() + " " + p.toString() );
            PrintServiceAttributeSet s = p.getAttributes();
            for ( Attribute a : s.toArray() ) {
                Logger.error( a.getName() + " " + a.toString() );
            }
            if ( p.getName().equalsIgnoreCase( "san" ) ) {
                DocPrintJob j = p.createPrintJob();
                try {
                    j.print( myDoc, aSet );
                } catch ( PrintException ex ) {
                    Logger.error( ex.toString() );
                }
            }
        }
    }
}
