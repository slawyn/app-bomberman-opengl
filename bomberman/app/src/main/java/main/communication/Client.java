package main.communication;

import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.net.Inet4Address;

import main.communication.PacketQueue;

public abstract class Client
{
    protected final String TAG = "Client";
    protected boolean mConnectionAlive;
    protected PacketQueue mPacketQueue;
    protected int mPort = 55555;
    protected String mAddress;
    protected Inet4Address mInet4Address;
    protected OutputStream mOutputStream;
    protected BufferedInputStream mInputStream;


    public void closeConnection()
    {

    }

    public PacketQueue createPacketQueue()
    {
        mPacketQueue = new PacketQueue();
        return mPacketQueue;
    }
}
