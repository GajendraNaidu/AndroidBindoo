package com.yellowmango.bindu;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GameActivity extends Activity {

  private GameState _gameState = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    LinearLayout layout =  new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);
    final TextView _textView = new TextView(this);
    layout.addView(_textView);
    AddingGameView(_textView, layout);

    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    setContentView(layout);
  }

  private void AddingGameView(final TextView _textView, final LinearLayout layout) {
    _gameState = new GameState();
    GameView  _gameView  = new GameView(this, _gameState, new Handler() {
      @Override
      public void handleMessage(Message m) {
        Integer playerOne = m.getData().getInt("1", 0);
        Integer playerTwo = m.getData().getInt("2", 0);
        String comments = getString(m.getData().getInt("3"));
        String finalString = String.format(getString(R.string.game_header_text), new Object[] { "" + playerOne, "" + playerTwo, comments });
        _textView.setText(Html.fromHtml(finalString));
        if (m.getData().getBoolean("0")) {
          comments += "\n"+getString(R.string.play_again_option);
          getAlertDialog(comments).show();
        }
      }
    });
    layout.addView(_gameView, 1);
    
    
    Display dis = getWindowManager().getDefaultDisplay();
    int height = dis.getHeight();
    int width = dis.getWidth();
    
    int rowHeight = Integer.parseInt(getString(R.string.row_height));
    int columnWidth = Integer.parseInt(getString(R.string.column_width));

    int noOfRows = (int)(height/rowHeight);
    int noOfColumns = (int)(width/columnWidth);
   
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
    _gameView.initialize();
    
  }

  public AlertDialog getAlertDialog(String comment) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage(comment).setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int id) {
        startActivity(new Intent("com.yellowmango.bindu.GameActivity"));
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
