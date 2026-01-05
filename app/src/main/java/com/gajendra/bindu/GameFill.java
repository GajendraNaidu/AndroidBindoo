package com.gajendra.bindu;

import java.io.Serializable;

public class GameFill implements Serializable {
  private static final long serialVersionUID = 1L;

  public GamePoint TopLeft;
  public GamePoint TopRight;
  public GamePoint BottomLeft;
  public GamePoint BottomRight;
  public boolean   isComputerFill;
  public boolean   isAddedToGameState;
  public int       scoreNumber;

  public GameFill(GamePoint _topLeft, GamePoint _topRight) {
    TopLeft = _topLeft;
    TopRight = _topRight;
    BottomLeft = new GamePoint(_topLeft.X, (_topLeft.Y + 1));
    BottomRight = new GamePoint(_topRight.X, (_topRight.Y + 1));

  }

  public GameFill(GamePoint _topLeft, GamePoint _topRight, GamePoint _bottomLeft, GamePoint _bottomRight) {
    TopLeft = _topLeft;
    TopRight = _topRight;
    BottomLeft = _bottomLeft;
    BottomRight = _bottomRight;

  }

  public boolean Equals(GameFill anotherObj) {
    return (this.BottomLeft.equals(anotherObj.BottomLeft) && this.BottomRight.equals(anotherObj.BottomRight) && this.TopLeft.equals(anotherObj.TopLeft) && this.TopRight.equals(anotherObj.TopRight));

  }
}