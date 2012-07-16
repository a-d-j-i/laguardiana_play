package devices;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;
import play.Logger;

public class SerialPortAdapter implements SerialPortEventListener {

    SerialPort                 serialPort;
    ArrayBlockingQueue< Byte > fifo = new ArrayBlockingQueue< Byte >( 1024 );

    public void serialEvent( SerialPortEvent event ) {
        if ( event.isRXCHAR() ) {
            try {
                for( byte b : serialPort.readBytes() ) {
                    fifo.add( b );
                }
            } catch ( SerialPortException e ) {
                Logger.error( "Glory error reading serial port" );
            }
        }
    }

    public SerialPortAdapter( String portName ) throws IOException {

        try {
            String[] ports = SerialPortList.getPortNames();
            Integer p = Integer.parseInt( portName );
            portName = ports[ p ];
        } catch ( NumberFormatException e ) {
        }

        Logger.debug( String.format( "Configuring serial port %s", portName ) );
        serialPort = new SerialPort( portName );
        try {
            Logger.debug( String.format( "Opening serial port %s", portName ) );
            serialPort.openPort();
            serialPort.setParams( SerialPort.BAUDRATE_9600, SerialPort.DATABITS_7, SerialPort.STOPBITS_1,
                            SerialPort.PARITY_EVEN );
            serialPort.setFlowControlMode( SerialPort.FLOWCONTROL_NONE );
            serialPort.setEventsMask( SerialPort.MASK_RXCHAR );
            serialPort.addEventListener( this );
        } catch ( SerialPortException e ) {
            Logger.error( "SerialPortAdapter : " + e.getMessage() );
            throw new IOException( String.format( "Error initializing serial port %s", portName ), e );
        }
    }

    public void close() throws IOException {
        try {
            Logger.debug( String.format( "Closing serial port %s", serialPort.getPortName() ) );
            serialPort.closePort();
        } catch ( SerialPortException e ) {
            throw new IOException( String.format( "Error closing serial port %s", serialPort.getPortName() ), e );
        }
    }

    public void write( byte[] buffer ) throws IOException {
        try {
            serialPort.writeBytes( buffer );
        } catch ( SerialPortException e ) {
            throw new IOException( String.format( "Error wrting to serial port %s", serialPort.getPortName() ), e );
        }
    }

    public byte read() throws IOException {
        Byte ch;
        try {
            ch = fifo.poll( 2, TimeUnit.SECONDS );
        } catch ( InterruptedException e ) {
            throw new IOException( "Interrupt reading from port", e );
        }
        if ( ch == null ) {
            throw new IOException( String.format( "Error reading from port %s", serialPort.getPortName() ) );
        }
        return ch;
    }

    public InputStream getInputStream() {
        return new SerialInputStream();
    }

    class SerialInputStream extends InputStream {
        @Override
        public int read() throws IOException {
            Byte ch = fifo.poll();
            if ( ch == null ) {
                throw new IOException( serialPort.getPortName() + " read  fifo empty" );
            }
            return ch;
        }
    }
}
