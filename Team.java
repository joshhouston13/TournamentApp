import java.util.*;

public class Team {
  private String teamName;
  private int seed;
  private ArrayList<Player> roster;
  private double winChance;
  private int leaguePoints = 0;
  private int roundRobinWins = 0;
  private int roundRobinDraws = 0;
  private int roundRobinLosses = 0;
//Construcs a team with a name and seed, and initializes an empty roster and default win chance.
  public Team(String teamName, int seed) {
    this.teamName = teamName;
    this.seed = seed;
    this.roster = new ArrayList<>();
    this.winChance = 0.0;
  }
//Adds a player to the team's roster.
  public void addPlayer(Player p) {
    this.roster.add(p);
  }
//Gets the team's win chance by averaging the skill ratings of all players in the roster and normalizing it to a percentage.
  public double calcWinChance() {
    double total = 0.0;
    for(Player p : this.roster) {
      total += p.getSkillRating();
    }
    this.winChance = ((total / this.roster.size())/10)*100;
    return this.winChance;
  }
//Accessor methods for the team's name, seed, roster, and win chance.
  public String getTeamName() {
    return teamName;
  }

  public int getSeed() {
    return seed;
  }

  public ArrayList<Player> getRoster() {
    return roster;
  }

  public double getWinChance() {
    return winChance;
  }
//Calculates the team's match power, which is a base value of 50.0 that can be overridden by subclasses for special team types.
  public double matchPower() {
    return 50.0;
  }
//Stores a teams wins, draws, and losses in the round robin tournament so the league table can be updated.
  public void addLeagueResult(int points, String outcome) {
    this.leaguePoints += points;
    if(outcome.equalsIgnoreCase("win")) {
      this.roundRobinWins++;
    } else if(outcome.equalsIgnoreCase("draw")) {
      this.roundRobinDraws++;
    } else if(outcome.equalsIgnoreCase("loss")) {
      this.roundRobinLosses++;
    }
  }
//Accessor methods for the team's league points and a formatted string of their round robin record.
  public int getLeaguePoints() {
    return this.leaguePoints;
  }

  public String getRecordString() {
    return this.roundRobinWins + "W - " + this.roundRobinDraws + "D - " + this.roundRobinLosses + "L";
  }
}
