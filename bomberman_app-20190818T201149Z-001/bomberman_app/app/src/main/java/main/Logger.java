package main;

import android.app.ActivityManager;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

public class Logger
{
    public static final int DEBUG = 0;
    public static final int VERBOSE = 1;
    public static final int INFO = 2;
    public static final int WARNING = 3;
    public static final int ERROR = 4;
    public static final int ASSERT = 5;
    static StringBuilder mOutput = new StringBuilder(100);
    static StringBuilder mOutput2 = new StringBuilder(100);
    static final String LOG = String.format("%-8s", "BOMBER");
    static final String TIME = String.format("%-8s", "TIME");
    static int mColors;
    private static ActivityManager mActivityManager;
    private static ActivityManager.MemoryInfo mMemoryInformation;

    public static void init(String mod, ActivityManager activityman)
    {
        mColors = 0;
        mActivityManager = activityman;
        mMemoryInformation = new ActivityManager.MemoryInfo();
        if(mod.equals("Ulefone_S1"))
        {
            mColors = 1;
        }
    }

    public static int getAvailableMemory()
    {
        mActivityManager.getMemoryInfo(mMemoryInformation);
        return (int) (mMemoryInformation.availMem / 0x100L);
    }

    public static void logTimes(long deltaerror)
    {
        mOutput2.setLength(0);

        mOutput2.append("\n>------------------------------<\n");
        mOutput2.append("Free Memory: (KB): ");
        mOutput2.append(getAvailableMemory());
        mOutput2.append("\nSV: offset(us): ");
        mOutput2.append(deltaerror / 1000);
        mOutput2.append("\n    loop  (us): [");
        mOutput2.append(Globals.mDebugLoopMin / 1000);
        mOutput2.append(", ");
        mOutput2.append(Globals.mDebugLoopMax / 1000);
        mOutput2.append("] (");
        mOutput2.append(Globals.mDebugLoopTotal / 1000);
        mOutput2.append(")\nGL: render(us): [");
        mOutput2.append(Globals.mDebugRendererTimeMin / 1000);
        mOutput2.append(", ");
        mOutput2.append(Globals.mDebugRendererTimeMax / 1000);
        mOutput2.append("]");
        if(Globals.mDebugNetworkTimeMax != 0)
        {
            mOutput2.append("\nNW: network(us): [");
            mOutput2.append(Globals.mDebugNetworkTimeMin / 1000);
            mOutput2.append(", ");
            mOutput2.append(Globals.mDebugNetworkTimeMax / 1000);
            mOutput2.append("] rx:");
            mOutput2.append(Globals.mDebugNumberOfReceivedPackets );
            mOutput2.append(" tx:");
            mOutput2.append(Globals.mDebugNumberOfTransmittedPackets );
        }

        if(Globals.mDebugCountLatency != 0)
        {
            mOutput2.append("\nTick: (10 ms)  Mean:");
            mOutput2.append(Globals.mDebugMeanLatency / Globals.mDebugCountLatency);
            mOutput2.append(" [");
            mOutput2.append(Globals.mDebugMinLatency);
            mOutput2.append(", ");
            mOutput2.append(Globals.mDebugMaxLatency);
            mOutput2.append("] Deviance: ");
            mOutput2.append(Globals.mDebugMaxLatency - Globals.mDebugMinLatency);

            Globals.mDebugMeanLatency = 0;
            Globals.mDebugMaxLatency = 0;
            Globals.mDebugMinLatency = 0xFFFF;
            Globals.mDebugCountLatency = 0;
        }


        if(mColors > 0)
            Log.i(TIME, mOutput2.toString());
        else
            Log.v(TIME, mOutput2.toString());

        Globals.mDebugLoopTotal = 0;
        Globals.mDebugLoopMax = 0;
        Globals.mDebugLoopMin = 0xFFFFFFFFFFFFL;
        Globals.mDebugRendererTimeMax = 0;
        Globals.mDebugRendererTimeMin = 0xFFFFFFFFFFFFL;
        Globals.mDebugNetworkTimeMin = 0xFFFFFFFFFFFFL;
        Globals.mDebugNetworkTimeMax = 0;
    }

    public static void log(int severety, String tag, String s)
    {

        mOutput.setLength(0);
        mOutput.append(SystemClock.elapsedRealtime() % 10000);
        mOutput.append(" ");
        mOutput.append(s);
        switch(severety)
        {
            case DEBUG:
                Log.d(LOG, mOutput.toString());
                break;
            case VERBOSE:
                Log.v(LOG, mOutput.toString());
                break;
            case INFO:
                Log.i(LOG, mOutput.toString());
                break;
            case WARNING:
                Log.w(LOG, mOutput.toString());
                break;
            case ERROR:
                Log.e(LOG, mOutput.toString());
                break;
            case ASSERT:
                Log.wtf(LOG, mOutput.toString());
        }
    }
}
