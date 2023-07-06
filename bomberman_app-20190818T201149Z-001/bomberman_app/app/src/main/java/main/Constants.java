package main;

import com.game.bomber.BuildConfig;
import com.game.bomber.R;

import java.util.UUID;

public final class Constants
{


    /* Network */
    public final static int PACKET_HEADER_SIZE = 5;
    public final static int PACKET_MIN_SIZE = PACKET_HEADER_SIZE + 4;
    public final static int PACKET_MAX_SIZE = PACKET_MIN_SIZE + 60;

    public final static int PROTOCOL_OFFSET_ID = 0x00;
    public final static int PROTOCOL_OFFSET_LEN = 0x01;
    public final static int PROTOCOL_OFFSET_SEQ = 0x02;

    public final static int PROTOCOL_OFFSET_FLAGS = 0x03;
    public final static int PROTOCOL_FLAGS_CONTROL = 1;

    public final static int PROTOCOL_OFFSET_TYPE = 0x04;
    public final static byte PROTOCOL_TYPE_RGAME = 1;
    public static final byte PROTOCOL_TYPE_GSTATE = 2;
    public static final byte PROTOCOL_TYPE_IDDIST = 3;
    public static final byte PROTOCOL_TYPE_INPUT = 4;
    public static final byte PROTOCOL_TYPE_CCON = 5;
    public static final byte PROTOCOL_TYPE_SYNC = 6;

    public final static int PROTOCOL_OFFSET_PAYLOAD = PACKET_HEADER_SIZE;

    /* Object States*/
    public static final int STATE_ALIVE = 0;
    public static final int STATE_MOVEDOWN = 1;
    public static final int STATE_MOVEUP = 2;
    public static final int STATE_MOVELEFT = 3;
    public static final int STATE_MOVERIGHT = 4;
    public static final int STATE_DEAD = 5;
    public static final int STATE_PRESSED = 6;
    public static final int STATE_UNPRESSED = 7;
    public static final int STATE_DETONATED = 8;

    /* Types of input*/
    public static final int INPUT_NONE = 0x00;
    public static final int INPUT_MOVE_RIGHT = 0x01;
    public static final int INPUT_MOVE_LEFT = 0x02;
    public static final int INPUT_MOVE_UP = 0x03;
    public static final int INPUT_MOVE_DOWN = 0x04;
    public static final int INPUT_PLACE_BOMB = 0x10;


    public final static int SOUND_NONE = 0;
    public final static int SOUND_EXPLOSION = 1;
    public final static int SOUND_DEATH = 10;
    public final static int SOUNDABLES[][] = {
            {SOUND_EXPLOSION, R.raw.explosion},
            {SOUND_DEATH, R.raw.death}
    };

    /* Size configurations*/
    public final static int NUMBER_OF_RENDER_OBJECTS = 50;
    public final static int NUMBER_OF_ANIMATED_OBJECTS = 50;
    public final static int NUMBER_OF_TEXTURES = 300;


    public final static int EXTRACT_OBJ_ID = 0x00000fff;    // TODO if we ever have object overrun then there we need to change this approach
    public final static int EXTRACT_OBJ_TYPE = 0xfff000;
    //////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////
    /* DEBUG_INFORMATION*/
    public final static boolean DEBUG_DRAW_HITBOXES = true;
    public final static boolean DEBUG_DRAW_DEBUG_INFO = false;


    /* Other Constants */
    public final static int CONNECTION_TYPE_CLASSIC = 0;
    public final static int CONNECTION_TYPE_GATT = 1;
    public final static int CONNECTION_TYPE_WLAN = 2;


    public final static String SERVER_UUID = "4e5d48e0-75df-11e3-981f-0800200c9a66";
    public final static String SERVER_UUID2 = "4e5d48e1-75df-11e3-981f-0800200c9a66";
    public final static String SERVER_UUID3 = "4e5d48e2-75df-11e3-981f-0800200c9a66";
    public final static String SERVER_NAME = "BOMBER";

    public static final UUID SERVER_GATT_SERVICE = UUID.fromString(SERVER_UUID);
    public static final UUID SERVER_GATT_CHARACTERISTIC = UUID.fromString(SERVER_UUID2);
    public static final UUID SERVER_GATT_DESCRIPTOR = UUID.fromString(SERVER_UUID3);

    public final static int SERVER_TICK_TIME = (int) (1000.0 / 10);
    public final static int MAX_TOUCH_INSTANCES = 2;
    public final static int MAX_NUMBER_OF_PLAYERS = 2;
    public final static int MOVEMENT_THRESHOLD = 30;
    public final static int BOUNDS = 20;
    public final static int DISCOVERABLE_REQUEST_CODE = 0x1;
    public final static int BLUETOOTH_ENABLE_REQUEST_CODE = 0x2;
    public final static int FINE_LOCATION_ENABLE_REQUEST_CODE = 0x3;
    public final static int NUM_OF_PACKET_DESCS = 20;

    public final static int INPUT_LAG = 75;

    //##################################### Game Phases #####################################
    public static final int ST_APP_START = 0;
    public static final int ST_APP_PRELOAD = ST_APP_START + 1;
    public static final int ST_APP_LOAD = ST_APP_PRELOAD + 1;
    public static final int ST_APP_SELECTION = ST_APP_LOAD + 1;
    public static final int ST_APP_SELECTION_MAIN = ST_APP_SELECTION + 1;
    public static final int ST_APP_SELECTION_BLUETOOTH = ST_APP_SELECTION_MAIN + 1;
    public static final int ST_APP_SELECTION_WLAN = ST_APP_SELECTION_BLUETOOTH + 1;

    public static final int ST_APP_CLIENT_DISCOVER_WLAN = ST_APP_SELECTION_WLAN + 1;
    public static final int ST_APP_CLIENT_DISCOVER_BLUETOOTH = ST_APP_CLIENT_DISCOVER_WLAN + 1;

    public static final int ST_APP_CLIENT_SELECT_SERVER = ST_APP_CLIENT_DISCOVER_BLUETOOTH + 1;
    public static final int ST_APP_CLIENT_SYNC = ST_APP_CLIENT_SELECT_SERVER + 1;
    public static final int ST_APP_CLIENT_GAME_RUNNING = ST_APP_CLIENT_SYNC + 1;

    public static final int ST_APP_SERVER_BLUETOOTH = ST_APP_CLIENT_GAME_RUNNING + 1;
    public static final int ST_APP_SERVER_WLAN = ST_APP_SERVER_BLUETOOTH + 1;

    public static final int ST_APP_SERVER_DISCOVERABLE = ST_APP_SERVER_WLAN + 1;
    public static final int ST_APP_SERVER_WAIT_FOR_DISCOVERABLE = ST_APP_SERVER_DISCOVERABLE + 1;
    public static final int ST_APP_SERVER_SYNC = ST_APP_SERVER_WAIT_FOR_DISCOVERABLE + 1;
    public static final int ST_APP_SERVER_GAME_RUNNING = ST_APP_SERVER_SYNC + 1;

    public static final int ST_APP_OFFLINE_START = ST_APP_SERVER_GAME_RUNNING + 1;
    public static final int ST_APP_OFFLINE_GAME_RUNNING = ST_APP_OFFLINE_START + 1;


}
