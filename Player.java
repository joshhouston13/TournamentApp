public class Player{
  private String name;
  private double skillRating;

//Construcs a player with a name and skill rating.
  public Player(String name, double skillRating) {
    this.name = name;
    this.skillRating = skillRating;
  }
//Accessor methods for the player's name and skill rating.
  public String getName() {
    return name;
  }

  public double getSkillRating() {
    return skillRating;
  }
}
