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
    private UsbRequest controlOneRequest;
    private UsbRequest controlTwoRequest;
    private PendingIntent mPermissionIntent;

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

        while(deviceIterator.hasNext()) {

            UsbDevice device = deviceIterator.next();

            // control knobs
            if (device.getVendorId() == 1917 && device.getProductId() == 1040) {
//                Log.e("setting", "controlOne");
//                controlOne = device;
//                mUsbManager.requestPermission(controlOne, mPermissionIntent);
//
                // grant permission here...
                mUsbManager.requestPermission(device, mPermissionIntent);
                if (controlOne == null) {
                    controlOne = device;
                    controlOneConnection = mUsbManager.openDevice(controlOne);
                }
                else {
                    controlTwo = device;
                    controlTwoConnection = mUsbManager.openDevice(controlTwo);
                }
            }
        }

        if (controlOneConnection != null && controlTwoConnection != null) {

            final int TIMEOUT = 100;
            final boolean forceClaim = true;

            UsbInterface controlOneIntf = controlOne.getInterface(0);
            final UsbEndpoint controlOneEndpoint = controlOneIntf.getEndpoint(0);
            final int size = controlOneEndpoint.getMaxPacketSize();
            controlOneConnection.claimInterface(controlOneIntf, forceClaim);

            UsbInterface controlTwoIntf = controlTwo.getInterface(0);
            final UsbEndpoint controlTwoEndpoint = controlTwoIntf.getEndpoint(0);
            controlTwoConnection.claimInterface(controlOneIntf, forceClaim);

            new Thread(new Runnable() {
                int byteArrayToInt(byte[] b) {
                    return   b[3] & 0xFF |
                            (b[2] & 0xFF) << 8 |
                            (b[1] & 0xFF) << 16 |
                            (b[0] & 0xFF) << 24;
                }
                String byteArrayToString(byte[] in) {
                    char out[] = new char[in.length * 2];
                    for (int i = 0; i < in.length; i++) {
                        out[i * 2] = "0123456789ABCDEF".charAt((in[i] >> 4) & 15);
                        out[i * 2 + 1] = "0123456789ABCDEF".charAt(in[i] & 15);
                    }
                    return new String(out);
                }
                @Override
                public void run() {

                    UsbRequest request = new UsbRequest(); // create an URB
                    boolean initialized = request.initialize(controlOneConnection, controlOneEndpoint);

                    if (!initialized) {
                        Log.e("USB CONNECTION FAILED", "Request initialization failed for reading");
                        return;
                    }

                    while (true) {
                        ByteBuffer buffer = ByteBuffer.allocate(size);

                        if (request.queue(buffer, size) == true) {
                            if (controlOneConnection.requestWait() == request) {

                                char result = buffer.getChar(0);

                                // clockwise
                                if (result == 1) {
                                    cv.x += 6;
                                // counterclockwise
                                } else if (result == 255) {
                                    cv.x -= 6;
                                }
                            }
                        }
                    }
                }
            }).start();
        }

        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        super.onTouchEvent(e);

        if (e.getAction() == MotionEvent.ACTION_DOWN) cv.onTouchStart(e);
        if (e.getAction() == MotionEvent.ACTION_UP) cv.onTouchEnd(e);

        cv.update(e, false);

        return true;
    }

    private static final String ACTION_USB_PERMISSION =
            "cc.scottland.sketchpad.USB_PERMISSION";
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            Log.e("received", action);

            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null){
                            //call method to set up device communication
                            Log.e("device", device.toString());
                        }
                    }
                }
            }
        }
    };
}
