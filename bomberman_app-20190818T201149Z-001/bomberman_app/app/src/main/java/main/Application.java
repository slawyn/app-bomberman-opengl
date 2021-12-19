package main;

import android.os.Build;

import main.communication.PacketQueue;
import main.game.GameLogic;
import main.game.NetcodeLogic;
import main.game.SceneManager;
import main.rendering.GameRenderer;
import main.rendering.animation.DisplayManager;
import main.sounds.SoundManager;

import static main.Constants.*;
import static main.Globals.mSyncObject;


public class Application implements Runnable
{
    public final String TAG = "Application";
    private Connector mConnector;
    private long mAppPhaseTime;
    private long mAppWaitTime;
    private int mAppPhase;
    private NetcodeLogic mNetcodeLogicManager;
    private GameLogic mGameLogicManager;
    private SceneManager mSceneManager;
    private SoundManager mSoundManager;
    private GameRenderer mGameRenderer;
    private DisplayManager mDisplayManager;

    Application(Connector connector,GameRenderer gr)
    {
        mConnector = connector;
        mDisplayManager = gr.getmDisplayManager();
        mGameRenderer = gr;


        /* Sound Thread*/
        mSoundManager = new SoundManager(connector);
        Thread sm = new Thread(mSoundManager);
        sm.setPriority(5);
        sm.start();


        /* Game Thread */
        mAppPhase = ST_APP_START;
        Thread app = new Thread(this, TAG);
        app.setPriority(6);
        app.start();
    }

    private boolean updateGamePhaseTime(int dt)
    {
        return  (mAppPhaseTime += dt) >= mAppWaitTime;
    }

    private void phaseDelay(int wait)
    {
        mAppWaitTime = mAppPhaseTime + wait;
    }

    /////////////////////
    // All Game States //
    /////////////////////
    private void updateLogic(int dt, int[] input)
    {
        // 0: x
        // 1: y
        // 2: touch
        // 3: x2
        // 4: y2
        // 5: touch
        // 6: player movement player action
        if(updateGamePhaseTime(dt))
        {
            switch(mAppPhase)
            {
                //////////////////////////////////////////////////
                ////////////////////   LOADING  //////////////////
                //////////////////////////////////////////////////
                case ST_APP_START:
                    mGameLogicManager = new GameLogic(50);// Game container
                    mSceneManager = new SceneManager(mDisplayManager);
                    mAppPhase = ST_APP_PRELOAD;
                    break;
                case ST_APP_PRELOAD:
                    if(mGameRenderer.getLoadedResourcesCount() > 4)
                    {
                        mSceneManager.createFirstScene();
                        mAppPhase = ST_APP_LOAD;
                    }
                    break;

                case ST_APP_LOAD:
                    /**/
                    if(mGameRenderer.getLoadedResourcesCount() == mDisplayManager.getResourceCount())
                    {
                        mSceneManager.createSelectionScene();
                        mAppPhase = ST_APP_SELECTION;
                    } else
                    {
                        mSceneManager.showLoadingProgress();
                    }
                    break;

                // Selection screen
                case ST_APP_SELECTION:

                    if(Build.MODEL.equals("SM-J415FN")) //
                    {
                        mAppPhase = ST_APP_SERVER_DISCOVERABLE; //ST_APP_CLIENT_DISCOVER_BLUETOOTH;
                    } else if(Build.MODEL.equals("Ulefone_S1")) //SM-J415FN
                    {
                        mAppPhase = ST_APP_CLIENT_DISCOVER_BLUETOOTH;
                    } else
                    {

                        int selectedState = mSceneManager.parseSelection(input);
                        if(selectedState != -1)
                        {
                            mAppPhase = selectedState;
                            phaseDelay(100);
                        }
                    }
                    break;

                //////////////////////////////////////////////////
                ///////////////////   OFFLINE  ///////////////////
                //////////////////////////////////////////////////
                // No Bluetooth routines
                case ST_APP_OFFLINE_START:
                    mSceneManager.createGameScene();
                    mGameLogicManager.createGameOffline();
                    mAppPhase = ST_APP_OFFLINE_GAME_RUNNING;
                    break;

                // Running offline game with local input
                case ST_APP_OFFLINE_GAME_RUNNING:
                    mGameLogicManager.updateGameTicker();
                    mGameLogicManager.updateGameOfflineInput(input[6]);
                    mGameLogicManager.updateGameState(dt);
                    break;


                //////////////////////////////////////////////////
                ///////////////////  BLUETOOTH SELECTION /////////
                //////////////////////////////////////////////////
                case ST_APP_SELECTION_BLUETOOTH:
                    if(mConnector.isBluetoothEnabled() && mConnector.isFineLocationEnabled())
                    {
                        mAppPhase = ST_APP_SELECTION;
                        mSceneManager.createSelectionBluetoothScene();
                    } else
                    {
                        phaseDelay(500);           // loop until bluetooth has been enabled
                    }
                    break;

                //////////////////////////////////////////////////
                ///////////////////  WLAN SELECTION /////////
                //////////////////////////////////////////////////
                case ST_APP_SELECTION_WLAN:
                    if(true)
                    {
                        mAppPhase = ST_APP_SELECTION;
                        mSceneManager.createSelectionWlanScene();
                    } else
                    {
                        phaseDelay(500);           // loop until bluetooth has been enabled
                    }
                    break;
                //////////////////////////////////////////////////
                ///////////////////  DISCOVER  //////////////
                //////////////////////////////////////////////////
                case ST_APP_CLIENT_DISCOVER_WLAN:
                    mConnector.setConnectionType(CONNECTION_TYPE_WLAN);            // Ips are expected
                    mSceneManager.createLoadingScene();
                    mConnector.startDiscovery();
                    mAppPhase = ST_APP_CLIENT_SELECT_SERVER;
                    break;

                // Look for devices
                case ST_APP_CLIENT_DISCOVER_BLUETOOTH:
                    mConnector.setConnectionType(CONNECTION_TYPE_CLASSIC);
                    mSceneManager.createLoadingScene();
                    mConnector.startDiscovery();
                    mAppPhase = ST_APP_CLIENT_SELECT_SERVER;
                    break;


                //////////////////////////////////////////////////
                ////////////////////   CLIENT  ///////////////////
                //////////////////////////////////////////////////
                // Select server from list
                case ST_APP_CLIENT_SELECT_SERVER:
                    if(mConnector.hasDevices())
                    {
                        String t = mConnector.discoveredDevice();
                        if(t.contains(SERVER_NAME))
                        {
                            PacketQueue packetqueue = mConnector.attachGameToClient(t);
                            if(packetqueue != null)
                            {
                                mNetcodeLogicManager = new NetcodeLogic(mGameLogicManager, packetqueue);
                                mConnector.stopDiscovery();
                                mAppPhase = ST_APP_CLIENT_SYNC;
                            }
                        }
                    }
                    break;

                // Connected to select server
                case ST_APP_CLIENT_SYNC:
                    if(mNetcodeLogicManager.getDistributedId())
                    {
                        mSceneManager.createGameScene();
                        mAppPhase = ST_APP_CLIENT_GAME_RUNNING;
                    }
                    break;

                // Game from client perspective
                case ST_APP_CLIENT_GAME_RUNNING:
                    mNetcodeLogicManager.getUpdatesFromServer();
                    mNetcodeLogicManager.updateClientState(dt, input[6]);

                    break;

                //////////////////////////////////////////////////
                ////////////////////  SERVER  ////////////////////
                //////////////////////////////////////////////////
                // Broadcast server info
                case ST_APP_SERVER_DISCOVERABLE:
                    mConnector.broadcastServerInformation();
                    mSceneManager.createLoadingScene();
                    mAppPhase = ST_APP_SERVER_WAIT_FOR_DISCOVERABLE;
                    break;

                // Wait for device to become discoverable
                case ST_APP_SERVER_WAIT_FOR_DISCOVERABLE:
                    PacketQueue packetqueue = mConnector.attachGameToServer();
                    if(packetqueue != null)
                    {
                        mNetcodeLogicManager = new NetcodeLogic(mGameLogicManager, packetqueue);
                        mAppPhase = ST_APP_SERVER_SYNC;
                    }
                    break;
                // Create Server game
                case ST_APP_SERVER_SYNC:
                    mSceneManager.createGameScene();
                    mNetcodeLogicManager.serverInit();
                    mNetcodeLogicManager.resetMasterState();
                    mAppPhase = ST_APP_SERVER_GAME_RUNNING;
                    break;

                // Server game
                case ST_APP_SERVER_GAME_RUNNING:
                    mNetcodeLogicManager.controlClients();
                    mNetcodeLogicManager.updateGameMasterState(dt, (byte) input[6]);
                    break;
            }
        }

        mSceneManager.updateSceneObjects(dt, input);
    }

    @Override
    public void run()
    {
        long frameStartTime;
        long lastframeStartTime = System.nanoTime();
        long deltaMax = 0;
        long oneSecond = 0;
        long desiredFrameDelta = SERVER_TICK_TIME * 1000000;      // time in nanoseconds
        long loopTime;
        long accumulator = 0;
        boolean mGameRunning = true;

        // Game Loop
        while(mGameRunning)
        {
            frameStartTime = System.nanoTime();                                             // <------ here starts the frame
            accumulator += frameStartTime - (lastframeStartTime);
            lastframeStartTime = frameStartTime;

            if(accumulator >= desiredFrameDelta)
            {
                // Update Game
                updateLogic(SERVER_TICK_TIME, mConnector.getInput());

                loopTime = System.nanoTime() - frameStartTime;

                if(loopTime > Globals.mDebugLoopMax)
                    Globals.mDebugLoopMax = loopTime;
                else if(loopTime < Globals.mDebugLoopMin)
                    Globals.mDebugLoopMin = loopTime;

                synchronized(mSyncObject){
                    mSyncObject.notify();
                }

                // Update Renderobjects
                mDisplayManager.updateRenderObjects(mGameLogicManager, mSceneManager);

                loopTime = System.nanoTime() - frameStartTime;
                if(loopTime > Globals.mDebugLoopTotal)
                {
                    Globals.mDebugLoopTotal = loopTime;
                }

                accumulator -= desiredFrameDelta;
                if(accumulator > deltaMax)
                {
                    deltaMax = accumulator;
                }

                if((oneSecond += SERVER_TICK_TIME) >= 1000)
                {
                    Logger.logTimes(deltaMax);
                    oneSecond %= 1000;
                    deltaMax = 0;
                }

                Thread.yield();

            } else
            {

            }
        }
        Logger.log(Logger.DEBUG, TAG, Messages.dTextApplicationFinished);
    }
}

