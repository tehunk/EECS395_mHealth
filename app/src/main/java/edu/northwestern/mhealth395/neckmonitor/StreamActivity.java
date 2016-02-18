package edu.northwestern.mhealth395.neckmonitor;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


public class StreamActivity extends AppCompatActivity {

    public static final String DEVICE_EXTRA = "DEVICE";
    public static final String START_STREAM_EXTRA = "start";

    public static final String BROADCAST_NAME = "broadcast";
    public static final String BROADCAST_DISCONNECTED = "disconnected";
    public static final String BROADCAST_CONNECTED = "connected";
    public static final String BROADCAST_DATA = "data";
    public static final String B_DATA_ACC_X = "accx";
    public static final String B_DATA_ACC_Y = "accy";
    public static final String B_DATA_ACC_Z = "accz";
    public static final String B_DATA_AUDIO = "audio";
    public static final String B_DATA_PEIZO = "peizo";


    private final String TAG = "StreamActivity:";
    private BluetoothDevice device;
    private GraphView graph;
    private LineGraphSeries<DataPoint> mSeries;
    private double graph2LastXValue = 5d;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);

        Bundle extras = getIntent().getExtras();
        graph = (GraphView) findViewById(R.id.graph);
        mSeries = new LineGraphSeries<>();
        graph.addSeries(mSeries);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(40);



        if (extras != null) {
            device = (BluetoothDevice) extras.get(DEVICE_EXTRA);

            if (device != null) {
                Intent intent = new Intent(StreamActivity.this, DataHandlerService.class);
                intent.putExtra(DataHandlerService.DEVICE_EXTRA, device);
                intent.putExtra(DataHandlerService.START_EXTRA, extras.getBoolean(START_STREAM_EXTRA));
                startService(intent);
            } else {
                ((TextView) findViewById(R.id.connectionText)).setText("No device given");
            }
        } else {
            Log.e(TAG, "StreamActivity called but no device was passed");
        }

        // Start the service using the device
        Log.v(TAG, "Started activity...");
        IntentFilter filter = new IntentFilter(BROADCAST_CONNECTED);
        filter.addAction(BROADCAST_NAME);
        filter.addAction(BROADCAST_DATA);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "RECEIVED BROADCAST " + intent.getAction());
            switch (intent.getAction()) {
                case BROADCAST_CONNECTED:
                    Log.v(TAG, "Connected!");
                    ((TextView) findViewById(R.id.connectionText)).setText("Connected");
                    break;
                case BROADCAST_DISCONNECTED:
                    ((TextView) findViewById(R.id.connectionText)).setText("Disconnected");
                    break;
                case BROADCAST_DATA:
                    StringBuilder message = new StringBuilder("Data received...");
                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        /*if (extras.containsKey(B_DATA_ACC_X)) {
                            message.append("\naccelerometer x: ");
                            message.append(Float.toString(extras.getFloat(B_DATA_ACC_X)));
                        }
                        if (extras.containsKey(B_DATA_ACC_Y)){
                            message.append("\naccelerometer y: ");
                            message.append(Float.toString(extras.getFloat(B_DATA_ACC_Y)));
                        }
                        if (extras.containsKey(B_DATA_ACC_Z)) {
                            message.append("\naccelerometer z: ");
                            message.append(Float.toString(extras.getFloat(B_DATA_ACC_Z)));
                        }
                        if (extras.containsKey(B_DATA_PEIZO)) {
                            message.append("\nPiezo: ");
                            message.append(Float.toString(extras.getFloat(B_DATA_PEIZO)));
                        }
                        */
                        if (extras.containsKey(B_DATA_AUDIO)) {
                            message.append("\nAudio: ");
                            message.append(Float.toString(extras.getFloat(B_DATA_AUDIO)));
                            graph2LastXValue += 1d;
                            mSeries.appendData(new DataPoint(graph2LastXValue, 1000 - extras.getFloat(B_DATA_AUDIO)), true, 40);
                        }
                    }
                    ((TextView) findViewById(R.id.connectionText)).setText(message);
                    break;
                default:
                    Log.e(TAG, "Unknown broadcast with correct action");
            }
        }
    };
}
