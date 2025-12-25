package com.yellowmango.bindu;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

  private GameState    _gameState;
  private float        singleColumnWidth     = 0;
  private float        singleColumnHeight    = 0;
  private int          margin                = 13;
  private Handler      handler;
  private GamePoint    previouslyClickGamePoint;
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

  public void initialize() {
    Log.d("GameView", "initialize() called - loading bitmaps");
    me = BitmapFactory.decodeResource(getResources(), R.drawable.me);
    you = BitmapFactory.decodeResource(getResources(), R.drawable.you);
    highlight = BitmapFactory.decodeResource(getResources(), R.drawable.highlight);
    highlighta = BitmapFactory.decodeResource(getResources(), R.drawable.highlighta);
    bindudr = BitmapFactory.decodeResource(getResources(), R.drawable.bindudr);
    binduldr = BitmapFactory.decodeResource(getResources(), R.drawable.binduldr);
    binduld = BitmapFactory.decodeResource(getResources(), R.drawable.binduld);
    bindutr = BitmapFactory.decodeResource(getResources(), R.drawable.bindutr);
    bindultr = BitmapFactory.decodeResource(getResources(), R.drawable.bindultr);
    bindult = BitmapFactory.decodeResource(getResources(), R.drawable.bindult);
    bindudrt = BitmapFactory.decodeResource(getResources(), R.drawable.bindudrt);
    binduldrt = BitmapFactory.decodeResource(getResources(), R.drawable.binduldrt);
    bindudlt = BitmapFactory.decodeResource(getResources(), R.drawable.bindudlt);
    Log.d("GameView", "Bitmaps loaded, creating thread");
    _thread = new PaintThread(getHolder());
    _gameState.statusMessage = R.string.player_two_turn;
    updateScore(_gameState);
    Log.d("GameView", "initialize() complete - Points: " + _gameState.Points.size());
  }

  @Override
  public boolean onTouchEvent(MotionEvent mv) {
    try {
      if (mv.getAction() == MotionEvent.ACTION_DOWN) {
        float xClick = mv.getX();
        float yClick = mv.getY();
        GamePoint point = getGamePoint(xClick, yClick);
        if (point != null && previouslyClickGamePoint == null) {
          previouslyClickGamePoint = point;
          _gameState.pointSelected = previouslyClickGamePoint;
          point = null;
        }

        if (point != null && previouslyClickGamePoint != null) {
          GameLine gameLine = new GameLine(previouslyClickGamePoint, point);
          boolean canLineDrawn = GameUtils.CanDrawLine(previouslyClickGamePoint, point);
          if (canLineDrawn) {
            GameLine lineToBeDrawn = GameUtils.GetGameLineWithThisLine(gameLine, _gameState);
            if (lineToBeDrawn != null) {
              lineToBeDrawn.SetLineDrawn(true, false);
              boolean isFillDrawn = drawFillIfExist(false);
              if (!isFillDrawn) {
                ComputerToPlay();
              }
            }
          }
          _gameState.pointSelected = null;
          previouslyClickGamePoint = null;
        }

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
    }
    boolean isFillDrawn = drawFillIfExist(true);
    if (isFillDrawn) {
      ComputerToPlay();
    } else {
      _gameState.statusMessage = R.string.player_two_turn;
      return;
    }
  }

  @Override
  public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
    Log.d("GameView", "surfaceChanged: format=" + arg1 + " width=" + arg2 + " height=" + arg3);
    singleColumnWidth = (arg2 - (2 * margin)) / (float) _gameState.getNumberOfColumns();
    singleColumnHeight = (arg3 - (2 * margin)) / (float) _gameState.getNumberOfRows();
    Log.d("GameView", "Cell size: " + singleColumnWidth + "x" + singleColumnHeight);
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder arg0) {
    boolean retry = true;
    _thread.state = PaintThread.PAUSED;
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

      if (_gameState.Points != null) {
        for (GamePoint point : _gameState.Points) {
          drawPoint(canvas, point);
        }
      }
      if (_gameState.ConnectedLines != null) {
        for (GameLine line : _gameState.ConnectedLines) {
          if (line.isLineDrawn) {
            drawLine(canvas, line);
          }
        }
      }

      if (_gameState.GameFills != null) {
        for (GameFill gameFill : _gameState.GameFills) {
          if (gameFill.isAddedToGameState) {
            drawFill(canvas, gameFill);
          }
        }
      }

      if(_gameState.isGameOver) {
        _thread.state = PaintThread.PAUSED;
      }else {
        if(previouslyClickGamePoint != null) {
          drawHighlight(canvas, previouslyClickGamePoint);
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
    canvas.drawBitmap(highlight, (x-20), (y-20), paint);
    
    List<GamePoint> anotherJoinPoints = GameUtils.GetHighlightedGamePoint(point, _gameState);
    for (GamePoint gamePoint : anotherJoinPoints) {
      x = getLeft(gamePoint.X);
      y = getTop(gamePoint.Y);
      canvas.drawBitmap(highlighta, (x-20), (y-20), paint);
    }
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

    if (line.isComputerDrawn) {
      paint1.setColor(Color.rgb(253, 155, 22));  // Orange for computer
    } else {
      paint1.setColor(Color.rgb(67, 23, 213));   // Blue for player
    }
    paint1.setStrokeWidth(8);  // Much thicker lines for visibility
    
    if(line.PointOne.X == line.PointTwo.X) {
      canvas.drawLine(getLeft(line.PointOne.X), getTop(line.PointOne.Y)+11, getLeft(line.PointTwo.X), getTop(line.PointTwo.Y)-9, paint1);
    } else {
      canvas.drawLine(getLeft(line.PointOne.X)+11, getTop(line.PointOne.Y), getLeft(line.PointTwo.X)-9, getTop(line.PointTwo.Y), paint1);
    }
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

    int x = getLeft(point.X)-20;
    int y = getTop(point.Y)-20;
    Paint paint1 = new Paint();
    canvas.drawBitmap(bindoo, x, y, paint1);
  }

  private void drawFill(Canvas canvas, GameFill fill) {
    int x = getLeft(fill.TopLeft.X) + 2;
    int y = getTop(fill.TopLeft.Y) + 2;
    if(singleColumnWidth >= 100) {
      x =  x + (int)(singleColumnWidth - (100*0.9))/2;
    }
    if(singleColumnHeight >= 66) {
      y =  y + (int)(singleColumnHeight - (66*0.9))/2;
    }
    Paint paint = new Paint();
    if (fill.isComputerFill) {
      canvas.drawBitmap(you, x, y, paint);
    } else {
      canvas.drawBitmap(me, x, y, paint);
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
    return (int) ((Y * singleColumnHeight) + margin);
  }

  public int getLeft(int X) {
    return (int) ((X * singleColumnWidth) + margin);
  }

  public GamePoint getGamePoint(float x, float y) {
    for (GamePoint point : _gameState.Points) {
      float pointX = getLeft(point.X);
      float pointY = getTop(point.Y);

      if (Math.abs(x - pointX) <= 50 && Math.abs(y - pointY) <= 50) {
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
