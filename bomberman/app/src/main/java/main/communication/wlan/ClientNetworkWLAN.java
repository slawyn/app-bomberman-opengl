package main.communication.wlan;

import android.os.SystemClock;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;

import main.Globals;
import main.Logger;
import main.communication.Client;
import main.communication.SimpleQueue;

import static main.Constants.*;

// TODO, improve throuput and reduce latency
// https://github.com/greatscottgadgets/ubertooth/wiki/One-minute-to-understand-BLE-MTU-data-package
// https://punchthrough.com/maximizing-ble-throughput-part-2-use-larger-att-mtu-2/
public class ClientNetworkWLAN extends Client
{
    private Socket mServerSocketTCP;
    private DatagramSocket mServerSocketUDP;
    private byte udpbuffer[];


    public ClientNetworkWLAN(String address)
    {
        mAddress = address;
        new Thread(createClient).start();
    }

    public Runnable createClient = new Runnable(){
        public void run(){
        try
        { ;
            mServerSocketTCP = new Socket(mAddress, mPort);        // TCP
            mServerSocketUDP = new DatagramSocket(mPort);         // UDP
            mInet4Address = (Inet4Address) InetAddress.getByName(mAddress);

        } catch(IOException e)
        {
            e.printStackTrace();
            return;
        }

        Logger.log(Logger.DEBUG, TAG, "Connected...");
        try
        {
            // Bluetooth Streams
            mOutputStream = mServerSocketTCP.getOutputStream();
            mInputStream = new BufferedInputStream(mServerSocketTCP.getInputStream());

        } catch(IOException e)
        {
            e.printStackTrace();
        }

        /* Start threads*/
        new Thread(tcpThreadSendReceive, "##TCPClientReadingThread").start();
        Logger.log(Logger.DEBUG, TAG, "Started threads...");
    }

    };



    public void closeConnection()
    {
        try
        {

            if(mInputStream != null)
            {
                mInputStream.close();
            }
            if(mOutputStream != null)
            {
                mOutputStream.close();
            }

        } catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    private Runnable udpSendThread= new Runnable()
    {

        @Override
        public void run()
        {
            while(true)
            {
                DatagramPacket dp;

                SimpleQueue cqout = mPacketQueue.getControlQueueOut();
                while(cqout.notEmpty())
                {
                    // Use UDP for game input and state
                    byte c[] = cqout.dequeue();
                    dp = new DatagramPacket(c, c.length, mInet4Address, mPort);
                    try
                    {
                        mServerSocketUDP.send(dp);
                    } catch(IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }

        }
    };




    private Runnable udpReceiveThread = new Runnable()
    {

        @Override
        public void run()
        {
            try
            {
                DatagramPacket dp;

                SimpleQueue uqin = mPacketQueue.getUpdatesQueueIn();
                while(true)
                {
                    udpbuffer = uqin.next();
                    dp = new DatagramPacket(udpbuffer, udpbuffer.length, mInet4Address, mPort);
                    mServerSocketUDP.receive(dp);
                    uqin.enqueue();
                    Globals.mDebugNumberOfReceivedPackets++;
                }
            } catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    };

    private Runnable tcpThreadSendReceive = new Runnable()
    {

        @Override
        public void run()
        {
            try
            {



                Logger.log(Logger.DEBUG, TAG, "This connection has been approved");

                SimpleQueue cqin = mPacketQueue.getUpdatesQueueIn();
                SimpleQueue cqout = mPacketQueue.getControlQueueOut();

                new Thread(udpReceiveThread, "##UDPClientReadingThread").start();
                new Thread(udpReceiveThread, "##UDPClientSendinghread").start();

                long ttime=0;
                long start = 0;
                while(true)
                {

                    start = SystemClock.elapsedRealtimeNanos();

                    // Incoming TCP
                    if(mInputStream.available() >= PACKET_MAX_SIZE && mInputStream.read(cqin.next(), 0, PACKET_MAX_SIZE) != -1)
                    {
                        cqin.enqueue();
                        Globals.mDebugNumberOfReceivedPackets++;
                    }


                    while(cqout.notEmpty())
                    {
                        // Outgoing TCP
                        mOutputStream.write(cqout.dequeue());
                        mOutputStream.flush();
                        Globals.mDebugNumberOfTransmittedPackets++;
                    }


                    ttime =  (SystemClock.elapsedRealtimeNanos() - start);
                    if(ttime>Globals.mDebugNetworkTimeMin)
                    {
                        Globals.mDebugNetworkTimeMin = ttime;
                    }
                    Thread.sleep(1);
                }
            } catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    };
}



