package com.gajendra.bindu;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;

public class GameState {
  public List<GamePoint> Points          = null;//new ArrayList<GamePoint>();
  public Color           playerOneSymbol;
  public Color           playerTwoSymbol;
  private int             NumberOfRows;
  private int             NumberOfColumns;
  public List<GameFill>  GameFills       = null;//new ArrayList<GameFill>();
  public boolean         isComputerNextToPlay;
  public List<GameLine>  ConnectedLines  = null;//new ArrayList<GameLine>();
  public boolean         PlayWithFriend;
  public boolean         isGameOver      = false;
  public int             statusMessage   = 0;
  public GamePoint       pointSelected = null;
  public List<GameFill> strayFills = null;
  public GameLine        lastComputerLine = null;  // Track last computer move

  public GameState() {
    Points          = new ArrayList<GamePoint>();
    GameFills       = new ArrayList<GameFill>();
    ConnectedLines  = new ArrayList<GameLine>();
    int             statusMessage   = 0;
    isGameOver      = false;
    pointSelected = null;
    strayFills = new ArrayList<GameFill>();
  }
  
  public void AddPoint(GamePoint _gamePoint) {
    Points.add(_gamePoint);
  }

  public void AddFill(GameFill gameFill) {
    GameFills.add(gameFill);
  }

  public boolean PlayerTwoDrawFirst;

  public void AddInitialLines(List<GameLine> gameLines, List<GameFill> strayFills) {
   this.ConnectedLines = gameLines;
    this.strayFills = strayFills;
  }

  public void setNumberOfRows(int numberOfRows) {
    NumberOfRows = numberOfRows;
  }

  public int getNumberOfRows() {
    return NumberOfRows;
  }

  public void setNumberOfColumns(int numberOfColumns) {
    NumberOfColumns = numberOfColumns;
  }

  public int getNumberOfColumns() {
    return NumberOfColumns;
  }

  public List<GameFill> getStrayFills() {
    return strayFills;
  }
}