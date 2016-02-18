package edu.northwestern.mhealth395.neckmonitor;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.UUID;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class DataHandlerService extends Service {
    public DataHandlerService() {
    }

    public static final String DEVICE_EXTRA = "device";
    public static final String START_EXTRA = "start";
    public static final String DB_EXTRA = "database";


    public static String shortUuidFormat = "0000%04X-0000-1000-8000-00805F9B34FB";
    public final static UUID UUID_SERVICE = sixteenBitUuid(0x2220);
    public final static UUID UUID_RECEIVE = sixteenBitUuid(0x2221);
    public final static UUID UUID_CLIENT_CONFIGURATION = sixteenBitUuid(0x2902);
    private final static String ACTION_DATA_AVAILABLE =
            "com.rfduino.ACTION_DATA_AVAILABLE";

    public static UUID sixteenBitUuid(long shortUuid) {
        assert shortUuid >= 0 && shortUuid <= 0xFFFF;
        return UUID.fromString(String.format(shortUuidFormat, shortUuid & 0xFFFF));
    }

    private final String TAG = "DataHandlerService";
    private HashMap<BluetoothDevice, BluetoothGatt> devices = new HashMap();
    private DataStorageContract.NecklaceDbHelper dbHelper;
    private SQLiteDatabase db;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//
//        if (dbHelper == null) {
//            dbHelper = new DataStorageContract.NecklaceDbHelper(getApplicationContext());
//
//            db = dbHelper.getWritableDatabase();
//            if (db == null) {
//                dbHelper.onCreate(db);
//            }
//        }
        Log.v(TAG, "Started");
        Bundle extras = intent.getExtras();

        BluetoothDevice device = null;
        boolean startStream = true;


        if (extras != null) {
            device = (BluetoothDevice) extras.get(DEVICE_EXTRA);
            startStream = extras.getBoolean(START_EXTRA);
            if (dbHelper == null) {
                dbHelper = (DataStorageContract.NecklaceDbHelper) extras.get(DB_EXTRA);
                try {
                    db = dbHelper.getWritableDatabase();
                    if (db == null) {
                        dbHelper.onCreate(db);
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

            }
        }

        if (device != null) {
            if (!devices.containsKey(device) && startStream) {
                Log.v(TAG, "Starting stream...");
                devices.put(device, device.connectGatt(this, true, gattCallback));
                Intent bIntent = new Intent(StreamActivity.BROADCAST_NAME);
                bIntent.putExtra(StreamActivity.BROADCAST_CONNECTED, "");
                sendBroadcast(bIntent);

            } else if (devices.containsKey(device) && !startStream) {
                Log.v(TAG, "Stopping stream...");

                ((BluetoothGatt) devices.get(device)).disconnect();
                devices.remove(device);
            } else {
                Log.w(TAG, "Device is already streaming and startstream called (or opposite)");
            }
        }


        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.v("BluetoothLE ", "Status: " + status);
            Intent broadcastIntent = new Intent("asdf");
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    broadcastIntent.setAction(StreamActivity.BROADCAST_CONNECTED);
                    gatt.discoverServices();
                    sendBroadcast(broadcastIntent);
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.w("gattCallback", "STATE_DISCONNECTED");
                    // TODO provide user some warning
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService mBluetoothGattService = gatt.getService(UUID_SERVICE);
                if (mBluetoothGattService == null) {
                    Log.e(TAG, "RFduino GATT service not found!");
                    return;
                }

                BluetoothGattCharacteristic receiveCharacteristic =
                        mBluetoothGattService.getCharacteristic(UUID_RECEIVE);
                if (receiveCharacteristic != null) {
                    BluetoothGattDescriptor receiveConfigDescriptor =
                            receiveCharacteristic.getDescriptor(UUID_CLIENT_CONFIGURATION);
                    if (receiveConfigDescriptor != null) {
                        gatt.setCharacteristicNotification(receiveCharacteristic, true);

                        receiveConfigDescriptor.setValue(
                                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(receiveConfigDescriptor);
                    } else {
                        Log.e(TAG, "RFduino receive config descriptor not found!");
                    }

                } else {
                    Log.e(TAG, "RFduino receive characteristic not found!");
                }
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Log.v(TAG, "Characteristic read!!!");

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            //Log.v(TAG, "Characteristic changed!!!");
            NecklaceEvent event = new NecklaceEvent(characteristic.getValue());
            new StoreEventTask().doInBackground(event);
            try {
                String myString = new String(characteristic.getValue(), "UTF-8");
             //   Log.v(TAG, myString);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };

//    IntentFilter getFilter() {
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(ACTION_DATA_AVAILABLE);
//        return filter;
//    }

    public class StoreEventTask extends AsyncTask<NecklaceEvent, Void, Void> {
        @Override
        protected Void doInBackground(NecklaceEvent... params) {
            NecklaceEvent event = params[0];
            if (event != null) {
                Log.v(TAG, "Task Started");

                Log.v(TAG, "Accx: " + Float.toString(event.getAccX()));
                Log.v(TAG, "Accy: " + Float.toString(event.getAccY()));
                Log.v(TAG, "Accz: " + Float.toString(event.getAccZ()));
                Log.v(TAG, "Audio: " + Float.toString(event.getAudio()));
                Log.v(TAG, "Vib: " + Float.toString(event.getVib()));

                Intent bIntent = new Intent(StreamActivity.BROADCAST_DATA);
                bIntent.putExtra(StreamActivity.B_DATA_ACC_X, event.getAccX());
                bIntent.putExtra(StreamActivity.B_DATA_ACC_Y, event.getAccY());
                bIntent.putExtra(StreamActivity.B_DATA_ACC_Z, event.getAccZ());
                bIntent.putExtra(StreamActivity.B_DATA_AUDIO, event.getAudio());
                bIntent.putExtra(StreamActivity.B_DATA_PEIZO, event.getVib());
                sendBroadcast(bIntent);


                ContentValues values = new ContentValues();
                values.put(DataStorageContract.NecklaceTable.COLUMN_NAME_ACCX, event.getAccX());
                values.put(DataStorageContract.NecklaceTable.COLUMN_NAME_ACCY, event.getAccY());
                values.put(DataStorageContract.NecklaceTable.COLUMN_NAME_ACCZ, event.getAccZ());
                values.put(DataStorageContract.NecklaceTable.COLUMN_NAME_AUDIO, event.getAudio());
                values.put(DataStorageContract.NecklaceTable.COLUMN_NAME_VIBRATION, event.getVib());

                try {
//                    db.insertOrThrow(DataStorageContract.NecklaceTable.TABLE_NAME,
//                            null,
//                            values);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                }else{
                    Log.e(TAG, "No event passed to StoreEventTask");
                }
                return null;
            }

        }
    }
