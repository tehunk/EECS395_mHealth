package edu.northwestern.mhealth395.neckmonitor;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by William on 2/6/2016.
 */
public class NecklaceEvent {

    private float accX;
    private float accY;
    private float accZ;
    private float vib;
    private float audio;
    private long  timeStamp;

    private final String TAG = "NecklaceEvent";

    public NecklaceEvent (byte[] bytes) {
        timeStamp = getTimeStamp();
        if (bytes.length == 20) {
            accX = ByteBuffer.wrap(bytes, 0, 4).order(ByteOrder.nativeOrder()).getFloat();
            accY = ByteBuffer.wrap(bytes, 4, 4).order(ByteOrder.nativeOrder()).getFloat();
            accZ = ByteBuffer.wrap(bytes, 8, 4).order(ByteOrder.nativeOrder()).getFloat();
            vib = ByteBuffer.wrap(bytes, 12, 4).order(ByteOrder.nativeOrder()).getFloat();
            audio = ByteBuffer.wrap(bytes, 16, 4).order(ByteOrder.nativeOrder()).getFloat();
        } else {
            Log.e(TAG, "Wrong number of bytes in constrctor array");
            accX = 0;
            accY = 0;
            accZ = 0;
            vib = 0;
            audio = 0;
        }
    }

    public float getAccX() { return accX; }
    public float getAccY() { return accY; }
    public float getAccZ() { return accZ; }
    public float getVib()  { return vib;  }
    public float getAudio(){ return audio;}
    public long  getTimeStamp() { return timeStamp; }


}
