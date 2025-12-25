package com.yellowmango.bindu;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;

public class SplashActivity extends AppCompatActivity {
  protected int      _splashTime   = 4000;
  protected Handler  _exitHandler  = null;
  protected Runnable _exitRunnable = null;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.splash);

    _exitRunnable = new Runnable() {
      public void run() {
        exitSplash();
      }
    };

    _exitHandler = new Handler();
    _exitHandler.postDelayed(_exitRunnable, _splashTime);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (event.getAction() == MotionEvent.ACTION_DOWN) {
      _exitHandler.removeCallbacks(_exitRunnable);
      exitSplash();
    }
    return true;
  }

  private void exitSplash() {
    finish();
    startActivity(new Intent(this, GameActivity.class));
  }

}
