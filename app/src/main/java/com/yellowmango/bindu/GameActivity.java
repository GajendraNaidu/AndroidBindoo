package com.yellowmango.bindu;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GameActivity extends AppCompatActivity {

  private GameState _gameState = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    LinearLayout layout =  new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);

    AddingGameView(layout);

    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    setContentView(layout);
  }

  private void AddingGameView(final LinearLayout layout) {
    _gameState = new GameState();
    GameView  _gameView  = new GameView(this, _gameState, new Handler() {
      @Override
      public void handleMessage(Message m) {
        Integer playerOne = m.getData().getInt("1", 0);
        Integer playerTwo = m.getData().getInt("2", 0);
        String comments = getString(m.getData().getInt("3"));

        if (m.getData().getBoolean("0")) {
          String finalMessage = "Jerry: " + playerOne + "  Tom: " + playerTwo + "\n" + comments + "\n" + getString(R.string.play_again_option);
          getAlertDialog(finalMessage).show();
        }
      }
    });
    layout.addView(_gameView, 0);
    
    
    Display dis = getWindowManager().getDefaultDisplay();
    Point size = new Point();
    dis.getSize(size);
    int height = size.y;
    int width = size.x;
    
    int rowHeight = Integer.parseInt(getString(R.string.row_height));
    int columnWidth = Integer.parseInt(getString(R.string.column_width));

    int noOfRows = (int)(height/rowHeight);
    int noOfColumns = (int)(width/columnWidth);

    // Limit grid size for better performance
    noOfRows = Math.min(noOfRows, 8);
    noOfColumns = Math.min(noOfColumns, 6);

    android.util.Log.d("GameActivity", "Grid size: " + noOfColumns + "x" + noOfRows + " (screen: " + width + "x" + height + ")");

    _gameState.setNumberOfRows(noOfRows);
    _gameState.setNumberOfColumns(noOfColumns);
    
    for (int i = 0; i < noOfColumns; i++) {
      for (int j = 0; j < noOfRows; j++) {
        GamePoint _gamePoint = new GamePoint(i, j);
        _gameState.AddPoint(_gamePoint);
      }
    }
    
    List<GameLine> gameLines = GameUtils.GetTempGameLines(noOfRows, noOfColumns);
    List<GameFill> strayFills = GameUtils.GetTempGameFills(noOfRows, noOfColumns);
    _gameState.AddInitialLines(gameLines,strayFills);

    android.util.Log.d("GameActivity", "About to call _gameView.initialize()");
    try {
      _gameView.initialize();
      android.util.Log.d("GameActivity", "initialize() completed successfully");
    } catch (Exception e) {
      android.util.Log.e("GameActivity", "Error in initialize(): " + e.getMessage(), e);
    }

  }

  public AlertDialog getAlertDialog(String comment) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage(comment).setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int id) {
        startActivity(new Intent(GameActivity.this, GameActivity.class));
        dialog.cancel();
        finish();
      }

    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int id) {
        dialog.cancel();
        finish();
      }
    });
    AlertDialog alert = builder.create();
    return alert;
  }
  
  @Override
  protected void onPause() {
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  protected void onDestroy() {
    try {
      super.onDestroy();
      if (_gameState != null) {
        _gameState = null;
      }
    } catch (Exception ex) {
      // FIXME: Just ignore or lets see what we should do
    }
  }
}
