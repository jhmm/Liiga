package com.jere.liiga;

public class Game
{
	public int gameId;
	
	public String awayTeam;
	public String awayLogo;
	public int awayGoals;
	
	public String homeTeam;
	public String homeLogo;
	public int homeGoals;
	
	public boolean isFinished;
	public boolean isStarted;
	public String currentStatus;
	
	public String latestEventTeam;
	public String latestEventText;
	public String latestEventTime;
	
	public String urlReport;
	
	public Game()
	{
		gameId = 0;
		awayTeam = "";
		awayLogo = null;
		awayGoals = 0;
		homeTeam = "";
		homeLogo = null;
		homeGoals = 0;
		
		isFinished = false;
		isStarted = false;
		currentStatus = "";
		
		latestEventTeam = "";
		latestEventText = "";
		latestEventTime = "";
		
		urlReport = "";
	}
	
	
}
