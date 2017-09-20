package cc.scottland.sketchpad;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private CanvasView cv;

    private UsbManager mUsbManager;
    private UsbDevice controlOne;
    private UsbDevice controlTwo;
    private UsbDeviceConnection controlOneConnection;
    private UsbDeviceConnection controlTwoConnection;
    private PendingIntent mPermissionIntent;

    private static final String ACTION_USB_PERMISSION =
            "cc.scottland.sketchpad.USB_PERMISSION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Hide UI first
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(uiOptions);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.hide();

        // set up canvas view
        cv = (CanvasView) findViewById(R.id.canvas_view);
        cv.setFocusable(true);

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

        while (deviceIterator.hasNext()) {

            UsbDevice device = deviceIterator.next();

            // control knobs
            if (device.getVendorId() == 1917 && device.getProductId() == 1040) {

                if (controlOne == null) {
                    controlOne = device;
                    controlOneConnection = mUsbManager.openDevice(controlOne);
                } else {
                    controlTwo = device;
                    controlTwoConnection = mUsbManager.openDevice(controlTwo);
                }
            }
        }

        if (controlOneConnection != null && controlTwoConnection != null) {

            final boolean forceClaim = true;

            UsbInterface controlOneIntf = controlOne.getInterface(0);
            final UsbEndpoint controlOneEndpoint = controlOneIntf.getEndpoint(0);
            final int size = controlOneEndpoint.getMaxPacketSize();
            controlOneConnection.claimInterface(controlOneIntf, forceClaim);

            UsbInterface controlTwoIntf = controlTwo.getInterface(0);
            final UsbEndpoint controlTwoEndpoint = controlTwoIntf.getEndpoint(0);
            controlTwoConnection.claimInterface(controlOneIntf, forceClaim);

            final ByteBuffer bufferOne = ByteBuffer.allocate(size);
            final ByteBuffer bufferTwo = ByteBuffer.allocate(size);

            final Runnable updaterOne = new Runnable() {
                public void run() {
                    char result = bufferOne.getChar(0);
                    cv.knob(1, result == 1 ? 1 : -1);
                }
            };

            final Runnable updaterTwo = new Runnable() {
                public void run() {
                    char result = bufferTwo.getChar(0);
                    cv.knob(2, result == 1 ? 1 : -1);
                }
            };

            final UsbRequest requestOne = new UsbRequest(); // create an URB
            final boolean initOne = requestOne.initialize(controlOneConnection, controlOneEndpoint);

            final UsbRequest requestTwo = new UsbRequest(); // create an URB
            final boolean initTwo = requestTwo.initialize(controlTwoConnection, controlTwoEndpoint);

            new Thread(new Runnable() {
                @Override
                public void run() {

                    if (!initOne) {
                        Log.e("USB CONNECTION FAILED", "Request initialization failed for reading");
                        return;
                    }

                    while (true) {

                        if (requestOne.queue(bufferOne, size) == true && controlOneConnection.requestWait() == requestOne) {

                            runOnUiThread(updaterOne);
                        }
                    }
                }
            }).start();

            new Thread(new Runnable() {
                @Override
                public void run() {

                    if (!initTwo) {
                        Log.e("USB CONNECTION FAILED", "Request initialization failed for reading");
                        return;
                    }

                    while (true) {

                        if (requestTwo.queue(bufferTwo, size) == true && controlTwoConnection.requestWait() == requestTwo) {

                            runOnUiThread (updaterTwo);
                        }
                    }
                }
            }).start();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        super.onTouchEvent(e);

        if (e.getAction() == MotionEvent.ACTION_DOWN) cv.onTouchStart(e);
        if (e.getAction() == MotionEvent.ACTION_UP) cv.onTouchEnd(e);

        cv.update(e, false);

        return true;
    }
}
