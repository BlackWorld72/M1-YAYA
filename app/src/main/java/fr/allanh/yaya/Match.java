package fr.allanh.yaya;

import android.graphics.drawable.Drawable;

public class Match {
    private String team_home, team_away;
    private int score_home = 0, score_away = 0;
    private String logo_home, logo_away;

    public Match(String team_home, String team_away, int score_home, int score_away, String logo_home, String logo_away) {
        this.team_away = team_away;
        this.team_home = team_home;
        this.score_home = score_home;
        this.score_away = score_away;
        this.logo_away = logo_away;
        this.logo_home = logo_home;
    }

    public String getLogo_home() {
        return logo_home;
    }

    public String getLogo_away() {
        return logo_away;
    }

    public String getTeam_home() {
        return team_home;
    }

    public String getTeam_away() {
        return team_away;
    }

    public int getScore_home() {
        return score_home;
    }

    public int getScore_away() {
        return score_away;
    }
}
