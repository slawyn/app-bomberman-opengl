package main.game;


import android.util.SparseArray;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import main.Globals;
import main.Logger;
import main.communication.PacketQueue;
import main.communication.SimpleQueue;


import static main.Constants.MAX_NUMBER_OF_PLAYERS;
import static main.Constants.PROTOCOL_OFFSET_ID;
import static main.Constants.PROTOCOL_OFFSET_PAYLOAD;
import static main.Constants.PROTOCOL_OFFSET_TYPE;
import static main.Constants.PROTOCOL_TYPE_CCON;
import static main.Constants.PROTOCOL_TYPE_GSTATE;
import static main.Constants.PROTOCOL_TYPE_IDDIST;
import static main.Constants.PROTOCOL_TYPE_INPUT;
import static main.Constants.PROTOCOL_TYPE_RGAME;


public class NetcodeLogic
{
    private final String TAG = "NetcodeLogic";
    private PacketQueue mPacketqueue;
    private GameLogic mGameLogicManager;

    private byte mIdentification;
    private int mQunatifiedByteOffset;
    private int mFullUpdate;

    private InputBuffer mInputBuffer;
    private ConcurrentLinkedQueue<byte[]> mMasterStates;
    private ConcurrentLinkedQueue<byte[]> mDelayedMasterStates;

    private static long mClientTime;
    private static long mServerTime;

    private float[] mErrorPositionX;
    private float[] mErrorPositionY;

    private byte mSavedLastInput;
    private SparseArray<Integer> mNetworkIdTranslator;


    public NetcodeLogic(GameLogic gamelogicmanager, PacketQueue packetqueue)
    {
        mGameLogicManager = gamelogicmanager;
        mPacketqueue = packetqueue;
        mMasterStates = new ConcurrentLinkedQueue<>();
        mDelayedMasterStates = new ConcurrentLinkedQueue<>();
        mSavedLastInput = 0;
        mIdentification = 0;
        mNetworkIdTranslator = new SparseArray<>();
        mInputBuffer = new InputBuffer();

        mErrorPositionX = new float[MAX_NUMBER_OF_PLAYERS];
        mErrorPositionY = new float[MAX_NUMBER_OF_PLAYERS];
    }

    public void serverInit()
    {
        mNetworkIdTranslator.put(0, 0);
    }

    public void updateClientState(int dt,int input)
    {

        long delta = dt;
        long timeOffset;
        boolean changed;
        boolean extrapolate = true;
        int gameticker;
        mGameLogicManager.updateGameTicker();

        // input
        changed = (mSavedLastInput != input);
        if(changed)
        {
            gameticker = mGameLogicManager.getGameTicker();

            mInputBuffer.bufferInput(mIdentification, new Input((byte) gameticker, (byte) input));
            mSavedLastInput = (byte) input;


            SimpleQueue q = mPacketqueue.getControlQueueOut();
            byte[] sendInput = q.next();
            sendInput[PROTOCOL_OFFSET_ID] = mIdentification;
            sendInput[PROTOCOL_OFFSET_TYPE] = PROTOCOL_TYPE_INPUT;
            sendInput[PROTOCOL_OFFSET_PAYLOAD] = (byte) gameticker;     // when to execute
            sendInput[PROTOCOL_OFFSET_PAYLOAD + 1] = (byte) input;
            q.enqueue();
            Logger.log(Logger.INFO, TAG, String.format("CL(%d) %-8d", gameticker & 0x00FF, mClientTime));
        }


        timeOffset = mClientTime + dt - mServerTime;


        gameticker = mGameLogicManager.getGameTicker();
        while(mMasterStates.size() > 0)
        {

            byte[] masterstate = mMasterStates.remove();       //TODO change
            int servergameticker = 0x000000FF&masterstate[PROTOCOL_OFFSET_PAYLOAD];
            int d1,d2;


            d1 = (gameticker-servergameticker)&0xFF;
            d2 = (servergameticker-gameticker)&0xFF;


            if(d1<d2){

                Globals.mDebugMeanLatency+=d1;
                if(d1>  Globals.mDebugMaxLatency){
                    Globals.mDebugMaxLatency = d1;
                }

                if(d1<Globals.mDebugMinLatency){
                    Globals.mDebugMinLatency = d1;
                }
            }
            else{
                Globals.mDebugMeanLatency+=d2;
                if(d2>Globals.mDebugMaxLatency){
                    Globals.mDebugMaxLatency = d2;
                }

                if(d2<Globals.mDebugMinLatency){
                    Globals.mDebugMinLatency = d2;
                }
            }

            ++Globals.mDebugCountLatency;

            /*
            if(d1<d2){
                Logger.log(Logger.INFO,TAG,"c:"+gameticker+"s:"+servergameticker+" "+(d1));
            }
            else{
                Logger.log(Logger.DEBUG,TAG,"c:"+gameticker+"s:"+servergameticker+" "+(d2));
            }*/

            /*

            int size = masterstate[PROTOCOL_OFFSET_PAYLOAD + 4];
            while(mGameLogicManager.getPlayersCount() < size)
            {
                mGameLogicManager.createPlayerAtDefaultPosition(mGameLogicManager.getPlayersCount());
            }

            int offset = PROTOCOL_OFFSET_PAYLOAD + 5;
            for(int idx = 0; idx < size; idx++)
            {
                clientstate[idx * 5] =    twoBytesToInt(offset+1, masterstate);
                clientstate[idx * 5 + 1] =  twoBytesToInt(offset+3, masterstate);


                if(clientstate[idx * 5 + 1]>65500){
                    clientstate[idx * 5 + 1] = 0;
                }
                //Logger.log(Logger.INFO, TAG,"posx:"+clientstate[idx * 5]+"posy:"+clientstate[idx * 5+1]);
                offset += 5;
            }

            // First cycle
            int dt0 = masterstate[PROTOCOL_OFFSET_PAYLOAD + 1];
            for(int idx = 0; idx < size; idx++)
            {
                clientstate[idx * 5+3] =  masterstate[offset];
                offset += 1;
            }

            mGameLogicManager.updateGameStateFast(dt0);

            // Second Cycle
            int dt1 = masterstate[PROTOCOL_OFFSET_PAYLOAD + 2];
            for(int idx = 0; idx < size; idx++)
            {
                clientstate[idx * 5+3] =    masterstate[offset];
                offset += size;
            }
            mGameLogicManager.updateGameStateFast(dt0);


            // Third Cycle
            int dt2 = masterstate[PROTOCOL_OFFSET_PAYLOAD + 3];
            for(int idx = 0; idx < size; idx++)
            {
                clientstate[idx * 5+3] =    masterstate[offset];

                offset += size;
            }

            mGameLogicManager.updateGameStateFast(dt0);
            */
        }



        /*
        // Get current erroneus position
        // -----------------------------------------
        SparseArray<Player> players = mGameLogicManager.getPlayers();
        for(int idx = 0; idx < players.size(); idx++)
        {
            Player player = players.get(idx);
            mErrorPositionX[idx] = (player.mPositionX); //+ player.mPositionErrorX);
            mErrorPositionY[idx] = (player.mPositionY); //+ player.mPositionErrorY);
        }


        boolean updated = false;
        Player player;
        // Parse master states
        // -----------------------------------------
        while(mMasterStates.size() > 0)
        {

            byte[] state = mMasterStates.remove();       //TODO change


            int masterDelta = state[PROTOCOL_OFFSET_PAYLOAD + 10];
            timeOffset = mClientTime - mServerTime;
            player = players.get(0);

            // first tick
            if(timeOffset >= masterDelta)
            {

                // we update the update the past
                player.mPositionX = (short) ((state[PROTOCOL_OFFSET_PAYLOAD + 2] & 0x000000FF) | ((state[PROTOCOL_OFFSET_PAYLOAD + 3] & 0x000000FF) << 8));
                player.mPositionY = (short) ((state[PROTOCOL_OFFSET_PAYLOAD + 4] & 0x000000FF) | ((state[PROTOCOL_OFFSET_PAYLOAD + 5] & 0x000000FF) << 8));

                mServerTime += masterDelta;

                mGameLogicManager.updatePlayerInput((byte) 0, state[PROTOCOL_OFFSET_PAYLOAD + 11], state[PROTOCOL_OFFSET_PAYLOAD + 12]);

                // local input came back 15 16 if we have the index
                // TODO remove local input
                if(mInputBuffer.getBufferedInput(state[PROTOCOL_OFFSET_PAYLOAD + 13]) != null)
                {
                    //
                }


                player = players.get(1);


                player.mPositionX = (short) ((state[PROTOCOL_OFFSET_PAYLOAD + 6] & 0x000000FF) | ((state[PROTOCOL_OFFSET_PAYLOAD + 7] & 0x000000FF) << 8));
                ;
                player.mPositionY = (short) ((state[PROTOCOL_OFFSET_PAYLOAD + 8] & 0x000000FF) | ((state[PROTOCOL_OFFSET_PAYLOAD + 9] & 0x000000FF) << 8));
                ;

                mGameLogicManager.updatePlayerInput((byte) 1, state[PROTOCOL_OFFSET_PAYLOAD + 13], state[PROTOCOL_OFFSET_PAYLOAD + 14]);


                mGameLogicManager.updateGameStateFast(masterDelta);
                //Logger.log(Logger.ERROR, TAG, String.format("#FIRST UPDATE(%d) s:%-7d c:%-7d diff:%-3d ex:%-8f ey:%-8f", state[2]& 0x00FF, Globals.mServerTime, Globals.mClientTime, timeOffset, player.mPositionErrorX, player.mPositionErrorY));

            } else
            {
                //PacketQueue.receiveControlBuffer();
                //break;  // nothing to process we are too far ahead
            }


            timeOffset = mClientTime - mServerTime;
            masterDelta = state[PROTOCOL_OFFSET_PAYLOAD + 15];

            //second tick
            if(timeOffset >= masterDelta)
            {

                // we update the update the past
                mServerTime += masterDelta;
                mGameLogicManager.updatePlayerInput((byte) 0, state[PROTOCOL_OFFSET_PAYLOAD + 16], state[PROTOCOL_OFFSET_PAYLOAD + 17]);


                // TODO remove local input
                if(mInputBuffer.getBufferedInput(state[PROTOCOL_OFFSET_PAYLOAD + 16]) != null)
                {
                    //
                }

                mGameLogicManager.updatePlayerInput((byte) 1, state[PROTOCOL_OFFSET_PAYLOAD + 18], state[PROTOCOL_OFFSET_PAYLOAD + 19]);
                mGameLogicManager.updateGameStateFast(masterDelta);

                // Logger.log(Logger.ERROR, TAG, String.format("#SECOND UPDATE(%d) s:%-7d c:%-7d diff:%-3d ex:%-8f ey:%-8f", state[2]& 0x00FF, Globals.mServerTime, Globals.mClientTime, timeOffset, player.mPositionErrorX, player.mPositionErrorY));
            } else
            {
                byte[] n = new byte[7];
                n[0] = state[PROTOCOL_OFFSET_PAYLOAD + 15];
                n[1] = 0;       // Id's
                n[2] = 1;
                n[3] = state[PROTOCOL_OFFSET_PAYLOAD + 16];
                n[4] = state[PROTOCOL_OFFSET_PAYLOAD + 17];
                n[5] = state[PROTOCOL_OFFSET_PAYLOAD + 18];
                n[6] = state[PROTOCOL_OFFSET_PAYLOAD + 19];

                mDelayedMasterStates.add(n);    // we will process it next time
                extrapolate = false;
            }

            updated = true;

            //Player player2= mPlayers.get(1);
            //Logger.log(Logger.ERROR, TAG, String.format("#UPDATE(%d) s:%-7d c:%-7d diff:%-3d ex:%-8d ey:%-8d", state[2]& 0x00FF, Globals.mServerTime, Globals.mClientTime, timeOffset, player2.mPositionX, player2.mPositionY));

        }
        timeOffset = mClientTime - mServerTime;

        // Fast forward
        //----------------------------------
        while(timeOffset > 0 && updated)
        {
            long goforward = SERVER_TICK_TIME;
            if(timeOffset < SERVER_TICK_TIME)
                goforward = timeOffset;
            mGameLogicManager.updateGameStateFast(goforward);
            timeOffset -= goforward;
        }

        // Calculate Error
        //-------------------------------------------
        for(int idx = 0; idx < players.size(); idx++)
        {
            player = players.get(0);
          //  player.mPositionErrorX = mErrorPositionX[idx] - player.mPositionX;
          //  player.mPositionErrorY = mErrorPositionY[idx] - player.mPositionY;
        }

        // Extrapolate
        //-----------------------------------
        if(extrapolate)
        {
            timeOffset = mClientTime - mServerTime;
            mGameLogicManager.updateGameState(delta);
            //Logger.log(Logger.DEBUG, TAG, String.format("#>>>>>> s:%-7d c:%-7d diff:%-3d ex:%-8f ey:%-8f", Globals.mServerTime, Globals.mClientTime, timeOffset, player.mPositionErrorX, player.mPositionErrorY));
        }
*/

    }


    public void updateGameMasterState(int dt, byte input)
    {
        mGameLogicManager.updateGameTicker();
        mGameLogicManager.updatePlayerInput(0, input);

        // TODO: add multiple buffers

        /*
        SparseArray<Player> players = mGameLogicManager.getPlayers();

        Input networkinput;
        for(int idx = 0; idx < players.size(); idx++)
        {
            byte id = (byte) mGameLogicManager.getIndex(idx);
            if(id == 0)
                continue;

            networkinput = mInputBuffer.getBufferedInput(id).get(0);
            if(networkinput != null)
            {
                mGameLogicManager.updatePlayerInput(id, networkinput.mTick, networkinput.mPlayerInput);
            }
        }*/

        mGameLogicManager.updateGameState(dt);
        quantifyMasterState(dt);
    }

    public void resetMasterState()
    {
        mGameLogicManager.deleteAllGameObjects();
        mGameLogicManager.createGameLevel(Globals.selectedMap);
        for(int idx = 0; idx < mNetworkIdTranslator.size(); idx++)
        {
            // set 0 1 2 3  to the playerid
            mNetworkIdTranslator.setValueAt(idx, mGameLogicManager.createPlayerAtDefaultPosition(idx));
        }

        /*
        // TODO need to check
        boolean reset = false;
        if(reset)
        {
            byte[] sendInput = mPacketqueue.getFreeControlBufferOUT();
            sendInput[PROTOCOL_OFFSET_ID] = mIdentification;
            sendInput[PROTOCOL_OFFSET_TYPE] = PROTOCOL_TYPE_RGAME;
            sendInput[PROTOCOL_OFFSET_PAYLOAD] = (byte) mGameLogicManager.getGameTicker();
            mPacketqueue.enqueueControlBufferOUT();
        }*/
    }


    public void quantifyMasterState(long dt)
    {

        int gameticker = mGameLogicManager.getGameTicker();
        SimpleQueue q = mPacketqueue.getUpdatesQueueOut();

        switch(gameticker % 3)
        {
            case 0:

                byte[] masterstate = q.next();
                masterstate[PROTOCOL_OFFSET_ID] = mIdentification;
                masterstate[PROTOCOL_OFFSET_TYPE] = PROTOCOL_TYPE_GSTATE;
                masterstate[PROTOCOL_OFFSET_PAYLOAD] = (byte) gameticker;

                // initial positions

                /*
                offset = PROTOCOL_OFFSET_PAYLOAD + 5;
                for(int idx = 0; idx < size; idx++)
                {

                    masterstate[offset] = 0;//(byte) player.playerID;
                    intToTwoBytes(offset+1,masterstate, playerstate[idx * 5]);
                    intToTwoBytes(offset+3,masterstate, playerstate[idx * 5 + 1]);
                   // Logger.log(Logger.INFO, TAG,"posy:"+playerstate[idx * 5 + 1]);
                    offset += 5;
                }

                // inputs
                for(int idx = 0; idx < size; idx++)
                {
                    masterstate[offset] = (byte) playerstate[idx * 5 + 3];
                    offset += 1;
                }


                mQunatifiedByteOffset = offset;*/
                break;
            case 1:

                /*
                masterstate[PROTOCOL_OFFSET_PAYLOAD + 2] = (byte) dt;

                offset = mQunatifiedByteOffset;
                for(int idx = 0; idx < masterstate[PROTOCOL_OFFSET_PAYLOAD + 4]; idx++)
                {
                    masterstate[offset] = (byte) playerstate[idx * 5 + 3];
                    offset += 1;
                }

                mQunatifiedByteOffset = offset;
                break;*/

            case 2:

                /*
                masterstate[PROTOCOL_OFFSET_PAYLOAD + 3] = (byte) dt;
                offset = mQunatifiedByteOffset;
                for(int idx = 0; idx < masterstate[PROTOCOL_OFFSET_PAYLOAD + 4]; idx++)
                {
                    masterstate[offset] = (byte) playerstate[idx * 5 + 3];
                    offset += 1;
                }

                mQunatifiedByteOffset = offset;*/
                q.enqueue();
                //Logger.log(Logger.INFO, TAG, String.format("SERVER(%d) %-8d",  masterstate[2] & 0x00FF, Globals.mServerTime));
                break;
        }
        mServerTime += dt;

    }

    public boolean getDistributedId()
    {
        boolean joinin = false;

        SimpleQueue qin = mPacketqueue.getControlQueueIn();
        if(qin.notEmpty())
        {
            byte[] buf = qin.dequeue();
            switch(buf[PROTOCOL_OFFSET_TYPE])
            {
                case PROTOCOL_TYPE_IDDIST:
                    mIdentification = buf[PROTOCOL_OFFSET_ID];

                    SimpleQueue qout = mPacketqueue.getControlQueueOut();
                    byte[] buffer = qout.next();
                    buffer[PROTOCOL_OFFSET_ID] = mIdentification;
                    buffer[PROTOCOL_OFFSET_TYPE] = PROTOCOL_TYPE_IDDIST;
                    qout.enqueue();
                    joinin = true;
                    break;
                case PROTOCOL_TYPE_CCON:
                    mGameLogicManager.deleteAllGameObjects();
                    break;
            }
        }
        return joinin;
    }


    public void controlClients()
    {
        SimpleQueue cqin = mPacketqueue.getControlQueueIn();
        SimpleQueue uqin = mPacketqueue.getUpdatesQueueIn();
        while(cqin.notEmpty())
        {
            byte[] buffer = cqin.dequeue();
            switch(buffer[PROTOCOL_OFFSET_TYPE])
            {
                // Create next player using index
                case PROTOCOL_TYPE_IDDIST:
                    int playerinternalid = mGameLogicManager.createPlayerAtDefaultPosition(mNetworkIdTranslator.size());
                    mNetworkIdTranslator.put(buffer[PROTOCOL_OFFSET_ID], playerinternalid);
                    break;
                case PROTOCOL_TYPE_CCON:
                    int connectionid = buffer[PROTOCOL_OFFSET_ID];
                    int index = mNetworkIdTranslator.indexOfKey(connectionid);
                    if(index >= 0)
                        mNetworkIdTranslator.removeAt(index);   // Remove Id
                    break;
            }
        }


        while(uqin.notEmpty())
        {
            byte[] buffer = uqin.dequeue();
            switch(buffer[PROTOCOL_OFFSET_TYPE])
            {
                case PROTOCOL_TYPE_INPUT:
                    mInputBuffer.bufferInput(buffer[PROTOCOL_OFFSET_ID], new Input(buffer[PROTOCOL_OFFSET_PAYLOAD], buffer[PROTOCOL_OFFSET_PAYLOAD + 1]));
                    break;
            }
        }

    }

    public void getUpdatesFromServer()
    {
        byte[] buffer;

        SimpleQueue qin = mPacketqueue.getControlQueueIn();
        while(qin.notEmpty())
        {
            buffer = qin.dequeue();
            switch(buffer[PROTOCOL_OFFSET_TYPE])
            {
                case PROTOCOL_TYPE_CCON:
                    mGameLogicManager.deleteAllGameObjects();
                    break;
                case PROTOCOL_TYPE_RGAME:
                    break;
            }
        }

        qin = mPacketqueue.getUpdatesQueueIn();
        while(qin.notEmpty())
        {
            buffer = qin.dequeue();
           // Logger.log(Logger.INFO, TAG, "Client: Packet received" + buffer[PROTOCOL_OFFSET_TYPE]);
            switch(buffer[PROTOCOL_OFFSET_TYPE])
            {
                case PROTOCOL_TYPE_GSTATE:
                    mMasterStates.add(buffer);
                    break;
            }
        }
    }

    public int twoBytesToInt(int offset, byte[] arr)
    {
        return (0x00000000FF & arr[offset]) | (arr[offset + 1]) << 8;
    }

    public void intToTwoBytes(int offset, byte[] arr, int val)
    {

        arr[offset] = (byte) val;
        arr[offset + 1] = (byte) (val >> 8);
    }

    class InputBuffer
    {
        Vector<Input> mBufferedInput0;
        Vector<Input> mBufferedInput1;
        Vector<Input> mBufferedInput2;
        Vector<Input> mBufferedInput3;

        InputBuffer()
        {
            mBufferedInput0 = new Vector<>();
            mBufferedInput1 = new Vector<>();
            mBufferedInput2 = new Vector<>();
            mBufferedInput3 = new Vector<>();
        }

        Vector<Input> getBufferedInput(int id) // TODO add more buffers
        {
            return mBufferedInput0;
        }

        void bufferInput(int id, Input input)
        {
            mBufferedInput0.add(input);
        }
    }
}
