package com.jere.liiga;

import android.widget.TextView;

public class UpdatableGameView {
	TextView statusView;
	TextView latestEventTextView;
	TextView latestEventTeamView;
	TextView latestEventTimeView;
	TextView homeGoals;
	TextView awayGoals;
	TextView goalSeparator;
	int gameId;
	
	public UpdatableGameView(int gameId)
	{
		this.gameId = gameId;
	}
}
