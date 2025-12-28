package com.gajendra.bindu;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameUtils {
  public static GamePoint GetNearstGamePoint(GameState state, double XPosition, double YPosition) {
    double minDistance = Integer.MAX_VALUE;
    double tempDouble = 0;
    GamePoint point = null;

    for (GamePoint item : state.Points) {
      tempDouble = getDistance(item.X, item.Y, XPosition, YPosition);
      if (tempDouble < minDistance) {
        minDistance = tempDouble;
        point = item;
      }
    }
    return point;

  }

  public static double getDistance(double x1, double y1, double x2, double y2) {
    double distance = 0;
    double x = (x2 - x1);
    double y = (y2 - y1);
    distance = Math.sqrt((x * x) + (y * y));
    return distance;
  }

  public static boolean CanDrawLine(GamePoint oldPoint, GamePoint newPoint) {
    if ((oldPoint.X == newPoint.X && ((Math.abs(oldPoint.Y - newPoint.Y) == 1))) || (oldPoint.Y == newPoint.Y && ((Math.abs(oldPoint.X - newPoint.X) == 1)))) {
      return true;
    }
    return false;
  }

  public static List<GameFill> GetTempGameFills(int rows, int columns) {
    List<GameFill> gameFills = new ArrayList<GameFill>();
    for (int i = 0; i < (columns-1); i++) {
      for (int j = 0; j < (rows-1); j++) {
        GamePoint gp1 = new GamePoint(i, j);
        GamePoint gp2 = new GamePoint(i + 1, j);
        GamePoint gp3 = new GamePoint(i, j + 1);
        GamePoint gp4 = new GamePoint(i + 1, j + 1);
        GameFill gameFill = new GameFill(gp1, gp2, gp3, gp4);
        if(!(gameFills.contains(gameFill))) {
          gameFills.add(gameFill);
        }
      }
    }
    return gameFills;
  }
  
  
  
  public static List<GameLine> GetTempGameLines(int rows, int columns) {
    List<GameLine> gameLines = new ArrayList<GameLine>();

    for (int i = 0; i < (columns - 1); i++) {
      for (int j = 0; j < (rows); j++) {
        GamePoint gp1 = new GamePoint(i, j);
        GamePoint gp2 = new GamePoint(i + 1, j);
        GameLine gameFill = new GameLine(gp1, gp2);
        gameLines.add(gameFill);
      }
    }

    for (int j = 0; j < (rows - 1); j++) {
      for (int i = 0; i < (columns); i++) {
        GamePoint gp1 = new GamePoint(i, j);
        GamePoint gp2 = new GamePoint(i, j + 1);
        GameLine gameFill = new GameLine(gp1, gp2);
        gameLines.add(gameFill);
      }
    }

    return gameLines;
  }

  /*
   * Return GameFill where fill can be drawn otherwise return null
   */
  public static GameFill IsNewFillExist(GameState gameState) {
    List<GameFill> GetGameFills = gameState.getStrayFills();

    // Loop through all the points
    for (GameFill gf : GetGameFills) {
      boolean isLeftLineExist = checkIfLineExistWithCoords(gf.BottomLeft, gf.TopLeft, gameState);
      boolean isTopLineExist = checkIfLineExistWithCoords(gf.TopLeft, gf.TopRight, gameState);
      boolean isRightLineExist = checkIfLineExistWithCoords(gf.BottomRight, gf.TopRight, gameState);
      boolean isBottomLineExist = checkIfLineExistWithCoords(gf.BottomLeft, gf.BottomRight, gameState);

      if (isLeftLineExist && isTopLineExist && isRightLineExist && isBottomLineExist) {
        boolean isNew = checkIfThisIsNewFill(gf, gameState);
        if (isNew && !gf.isAddedToGameState) {
          return gf;
        }
      }
    }
    return null;
  }

  private static boolean checkIfThisIsNewFill(GameFill newFill, GameState gameState) {
    for (GameFill gf : gameState.GameFills) {
      if (gf.Equals(newFill)) {
        return false;
      }
    }
    return true;
  }

  /*
   * Return GameFill where fill can be drawn otherwise return null
   */
  public static GameLine CanDrawFillWithThisLine(GameState gameState) {

    List<GameFill> GetGameFills =  gameState.getStrayFills();

    // Loop through all the points
    for (GameFill gf : GetGameFills) {
      boolean isLeftLineExist = checkIfLineExistWithCoords(gf.BottomLeft, gf.TopLeft, gameState);
      boolean isTopLineExist = checkIfLineExistWithCoords(gf.TopLeft, gf.TopRight, gameState);
      boolean isRightLineExist = checkIfLineExistWithCoords(gf.BottomRight, gf.TopRight, gameState);
      boolean isBottomLineExist = checkIfLineExistWithCoords(gf.BottomLeft, gf.BottomRight, gameState);

      if (isLeftLineExist && isTopLineExist && isRightLineExist && isBottomLineExist) {
        continue;
      } else if (isLeftLineExist && isTopLineExist && isRightLineExist && !isBottomLineExist) {
        return new GameLine(gf.BottomLeft, gf.BottomRight);
      } else if (isLeftLineExist && isTopLineExist && !isRightLineExist && isBottomLineExist) {
        return new GameLine(gf.BottomRight, gf.TopRight);
      } else if (isLeftLineExist && !isTopLineExist && isRightLineExist && isBottomLineExist) {
        return new GameLine(gf.TopLeft, gf.TopRight);
      } else if (!isLeftLineExist && isTopLineExist && isRightLineExist && isBottomLineExist) {
        return new GameLine(gf.BottomLeft, gf.TopLeft);
      }
    }
    return null;
  }

  private static boolean checkIfLineExistWithCoords(GamePoint pointA, GamePoint pointB, GameState gameState) {
    GameLine newLine = new GameLine(pointA, pointB);
    for (GameLine line : gameState.ConnectedLines) {
      if (line.equals(newLine)) {
        if ((line.isLineDrawn)) {
          return true;
        }
      }
    }
    return false;
  }

  public static GameLine GetGameLineWithThisLine(GameLine newLine, GameState gameState) {

    for (GameLine line : gameState.ConnectedLines) {
      if (line.equals(newLine)) {
        return line;
      }
    }
    return null;
  }

  public static GameLine GuesssALine(GameState gameState) {

    List<GameLine> gameLines = gameState.ConnectedLines;
    List<GameLine> tempLines = new ArrayList<GameLine>();

    // Check once if there's a line that completes a box (moved outside loop for performance)
    GameLine tempGameLine = CanDrawFillWithThisLine(gameState);

    for (GameLine item : gameLines) {
      if (!item.isLineDrawn) {
        if(tempGameLine != null && item.equals(tempGameLine)) {
          return item;
        }else {
          tempLines.add(item);
        }
      }
    }
    int ramdomXPoint1 = GuessRandomNumber(0, tempLines.size());
    return tempLines.get(ramdomXPoint1);
  }

  private static int GuessRandomNumber(int min, int max) {
    Random random = new Random();
    return random.nextInt(max);
  }



  public static boolean isGameFinished(GameState gameState) {
    List<GameFill> TempGameFills = gameState.getStrayFills();// GetTempGameFills(gameState.getNumberOfRows(), gameState.getNumberOfColumns());
    List<GameFill> ActualFills = gameState.GameFills;
    if (TempGameFills.size() == ActualFills.size()) {
      return true;
    } else {
      return false;
    }

  }

  public static GameScore GetGameScrore(GameState gameState) {
    List<GameFill> TempGameFills = gameState.getStrayFills();
    int playerOneFills = 0;
    int playerTwoFills = 0;

    for (GameFill fill : gameState.GameFills) {
      if (fill.isComputerFill) {
        playerTwoFills++;
      } else {
        playerOneFills++;
      }
    }
    
    gameState.isGameOver = ((playerTwoFills+playerOneFills)==TempGameFills.size());
    GameScore score = new GameScore(TempGameFills.size(), playerOneFills, playerTwoFills);
    return score;
  }
  
  public static List<GamePoint> GetHighlightedGamePoint(GamePoint pointSelected, GameState gameState) {
    List<GamePoint> gamePoints = new ArrayList<GamePoint>(4);
    List<GamePoint> gamePointsA = new ArrayList<GamePoint>(4);
      int x1 = pointSelected.X-1;
      int y1 = pointSelected.Y;
      GamePoint tempPoint1 = GetGamePoint(gameState, x1, y1);
      if(tempPoint1 != null) {
        gamePoints.add(tempPoint1);
      }

      int x2 = pointSelected.X+1;
      int y2 = pointSelected.Y;
      GamePoint tempPoint2 = GetGamePoint(gameState, x2, y2);
      if(tempPoint2 != null) {
        gamePoints.add(tempPoint2);
      }
      
      int x3 = pointSelected.X;
      int y3 = pointSelected.Y-1;
      GamePoint tempPoint3 = GetGamePoint(gameState, x3, y3);
      if(tempPoint3 != null) {
        gamePoints.add(tempPoint3);
      }

      int x4 = pointSelected.X;
      int y4 = pointSelected.Y+1;
      GamePoint tempPoint4 = GetGamePoint(gameState, x4, y4);
      if(tempPoint4 != null) {
        gamePoints.add(tempPoint4);
      }
      
      
      for (GamePoint gamePoint : gamePoints) {
        boolean isLineExist = checkIfLineExistWithCoords(pointSelected, gamePoint, gameState);
        if(!isLineExist) {
          gamePointsA.add(gamePoint);
        }
      }
      
    return gamePointsA;
  }

  private static GamePoint GetGamePoint(GameState gameState,int x1, int y1) {
    if( x1>=0 && x1<gameState.getNumberOfColumns() && y1 >= 0 && y1 < gameState.getNumberOfRows()){
      return new GamePoint(x1, y1);
    }
    return null;
  }
}
