package main.communication;


import java.util.Vector;
import main.communication.PacketQueue;

public abstract class Server {
    protected PacketQueue mPacketQueue;
    protected boolean mServerListening;
    protected boolean mServerAlive;
    protected Vector<Byte> mConnectedIds;
    protected int mPort;

    protected Server(){
        mConnectedIds = new Vector<>();
        mPort = 55555;
    }

    public void closeConnection(){}
    public PacketQueue createPacketQueue(){
        mPacketQueue = new PacketQueue();
        return mPacketQueue;
    }

    protected byte generateClientID(){
        int sz = mConnectedIds.size()-1;

        byte id = 0;
        if(sz<0){

            id = 1;
        }else
        {
            id = mConnectedIds.get(sz);

            if(id != 0)
            {
                do
                {
                    ++id;
                } while(mConnectedIds.contains(id));
            }
        }
        mConnectedIds.add(id);
        return id;
    }


    protected void removeClientID(byte id){
        mConnectedIds.remove(mConnectedIds.indexOf(id));
    }
}
