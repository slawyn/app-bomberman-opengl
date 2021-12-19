package main;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;

import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.game.bomber.BuildConfig;
import com.game.bomber.R;

import java.util.Vector;

import main.communication.PacketQueue;
import main.communication.Server;
import main.communication.bluetooth.ServerBluetoothCLASSIC;
import main.communication.bluetooth.ClientBluetoothCLASSIC;
import main.communication.Client;
import main.communication.wlan.ClientNetworkWLAN;
import main.communication.wlan.ServerWLAN;
import main.rendering.GameRenderer;

import static main.Constants.FINE_LOCATION_ENABLE_REQUEST_CODE;


/* Bluetooth: http://www.doepiccoding.com/blog/?p=232 */
/* Optimization: https://developer.android.com/training/articles/perf-tips*/
public class Connector extends Activity
{
    private static final String TAG = "MainActivity";
    private static Application mApplication;
    private static final int mBluetoothBroadcastTime = 30;

    //Render
    public static GLSurfaceView glSurfaceView;

    // Touch
    private static int[] tempReleased;
    private static int[] tempTouched;
    private static int[] tempMoved;
    private static int[] dx;
    private static int[] dy;
    private static int[] x;
    private static int[] y;
    private static int tempGameinputMovement;
    private static int tempGameinputAction;
    private static int[] mGameInput;

    // Comm
    private static boolean mLocalServerBootable;
    private static boolean mBluetoothEnabled;
    private static boolean mBluetoothBeingEnabled;
    private static boolean mFineLocationEnabled;
    private static boolean mFineLocationBeingEnabled;
    private static Vector<BluetoothDevice> devices;
    private static Vector<String> deviceNames;
    private static int lastDiscoveredDevice;
    private static Client mClient;
    private static Server mServer;
    private static BroadcastReceiver mBluetoothBroadcastReceiver;

    private static int typeOfConnection;


    public void setConnectionType(int conntype)
    {
        typeOfConnection = conntype;
    }

    public PacketQueue attachGameToServer()
    {
        try
        {
            mLocalServerBootable = true;
            Logger.log(Logger.DEBUG, TAG, Messages.dTextConnectingServerToPipe);
            if(mLocalServerBootable)
            {
                switch(typeOfConnection)
                {
                    case Constants.CONNECTION_TYPE_CLASSIC:
                        mServer = new ServerBluetoothCLASSIC(mBluetoothBroadcastTime);
                        break;
                    case Constants.CONNECTION_TYPE_GATT:
                        //mServer = new ServerBluetoothGATT(this);
                        break;
                    case Constants.CONNECTION_TYPE_WLAN:
                        mServer = new ServerWLAN();
                        break;
                }
                return mServer.createPacketQueue();
            }

        } catch(Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public PacketQueue attachGameToClient(String name)
    {
        Logger.log(Logger.DEBUG, TAG, "Connecting mClient pipe: " + name);
        try
        {

            switch(typeOfConnection)
            {
                case Constants.CONNECTION_TYPE_CLASSIC:
                    mClient = new ClientBluetoothCLASSIC(devices.get(deviceNames.indexOf(name)));
                    devices.clear();
                    deviceNames.clear();
                    break;
                case Constants.CONNECTION_TYPE_WLAN:
                    mClient = new ClientNetworkWLAN(name);
                    break;
                default:
                    Logger.log(Logger.ERROR, TAG, Messages.eTextNoConnectionTypeFound);
                    break;
            }

            return mClient.createPacketQueue();
        } catch(Exception e)
        {
            e.printStackTrace();

        }
        return null;
    }

    public boolean hasDevices()
    {
        return lastDiscoveredDevice < devices.size();
    }

    public String discoveredDevice()
    {
        String s = deviceNames.get(lastDiscoveredDevice);
        lastDiscoveredDevice++;
        return s;
    }

    // For mClient
    public void startDiscovery()
    {
        BluetoothAdapter adapter;

        switch(typeOfConnection)
        {
            case Constants.CONNECTION_TYPE_CLASSIC:
                mBluetoothBroadcastReceiver = new BroadcastReceiver()
                {

                    @Override
                    public void onReceive(Context context, Intent intent)
                    {
                        BluetoothDevice bdevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        String name = bdevice.getName() + " " + bdevice.getAddress();
                        if(!deviceNames.contains(name))
                        {
                            devices.add(bdevice);
                            deviceNames.add(name);
                            Logger.log(Logger.INFO, TAG, "Found new device:" + name);
                        }
                    }
                };

                registerReceiver(mBluetoothBroadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
                adapter = BluetoothAdapter.getDefaultAdapter();
                if(adapter != null)
                {
                    if(adapter.isDiscovering())
                        adapter.cancelDiscovery();
                    adapter.startDiscovery();
                }
                Logger.log(Logger.INFO, TAG, "Bluetooth discovery started");

                break;
            case Constants.CONNECTION_TYPE_WLAN:
                break;
            default:
                Logger.log(Logger.ERROR, TAG, "No Connection Type found");
                break;
        }
    }

    // Stop discovery
    public void stopDiscovery()
    {
        BluetoothAdapter adapter;
        switch(typeOfConnection)
        {
            case Constants.CONNECTION_TYPE_CLASSIC:
                adapter = BluetoothAdapter.getDefaultAdapter();
                if(adapter != null)
                {
                    adapter.cancelDiscovery();
                }
                unregisterReceiver(mBluetoothBroadcastReceiver);
                break;
            case Constants.CONNECTION_TYPE_WLAN:
                break;
            default:
                Logger.log(Logger.ERROR, TAG, Messages.eTextNoConnectionTypeFound);
                break;
        }
    }

    // For bluetoothServer
    public void broadcastServerInformation()
    {
        mBluetoothEnabled = true;
        switch(typeOfConnection)
        {
            case Constants.CONNECTION_TYPE_WLAN:
                break;
            case Constants.CONNECTION_TYPE_CLASSIC:
                if(mBluetoothEnabled)
                {

                    Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, mBluetoothBroadcastTime);
                    startActivityForResult(discoverableIntent, Constants.DISCOVERABLE_REQUEST_CODE);
                }
                break;
            case Constants.CONNECTION_TYPE_GATT:
                if(mBluetoothEnabled)
                {
                    mLocalServerBootable = true;
                }
                break;
        }
    }

    public boolean isBluetoothEnabled()
    {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if(adapter == null)
        {
            mBluetoothEnabled = false;           // no bluetooth allowed
            Toast.makeText(this, Messages.eTextBluetoothNotSupported, Toast.LENGTH_LONG);
        } else if(adapter.isEnabled())
        {
            mBluetoothEnabled = true;
        } else if(!mBluetoothBeingEnabled)
        {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, Constants.BLUETOOTH_ENABLE_REQUEST_CODE);
            mBluetoothBeingEnabled = true;
        }
        return mBluetoothEnabled;
    }

    public boolean isFineLocationEnabled()
    {

        if(!mFineLocationBeingEnabled)
        {

            // TODO implement with onRequestPermissionResult, and only then start discovery!
            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            if(permissionCheck != PackageManager.PERMISSION_GRANTED)
            {

                mFineLocationEnabled = false;
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
                {
                    Toast.makeText(this, Messages.sTextPermissionToGetBleLocationData, Toast.LENGTH_SHORT).show();
                } else
                {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    {
                        mFineLocationBeingEnabled = true;
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ENABLE_REQUEST_CODE);
                    }
                }
            } else
            {
                mFineLocationEnabled = true;
            }
        }
        return mFineLocationEnabled;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == FINE_LOCATION_ENABLE_REQUEST_CODE)
        {
            mFineLocationBeingEnabled = false;
            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                mFineLocationEnabled = true;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Constants.DISCOVERABLE_REQUEST_CODE)
        {
            if(resultCode != RESULT_CANCELED)
            {
                mLocalServerBootable = true;
            } else
            {
                mLocalServerBootable = false;
            }
        } else if(requestCode == Constants.BLUETOOTH_ENABLE_REQUEST_CODE)
        {
            mBluetoothBeingEnabled = false;
            if(resultCode != RESULT_CANCELED)
            {
                mBluetoothEnabled = true;
            } else
            {
                mBluetoothEnabled = false;
            }
        }
    }


    public void render()
    {
        glSurfaceView.requestRender();
    }

    public int[] getInput()
    {

        /* Touch one is Movement */
        if(tempTouched[0] > 0)
        {
            if(Math.abs(dx[0]) > Math.abs(dy[0]))
            {
                dy[0] = 0;
                if(dx[0] >= Constants.MOVEMENT_THRESHOLD)
                {   // X
                    tempGameinputMovement = Constants.INPUT_MOVE_RIGHT;
                    dx[0] = 1;
                } else if(dx[0] <= -Constants.MOVEMENT_THRESHOLD)
                {
                    tempGameinputMovement = Constants.INPUT_MOVE_LEFT;
                    dx[0] = -1;
                }
            } else
            {
                dx[0] = 0;
                if(dy[0] >= Constants.MOVEMENT_THRESHOLD)
                { // Y
                    tempGameinputMovement = Constants.INPUT_MOVE_DOWN;
                    dy[0] = 1;
                } else if(dy[0] <= -Constants.MOVEMENT_THRESHOLD)
                {
                    tempGameinputMovement = Constants.INPUT_MOVE_UP;
                    dy[0] = -1;
                }
            }
        } else
        {
            tempGameinputMovement = Constants.INPUT_NONE;
        }

        /* Touch two is Action */
        if(tempReleased[1] != 0)
        {
            tempReleased[1] = 0;          // Consumed
            tempGameinputAction = Constants.INPUT_PLACE_BOMB;
        } else
        {
            tempGameinputAction = 0;
        }

        mGameInput[0] = x[0];
        mGameInput[1] = y[0];
        mGameInput[2] = tempTouched[0];
        mGameInput[3] = x[1];
        mGameInput[4] = y[1];
        mGameInput[5] = tempTouched[1];
        mGameInput[6] = tempGameinputMovement | tempGameinputAction;


        // Log.w(TAG, "x:" + x[0] + " y:" + y[0] + " x2:" + x[1] +" y2:" +y[1] +" m:"+tempGameinputMovement+" a:"+tempGameinputAction);
        return mGameInput;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        super.onTouchEvent(event);
        int action = event.getActionMasked();
        int pointerCnt = event.getPointerCount();
        int act_id = event.getPointerId(event.getActionIndex());

        if(act_id > Constants.MAX_TOUCH_INSTANCES) // TODO currently no support !
            return true;

        // if moved we set it to true
        tempMoved[act_id] = 0;
        switch(action)
        {
            case MotionEvent.ACTION_MOVE:
                tempMoved[act_id] = 1;
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_DOWN:
                tempTouched[act_id] = 1;
                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                if(tempTouched[act_id] > 0)
                {
                    tempReleased[act_id] = 1;
                }
                tempTouched[act_id] = 0;
                break;
        }

        for(int i = 0; i < pointerCnt; i++)
        {
            int id = event.getPointerId(i);

            /* Calculate player movement */
            if(tempMoved[id] > 0)
            {
                dx[id] = ((int) (event.getX(i)) - x[id] + dx[id]);
                dy[id] = ((int) (event.getY(i)) - y[id] + dy[id]);
            } else
            {
                dx[id] = 0;
                dy[id] = 0;
            }

            x[id] = (int) event.getX(i);
            y[id] = (int) event.getY(i);
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_initial);

        final ActivityManager activitymanager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        Logger.init(Build.MODEL, activitymanager);


        Display.Mode[] mode =  getWindowManager().getDefaultDisplay().getSupportedModes();

        // TODO Show Display Modes in the Options
        for(int idx=0;idx<mode.length;idx++){
            Logger.log(Logger.INFO,TAG, "Refreshrate Mode:" +mode[idx].getRefreshRate());
        }

        // Logger.log(Logger.VERBOSE,TAG,(String.format("0x%08X", NativeInterface.initFreeType())));
        //preferMinimalPostProcessing TODO Check if needed
        // Analysis


        try
        {
            final ConfigurationInfo configurationInfo = activitymanager.getDeviceConfigurationInfo();
            final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;



            if(supportsEs2)
            {
                // Bluetooth
                devices = new Vector<>();
                deviceNames = new Vector<>();
                mLocalServerBootable = false;
                mBluetoothEnabled = false;
                lastDiscoveredDevice = 0;

                // Variables for touch
                tempTouched = new int[Constants.MAX_TOUCH_INSTANCES];
                tempMoved = new int[Constants.MAX_TOUCH_INSTANCES];
                x = new int[Constants.MAX_TOUCH_INSTANCES];
                y = new int[Constants.MAX_TOUCH_INSTANCES];
                dx = new int[Constants.MAX_TOUCH_INSTANCES];
                dy = new int[Constants.MAX_TOUCH_INSTANCES];
                tempReleased = new int[Constants.MAX_TOUCH_INSTANCES];

                // Input
                mGameInput = new int[Constants.MAX_TOUCH_INSTANCES * 3 + 1];   // TODO need to see if I can add more touches

                // Create app
                DisplayMetrics dm = getResources().getDisplayMetrics();

                GameRenderer gr = new GameRenderer(getResources(), dm.widthPixels, dm.heightPixels);
                mApplication = new Application(this, gr);

                /* Configure Surface*/
                glSurfaceView = findViewById(R.id.glsurfaceview);//new GLSurfaceView(this);
                glSurfaceView.setEGLContextClientVersion(2);
                glSurfaceView.setRenderer(gr);
                glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
                glSurfaceView.setPreserveEGLContextOnPause(true);
                //glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY); // Draw using requestRender()



                TextView version = findViewById(R.id.textversion);
                version.setText(BuildConfig.VERSION_NAME);


            } else
            {
                Toast.makeText(this, Messages.eTextGLES2notSupported, Toast.LENGTH_LONG).show();
            }
        } catch(Exception e)
        {
            e.printStackTrace();
            Toast.makeText(this, Messages.eTextInitException, Toast.LENGTH_LONG).show();
        }
    }


    @Override
    protected void onPause()
    {
        super.onPause();
        if(glSurfaceView != null)
        {
            glSurfaceView.onPause();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        Logger.log(Logger.INFO, TAG, Messages.sTextActivityResume);
        if(glSurfaceView != null)
        {
            glSurfaceView.onResume();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();


        Logger.log(Logger.INFO, TAG, Messages.sTextActivityDestroyed);

        if(mBluetoothBroadcastReceiver != null)
        {
            unregisterReceiver(mBluetoothBroadcastReceiver);
        }
        if(mClient != null)
        {
            mClient.closeConnection();
            mClient = null;
        }
        if(mServer != null)
        {
            mServer.closeConnection();
            mServer = null;
        }
    }

}

