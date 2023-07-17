package main;

import android.os.Build;

import main.communication.PacketQueue;
import main.nativeclasses.GameManager;
import main.game.logic.NetcodeLogic;
import main.game.SceneManager;
import main.rendering.GameRenderer;
import main.rendering.display.DisplayManager;
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
    private GameManager mGameManager;
    private SceneManager mSceneManager;
    private SoundManager mSoundManager;
    private GameRenderer mGameRenderer;
    private DisplayManager mDisplayManager;

    Application(Connector connector,GameRenderer gr)
    {
        mConnector = connector;
        mDisplayManager = gr.getDisplayManager();
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

    private void phaseDelay(int wait)
    {
        mAppWaitTime = mAppPhaseTime + wait;
    }

    /////////////////////
    // All Game States //
    /////////////////////
    private void updateApp(int dt, int[] input)
    {
        // 0: x
        // 1: y
        // 2: touch
        // 3: x2
        // 4: y2
        // 5: touch
        // 6: player movement player action
        if((mAppPhaseTime += dt) >= mAppWaitTime)
        {
            switch(mAppPhase)
            {
                //////////////////////////////////////////////////
                ////////////////////   LOADING  //////////////////
                //////////////////////////////////////////////////
                case ST_APP_START:
                    mGameManager = new GameManager(50);// Game container
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
                    }
                    else if(Build.MODEL.equals("Ulefone_S1")) //SM-J415FN
                    {
                        mAppPhase = ST_APP_CLIENT_DISCOVER_BLUETOOTH;
                    }
                    else
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
                    mGameManager.createGameLevel(0);
                    mAppPhase = ST_APP_OFFLINE_GAME_RUNNING;
                    break;

                // Running offline game with local input
                case ST_APP_OFFLINE_GAME_RUNNING:
                    mSceneManager.setTimer( mGameManager.getGameTime());
                    mGameManager.updateGameTicker();
                    mGameManager.updateGameOfflineInput(input[6]);

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
                                mNetcodeLogicManager = new NetcodeLogic(mGameManager, packetqueue);
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
                        mNetcodeLogicManager = new NetcodeLogic(mGameManager, packetqueue);
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

        /* Update logic of game and scene */
        mGameManager.run(dt);
        mSceneManager.run(dt, input);

        /* Update Renderobjects for the next run */
        mDisplayManager.updateRenderObjectsForGPU(mGameManager, mSceneManager);

        /* Wait for Bluetooth to render */
        synchronized(mSyncObject)
        {
            mSyncObject.notify();
        }
    }

    @Override
    public void run()
    {
        long frameStartTime;
        long lastframeStartTime = System.nanoTime();
        long deltaMax = 0;
        long printPeriodCounter = 0;
        long desiredFrameDeltaNano = SERVER_TICK_TIME * 1000000;
        long execTime;
        long accumulator = 0;
        boolean mGameRunning = true;
        final long printPeriod = 1000;

        // Game Loop
        while(mGameRunning)
        {
            /* Frame starts here */
            frameStartTime = System.nanoTime();
            accumulator += frameStartTime - (lastframeStartTime);
            lastframeStartTime = frameStartTime;


            if(accumulator >= desiredFrameDeltaNano)
            {
                /* Update Game */
                updateApp(SERVER_TICK_TIME, mConnector.getInput());


                /* Subtract tick time: deltaMax symbolizes max logic update withhold time */
                if((accumulator -= desiredFrameDeltaNano) > deltaMax)
                {
                    deltaMax = accumulator;
                }

                /* @measure server exec logic */
                if((execTime = System.nanoTime() - frameStartTime) > Globals.mDebugLoopMax)
                {
                    Globals.mDebugLoopMax = execTime;
                }
                else if(execTime < Globals.mDebugLoopMin)
                {
                    Globals.mDebugLoopMin = execTime;
                }

                /* @measure server exec total */
                if((execTime = System.nanoTime() - frameStartTime)  > Globals.mDebugLoopTotal)
                {
                    Globals.mDebugLoopTotal = execTime;
                }

                /* Print deltaMax every second */
                if((printPeriodCounter += SERVER_TICK_TIME)  >= printPeriod)
                {
                    Logger.logTimes(deltaMax);
                    printPeriodCounter %= printPeriod;
                    deltaMax = 0;
                }

                /* Wait to be scheduled */
                Thread.yield();
            }
            else
            {
                /* Update only when accumulator is >= server_tick*/
            }
        }
        Logger.log(Logger.DEBUG, TAG, Messages.dTextApplicationFinished);
    }
}

