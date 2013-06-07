package controllers;

import devices.DeviceFactory;
import java.io.IOException;
import play.Play;
import play.mvc.Before;
import play.mvc.Router;

public class IoBoardController extends Application {

    static devices.ioboard.IoBoard ioBoard;

    @Before
    static void getBoard() throws Throwable {
        ioBoard = DeviceFactory.getIoBoard(Play.configuration.getProperty("io_board.port"));
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
        renderArgs.put("status", ioBoard.getInternalState());
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

    public static void aproveBag() throws IOException {
        ioBoard.aproveBag();
        index();
    }

    public static void aproveBagConfirm() {
        ioBoard.aproveBagConfirm();
        index();
    }

    public static void clearError() {
        ioBoard.clearError();
        index();
    }
}
