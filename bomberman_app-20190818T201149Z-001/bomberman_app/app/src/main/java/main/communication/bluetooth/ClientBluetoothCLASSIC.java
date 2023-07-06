package main.communication.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.BufferedInputStream;
import java.io.IOException;

import main.Constants;
import main.Globals;

import java.util.UUID;

import main.Logger;
import main.communication.Client;
import main.communication.SimpleQueue;

import static main.Constants.*;
import static main.Globals.mSyncObject;

// TODO, improve throuput and reduce latency
// https://github.com/greatscottgadgets/ubertooth/wiki/One-minute-to-understand-BLE-MTU-data-package
// https://punchthrough.com/maximizing-ble-throughput-part-2-use-larger-att-mtu-2/
public class ClientBluetoothCLASSIC extends Client
{
    private final String TAG = "Bluetooth-Client";
    private BluetoothDevice mServerDevice;
    private BluetoothSocket serversocket;
    private byte[] buffer;

    public ClientBluetoothCLASSIC(BluetoothDevice serverdevice)
    {
        this.mServerDevice = serverdevice;
        Thread t = new Thread(commThread, TAG);
        t.setPriority(6);
        t.start();

    }

    public Runnable commThread = new Runnable()
    {

        public void run()
        {
            UUID uuid = UUID.fromString(SERVER_UUID);
            try
            {

                Logger.log(Logger.DEBUG, TAG, "Services: ".concat(Boolean.toString(mServerDevice.fetchUuidsWithSdp())));
                serversocket = mServerDevice.createInsecureRfcommSocketToServiceRecord(uuid);
                serversocket.connect();

                Logger.log(Logger.DEBUG, TAG, "Connected...");

                // Bluetooth Streams
                mOutputStream = serversocket.getOutputStream();

                int bufferSize = 20 * 64;
                mInputStream = new BufferedInputStream(serversocket.getInputStream(),bufferSize);
            } catch(IOException e)
            {
                e.printStackTrace();
            }

            /* Start threads*/
            Logger.log(Logger.DEBUG, TAG, "Started loop...");
            mConnectionAlive = true;

            // TODO optimize even further
            long start;
            long ttime;
            byte[] headerbuf = new byte[PACKET_HEADER_SIZE];

            SimpleQueue cqin = mPacketQueue.getControlQueueIn();
            SimpleQueue cqout = mPacketQueue.getControlQueueOut();
            SimpleQueue uqin = mPacketQueue.getUpdatesQueueIn();
            SimpleQueue uqout = mPacketQueue.getUpdatesQueueOut();
            while(mConnectionAlive)
            {

                try
                {

                    synchronized(mSyncObject){
                        mSyncObject.wait();
                    }

                    start = System.nanoTime();
                    // Incoming
                    if(mInputStream.available() >= PACKET_MAX_SIZE && mInputStream.read(headerbuf, 0, PACKET_HEADER_SIZE) != -1)
                    {
                        if((headerbuf[PROTOCOL_OFFSET_FLAGS] & PROTOCOL_FLAGS_CONTROL) > 0)
                        {
                            buffer = cqin.next();
                            for(int idx = 0; idx < PACKET_HEADER_SIZE; idx++)
                            {
                                buffer[idx] = headerbuf[idx];
                            }

                            mInputStream.read(buffer, PACKET_HEADER_SIZE, PACKET_MAX_SIZE - PACKET_HEADER_SIZE);
                            cqin.enqueue();
                        } else
                        {
                            buffer =  uqin.next();
                            for(int idx = 0; idx < PACKET_HEADER_SIZE; idx++)
                            {
                                buffer[idx] = headerbuf[idx];
                            }

                            mInputStream.read(buffer, PACKET_HEADER_SIZE, PACKET_MAX_SIZE - PACKET_HEADER_SIZE);
                            uqin.enqueue();
                        }
                        Globals.mDebugNumberOfReceivedPackets++;
                    }

                    // Outgoing
                    while(cqout.notEmpty())
                    {
                        buffer = cqout.dequeue();
                        buffer[PROTOCOL_OFFSET_FLAGS] |= PROTOCOL_FLAGS_CONTROL;
                        mOutputStream.write(buffer);
                        mOutputStream.flush();
                        Globals.mDebugNumberOfTransmittedPackets++;
                    }

                    while(uqout.notEmpty())
                    {
                        mOutputStream.write(uqout.dequeue());
                        mOutputStream.flush();
                        Globals.mDebugNumberOfTransmittedPackets++;
                    }

                    ttime = System.nanoTime() - start;
                    if(ttime > Globals.mDebugNetworkTimeMax)
                    {
                        Globals.mDebugNetworkTimeMax = ttime;
                    }

                    if(ttime < Globals.mDebugNetworkTimeMin)
                    {
                        Globals.mDebugNetworkTimeMin = ttime;
                    }


                } catch(Exception e)
                {   mConnectionAlive = false;
                    closeConnection();
                    e.printStackTrace();
                }
            }
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

            serversocket.close();
        } catch(IOException e)
        {
            e.printStackTrace();
        }
    }

}



