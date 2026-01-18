package com.gajendra.bindu;

import java.io.Serializable;

public class GameLine implements Serializable {
  private static final long serialVersionUID = 1L;
  public GamePoint PointOne;
  public GamePoint PointTwo;
  public boolean   isLineDrawn;
  public boolean   isComputerDrawn;

  public GameLine(GamePoint point1, GamePoint point2) {
    this.PointOne = point1;
    this.PointTwo = point2;
  }

  public boolean equals(GameLine tempLine) {
    boolean isEqual = false;

    boolean pointOneIsSame = (this.PointOne.X == tempLine.PointOne.X && this.PointOne.Y == tempLine.PointOne.Y);
    boolean pointTwoIsSame = (this.PointTwo.X == tempLine.PointTwo.X && this.PointTwo.Y == tempLine.PointTwo.Y);

    boolean pointOneXIsSame = (this.PointTwo.X == tempLine.PointOne.X && this.PointTwo.Y == tempLine.PointOne.Y);
    boolean pointTwoXIsSame = (this.PointOne.X == tempLine.PointTwo.X && this.PointOne.Y == tempLine.PointTwo.Y);

    if ((pointOneIsSame && pointTwoIsSame) || (pointOneXIsSame && pointTwoXIsSame)) {
      isEqual = true;
    }

    return isEqual;
  }

  public void SetLineDrawn(boolean flag, boolean isComputerDrawn) {
    this.isLineDrawn = flag;
    this.isComputerDrawn = isComputerDrawn;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("GameLine [PointOne=");
    builder.append(PointOne);
    builder.append(", PointTwo=");
    builder.append(PointTwo);
    builder.append(", isComputerDrawn=");
    builder.append(isComputerDrawn);
    builder.append(", isLineDrawn=");
    builder.append(isLineDrawn);
    builder.append("]");
    return builder.toString();
  }
  

}