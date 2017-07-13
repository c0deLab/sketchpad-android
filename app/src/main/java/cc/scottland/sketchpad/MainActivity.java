package cc.scottland.sketchpad;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private CanvasView cv;

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
