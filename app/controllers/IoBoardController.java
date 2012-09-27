package controllers;

import devices.CounterFactory;
import devices.io_board.IoBoard.IoBoardStatus;
import java.io.IOException;
import play.Logger;
import play.Play;
import play.mvc.Before;
import play.mvc.Router;

public class IoBoardController extends Application {

    static devices.io_board.IoBoard ioBoard;

    @Before
    static void getBoard() throws Throwable {
        ioBoard = CounterFactory.getIoBoard(Play.configuration.getProperty("io_board.port"));
        if (ioBoard == null) {
            flash.put("error", "Error opening port");
            redirect(Router.reverse("IoBoardController.index").url);
        }
    }

    public static void index() {
        String error = flash.get("error");
        if (error != null) {
            renderArgs.put("error", error);
            render();
        }
        IoBoardStatus s = null;
        try {
            s = ioBoard.getStatus(500);
        } catch (InterruptedException ex) {
            Logger.error("Timeout waiting for status %s", ex.getMessage());
        }
        if (s != null) {
            renderArgs.put("status", s);
            renderArgs.put("A", Integer.toHexString(s.A));
            renderArgs.put("B", Integer.toHexString(s.B));
            renderArgs.put("C", Integer.toHexString(s.C));
            renderArgs.put("D", Integer.toHexString(s.D));
        }
        render();
    }

    public static void openGate() throws IOException {
        ioBoard.openGate();
        index();
    }

    public static void closeGate() {
        ioBoard.closeGate();
        index();
    }
}
