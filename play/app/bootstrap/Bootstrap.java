package bootstrap;

import play.PlayPlugin;
/*
 @OnApplicationStart
 public class Bootstrap extends Job {
 */

/**
 * Plugin to close all devices on app shutdown.
 *
 * Also implements a single thread queue for event so all events are serialized
 * in one worker thread. Based on the JobsPlugin.
 *
 * @author adji
 */
public class Bootstrap extends PlayPlugin {

    @Override
    public void onApplicationStart() {
    }

    @Override
    public void onApplicationStop() {

    }
}
