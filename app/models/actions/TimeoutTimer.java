/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.actions;

import java.util.Timer;
import java.util.TimerTask;
import models.actions.UserAction;
import play.Logger;
import play.Play;
import play.jobs.Job;

/**
 *
 * @author adji
 */
public class TimeoutTimer extends Job {

    public enum State {

        WARN, CANCEL;
    }

    class JobAdapter extends TimerTask {

        @Override
        public void run() {
            TimeoutTimer.this.now();
        }
    }
    public State state = State.WARN;
    protected int warn_timeout = 60;
    protected int cancel_timeout = 60;
    final public UserAction userAction;
    protected Timer timer;

    public TimeoutTimer(UserAction userAction) {
        this.userAction = userAction;
        try {
            warn_timeout = Integer.parseInt(Play.configuration.getProperty("timer.warn_timeout"));
        } catch (NumberFormatException e) {
            Logger.debug("Error parsing timer.cancel_timeout %s", e.getMessage());
        }
        try {
            cancel_timeout = Integer.parseInt(Play.configuration.getProperty("timer.warn_timeout"));
        } catch (NumberFormatException e) {
            Logger.debug("Error parsing timer.cancel_timeout %s", e.getMessage());
        }
    }

    public void start() {
        Logger.debug("TimeoutTimer start %s current %s", state.name(), userAction.state.getClass().getSimpleName());
        state = State.WARN;
        timer = new Timer();
        timer.schedule(new JobAdapter(), warn_timeout * 1000);
    }

    public void cancel() {
        Logger.debug("TimeoutTimer cancel %s current %s", state.name(), userAction.state.getClass().getSimpleName());
        if (timer != null) {
            timer.cancel();
        }
        timer = null;
    }

    @Override
    public void doJob() {
        Logger.debug("TimeoutTimer doJob %s current %s", state.name(), userAction.state.getClass().getSimpleName());
        cancel();
        switch (state) {
            case WARN:
                userAction.onTimeoutEvent(this);
                state = State.CANCEL;
                timer = new Timer();
                timer.schedule(new JobAdapter(), cancel_timeout * 1000);
                break;
            case CANCEL:
                userAction.onTimeoutEvent(this);
                break;
        }
    }
}
