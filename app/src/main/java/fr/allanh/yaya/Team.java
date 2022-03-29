package fr.allanh.yaya;


public class Team {

    private String name;
    private int points;
    private int played;
    private int wins;
    private int losses;

    public Team(String name, int points, int wins, int losses, int played, String logoURL) {
        this.name = name;
        this.points = points;
        this.played = played;
        this.wins = wins;
        this.losses = losses;
    }

    public String getName() {
        return name;
    }

    public int getPoints() {
        return points;
    }

    public int getPlayed() {
        return played;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public int getDraws() {
        return played-losses-wins;
    }
}
