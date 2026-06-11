import java.io.*;
import java.util.*;

public class FileManager {
  //Reads data from the team and player text files.
  public static ArrayList<Team> loadTeams(String teamsFile, String playersFile, int teamsToLoad) throws FileNotFoundException {
    ArrayList<Team> allTeamsFromFile = new ArrayList<>();
    java.io.InputStream teamStream = FileManager.class.getResourceAsStream("/" + teamsFile);
    Scanner teamScanner = new Scanner(teamStream);
    while(teamScanner.hasNextLine()) {
      String teamLine = teamScanner.nextLine();
      if(teamLine.trim().isEmpty()) {
        continue;
      }
      //Splits each line of the team file into parts and constructs either a regular Team or an Underdog based on the presence of a type specifier.
      String[] teamParts = teamLine.split(", ");
      int seed = Integer.parseInt(teamParts[0]);
      String name = teamParts[1];
      String type = "";
      if(teamParts.length > 2){
        type = teamParts[2];
      }
      if(type.equalsIgnoreCase("Underdog")) {
        Team tempUnderdogTeam = new Underdog(name, seed, 1.5);
        allTeamsFromFile.add(tempUnderdogTeam);
      } else {
        Team tempTeam = new Team(name, seed);
        allTeamsFromFile.add(tempTeam);
      }
    }
    teamScanner.close();
    for(int i = 0; i < allTeamsFromFile.size() - 1; i++) {
      for(int j = 0; j < allTeamsFromFile.size() - i - 1; j++) {
        if(allTeamsFromFile.get(j).getSeed() > allTeamsFromFile.get(j + 1).getSeed()) {
          Team temp = allTeamsFromFile.get(j);
          allTeamsFromFile.set(j, allTeamsFromFile.get(j + 1));
          allTeamsFromFile.set(j + 1, temp);
        }
      }
    }
    //Checks if the requested number of teams to load exceeds the number of teams available in the file and adjusts accordingly.
    if(allTeamsFromFile.size() < teamsToLoad) {
      System.out.println("WARNING: Requested " + teamsToLoad + " teams, but file only has " + allTeamsFromFile.size() + ".");
      teamsToLoad = allTeamsFromFile.size();
    }
    ArrayList<Team> selectedTeams = new ArrayList<>();
    for(int i = 0; i < teamsToLoad; i++) {
      selectedTeams.add(allTeamsFromFile.get(i));
    }
    java.io.InputStream playerStream = FileManager.class.getResourceAsStream("/" + playersFile);
    Scanner playerScanner = new Scanner(playerStream);
    while(playerScanner.hasNextLine()) {
      String playerLine = playerScanner.nextLine();
      if(playerLine.trim().isEmpty()) {
        continue;
      }
      //Splits each line of the player file into parts, constructs a Player object, and adds it to the appropriate team.
      String[] playerParts = playerLine.split("; ");
      String playerName = playerParts[0];
      double playerRating = Double.parseDouble(playerParts[1]);
      String teamName = playerParts[2];
      Player tempPlayer = new Player(playerName, playerRating);
      for(Team currentTeam : selectedTeams) {
        if(teamName.equals(currentTeam.getTeamName())) {
          currentTeam.addPlayer(tempPlayer);
          break;
        }
      }
    }
    playerScanner.close();
    return selectedTeams;
  }
}
