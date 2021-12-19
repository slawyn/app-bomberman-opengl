package main.communication.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.SparseArray;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import main.Globals;
import main.Logger;
import main.communication.Server;
import main.communication.SimpleQueue;

import static main.Constants.*;
import static main.Globals.mSyncObject;

public class ServerBluetoothCLASSIC extends Server
{
    private static final String TAG = "ServerBluetoothCLASSIC";
    private SparseArray<BluetoothConnection> mConnections;
    private BluetoothServerSocket serverSocket;
    private static byte[] buffer;
    private int mListeningTime;

    public ServerBluetoothCLASSIC(int listentimeout)
    {
        super();
        mListeningTime = listentimeout;
        mServerListening = false;
        mConnections = new SparseArray<>();
        new Thread(serverListener, "BluetoothClassicListener").start();
    }

    /* https://stackoverflow.com/questions/35953413/bluetooth-connect-without-pairing */
    private Runnable serverListener = new Runnable()
    {
        public void run()
        {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            adapter.setName(SERVER_NAME);

            UUID uuid = UUID.fromString(SERVER_UUID);

            try
            {
                serverSocket = adapter.listenUsingInsecureRfcommWithServiceRecord("BomberServer", uuid);   //listenUsingRfcommWithServiceRecord
            } catch(IOException e)
            {
                e.printStackTrace();
            }

            Logger.log(Logger.DEBUG, TAG, "Listening...");


            new Thread(commThread, TAG).start();
            BluetoothSocket socket;
            while(mListeningTime >= 0)
            {
                byte newid = 0;
                try
                {
                    socket = serverSocket.accept(1000);
                    if(socket != null)
                    {
                        if(mConnections.size() < (MAX_NUMBER_OF_PLAYERS - 1))
                        {
                            newid = generateClientID();
                            mConnections.put(newid, new BluetoothConnection(socket, newid));
                            Logger.log(Logger.DEBUG, TAG, "Socket accepted..." + (newid));
                        }
                    }


                } catch(IOException e)
                {
                    if(newid != 0)
                    {
                        removeClientID(newid);
                    }
                }

                --mListeningTime;
            }

            try
            {
                serverSocket.close();
            } catch(IOException e)
            {
                e.printStackTrace();
            }

            Logger.log(Logger.DEBUG, TAG, "Bluetooth timeout reached: ServerSocket closed");
        }
    };

    private Runnable commThread = new Runnable()
    {
        public void run()
        {
            mServerAlive = true;
            ;
            /* BluetoothServer exchange*/
            long start;
            long ttime;

            // Listen for incoming mConnections



            SimpleQueue cqout = mPacketQueue.getControlQueueOut();
            SimpleQueue uqout = mPacketQueue.getUpdatesQueueOut();

            while(mServerAlive)
            {
                // Start communicating



                try
                {

                    synchronized(mSyncObject){
                        mSyncObject.wait();
                    }

                    start = System.nanoTime();

                    /* Process incoming network data*/
                    for(int i = mConnections.size() - 1; i >= 0; i--)
                    {

                        BluetoothConnection connection = mConnections.valueAt(i);
                        if(connection != null)
                        {
                            try
                            {
                                connection.receiveData();
                            } catch(Exception e)
                            {

                                mConnections.removeAt(i);
                                connection.close();
                                e.printStackTrace();
                            }
                        }
                    }

                    /* Send mState/time data to players*/
                    while(cqout.notEmpty())
                    {
                        byte[] data = cqout.dequeue();
                        data[PROTOCOL_FLAGS_CONTROL] |= PROTOCOL_FLAGS_CONTROL;

                        int id = data[PROTOCOL_OFFSET_ID];
                        if(id != 0)
                        {

                            mConnections.get(id).sendControlData(data);
                        } else
                        {
                            for(int i = 0; i < mConnections.size(); i++)
                            {
                                BluetoothConnection connection = mConnections.get(i);
                                connection.sendControlData(data);
                            }
                        }
                    }

                    /* Send mState/time data to players*/
                    while(uqout.notEmpty())
                    {
                        byte[] data = uqout.dequeue();

                        int id = data[PROTOCOL_OFFSET_ID];
                        if(id != 0)
                        {
                            BluetoothConnection connection = mConnections.get(id);
                            if(connection != null)
                            {
                                connection.sendUpdatesData(data);
                            }

                        } else
                        {
                            for(int i = 0; i < mConnections.size(); i++)
                            {

                                BluetoothConnection connection = mConnections.valueAt(i);
                                connection.sendUpdatesData(data);
                            }
                        }
                    }

                    // Time measurement
                    ttime = System.nanoTime() - start;
                    if(ttime > Globals.mDebugNetworkTimeMax)
                    {
                        Globals.mDebugNetworkTimeMax = ttime;
                    } else if(ttime < Globals.mDebugNetworkTimeMin)
                    {
                        Globals.mDebugNetworkTimeMin = ttime;
                    }

                } catch(Exception e)
                {
                    e.printStackTrace();
                }


            }
        }
    };


    public void closeConnection()
    {
        try
        {
            /* Close mConnections */
            for(int idx = 0; idx < mConnections.size(); idx++)
            {
                int key = mConnections.keyAt(idx);
                BluetoothConnection connection = mConnections.get(key);
                connection.close();
            }

            /* Close server socket*/
            serverSocket.close();
            mServerAlive = false;
        } catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public class BluetoothConnection
    {
        private final String TAG = "Connection";
        private BluetoothSocket socket;
        private InputStream is;
        private OutputStream os;
        private byte id;
        private byte[] headerbuf;


        public BluetoothConnection(BluetoothSocket sock, byte conid) throws IOException
        {

            socket = sock;
            id = conid;
            is = socket.getInputStream();
            os = socket.getOutputStream();
            headerbuf = new byte[PACKET_HEADER_SIZE];

            // Send Id to Client
            byte[] buffer = new byte[PACKET_MAX_SIZE];
            buffer[PROTOCOL_OFFSET_TYPE] = PROTOCOL_TYPE_IDDIST;
            buffer[PROTOCOL_OFFSET_FLAGS] |= PROTOCOL_FLAGS_CONTROL;
            buffer[PROTOCOL_OFFSET_ID] = id;
            os.write(buffer);
            os.flush();


        }

        public byte getID()
        {
            return id;
        }

        public void receiveData() throws IOException
        {
            if(is.available() >= PACKET_MAX_SIZE && is.read(headerbuf, 0, PACKET_HEADER_SIZE) != -1)
            {
                if(headerbuf[PROTOCOL_OFFSET_ID] == id)
                {
                    if((headerbuf[PROTOCOL_FLAGS_CONTROL] & PROTOCOL_FLAGS_CONTROL) > 0)
                    {

                        SimpleQueue cqin = mPacketQueue.getControlQueueIn();
                        buffer = cqin.next();

                        for(int idx = 0; idx < PACKET_HEADER_SIZE; idx++)
                        {
                            buffer[idx] = headerbuf[idx];
                        }

                        if(is.read(buffer, PACKET_HEADER_SIZE, PACKET_MAX_SIZE - PACKET_HEADER_SIZE) != -1)
                        {
                            cqin.enqueue();
                            Globals.mDebugNumberOfReceivedPackets++;
                        }
                    } else
                    {
                        SimpleQueue uqin = mPacketQueue.getUpdatesQueueIn();
                        buffer = uqin.next();
                        for(int idx = 0; idx < PACKET_HEADER_SIZE; idx++)
                        {
                            buffer[idx] = headerbuf[idx];
                        }
                        if(is.read(buffer, PACKET_HEADER_SIZE, PACKET_MAX_SIZE - PACKET_HEADER_SIZE) != -1)
                        {
                            uqin.enqueue();
                            Globals.mDebugNumberOfReceivedPackets++;
                        }
                    }
                }
            }
        }


        public void sendUpdatesData(byte[] state) throws IOException
        {
            os.write(state);
            os.flush();
            Globals.mDebugNumberOfTransmittedPackets++;
        }

        public void sendControlData(byte[] state) throws IOException
        {
            os.write(state);
            os.flush();
            Globals.mDebugNumberOfTransmittedPackets++;
        }


        public void close()
        {
            try
            {   SimpleQueue cqin = mPacketQueue.getControlQueueIn();
                byte[] buff = cqin.next();
                buff[PROTOCOL_OFFSET_ID] = id;
                buff[PROTOCOL_OFFSET_TYPE] = PROTOCOL_TYPE_CCON;
                cqin.enqueue();

                socket.close();
                is.close();
                os.close();
            } catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}

