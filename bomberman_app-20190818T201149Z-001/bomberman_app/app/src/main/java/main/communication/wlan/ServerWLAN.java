package main.communication.wlan;

import android.os.SystemClock;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import main.Globals;
import main.Logger;
import main.communication.Server;
import main.communication.SimpleQueue;

import static main.Constants.MAX_NUMBER_OF_PLAYERS;
import static main.Constants.PACKET_MAX_SIZE;
import static main.Constants.PROTOCOL_FLAGS_CONTROL;
import static main.Constants.PROTOCOL_OFFSET_ID;
import static main.Constants.PROTOCOL_OFFSET_TYPE;
import static main.Constants.PROTOCOL_TYPE_CCON;
import static main.Constants.PROTOCOL_TYPE_IDDIST;

public class ServerWLAN extends Server
{
    private static final String TAG = "ServerBluetoothCLASSIC";
    private Vector<Connection> mConnections;
    private ServerSocket mServerSocketTCP;
    private DatagramSocket mServerSocketUDP;
    private static byte[] buffer;

    public ServerWLAN() {
        super();
        // Connect the Pipe to App
        mServerListening = false;
        // Init Operations
        mConnections = new Vector<>();
        new Thread(serverListener).start();
    }

    /* https://stackoverflow.com/questions/35953413/bluetooth-connect-without-pairing */
    private Runnable serverListener = new Runnable() {
        public void run() {



                try
                {
                    mServerSocketTCP = new ServerSocket(mPort);
                    mServerSocketUDP = new DatagramSocket(mPort);

                } catch(IOException e)
                {
                    e.printStackTrace();
                }

                Logger.log(Logger.DEBUG, TAG, "Listening...");

                mServerListening = true;
                Socket socket;
                new Thread(tcpThread, "##Server Writing Thread").start();

                // Listen for incoming mConnections
                while(mServerListening)
                {
                    byte newid = 0;
                    try
                    {
                        socket = mServerSocketTCP.accept();
                        if(mConnections.size() < (MAX_NUMBER_OF_PLAYERS - 1))
                        {
                            newid = generateClientID();
                            mConnections.add(new Connection(socket, newid));
                            Logger.log(Logger.DEBUG, TAG, "Socket accepted..." + (newid));
                        }
                    } catch(IOException e)
                    {
                        if(newid != 0)
                        {
                            removeClientID(newid);
                        }
                    }
                }
        }
    };

    private Runnable tcpThread = new Runnable() {

        @Override
        public void run() {
            try {


                Logger.log(Logger.DEBUG,TAG,"All Connections approved");

                /* BluetoothServer exchange*/
                long start;
                long ttime;



                SimpleQueue cqout = mPacketQueue.getControlQueueOut();


                while (true) {

                    start = SystemClock.elapsedRealtimeNanos();

                    /* Process incoming network data*/
                    for(int i = mConnections.size(); i >= 0; i--)
                    {
                        Connection connection = mConnections.get(i);
                        if(mConnections.get(i) != null)
                        {
                            try
                            {
                                connection.receiveData();
                            } catch(Exception e)
                            {
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
                        for(int i = 0; i < mConnections.size(); i++)
                        {
                            Connection connection = mConnections.get(i);
                            connection.sendData(data);
                        }
                    }

                    ttime = (SystemClock.elapsedRealtimeNanos() - start);
                    if(ttime>Globals.mDebugNetworkTimeMin){
                        Globals.mDebugNetworkTimeMin = ttime;
                    }

                    Thread.sleep(1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    public void closeConnection()
    {
        try
        {
            /* Close mConnections */
            for(Connection connection : mConnections)
            {
                connection.close();
            }

            /* Close server socket*/
            mServerSocketTCP.close();
            mServerSocketUDP.close();
            mServerAlive = false;
        } catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public class Connection
    {
        private final String TAG = "Connection";
        private Socket socket;
        private InputStream is;
        private OutputStream os;
        private byte id;
        private byte[] headerbuf;
        public boolean idSent;
        public boolean idEchoReceived;


        public Connection(Socket sock, byte conid) throws IOException
        {
            socket = sock;
            id = conid;
            is = socket.getInputStream();
            os = socket.getOutputStream();
            idEchoReceived = false;
            idSent = false;
            headerbuf = new byte[4];

            // Send Id to Client
            byte[] buffer = new byte[PACKET_MAX_SIZE];
            buffer[PROTOCOL_OFFSET_TYPE] = PROTOCOL_TYPE_IDDIST;
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
            SimpleQueue cqin = mPacketQueue.getControlQueueIn();
            SimpleQueue uqin = mPacketQueue.getUpdatesQueueIn();

            if(is.available() >= PACKET_MAX_SIZE && is.read(headerbuf, 0, 4) != -1)
            {
                if(headerbuf[PROTOCOL_OFFSET_ID] == id)
                {
                    if((headerbuf[PROTOCOL_FLAGS_CONTROL] & PROTOCOL_FLAGS_CONTROL) > 0)
                    {
                        buffer = cqin.next();
                        buffer[0] = headerbuf[0];
                        buffer[1] = headerbuf[1];
                        buffer[2] = headerbuf[2];
                        buffer[3] = headerbuf[3];
                        if(is.read(buffer, 4, PACKET_MAX_SIZE - 4) != -1)
                        {
                            cqin.enqueue();
                            Globals.mDebugNumberOfReceivedPackets++;
                        }
                    } else
                    {
                        buffer = uqin.next();
                        buffer[0] = headerbuf[0];
                        buffer[1] = headerbuf[1];
                        buffer[2] = headerbuf[2];
                        buffer[3] = headerbuf[3];
                        if(is.read(buffer, 4, PACKET_MAX_SIZE - 4) != -1)
                        {
                            uqin.enqueue();
                            Globals.mDebugNumberOfReceivedPackets++;
                        }
                    }
                }
            }
        }

        public void sendData(byte[] state) throws IOException
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

                is.close();
                os.close();
            } catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}

