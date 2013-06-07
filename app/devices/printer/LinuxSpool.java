/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.printer;

import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;

/**
 *
 * @author adji
 */
public class LinuxSpool {

    public interface LinuxCupsLib extends StdCallLibrary {

        LinuxCupsLib INSTANCE = (LinuxCupsLib) Native.loadLibrary("cups", LinuxCupsLib.class);
    }
}