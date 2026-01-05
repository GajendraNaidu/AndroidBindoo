package com.gajendra.bindu;

import java.util.ArrayList;
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
  private GameView _gameView = null;
  private Dialog _winnerDialog = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    LinearLayout layout =  new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);

    AddingGameView(layout, savedInstanceState);

    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    setContentView(layout);
  }

  private void AddingGameView(final LinearLayout layout, Bundle savedInstanceState) {
    _gameState = new GameState();
    _gameView = new GameView(this, _gameState, new Handler() {
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

    // Restore saved state if available
    if (savedInstanceState != null) {
      restoreGameState(savedInstanceState);
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
    // Dismiss existing dialog if any
    if (_winnerDialog != null && _winnerDialog.isShowing()) {
      _winnerDialog.dismiss();
    }

    // Create custom dialog
    _winnerDialog = new Dialog(this);
    _winnerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    _winnerDialog.setCancelable(false);

    // Inflate custom layout
    LayoutInflater inflater = getLayoutInflater();
    View dialogView = inflater.inflate(R.layout.winner_dialog, null);
    _winnerDialog.setContentView(dialogView);

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
        _winnerDialog.dismiss();
        startActivity(new Intent(GameActivity.this, GameActivity.class));
        finish();
      }
    });

    exitButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        _winnerDialog.dismiss();
        finish();
      }
    });

    // Make dialog background transparent
    if (_winnerDialog.getWindow() != null) {
      _winnerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    _winnerDialog.show();
  }
  
  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    if (_gameState != null) {
      android.util.Log.d("GameActivity", "Saving game state");

      // Save grid dimensions
      outState.putInt("numberOfRows", _gameState.getNumberOfRows());
      outState.putInt("numberOfColumns", _gameState.getNumberOfColumns());

      // Save game state flags
      outState.putBoolean("isComputerNextToPlay", _gameState.isComputerNextToPlay);
      outState.putBoolean("isGameOver", _gameState.isGameOver);
      outState.putInt("statusMessage", _gameState.statusMessage);

      // Save lines state
      ArrayList<GameLine> drawnLines = new ArrayList<>();
      if (_gameState.ConnectedLines != null) {
        for (GameLine line : _gameState.ConnectedLines) {
          if (line.isLineDrawn) {
            drawnLines.add(line);
          }
        }
        outState.putSerializable("drawnLines", drawnLines);
      }

      // Save fills state
      if (_gameState.GameFills != null && _gameState.GameFills.size() > 0) {
        outState.putSerializable("gameFills", new ArrayList<>(_gameState.GameFills));
      }

      // Save last computer line
      if (_gameState.lastComputerLine != null) {
        outState.putSerializable("lastComputerLine", _gameState.lastComputerLine);
      }

      // Save whether the winner dialog was showing
      outState.putBoolean("wasDialogShowing", _winnerDialog != null && _winnerDialog.isShowing());

      android.util.Log.d("GameActivity", "Game state saved: " + drawnLines.size() + " lines, " + _gameState.GameFills.size() + " fills");
    }
  }

  @SuppressWarnings("unchecked")
  private void restoreGameState(Bundle savedState) {
    android.util.Log.d("GameActivity", "Restoring game state");

    // Restore game state flags
    _gameState.isComputerNextToPlay = savedState.getBoolean("isComputerNextToPlay", false);
    _gameState.isGameOver = savedState.getBoolean("isGameOver", false);
    _gameState.statusMessage = savedState.getInt("statusMessage", 0);

    // Restore drawn lines
    ArrayList<GameLine> drawnLines = (ArrayList<GameLine>) savedState.getSerializable("drawnLines");
    if (drawnLines != null && _gameState.ConnectedLines != null) {
      for (GameLine savedLine : drawnLines) {
        for (GameLine line : _gameState.ConnectedLines) {
          if (line.equals(savedLine)) {
            line.SetLineDrawn(true, savedLine.isComputerDrawn);
            break;
          }
        }
      }
    }

    // Restore completed fills
    ArrayList<GameFill> savedFills = (ArrayList<GameFill>) savedState.getSerializable("gameFills");
    if (savedFills != null) {
      _gameState.GameFills.clear();
      _gameState.GameFills.addAll(savedFills);
    }

    // Restore last computer line
    GameLine savedLastComputerLine = (GameLine) savedState.getSerializable("lastComputerLine");
    if (savedLastComputerLine != null && _gameState.ConnectedLines != null) {
      for (GameLine line : _gameState.ConnectedLines) {
        if (line.equals(savedLastComputerLine)) {
          _gameState.lastComputerLine = line;
          break;
        }
      }
    }

    android.util.Log.d("GameActivity", "Game state restored: " + (drawnLines != null ? drawnLines.size() : 0) + " lines, " + _gameState.GameFills.size() + " fills");

    // Re-show winner dialog if it was showing before rotation
    boolean wasDialogShowing = savedState.getBoolean("wasDialogShowing", false);
    if (wasDialogShowing && _gameState.isGameOver) {
      // Calculate scores from GameFills
      int playerScore = 0;
      int computerScore = 0;
      for (GameFill fill : _gameState.GameFills) {
        if (fill.isComputerFill) {
          computerScore++;
        } else {
          playerScore++;
        }
      }

      // Re-show the dialog
      showFancyWinnerDialog(playerScore, computerScore, getString(_gameState.statusMessage));
    }
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

      // Dismiss dialog if showing to prevent window leak
      if (_winnerDialog != null && _winnerDialog.isShowing()) {
        _winnerDialog.dismiss();
      }

      if (_gameState != null) {
        _gameState = null;
      }
    } catch (Exception ex) {
      // FIXME: Just ignore or lets see what we should do
    }
  }
}
