package com.gajendra.bindu;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
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

    // Check if this is the first time opening the app
    SharedPreferences prefs = getSharedPreferences("BinduPrefs", MODE_PRIVATE);
    boolean tutorialSeen = prefs.getBoolean("tutorial_seen", false);

    if (tutorialSeen) {
      // User has seen tutorial, go directly to game
      startActivity(new Intent(this, GameActivity.class));
    } else {
      // First time user, show tutorial
      startActivity(new Intent(this, TutorialActivity.class));
    }
  }

}
