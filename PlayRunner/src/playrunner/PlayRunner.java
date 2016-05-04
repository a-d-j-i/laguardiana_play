package playrunner;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import play.Play;

/**
 *
 * @author adji
 */
public class PlayRunner {

    /**
     * @param args the command line arguments
     * @throws java.lang.InterruptedException
     * @throws java.io.IOException
     * @throws java.net.URISyntaxException
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
                        done = true;
                        break;
                    default:
                        Thread.sleep(100);
                }
            }
        } catch (InterruptedException ex) {
        } finally {
            server.stop();
            Play.stop();
        }
        System.exit(0);
    }

}
