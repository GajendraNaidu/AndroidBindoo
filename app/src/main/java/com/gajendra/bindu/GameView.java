package com.gajendra.bindu;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import android.graphics.drawable.Drawable;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

  private GameState    _gameState;
  private float        singleColumnWidth     = 0;
  private float        singleColumnHeight    = 0;
  private int          marginTop             = 80;  // Extra top margin
  private int          marginRight           = 80;  // Extra right margin
  private int          marginBottom          = 80;  // Bottom margin
  private int          marginLeft            = 80;  // Left margin
  private Handler      handler;
  private GamePoint    previouslyClickGamePoint;
  private GamePoint    dragStartPoint;
  private float        dragCurrentX;
  private float        dragCurrentY;
  private boolean      isDragging            = false;
  private PaintThread  _thread;
  private Bitmap me = null;
  private Bitmap you = null;
  private Bitmap highlight = null;
  private Bitmap highlighta = null;
  private Bitmap bindudr = null;
  private Bitmap binduldr = null;
  private Bitmap binduld = null;
  private Bitmap bindutr = null;
  private Bitmap bindultr = null;
  private Bitmap bindult = null;
  private Bitmap bindudrt = null;
  private Bitmap binduldrt = null;
  private Bitmap bindudlt = null;
  private int frameCount = 0;
  private long computerLineDrawTime = 0;  // Track when computer drew last line
  private Handler computerMoveHandler = new Handler();  // Handler for delayed computer moves

  public GameView(Context context) {
    super(context);
    getHolder().addCallback(this);
  }

  public GameView(Context context, GameState gameState, Handler handler) {
    super(context);
    getHolder().addCallback(this);
    this._gameState = gameState;
    this.handler = handler;
  }

  private Bitmap getBitmapFromVectorDrawable(int drawableId) {
    Drawable drawable = ContextCompat.getDrawable(getContext(), drawableId);
    if (drawable == null) return null;

    Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
        drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    drawable.draw(canvas);
    return bitmap;
  }

  public void initialize() {
    Log.d("GameView", "initialize() called - loading vector drawables");
    me = getBitmapFromVectorDrawable(R.drawable.player_avatar);
    you = getBitmapFromVectorDrawable(R.drawable.computer_avatar);
    highlight = BitmapFactory.decodeResource(getResources(), R.drawable.highlight);
    highlighta = BitmapFactory.decodeResource(getResources(), R.drawable.highlighta);
    bindudr = getBitmapFromVectorDrawable(R.drawable.bindudr);
    binduldr = getBitmapFromVectorDrawable(R.drawable.binduldr);
    binduld = getBitmapFromVectorDrawable(R.drawable.binduld);
    bindutr = getBitmapFromVectorDrawable(R.drawable.bindutr);
    bindultr = getBitmapFromVectorDrawable(R.drawable.bindultr);
    bindult = getBitmapFromVectorDrawable(R.drawable.bindult);
    bindudrt = getBitmapFromVectorDrawable(R.drawable.bindudrt);
    binduldrt = getBitmapFromVectorDrawable(R.drawable.binduldrt);
    bindudlt = getBitmapFromVectorDrawable(R.drawable.bindudlt);
    Log.d("GameView", "Vector drawables loaded, creating thread");
    _thread = new PaintThread(getHolder());
    _gameState.statusMessage = R.string.player_two_turn;
    updateScore(_gameState);
    Log.d("GameView", "initialize() complete - Points: " + _gameState.Points.size());
  }

  @Override
  public boolean onTouchEvent(MotionEvent mv) {
    try {
      switch (mv.getAction()) {
        case MotionEvent.ACTION_DOWN:
          // Start dragging from a dot
          float xDown = mv.getX();
          float yDown = mv.getY();
          GamePoint startPoint = getGamePoint(xDown, yDown);
          if (startPoint != null) {
            dragStartPoint = startPoint;
            dragCurrentX = xDown;
            dragCurrentY = yDown;
            isDragging = true;
            _gameState.pointSelected = dragStartPoint;
          }
          break;

        case MotionEvent.ACTION_MOVE:
          // Update drag position for preview line
          if (isDragging && dragStartPoint != null) {
            dragCurrentX = mv.getX();
            dragCurrentY = mv.getY();
          }
          break;

        case MotionEvent.ACTION_UP:
          // Complete the drag - try to draw line to end point
          if (isDragging && dragStartPoint != null) {
            float xUp = mv.getX();
            float yUp = mv.getY();
            GamePoint endPoint = getGamePoint(xUp, yUp);

            if (endPoint != null && !endPoint.equals(dragStartPoint)) {
              GameLine gameLine = new GameLine(dragStartPoint, endPoint);
              boolean canLineDrawn = GameUtils.CanDrawLine(dragStartPoint, endPoint);
              if (canLineDrawn) {
                GameLine lineToBeDrawn = GameUtils.GetGameLineWithThisLine(gameLine, _gameState);
                // Only draw if line exists and is not already drawn
                if (lineToBeDrawn != null && !lineToBeDrawn.isLineDrawn) {
                  // Cancel any pending computer moves
                  computerMoveHandler.removeCallbacksAndMessages(null);
                  _gameState.lastComputerLine = null;  // Clear highlight when player moves
                  lineToBeDrawn.SetLineDrawn(true, false);
                  boolean isFillDrawn = drawFillIfExist(false);
                  if (!isFillDrawn) {
                    // Add small delay before computer's first move
                    computerMoveHandler.postDelayed(new Runnable() {
                      @Override
                      public void run() {
                        ComputerToPlay();
                      }
                    }, 600);  // 600ms delay before computer's first move
                  }
                }
              }
            }

            // Reset drag state
            isDragging = false;
            dragStartPoint = null;
            _gameState.pointSelected = null;
            previouslyClickGamePoint = null;
          }
          break;

        case MotionEvent.ACTION_CANCEL:
          // Cancel drag
          isDragging = false;
          dragStartPoint = null;
          _gameState.pointSelected = null;
          previouslyClickGamePoint = null;
          break;
      }
      return true;
    } catch (Exception e) {
      return true;
    }
  }

  private boolean drawFillIfExist(boolean computerPlayed) {
    boolean isGameFilled = false;
    GameFill gameFill = GameUtils.IsNewFillExist(_gameState);
    while (gameFill != null) {
      gameFill.isComputerFill = computerPlayed;
      gameFill.isAddedToGameState = true;

      // Calculate and assign score number for this player
      int currentScore = 0;
      for (GameFill fill : _gameState.GameFills) {
        if (fill.isComputerFill == computerPlayed) {
          currentScore++;
        }
      }
      gameFill.scoreNumber = currentScore + 1;
      Log.d("GameView", "Assigned scoreNumber " + gameFill.scoreNumber + " to " + (computerPlayed ? "computer" : "player") + " fill");

      _gameState.AddFill(gameFill);
      isGameFilled = true;
      gameFill = GameUtils.IsNewFillExist(_gameState);
    }
    if (isGameFilled) {
      updateScore(_gameState);
    }
    return isGameFilled;
  }

  public void ComputerToPlay() {
    _gameState.statusMessage = R.string.player_one_turn;
    GameLine guessLine = GameUtils.GuesssALine(_gameState);
    if (guessLine != null) {
      guessLine.SetLineDrawn(true, true);
      _gameState.lastComputerLine = guessLine;  // Track last computer move
      computerLineDrawTime = System.currentTimeMillis();  // Record draw time for animation
    }
    boolean isFillDrawn = drawFillIfExist(true);
    if (isFillDrawn) {
      // Add delay before computer's next move to make it feel more natural
      computerMoveHandler.postDelayed(new Runnable() {
        @Override
        public void run() {
          ComputerToPlay();
        }
      }, 800);  // 800ms delay between computer moves
    } else {
      _gameState.statusMessage = R.string.player_two_turn;
      return;
    }
  }

  @Override
  public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
    Log.d("GameView", "surfaceChanged: format=" + arg1 + " width=" + arg2 + " height=" + arg3);
    // Calculate spacing between dots (divide by n-1 for proper grid spacing)
    int cols = _gameState.getNumberOfColumns();
    int rows = _gameState.getNumberOfRows();
    singleColumnWidth = (arg2 - marginLeft - marginRight) / (float) (cols - 1);
    singleColumnHeight = (arg3 - marginTop - marginBottom) / (float) (rows - 1);
    Log.d("GameView", "Dot spacing: " + singleColumnWidth + "x" + singleColumnHeight + " for " + cols + "x" + rows + " grid");
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder arg0) {
    boolean retry = true;
    _thread.state = PaintThread.PAUSED;
    // Cancel any pending computer moves
    computerMoveHandler.removeCallbacksAndMessages(null);
    while (retry) {
      try {
        _thread.join();
        retry = false;
      } catch (InterruptedException e) {
      }
    }
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    Log.d("GameView", "surfaceCreated called");
    if (_thread != null) {
      if (_thread.state == PaintThread.PAUSED) {
        _thread = new PaintThread(getHolder());
        _thread.start();
        Log.d("GameView", "Thread started (was paused)");
      } else {
        if(!(_thread.isAlive())){
        _thread.start();
        Log.d("GameView", "Thread started");
        }
      }
    } else {
      Log.e("GameView", "Thread is null!");
    }
  }

  protected void doDraw(Canvas canvas) {
    try {
      if (canvas == null) {
        Log.e("GameView", "Canvas is null in doDraw");
        return;
      }

      canvas.drawColor(Color.BLACK);

      // Draw edge border
      drawEdgeBorder(canvas);

      // Draw lines first (behind)
      if (_gameState.ConnectedLines != null) {
        for (GameLine line : _gameState.ConnectedLines) {
          if (line.isLineDrawn) {
            drawLine(canvas, line);
          }
        }
      }

      // Draw dots on top of lines
      if (_gameState.Points != null) {
        for (GamePoint point : _gameState.Points) {
          drawPoint(canvas, point);
        }
      }

      if (_gameState.GameFills != null) {
        for (GameFill gameFill : _gameState.GameFills) {
          if (gameFill.isAddedToGameState) {
            drawFill(canvas, gameFill);
          }
        }
      }

      // Draw preview line while dragging
      if (isDragging && dragStartPoint != null) {
        drawPreviewLine(canvas);
      }

      if(_gameState.isGameOver) {
        _thread.state = PaintThread.PAUSED;
      }else {
        if(dragStartPoint != null) {
          drawHighlight(canvas, dragStartPoint);
        }
      }

    } catch (Exception ex) {
      if(ex != null && ex.getMessage() != null) {
        Log.v("Drawing the Game", ex.getMessage());
      }else {
        Log.v("Drawing the Game", "Some Exception");
      }
    }
  }
  
  public void drawHighlight(Canvas canvas,GamePoint point) {
    int x = getLeft(point.X);
    int y = getTop(point.Y);
    Paint paint = new Paint();
    // Center the highlight bitmap on the dot
    int highlightOffsetX = highlight.getWidth() / 2;
    int highlightOffsetY = highlight.getHeight() / 2;
    canvas.drawBitmap(highlight, (x - highlightOffsetX), (y - highlightOffsetY), paint);

    List<GamePoint> anotherJoinPoints = GameUtils.GetHighlightedGamePoint(point, _gameState);
    for (GamePoint gamePoint : anotherJoinPoints) {
      x = getLeft(gamePoint.X);
      y = getTop(gamePoint.Y);
      int highlightaOffsetX = highlighta.getWidth() / 2;
      int highlightaOffsetY = highlighta.getHeight() / 2;
      canvas.drawBitmap(highlighta, (x - highlightaOffsetX), (y - highlightaOffsetY), paint);
    }
  }

  private void drawPreviewLine(Canvas canvas) {
    if (dragStartPoint == null) return;

    Paint paint = new Paint();
    paint.setAntiAlias(true);
    paint.setStrokeCap(Paint.Cap.ROUND);
    paint.setColor(Color.parseColor("#22C822"));  // Player green color
    paint.setAlpha(128);  // Semi-transparent
    paint.setStrokeWidth(8);

    // Draw line from start dot to current finger position
    canvas.drawLine(
        getLeft(dragStartPoint.X),
        getTop(dragStartPoint.Y),
        dragCurrentX,
        dragCurrentY,
        paint
    );
  }

  private void drawEdgeBorder(Canvas canvas) {
    Paint borderPaint = new Paint();
    borderPaint.setAntiAlias(true);
    borderPaint.setStyle(Paint.Style.STROKE);
    borderPaint.setColor(Color.rgb(80, 80, 80));  // Subtle gray
    borderPaint.setStrokeWidth(3);  // Thin line

    // Draw border at screen edges with small inset
    float inset = 10;  // Small inset from screen edge
    canvas.drawRect(
        inset,
        inset,
        canvas.getWidth() - inset,
        canvas.getHeight() - inset,
        borderPaint
    );
  }


  public void updateScore(GameState gameState) {
    GameScore score = GameUtils.GetGameScrore(gameState);
    Message msg = handler.obtainMessage();
    Bundle b = new Bundle();
    b.putBoolean("0", _gameState.isGameOver);
    b.putInt("1", score.PlayerOneFills);
    b.putInt("2", score.PlayerTwoFills);
    if(_gameState.isGameOver) {
      if(score.PlayerOneFills > score.PlayerTwoFills) {
        gameState.statusMessage = R.string.player_one_game_over;
      }else if(score.PlayerOneFills == score.PlayerTwoFills) {
        gameState.statusMessage = R.string.tie_game_over;
      }else {
        gameState.statusMessage = R.string.player_two_game_over;
      }
    }
    b.putInt("3", gameState.statusMessage);
    msg.setData(b);
    handler.sendMessage(msg);
  }

  private void drawLine(Canvas canvas, GameLine line) {
    Paint paint1 = new Paint();
    paint1.setAntiAlias(true);
    paint1.setStrokeCap(Paint.Cap.ROUND);

    // Check if this is the last computer move - highlight it
    boolean isLastComputerMove = (_gameState.lastComputerLine != null &&
                                   line.equals(_gameState.lastComputerLine));

    if (line.isComputerDrawn) {
      if (isLastComputerMove) {
        // Calculate time since line was drawn (in seconds)
        long timeSinceDrawn = System.currentTimeMillis() - computerLineDrawTime;
        float fadeTime = 3000f;  // Fade over 3 seconds
        float fadeProgress = Math.min(1.0f, timeSinceDrawn / fadeTime);

        // Create pulsing effect using sine wave
        float pulseSpeed = 4.0f;  // Pulses per second
        float pulsePhase = (timeSinceDrawn / 1000.0f) * pulseSpeed * (float)Math.PI * 2;
        float pulseIntensity = (float)Math.sin(pulsePhase) * 0.5f + 0.5f;  // 0 to 1

        // Reduce pulse intensity as it fades
        pulseIntensity *= (1.0f - fadeProgress * 0.7f);

        if (fadeProgress < 1.0f) {
          // Draw outer glow layer (pulsing)
          Paint glowPaint = new Paint();
          glowPaint.setAntiAlias(true);
          glowPaint.setStrokeCap(Paint.Cap.ROUND);
          glowPaint.setColor(Color.rgb(255, 200, 100));  // Bright yellow-orange glow
          glowPaint.setStrokeWidth(18 + pulseIntensity * 6);  // Pulsing width
          int glowAlpha = (int)(150 * (1.0f - fadeProgress) * (0.5f + pulseIntensity * 0.5f));
          glowPaint.setAlpha(glowAlpha);
          canvas.drawLine(getLeft(line.PointOne.X), getTop(line.PointOne.Y),
                         getLeft(line.PointTwo.X), getTop(line.PointTwo.Y), glowPaint);
        }

        paint1.setColor(Color.rgb(255, 200, 50));  // Brighter orange-yellow
        paint1.setStrokeWidth(10);  // Thicker
      } else {
        paint1.setColor(Color.rgb(253, 155, 22));  // Regular orange for computer
        paint1.setStrokeWidth(8);
      }
    } else {
      paint1.setColor(Color.parseColor("#22C822"));   // Player green color
      paint1.setStrokeWidth(8);
    }

    // Draw line from center to center of dots
    canvas.drawLine(getLeft(line.PointOne.X), getTop(line.PointOne.Y),
                    getLeft(line.PointTwo.X), getTop(line.PointTwo.Y), paint1);
  }



  private void drawPoint(Canvas canvas, GamePoint point) {

    Bitmap bindoo = null;
    int xEndPoint = (_gameState.getNumberOfRows() - 1);
    int yEndPoint = (_gameState.getNumberOfColumns() - 1);

    if(point.Y == 0) {
      if(point.X == 0) {
        bindoo = bindudr;
      }else if( (point.X > 0 && point.X < yEndPoint)) {
        bindoo = binduldr;
      }else if( (point.X > 0 && point.X == yEndPoint)) {
        bindoo = binduld;
      }
    }else if(point.Y == xEndPoint ) {
      if(point.X == 0) {
        bindoo = bindutr;
      }else if( (point.X > 0 && point.X < yEndPoint)) {
        bindoo = bindultr;
      }else if( (point.X > 0 && point.X == yEndPoint)) {
        bindoo = bindult;
      }
    }else {
      if(point.X == 0) {
        bindoo = bindudrt;
      }else if( (point.X > 0 && point.X < yEndPoint)) {
        bindoo = binduldrt;
      }else if( (point.X > 0 && point.X == yEndPoint)) {
        bindoo = bindudlt;
      }
    }

    // Center the dot bitmap - use actual bitmap dimensions
    int x = getLeft(point.X) - (bindoo.getWidth() / 2);
    int y = getTop(point.Y) - (bindoo.getHeight() / 2);
    Paint paint1 = new Paint();
    canvas.drawBitmap(bindoo, x, y, paint1);
  }

  private void drawFill(Canvas canvas, GameFill fill) {
    // Get the box boundaries
    int boxLeft = getLeft(fill.TopLeft.X);
    int boxTop = getTop(fill.TopLeft.Y);
    int boxRight = getLeft(fill.BottomRight.X);
    int boxBottom = getTop(fill.BottomRight.Y);

    // Calculate box dimensions
    int boxWidth = boxRight - boxLeft;
    int boxHeight = boxBottom - boxTop;

    // Select the appropriate avatar
    Bitmap avatar = fill.isComputerFill ? you : me;

    // Calculate center position for avatar
    int centerX = boxLeft + boxWidth / 2;
    int centerY = boxTop + boxHeight / 2;
    int x = boxLeft + (boxWidth - avatar.getWidth()) / 2;
    int y = boxTop + (boxHeight - avatar.getHeight()) / 2;

    // Check if animation should be playing (2 seconds = 2000ms)
    long currentTime = System.currentTimeMillis();
    long elapsed = currentTime - fill.animationStartTime;
    boolean isAnimating = elapsed < 2000;

    Paint paint = new Paint();
    paint.setAntiAlias(true);

    if (isAnimating) {
      // Calculate animation progress (0.0 to 1.0)
      float progress = elapsed / 2000f;

      // Apply bounce easing with scale animation
      float scale;
      if (progress < 0.6f) {
        // Scale up from 0 to 1.2 (overshoot) in first 60%
        float scaleProgress = progress / 0.6f;
        scale = scaleProgress * 1.2f;
      } else {
        // Bounce back from 1.2 to 1.0 in last 40%
        float bounceProgress = (progress - 0.6f) / 0.4f;
        scale = 1.2f - (bounceProgress * 0.2f);
      }

      // Save canvas state
      canvas.save();

      // Apply scale transformation around center
      canvas.scale(scale, scale, centerX, centerY);

      // Add slight rotation for extra flair (oscillates)
      float rotation = (float) Math.sin(progress * Math.PI * 2) * 5; // ±5 degrees
      canvas.rotate(rotation, centerX, centerY);

      // Draw avatar with transformation
      canvas.drawBitmap(avatar, x, y, paint);

      // Restore canvas state
      canvas.restore();
    } else {
      // Draw avatar normally (no animation)
      canvas.drawBitmap(avatar, x, y, paint);
    }

    // Draw score number in top-right corner (with more padding from edges)
    // Only draw if scoreNumber is properly set (> 0)
    if (fill.scoreNumber > 0) {
      Paint textPaint = new Paint();
      textPaint.setAntiAlias(true);
      textPaint.setTextSize(36);
      textPaint.setStyle(Paint.Style.FILL);
      textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
      textPaint.setColor(fill.isComputerFill ? Color.parseColor("#FD9B16") : Color.parseColor("#22C822"));
      textPaint.setTextAlign(Paint.Align.RIGHT);

      // Add shadow for better visibility
      textPaint.setShadowLayer(3, 1, 1, Color.BLACK);

      String scoreText = String.valueOf(fill.scoreNumber);
      canvas.drawText(scoreText, boxRight - 12, boxTop + 42, textPaint);
    } else {
      Log.w("GameView", "Fill has scoreNumber = 0, not displaying. isComputerFill: " + fill.isComputerFill);
    }
  }

  class PaintThread extends Thread {

    private SurfaceHolder   surfaceHolder;
    private long            sleepTime;
    public long             delay   = 16;  // ~60 FPS for maximum responsiveness

    int                     state   = 1;
    public final static int RUNNING = 1;
    public final static int PAUSED  = 2;

    public PaintThread(SurfaceHolder surfaceHolder) {
      this.surfaceHolder = surfaceHolder;
    }

    @Override
    public void run() {
      Log.d("GameView", "PaintThread.run() started");
      Canvas c;
      int loopCount = 0;
      while (state == RUNNING) {
        long beforeTime = System.nanoTime();
        c = null;
        try {
          c = this.surfaceHolder.lockCanvas(null);
          if (c == null) {
            if (loopCount < 5) {
              Log.e("GameView", "lockCanvas returned null");
            }
          } else {
            synchronized (this.surfaceHolder) {
              doDraw(c);
            }
          }
        } catch (Exception e) {
          Log.e("GameView", "Exception in paint thread: " + e.getMessage(), e);
        } finally {
          if (c != null) {
            this.surfaceHolder.unlockCanvasAndPost(c);
          }
        }

        this.sleepTime = delay - ((System.nanoTime() - beforeTime) / 1000000L);
        try {
          if (sleepTime > 0) {
            sleep(sleepTime);
          }
        } catch (InterruptedException ex) {
        }
        loopCount++;
      }
      Log.d("GameView", "PaintThread.run() ended after " + loopCount + " loops");
    }

  }

  public int getTop(int Y) {
    return (int) ((Y * singleColumnHeight) + marginTop);
  }

  public int getLeft(int X) {
    return (int) ((X * singleColumnWidth) + marginLeft);
  }

  public GamePoint getGamePoint(float x, float y) {
    for (GamePoint point : _gameState.Points) {
      float pointX = getLeft(point.X);
      float pointY = getTop(point.Y);

      if (Math.abs(x - pointX) <= 80 && Math.abs(y - pointY) <= 80) {
        return point;
      }
    }
    return null;
  }

  public void pause() {
    if(_thread != null) {
      _thread.state = PaintThread.PAUSED;
    }
  }

  public void resume() {
    if(_thread != null) {
      _thread.state = PaintThread.RUNNING;
    }
  }
}
