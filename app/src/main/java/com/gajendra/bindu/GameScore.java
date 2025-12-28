package com.gajendra.bindu;

public class GameScore {

  public GameScore(int p, int playerOneFills, int playerTwoFills) {
    this.TotalEmptyFills = p;
    this.PlayerOneFills = playerOneFills;
    this.PlayerTwoFills = playerTwoFills;
  }

  public int TotalEmptyFills;
  public int PlayerOneFills;
  public int PlayerTwoFills;
}