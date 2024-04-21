package main.communication;

import main.Constants;

public class PacketQueue
{
    private SimpleQueue mControlQueueIn;
    private SimpleQueue mControlQueueOut;
    private SimpleQueue mUpdatesQueueIn;
    private SimpleQueue mUpdatesQueueOut;

    public PacketQueue(){
        mControlQueueIn = new SimpleQueue(50,Constants.PACKET_MAX_SIZE);
        mControlQueueOut = new SimpleQueue(50,Constants.PACKET_MAX_SIZE);
        mUpdatesQueueIn = new SimpleQueue(50,Constants.PACKET_MAX_SIZE);
        mUpdatesQueueOut = new SimpleQueue(50,Constants.PACKET_MAX_SIZE);

    }

    public SimpleQueue getControlQueueIn(){
        return mControlQueueIn;
    }

    public SimpleQueue getControlQueueOut(){
        return mControlQueueOut;
    }


    public SimpleQueue getUpdatesQueueIn(){
        return mUpdatesQueueIn;
    }


    public SimpleQueue getUpdatesQueueOut(){
        return mUpdatesQueueOut;
    }
}
