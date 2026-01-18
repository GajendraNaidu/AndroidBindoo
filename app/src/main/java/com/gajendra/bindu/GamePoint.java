package com.gajendra.bindu;

import java.io.Serializable;

public class GamePoint implements Serializable {
  private static final long serialVersionUID = 1L;
  public int X;
  public int Y;

  public GamePoint(int x, int y) {
    this.X = x;
    this.Y = y;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + X;
    result = prime * result + Y;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    GamePoint other = (GamePoint) obj;
    if (X != other.X)
      return false;
    if (Y != other.Y)
      return false;
    return true;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("GamePoint [X=");
    builder.append(X);
    builder.append(", Y=");
    builder.append(Y);
    builder.append("]");
    return builder.toString();
  }
  
  
  
  
}