package playrunner;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import play.Logger;
import play.Play;

/**
 *
 * @author adji
 */
public class PlayRunner {

    /**
     * @param args the command line arguments
     * @throws java.lang.InterruptedException dont care
     * @throws java.io.IOException dont care
     * @throws java.net.URISyntaxException dont care
     */
    public static void main(String[] args) throws InterruptedException, IOException, URISyntaxException {

        Play.usePrecompiled = true;
        File cwd = new File(PlayRunner.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        File appF = new File(cwd.getParent(), "application");
        String appDir = appF.getAbsolutePath();

        File root = new File(appDir);
        final String playId = "";
        // This is really important as we know this parameter already (we are running in a servlet container)
//        Play.frameworkPath = root.getParentFile();
        Play.init(root, playId);
        Server server = new Server();

        try {
            Logger.debug("START");
            System.out.println("START");
            server.start();
            boolean done = false;
            while (!done) {
                int ch = System.in.read();
                switch (ch) {
                    case 'C':
                    case 'X':
                    case 'Q':
                    case 'c':
                    case 'x':
                    case 'q':
                        Logger.debug("EXIT BY KEYBOARD");
                        System.out.println("EXIT BY KEYBOARD");
                        done = true;
                        break;
                    default:
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                        }
                }
            }
        } finally {
            Logger.debug("EXIT, CALLING STOP");
            System.out.println("EXIT, CALLING STOP");
            server.stop();
            Play.stop();
        }
        System.exit(0);
    }

}
