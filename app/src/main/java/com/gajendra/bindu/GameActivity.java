package com.gajendra.bindu;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
          showFancyWinnerDialog(playerOne, playerTwo, comments);
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

  private void showFancyWinnerDialog(int jerryScore, int tomScore, String winnerMessage) {
    // Create custom dialog
    final Dialog dialog = new Dialog(this);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialog.setCancelable(false);

    // Inflate custom layout
    LayoutInflater inflater = getLayoutInflater();
    View dialogView = inflater.inflate(R.layout.winner_dialog, null);
    dialog.setContentView(dialogView);

    // Get views
    TextView trophyIcon = dialogView.findViewById(R.id.trophyIcon);
    TextView winnerTitle = dialogView.findViewById(R.id.winnerTitle);
    TextView winnerName = dialogView.findViewById(R.id.winnerName);
    TextView playerScoreText = dialogView.findViewById(R.id.playerScore);
    TextView computerScoreText = dialogView.findViewById(R.id.computerScore);
    View scoreContainer = dialogView.findViewById(R.id.scoreContainer);
    View buttonContainer = dialogView.findViewById(R.id.buttonContainer);
    TextView playAgainButton = dialogView.findViewById(R.id.playAgainButton);
    TextView exitButton = dialogView.findViewById(R.id.exitButton);

    // Set scores
    playerScoreText.setText("You: " + jerryScore);
    computerScoreText.setText("Computer: " + tomScore);

    // Determine winner and set colors
    if (jerryScore > tomScore) {
      winnerName.setText("YOU WIN!");
      winnerName.setTextColor(0xFF4317D5);
      winnerTitle.setVisibility(View.GONE);
    } else if (tomScore > jerryScore) {
      winnerName.setText("COMPUTER WINS!");
      winnerName.setTextColor(0xFFFD9B16);
      winnerTitle.setVisibility(View.GONE);
    } else {
      winnerTitle.setText("DRAW!");
      winnerName.setText("");
    }

    // Set up animations
    Animation trophyAnim = AnimationUtils.loadAnimation(this, R.anim.trophy_bounce);
    Animation titleAnim = AnimationUtils.loadAnimation(this, R.anim.winner_title_anim);
    Animation nameAnim = AnimationUtils.loadAnimation(this, R.anim.winner_name_anim);
    Animation scoreAnim = AnimationUtils.loadAnimation(this, R.anim.score_fade_in);
    Animation buttonsAnim = AnimationUtils.loadAnimation(this, R.anim.buttons_fade_in);

    // Set initial alpha to 0 for animation effect, then start animations
    trophyIcon.setAlpha(0f);
    winnerTitle.setAlpha(0f);
    winnerName.setAlpha(0f);
    scoreContainer.setAlpha(0f);
    buttonContainer.setAlpha(0f);

    // Add animation listeners to ensure views stay visible after animation
    buttonsAnim.setAnimationListener(new Animation.AnimationListener() {
      @Override
      public void onAnimationStart(Animation animation) {}

      @Override
      public void onAnimationEnd(Animation animation) {
        trophyIcon.setAlpha(1f);
        winnerTitle.setAlpha(1f);
        winnerName.setAlpha(1f);
        scoreContainer.setAlpha(1f);
        buttonContainer.setAlpha(1f);
      }

      @Override
      public void onAnimationRepeat(Animation animation) {}
    });

    trophyIcon.startAnimation(trophyAnim);
    winnerTitle.startAnimation(titleAnim);
    winnerName.startAnimation(nameAnim);
    scoreContainer.startAnimation(scoreAnim);
    buttonContainer.startAnimation(buttonsAnim);

    // Button click listeners
    playAgainButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dialog.dismiss();
        startActivity(new Intent(GameActivity.this, GameActivity.class));
        finish();
      }
    });

    exitButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dialog.dismiss();
        finish();
      }
    });

    // Make dialog background transparent
    if (dialog.getWindow() != null) {
      dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    dialog.show();
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
