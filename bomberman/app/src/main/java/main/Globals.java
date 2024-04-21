package main;

import android.util.SparseArray;

import java.util.Vector;

public class Globals
{
    /* Variables */
    public static int selectedMap = 1;
    public static Object mSyncObject = new Object();
    public static long mDebugLoopMin = 0xFFFFFF;
    public static long mDebugLoopMax = 0;
    public static long mDebugLoopTotal = 0;
    public static long mDebugRendererTimeMax = 0;
    public static long mDebugRendererTimeMin = 0xFFFFFF;
    public static long mDebugNetworkTimeMin = 0xFFFFFF;
    public static long mDebugNetworkTimeMax = 0;
    public static int mDebugNumberOfTransmittedPackets = 0;
    public static int mDebugNumberOfReceivedPackets = 0;
    public static int mDebugMeanLatency;
    public static int mDebugMinLatency;
    public static int mDebugMaxLatency;
    public static int mDebugCountLatency;

}