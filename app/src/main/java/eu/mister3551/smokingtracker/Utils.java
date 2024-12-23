package eu.mister3551.smokingtracker;

import java.lang.ref.WeakReference;

import eu.mister3551.smokingtracker.database.Manager;
import eu.mister3551.smokingtracker.record.Settings;

public class Utils {

    public static WeakReference<MainActivity> mainActivity;
    public static WeakReference<Manager> manager;
    public static Settings settings;
    public static final String appVersionLink = "https://github.com/pintargasper/SmokingTracker/releases/tag/v1.0.4/";

    public static WeakReference<MainActivity> getMainActivity() {
        return mainActivity;
    }

    public static void setMainActivity(WeakReference<MainActivity> mainActivity) {
        Utils.mainActivity = mainActivity;
    }

    public static WeakReference<Manager> getManager() {
        return manager;
    }

    public static void setManager(WeakReference<Manager> manager) {
        Utils.manager = manager;
    }

    public static Settings getSettings() {
        return settings;
    }

    public static void setSettings(Settings settings) {
        Utils.settings = settings;
    }
}