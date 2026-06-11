/*
 * ----------------------------------------------------------------------------
 * COURSE:          Java 1 Programming / Intro to Computer Science
 * PROJECT:         Final Project - Tournament Simulation Engine
 * FILE:            TournamentApp.java
 * AUTHOR:          Joshua Houston
 * DATE:            June 2026
 * DESCRIPTION:
 * This desktop application provides a comprehensive Tournament Simulation Engine 
 * utilizing a graphical user interface built entirely with JavaFX. The software 
 * dynamically accommodates 4, 8, 16, 32, or 64 teams loaded from external 
 * data configurations and supports three core tournament models:
 * 1. Single Elimination (Standard Seeded Bracket Layout)
 * 2. Double Elimination (Symmetrical Winners & Losers Elimination Splits)
 * 3. Round Robin (Full Schedule Matrix with a Live Standings Leaderboard)
 *  The system supports both an 'Automatic Mode' driven by skill-weighted random 
 * probability algorithms, and a 'Manual Mode' providing strict input validation 
 * to handle manual score entries without allowing tie conditions.
 * OBJECT-ORIENTED & ARCHITECTURAL HIGHLIGHTS:
 * - OOP & Composition: Managed through relational class structuring where the 
 * Team object encapsulates a dynamic array collection of nested Player objects.
 * - Inheritance & Polymorphism: Demonstrated cleanly via the 'Underdog' 
 * subclass, which overrides 'matchPower()' to add variable clutch mechanics.
 * - Robust Input Validation: Handled across text harvesting, data parsing loops, 
 * and GUI Alert frames protecting the main execution context from thread crashes.
 * ----------------------------------------------------------------------------
 */
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import java.util.ArrayList;
import javafx.geometry.VPos;
import javafx.geometry.HPos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class TournamentApp extends Application {
  private Stage stage;
  private ArrayList<Team> tournamentTeams;
  private Boolean bracketResetFlag = false;
  private int currentRoundRobinRound = 0;
  private VBox leaderboardSideBar;
  public static void main(String[] args) {
    launch(args);
  }
//Set the stage for the JavaFX functionality.
  @Override
  public void start(Stage primaryStage) {
    this.stage = primaryStage;
    this.stage.setTitle("Tournament Simulation");
    showMenuScreen();
  }

  private void showMenuScreen() {
    VBox menuLayout = new VBox(20);
    menuLayout.setAlignment(Pos.CENTER);
    menuLayout.setPadding(new Insets(30));
    Label titleLabel = new Label("Tournament Configuration");
    //Allows user to select the number of teams playing in the tournament.
    Label teamsLabel = new Label("Select Number of Teams:");
    ComboBox<Integer> teamSizeBox = new ComboBox<>();
    teamSizeBox.getItems().addAll(4, 8, 16, 32, 64);
    teamSizeBox.setValue(4);
    //Allows the user to select the type of tournament they want to simulate.
    Label typeLabel = new Label("Select Tournament Type:");
    ComboBox<String> typeBox = new ComboBox<>();
    typeBox.getItems().addAll("Single Elimination", "Double Elimination", "Round Robin");
    typeBox.setValue("Single Elimination");
    //Allows the user to select whether they want to simulate matches automatically or manually input match results.
    Label simLabel = new Label("Select Simulation Type:");
    ComboBox<String> simType = new ComboBox<>();
    simType.getItems().addAll("Automatic", "Manual");
    simType.setValue("Automatic");
    Button generateButton = new Button("Launch Tournament");
    generateButton.setOnAction(e -> {
      int selectedSize = teamSizeBox.getValue();
      String selectedType = typeBox.getValue();
      String selectedSim = simType.getValue();
      //Based on the tournament type, launches different types of tournament screens.
      try {
        this.tournamentTeams = FileManager.loadTeams("teams.txt", "players.txt", selectedSize);
        if(selectedType.equals("Double Elimination")) {
          this.bracketResetFlag = false;
          doubleElimScreen(selectedSize, selectedSim);
        } else if(selectedType.equals("Single Elimination")) {
          singleElimScreen(selectedSize, selectedType, selectedSim);
        } else if(selectedType.equals("Round Robin")) {
          roundRobinScreen(selectedSize, selectedSim);
        }
      } catch(Exception ex) {
        IO.println("Error initializing JavaFX App data layers.");
        ex.printStackTrace();
      }
    });
    menuLayout.getChildren().addAll(titleLabel, teamsLabel, teamSizeBox, typeLabel, typeBox, simLabel, simType, generateButton);
    Scene menuScene = new Scene(menuLayout, 950, 600);
    stage.setScene(menuScene);
    stage.show();
  }

  private void singleElimScreen(int teamCount, String tournamentType, String simType) {
    javafx.scene.layout.BorderPane rootLayout = new javafx.scene.layout.BorderPane();
    rootLayout.setPadding(new Insets(15));
    Label headerLabel = new Label(teamCount + "-Team " + tournamentType + " Bracket");
    headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
    VBox topBar = new VBox(headerLabel);
    topBar.setAlignment(Pos.CENTER);
    topBar.setPadding(new Insets(0, 0, 15, 0));
    rootLayout.setTop(topBar);
    GridPane matchGrid = new GridPane();
    matchGrid.setAlignment(Pos.CENTER);
    matchGrid.setHgap(10);
    matchGrid.setVgap(15);
    //Sets a smaller width for the columns containing bracket lines for a better visual appeal.
    for (int col = 0; col < 20; col++) {
      javafx.scene.layout.ColumnConstraints cc = new javafx.scene.layout.ColumnConstraints();
      if (col % 2 != 0) {
        cc.setPrefWidth(25);
        cc.setMinWidth(25);
        cc.setMaxWidth(25);
        }
        matchGrid.getColumnConstraints().add(cc);
    }
    //Keeps the bracket scrollable for larger team counts.
    javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane();
    scrollPane.setContent(matchGrid);
    scrollPane.setPannable(true);
    scrollPane.setFitToHeight(true);
    scrollPane.setStyle("-fx-background-color:transparent; -fx-background: #ffffff;");
    rootLayout.setCenter(scrollPane);
    for(Team t : tournamentTeams) {
      t.calcWinChance();
    }
    //Organizes the teams in the order they will appear based on their seeding.
    ArrayList<Team> seededList = new ArrayList<>();
    int totalTeams = tournamentTeams.size();
    for(int i = 0; i < totalTeams / 2; i++) {
      seededList.add(tournamentTeams.get(i));
      seededList.add(tournamentTeams.get(totalTeams - 1 - i));
    }
    Button simButton = new Button("Simulate Round");
    simButton.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 8 20 8 20;");
    Button backButton = new Button("<- Back to Menu");
    backButton.setOnAction(e -> showMenuScreen());
    backButton.setStyle("-fx-font-size: 13px; -fx-padding: 7 15 7 15;");
    javafx.scene.layout.HBox bottomControlBar = new javafx.scene.layout.HBox(20);
    bottomControlBar.setAlignment(Pos.CENTER);
    bottomControlBar.setPadding(new Insets(15, 0, 0, 0));
    bottomControlBar.getChildren().addAll(backButton, simButton);
    rootLayout.setBottom(bottomControlBar);
  //Runs the single elimination tournament.
    displaySingle(matchGrid, seededList, simType, simButton, 0);
    Scene bracketScene = new Scene(rootLayout, 1024, 650);
    stage.setScene(bracketScene);
  }

  private void doubleElimScreen(int teamCount, String simType) {
    javafx.scene.layout.BorderPane rootLayout = new javafx.scene.layout.BorderPane();
    rootLayout.setPadding(new Insets(15));
    Label headerLabel = new Label(teamCount + "-Team Double Elimination Bracket");
    headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
    VBox topBar = new VBox(headerLabel);
    topBar.setAlignment(Pos.CENTER);
    topBar.setPadding(new Insets(0, 0, 15, 0));
    rootLayout.setTop(topBar);
    //Setting a winner grid since there are two tournaments happening simultaneously in double elimination.
    GridPane winnerGrid = new GridPane();
    winnerGrid.setAlignment(Pos.CENTER);
    winnerGrid.setHgap(10);
    winnerGrid.setVgap(15);
    //Adjusting column widths for bracket lines again.
    for (int col = 0; col < 20; col++) {
      javafx.scene.layout.ColumnConstraints cc = new javafx.scene.layout.ColumnConstraints();
      if (col % 2 != 0) {
        cc.setPrefWidth(25);
        cc.setMinWidth(25);
        cc.setMaxWidth(25);
      }
      winnerGrid.getColumnConstraints().add(cc);
    }
    //Making tournament scrollable for larger team counts again.
    javafx.scene.control.ScrollPane winnerScroll = new javafx.scene.control.ScrollPane();
    winnerScroll.setContent(winnerGrid);
    winnerScroll.setPannable(true);
    winnerScroll.setFitToHeight(true);
    winnerScroll.setStyle("-fx-background-color:transparent; -fx-background: #ffffff;");
    Label winnerTitle = new Label("WINNERS BRACKET");
    winnerTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #2980b9; -fx-padding: 5 0 5 10;");
    VBox winnerSection = new VBox(5, winnerTitle, winnerScroll);
    javafx.scene.layout.VBox.setVgrow(winnerScroll, javafx.scene.layout.Priority.ALWAYS);
    //And here is the loser grid.
    GridPane loserGrid = new GridPane();
    loserGrid.setAlignment(Pos.CENTER);
    loserGrid.setHgap(10);
    loserGrid.setVgap(15);
    for (int col = 0; col < 20; col++) {
      javafx.scene.layout.ColumnConstraints cc = new javafx.scene.layout.ColumnConstraints();
      if (col % 2 != 0) {
        cc.setPrefWidth(25);
        cc.setMinWidth(25);
        cc.setMaxWidth(25);
      }
      loserGrid.getColumnConstraints().add(cc);
    }
    javafx.scene.control.ScrollPane loserScroll = new javafx.scene.control.ScrollPane();
    loserScroll.setContent(loserGrid);
    loserScroll.setPannable(true);
    loserScroll.setFitToHeight(true);
    loserScroll.setStyle("-fx-background-color:transparent; -fx-background: #ffffff;");
    Label loserTitle = new Label("LOSERS BRACKET");
    loserTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #e74c3c; -fx-padding: 5 0 5 10;");
    VBox loserSection = new VBox(5, loserTitle, loserScroll);
    javafx.scene.layout.VBox.setVgrow(loserScroll, javafx.scene.layout.Priority.ALWAYS);
    javafx.scene.control.SplitPane splitPane = new javafx.scene.control.SplitPane();
    splitPane.setOrientation(javafx.geometry.Orientation.VERTICAL);
    splitPane.getItems().addAll(winnerSection, loserSection);
    splitPane.setDividerPositions(0.5);
    rootLayout.setCenter(splitPane);
    Button simButton = new Button("Simulate Round");
    simButton.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: #e67e22; -fx-text-fill: white; -fx-padding: 8 20 8 20;");
    Button backButton = new Button("<- Back to Menu");
    backButton.setOnAction(e -> showMenuScreen());
    backButton.setStyle("-fx-font-size: 13px; -fx-padding: 7 15 7 15;");
    javafx.scene.layout.HBox bottomControlBar = new javafx.scene.layout.HBox(20);
    bottomControlBar.setAlignment(Pos.CENTER);
    bottomControlBar.setPadding(new Insets(15, 0, 0, 0));
    bottomControlBar.getChildren().addAll(backButton, simButton);
    rootLayout.setBottom(bottomControlBar);
    for(Team t : tournamentTeams) {
      t.calcWinChance();
    }
    //Adds teams to a list order based on seed and then calls for the double elimination bracket to be simulated.
    ArrayList<Team> seededList = new ArrayList<>();
    int totalTeams = tournamentTeams.size();
    for(int i = 0; i < totalTeams / 2; i++) {
      seededList.add(tournamentTeams.get(i));
      seededList.add(tournamentTeams.get(totalTeams - 1 - i));
    }
    displayDouble(winnerGrid, loserGrid, seededList, new ArrayList<Team>(), simType, simButton, 0);
    
    Scene bracketScene = new Scene(rootLayout, 1024, 750);
    stage.setScene(bracketScene);
  }

  private void roundRobinScreen(int teamCount, String simType) {
    javafx.scene.layout.BorderPane rootLayout = new javafx.scene.layout.BorderPane();
    rootLayout.setPadding(new Insets(15));
    Label headerLabel = new Label(teamCount + "-Team Round Robin Schedule");
    headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
    VBox topBar = new VBox(headerLabel);
    topBar.setAlignment(Pos.CENTER);
    topBar.setPadding(new Insets(0, 0, 15, 0));
    rootLayout.setTop(topBar);
    //Sets up a grid where the matches happening each round can be viewed.
    GridPane leagueGrid = new GridPane();
    leagueGrid.setAlignment(Pos.CENTER);
    leagueGrid.setHgap(20);
    leagueGrid.setVgap(15);
    javafx.scene.control.ScrollPane matchPane = new javafx.scene.control.ScrollPane(leagueGrid);
    matchPane.setPannable(true);
    matchPane.setFitToWidth(true);
    matchPane.setStyle("-fx-background-color:transparent; -fx-background: #ffffff;");
    //Sets a live leaderboard that updates after the rounds.
    leaderboardSideBar = new VBox(10);
    leaderboardSideBar.setPadding(new Insets(10, 15, 10, 10));
    leaderboardSideBar.setMinWidth(340);
    leaderboardSideBar.setPrefWidth(340);
    leaderboardSideBar.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dcdde1; -fx-border-width: 0 0 0 1px;");
    javafx.scene.control.ScrollPane leaderboardScroll = new javafx.scene.control.ScrollPane(leaderboardSideBar);
    leaderboardScroll.setFitToWidth(true);
    leaderboardScroll.setStyle("-fx-background-color:transparent;");
    javafx.scene.layout.BorderPane centerSplitPane = new javafx.scene.layout.BorderPane();
    centerSplitPane.setLeft(leaderboardScroll);
    centerSplitPane.setCenter(matchPane);
    rootLayout.setCenter(centerSplitPane);
    for(Team t : tournamentTeams) {
      t.calcWinChance();
    }
    int numTeams = tournamentTeams.size();
    int numRounds = numTeams - 1;
    int matchesPerRound = numTeams / 2;
    ArrayList<ArrayList<Team[]>> fullSchedule = new ArrayList<>();
    ArrayList<Team> rotationList = new ArrayList<>(tournamentTeams);
    //An outside loop to step through all of the rounds.
    for(int round = 0; round < numRounds; round++) {
      ArrayList<Team[]> roundMatches = new ArrayList<>();
      //An inside loop to step through all of the matches in each round.
      for(int match = 0; match < matchesPerRound; match++) {
        Team home = rotationList.get(match);
        Team away = rotationList.get(numTeams - 1 - match);
        roundMatches.add(new Team[]{home, away});
      }
      fullSchedule.add(roundMatches);
      Team lastTeam = rotationList.remove(rotationList.size() - 1);
      rotationList.add(1, lastTeam);
    }
    this.currentRoundRobinRound = 0;
    Button simButton = new Button("Simulate Round");
    simButton.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 8 20 8 20;");
    Button backButton = new Button("<- Back to Menu");
    backButton.setOnAction(e -> showMenuScreen());
    backButton.setStyle("-fx-font-size: 13px; -fx-padding: 7 15 7 15;");
    javafx.scene.layout.HBox bottomControlBar = new javafx.scene.layout.HBox(20);
    bottomControlBar.setAlignment(Pos.CENTER);
    bottomControlBar.setPadding(new Insets(15, 0, 0, 0));
    bottomControlBar.getChildren().addAll(backButton, simButton);
    rootLayout.setBottom(bottomControlBar);
    //Allows us to see live stats as they change.
    updateLeaderboard();
    //Calls to have the round robin tournament simulated.
    displayRobin(leagueGrid, fullSchedule, simType, simButton);
    Scene leagueScene = new Scene(rootLayout, 1050, 700);
    stage.setScene(leagueScene);
  }

  private void displaySingle(GridPane grid, ArrayList<Team> currentRoundTeams, String simType, Button simButton, int curCol) {
    int totalInitialTeams = tournamentTeams.size();
    //Allows for centering of the tournament based on number of teams.
    int absoluteCenterRow = (totalInitialTeams / 2) - 1;
    //Tests for final round condition.
    if(currentRoundTeams.size() == 1) {
      VBox champBox = new VBox(5);
      champBox.setAlignment(Pos.CENTER);
      Label titleLabel = new Label("CHAMPION");
      titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: gold;");
      Label champLabel = new Label(currentRoundTeams.get(0).getTeamName());
      champLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: green;");
      champBox.getChildren().addAll(titleLabel, champLabel);
      GridPane.setValignment(champBox, VPos.CENTER);
      grid.add(champBox, curCol * 2, absoluteCenterRow);
      simButton.setText("Tournament Complete");
      simButton.setDisable(true);
      return;
    }
    //Title for tournament based on number of teams playing.
    String roundTitle = "MATCH";
    if(currentRoundTeams.size() == 64) roundTitle = "FIRST ROUND";
    if(currentRoundTeams.size() == 32) roundTitle = "ROUND OF 32";
    if(currentRoundTeams.size() == 16) roundTitle = "ROUND OF 16";
    if(currentRoundTeams.size() == 8) roundTitle = "QUARTERFINAL";
    if(currentRoundTeams.size() == 4) roundTitle = "SEMIFINAL";
    if(currentRoundTeams.size() == 2) roundTitle = "CHAMPIONSHIP";
    simButton.setText("SIMULATE " + roundTitle);
    int matchNum = 1;
    int firstRowOffset = (int)Math.pow(2, curCol) - 1;
    int rowStep = (int)Math.pow(2, curCol + 1);
    int curRow = firstRowOffset;
    ArrayList<Integer> rowHistory = new ArrayList<>();
    //Allows for manual entry of scores by the user.
    ArrayList<javafx.scene.control.TextField> roundFields = new ArrayList<>();
    for(int i = 0; i < currentRoundTeams.size(); i+=2) {
      Team teamA = currentRoundTeams.get(i);
      Team teamB = currentRoundTeams.get(i + 1);
      boolean isChampMatch = (currentRoundTeams.size() == 2);
      boolean isFirstRound = (currentRoundTeams.size() == totalInitialTeams);
      //Creates a match card to be displayed.
      javafx.scene.layout.HBox matchCard = matchMaker(isChampMatch ? roundTitle : roundTitle + " (Match " + matchNum + ")", teamA, teamB, isChampMatch, isFirstRound, simType);
      GridPane.setValignment(matchCard, VPos.CENTER);
      //Checks for manual simulation type so manual scores can be entered.
      if(simType.equals("Manual")) {
        harvestTextFields(matchCard, roundFields);
      }
      int targetRenderRow = isChampMatch ? absoluteCenterRow : curRow;
      grid.add(matchCard, curCol * 2, targetRenderRow);
      rowHistory.add(targetRenderRow);
      matchNum++;
      curRow += rowStep;
    }
    //Lots of code in here to make sure all of the teams and match cards are all aligned properly!
    if(currentRoundTeams.size() > 2) {
      int historyIndex = 0;
      int nextRowOffset = (int)Math.pow(2, curCol + 1) - 1;
      int nextRowStep = (int)Math.pow(2, curCol + 2);
      int targetRow = (currentRoundTeams.size() == 4) ? absoluteCenterRow : nextRowOffset;
      for(int j = 0; j < currentRoundTeams.size(); j += 4) {
        int rowA = rowHistory.get(historyIndex);
        int rowB = rowHistory.get(historyIndex + 1);
        drawBracketForks(grid, (curCol * 2) + 1, rowA, rowB, targetRow);
        historyIndex += 2;
        targetRow += nextRowStep;
      }
    }
    //Tells the program what to do when the simButton is clicked.
    simButton.setOnAction(e -> {
      ArrayList<Team> nextRoundWinners = new ArrayList<>();
      int fieldCounter = 0;
      //For manual sim types the user can enter scores in the text fields so we know it isn't a tie.
        if(!simType.equals("Automatic")) {
          int checkCounter = 0;
          for(int j = 0; j < currentRoundTeams.size(); j += 2) {
            try {
              javafx.scene.control.TextField checkFieldA = roundFields.get(checkCounter);
              javafx.scene.control.TextField checkFieldB = roundFields.get(checkCounter + 1);
              int scoreA = Integer.parseInt(checkFieldA.getText().trim());
              int scoreB = Integer.parseInt(checkFieldB.getText().trim());
              if(scoreA == scoreB) {
                Team tA = (Team)checkFieldA.getUserData();
                Team tB = (Team)checkFieldB.getUserData();
                //Checks for a tie and has the user re enter the score in this case.
                Alert tieAlert = new Alert(AlertType.WARNING);
                tieAlert.setTitle("TIE SCORE DETECTED!!!");
                tieAlert.setHeaderText("Tournament Rules Require a Winner");
                tieAlert.setContentText("The match between " + tA.getTeamName() + " and " + tB.getTeamName() + " ended in a tie and is going into overtime! Please enter the over time score now.");
                tieAlert.showAndWait();
                return;
              }
              checkCounter += 2;
            } catch(NumberFormatException ex) {
              IO.println("Validation Warning: Please enter integer scores.");
              return;
            }
          }
        }
        for(int j = 0; j < currentRoundTeams.size(); j += 2) {
          Team winner = null;
          if(simType.equals("Automatic")) {
            //Random simulation for automatic mode.
            winner = simulateMatch(currentRoundTeams.get(j), currentRoundTeams.get(j + 1));
          } else {
            //Manual score entries for manual mode. Already checked that it isn't a tie.
            javafx.scene.control.TextField textFieldA = roundFields.get(fieldCounter);
            javafx.scene.control.TextField textFieldB = roundFields.get(fieldCounter + 1);
            int scoreA = Integer.parseInt(textFieldA.getText().trim());
            int scoreB = Integer.parseInt(textFieldB.getText().trim());
            Team tA = (Team)textFieldA.getUserData();
            Team tB = (Team)textFieldB.getUserData();
            if(scoreA > scoreB) {
              winner = tA;
            } else {
              winner = tB;
            }
            fieldCounter += 2;
          }
          nextRoundWinners.add(winner);
        }
        //Calls method again to loop through all the rounds.
        displaySingle(grid, nextRoundWinners, simType, simButton, curCol + 1);
    });
  }

  private void displayDouble(GridPane winnerGrid, GridPane loserGrid, ArrayList<Team> currentWinnerTeams, ArrayList<Team> currentLoserTeams, String simType, Button simButton, int curCol) {
    //Checks for the last team left.
    if(currentWinnerTeams.size() == 1 && currentLoserTeams.size() == 0) {
      simButton.setText("CHAMPION!!!");
      return;
    }
    //Checks for final game since the winner of the loser bracket has to come back up and join.
    if(currentWinnerTeams.size() == 1 && currentLoserTeams.size() == 1) {
      Team grandWinnerTop = currentWinnerTeams.get(0);
      Team grandWinnerBottom = currentLoserTeams.get(0);
      simButton.setText("GRAND CHAMPIONSHIP MATCH");
      javafx.scene.layout.HBox finalCard = matchMaker("GRAND FINALS", grandWinnerTop, grandWinnerBottom, true, false, simType);
      GridPane.setValignment(finalCard, VPos.CENTER);
      int absoluteCenterRow = (tournamentTeams.size() / 2) - 1;
      winnerGrid.add(finalCard, curCol * 2, absoluteCenterRow);
      ArrayList<javafx.scene.control.TextField> finalFields = new ArrayList<>();
      if(simType.equals("Manual")) {
        harvestTextFields(finalCard, finalFields);
      }
      simButton.setOnAction(null);
      simButton.setOnAction(ev -> {
        Team matchWinner = null;
        Team matchLoser = null;
        //Simulates the match in automatic mode.
        if (simType.equals("Automatic")) {
          matchWinner = simulateMatch(grandWinnerTop, grandWinnerBottom);
          matchLoser = (matchWinner == grandWinnerTop) ? grandWinnerBottom : grandWinnerTop;
        } else {
          //Takes in manual entries and checks for ties in manual mode.
          try {
            javafx.scene.control.TextField fieldA = finalFields.get(0);
            javafx.scene.control.TextField fieldB = finalFields.get(1);
            int scoreA = Integer.parseInt(fieldA.getText().trim());
            int scoreB = Integer.parseInt(fieldB.getText().trim());
            if (scoreA == scoreB) {
              Alert finalTieAlert = new Alert(AlertType.WARNING);
              finalTieAlert.setTitle("Tie Score Detected");
              finalTieAlert.setHeaderText("Championship matches cannot end in a tie!");
              finalTieAlert.setContentText("Please input an overtime tie-breaker score.");
              finalTieAlert.showAndWait();
              return;
            }
            //Determines winner.
            if (scoreA > scoreB) { 
              matchWinner = grandWinnerTop; 
              matchLoser = grandWinnerBottom; 
            } else { 
              matchWinner = grandWinnerBottom; 
              matchLoser = grandWinnerTop; 
            }
          } catch (Exception ex) {
            System.out.println("Error detecting final match scores. Please ensure valid integers are entered for both teams.");
            return;
          }
        }
        //Checks to see if the winner of winner's bracket won again which ends the tournament.
        if(bracketResetFlag || matchWinner == grandWinnerTop) {
          simButton.setText("Tournament Completed!!!");
          simButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
          simButton.setDisable(true);
          VBox champBox = new VBox(5);
          champBox.setAlignment(Pos.CENTER);
          champBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #f1c40f; -fx-border-width: 2px; -fx-border-radius: 8px; -fx-padding: 15;");
          champBox.setPrefWidth(200);
          champBox.setMinWidth(200);
          Label titleLabel = new Label("GRAND CHAMPION!!!");
          titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #f1c40f; -fx-font-size: 11px;");
          Label nameLabel = new Label(matchWinner.getTeamName());
          nameLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
          champBox.getChildren().addAll(titleLabel, nameLabel);
          GridPane.setValignment(champBox, VPos.CENTER);
          winnerGrid.add(champBox, (curCol * 2) + 2, absoluteCenterRow);
        } else {
          //If the winner of the loser's bracket won, then they need to play another game to make one team have two losses.
          bracketResetFlag = true;
          Alert bracketResetAlert = new Alert(AlertType.INFORMATION);
          bracketResetAlert.setTitle("Bracket Reset Triggered!");
          bracketResetAlert.setHeaderText(grandWinnerBottom.getTeamName() + " Defeats the Undefeated Champion!");
          bracketResetAlert.setContentText("Both teams now have 1 loss. Generating the final tie-breaking game card now!");
          bracketResetAlert.showAndWait();
          ArrayList<Team> trueFinalWinners = new ArrayList<>();
          ArrayList<Team> trueFinalLosers = new ArrayList<>();
          trueFinalWinners.add(grandWinnerTop);
          trueFinalLosers.add(grandWinnerBottom);
          javafx.application.Platform.runLater(() -> {
            simButton.setOnAction(null);
            //Calls method again with the two final teams for the final showdown.
            displayDouble(winnerGrid, loserGrid, trueFinalWinners, trueFinalLosers, simType, simButton, curCol + 2);
          });
        }
      });
      return;
    }
    if (currentWinnerTeams.size() == 1 && currentLoserTeams.isEmpty()) {
       simButton.setText("Tournament Concluded");
       simButton.setDisable(true);
       return;
    }
    int matchNum = 1;
    int firstRowOffset = (int)Math.pow(2, curCol) - 1;
    int rowStep = (int)Math.pow(2, curCol + 1);
    int curRow = firstRowOffset;
    //Sets round names.
    String roundTitle = "MATCH";
    if(currentWinnerTeams.size() == 64) roundTitle = "FIRST ROUND";
    if(currentWinnerTeams.size() == 32) roundTitle = "ROUND OF 32";
    if(currentWinnerTeams.size() == 16) roundTitle = "ROUND OF 16";
    if(currentWinnerTeams.size() == 8)  roundTitle = "QUARTERFINAL";
    if(currentWinnerTeams.size() == 4)  roundTitle = "SEMIFINAL";
    if(currentWinnerTeams.size() == 2)  roundTitle = "CHAMPIONSHIP";
    ArrayList<Integer> winnerRowHistory = new ArrayList<>();
    ArrayList<javafx.scene.control.TextField> winnerRoundFields = new ArrayList<>();
    //Standard simulation for everything except edge cases.
    if(currentWinnerTeams.size() >= 2) {
      for(int i = 0; i < currentWinnerTeams.size(); i += 2) {
        Team teamA = currentWinnerTeams.get(i);
        Team teamB = currentWinnerTeams.get(i + 1);
        boolean isChampMatch = (currentWinnerTeams.size() == 2);
        boolean isFirstRound = curCol == 0;
        javafx.scene.layout.HBox matchCard = matchMaker(isChampMatch ? roundTitle : roundTitle + " (Match " + matchNum + ")", teamA, teamB, isChampMatch, isFirstRound, simType);
        GridPane.setValignment(matchCard, VPos.CENTER);
        winnerGrid.add(matchCard, curCol * 2, curRow);
        winnerRowHistory.add(curRow);
        //Grabs manual score fields for the match cards in manual mode.
        if(simType.equals("Manual")) {
          harvestTextFields(matchCard, winnerRoundFields);
        }
        curRow += rowStep;
        matchNum++;
      }
      if(currentWinnerTeams.size() > 2) {
        int historyIndex = 0;
        int nextRowOffset = (int)Math.pow(2, curCol + 1) - 1;
        int nextRowStep = (int)Math.pow(2, curCol + 2);
        int targetRow = (currentWinnerTeams.size() == 4) ? ((tournamentTeams.size() / 2) - 1) : nextRowOffset;
        for(int j = 0; j < currentWinnerTeams.size(); j += 4) {
          int rowA = winnerRowHistory.get(historyIndex);
          int rowB = winnerRowHistory.get(historyIndex + 1);
          drawBracketForks(winnerGrid, (curCol * 2) + 1, rowA, rowB, targetRow);
          historyIndex += 2;
          targetRow += nextRowStep;
        }
      }
    }
    ArrayList<Integer> loserRowHistory = new ArrayList<>();
    ArrayList<javafx.scene.control.TextField> loserRoundFields = new ArrayList<>();
    if(!currentLoserTeams.isEmpty()) {
      int loserRow = firstRowOffset;
      int losersMatchNum = 1;
      //Allows for a match card to hold the teams that don't have anyone to play this round until a future round when they do.
      for(int i = 0; i < currentLoserTeams.size(); i += 2) {
        if (i + 1 >= currentLoserTeams.size()) {
          Team soloTeam = currentLoserTeams.get(i);
          javafx.scene.layout.VBox byeBox = new javafx.scene.layout.VBox(5);
          byeBox.setAlignment(Pos.CENTER);
          byeBox.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1px; -fx-border-radius: 5px; -fx-background-color: #f8f9fa; -fx-padding: 10;");
          byeBox.setPrefWidth(200);
          Label byeTitle = new Label("BYE ADVANCEMENT");
          byeTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #e67e22; -fx-font-size: 11px;");
          Label teamLabel = new Label(soloTeam.getTeamName());
          teamLabel.setStyle("-fx-font-size: 13px;");
          byeBox.getChildren().addAll(byeTitle, teamLabel);
          javafx.scene.layout.HBox completePackage = new javafx.scene.layout.HBox(0);
          completePackage.setAlignment(Pos.CENTER_LEFT);
          //Gives the box a tail if the bye box doesn't fall in the first column.
          if (curCol > 0) {
            javafx.scene.layout.Region tail = new javafx.scene.layout.Region();
            tail.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1px 0 0 0;");
            tail.setPrefWidth(35);
            tail.setMinWidth(35);
            javafx.scene.layout.HBox.setMargin(tail, new Insets(20, 0, 0, 0));
            completePackage.getChildren().addAll(tail, byeBox);
          } else {
            completePackage.getChildren().add(byeBox);
          }
          
          GridPane.setValignment(completePackage, VPos.CENTER);
          loserGrid.add(completePackage, curCol * 2, loserRow);
          break;
        }
        //Same as winner's bracket but for the teams that already have one loss.
        Team teamA = currentLoserTeams.get(i);
        Team teamB = currentLoserTeams.get(i + 1);
        boolean isChampMatch = (currentLoserTeams.size() == 2);
        boolean isFirstRound = curCol == 0;
        int loserRoundNum = curCol;
        String loserTitle = "Round " + loserRoundNum + " (Match " + losersMatchNum + ")";
        javafx.scene.layout.HBox matchCard = matchMaker(loserTitle, teamA, teamB, isChampMatch, isFirstRound, simType);
        GridPane.setValignment(matchCard, VPos.CENTER);
        loserGrid.add(matchCard, curCol * 2, loserRow);
        loserRowHistory.add(loserRow);
        //Manual score entry for loser's bracket.
        if(simType.equals("Manual")) {
          harvestTextFields(matchCard, loserRoundFields);
        }
        loserRow += rowStep;
        losersMatchNum++;
      }
      //Spacing out teams for the loser's bracket.
      if(loserRowHistory.size() >= 2) {
        int historyIndex = 0;
        int nextRowOffset = (int)Math.pow(2, curCol + 1) - 1;
        int nextRowStep = (int)Math.pow(2, curCol + 2);
        int targetRow = nextRowOffset;
        for(int j = 0; j < currentLoserTeams.size(); j += 2) {
          if (historyIndex + 1 >= loserRowHistory.size()) {
            int rowA = loserRowHistory.get(historyIndex);
            int rowB = loserRowHistory.get(historyIndex + 1);
            drawBracketForks(loserGrid, (curCol * 2) + 1, rowA, rowB, targetRow);
            historyIndex += 2;
            targetRow += nextRowStep;
          }
        }
      }
    }
    simButton.setOnAction(null);
    simButton.setOnAction(e -> {
      if (simType.equals("Manual") && currentWinnerTeams.size() >= 2) {
        int checkCounter = 0;
        for (int i = 0; i < currentWinnerTeams.size(); i += 2) {
          //Checks for a tie in the manual score entry.
          try {
            javafx.scene.control.TextField checkFieldA = winnerRoundFields.get(checkCounter);
            javafx.scene.control.TextField checkFieldB = winnerRoundFields.get(checkCounter + 1);
            int scoreA = Integer.parseInt(checkFieldA.getText().trim());
            int scoreB = Integer.parseInt(checkFieldB.getText().trim());
            if (scoreA == scoreB) {
              Team tA = (Team) checkFieldA.getUserData();
              Team tB = (Team) checkFieldB.getUserData();
              Alert tieAlert = new Alert(AlertType.WARNING);
              tieAlert.setTitle("TIE SCORE DETECTED!!!");
              tieAlert.setHeaderText("Tournament Rules Require a Winner");
              tieAlert.setContentText("The match between " + tA.getTeamName() + " and " + tB.getTeamName() + " ended in a tie! Please enter a tie-breaker score.");
              tieAlert.showAndWait();
              return;
            }
            checkCounter += 2;
          } catch(Exception ex) { return; }
        }
      }
      ArrayList<Team> nextWinners = new ArrayList<>();
      ArrayList<Team> newLosers = new ArrayList<>();
      int fieldCounter = 0;
      if(currentWinnerTeams.size() >= 2) {
        for(int i = 0; i < currentWinnerTeams.size(); i += 2) {
          Team winner = null;
          Team loser = null;
          //Runs the selected sim type after a tie has been checked for.
          if(simType.equals("Automatic")) {
            winner = simulateMatch(currentWinnerTeams.get(i), currentWinnerTeams.get(i + 1));
            loser = (winner == currentWinnerTeams.get(i)) ? currentWinnerTeams.get(i + 1) : currentWinnerTeams.get(i);
          } else {
            javafx.scene.control.TextField textFieldA = winnerRoundFields.get(fieldCounter);
            javafx.scene.control.TextField textFieldB = winnerRoundFields.get(fieldCounter + 1);
            int scoreA = Integer.parseInt(textFieldA.getText().trim());
            int scoreB = Integer.parseInt(textFieldB.getText().trim());
            Team tA = (Team)textFieldA.getUserData();
            Team tB = (Team)textFieldB.getUserData();
            if(scoreA > scoreB) {
              winner = tA;
              loser = tB;
            } else {
              winner = tB;
              loser = tA;
            }
            fieldCounter += 2;
          }
          nextWinners.add(winner);
          newLosers.add(loser);
        }
      } else {
        nextWinners.addAll(currentWinnerTeams);
      }
      ArrayList<Team> survivingLosers = new ArrayList<>();
      int lFieldCounter = 0;
      //Adds the winners of the current round in the loser's bracket to the next round.
      for(int i = 0; i < currentLoserTeams.size(); i += 2) {
        Team survivor = null;
        if (i + 1 >= currentLoserTeams.size()) {
          survivor = currentLoserTeams.get(i); 
          survivingLosers.add(survivor);
          break;
        }
        if(simType.equals("Automatic")) {
          survivor = simulateMatch(currentLoserTeams.get(i), currentLoserTeams.get(i + 1));
        } else {
          javafx.scene.control.TextField textFieldA = loserRoundFields.get(lFieldCounter);
          javafx.scene.control.TextField textFieldB = loserRoundFields.get(lFieldCounter + 1);
          int scoreA = Integer.parseInt(textFieldA.getText().trim());
          int scoreB = Integer.parseInt(textFieldB.getText().trim());
          Team tA = (Team)textFieldA.getUserData();
          Team tB = (Team)textFieldB.getUserData();
          if(scoreA > scoreB) {
            survivor = tA;
          } else {
            survivor = tB;
          }
          lFieldCounter += 2;
        }
        survivingLosers.add(survivor);
      }
      ArrayList<Team> nextLoserPool = combineLosers(survivingLosers, newLosers);
      javafx.application.Platform.runLater(() -> {
          displayDouble(winnerGrid, loserGrid, nextWinners, nextLoserPool, simType, simButton, curCol + 1);
      });
    });
  }

  private void displayRobin(GridPane grid, ArrayList<ArrayList<Team[]>> schedule, String simType, Button simButton) {
    grid.getChildren().clear();
    ArrayList<javafx.scene.control.TextField> roundFields = new ArrayList<>();
    //Outer loop to set the boxes for the number of rounds there are.
    for(int colRound = 0; colRound < schedule.size(); colRound++) {
      VBox roundColumnBox = new VBox(10);
      roundColumnBox.setAlignment(Pos.TOP_CENTER);
      Label roundHeader = new Label("Round " + (colRound + 1));
      roundHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e; -fx-padding: 5;");
      roundColumnBox.getChildren().add(roundHeader);
      ArrayList<Team[]> roundMatches = schedule.get(colRound);
      //Inner loop to populate the number of matches there are in that round with the right teams.
      for(int matchNum = 0; matchNum < roundMatches.size(); matchNum++) {
        Team teamA = roundMatches.get(matchNum)[0];
        Team teamB = roundMatches.get(matchNum)[1];
        Boolean isFirstRound = (colRound == 0);
        javafx.scene.layout.HBox matchCard = matchMaker("Round " + (colRound + 1) + " (Match " + (matchNum + 1) + ")", teamA, teamB, false, isFirstRound, simType);
        roundColumnBox.getChildren().add(matchCard);
        if(simType.equals("Manual") && colRound == this.currentRoundRobinRound) {
          harvestTextFields(matchCard, roundFields);
        }
      }
      grid.add(roundColumnBox, colRound, 0);
    }
    simButton.setOnAction(e -> {
      if(this.currentRoundRobinRound >= schedule.size()) return;
      ArrayList<Team[]> activeMatches = schedule.get(this.currentRoundRobinRound);
      int fieldCounter = 0;
      //Checks the scores for manual entry mode. Allows for ties because there is a table with points.
      if(simType.equals("Manual")) {
        for(int i = 0; i < activeMatches.size(); i++) {
          try {
            Integer.parseInt(roundFields.get(fieldCounter).getText().trim());
            Integer.parseInt(roundFields.get(fieldCounter + 1).getText().trim());
            fieldCounter += 2;
          } catch(Exception ex) { return; }
        }
      }
      fieldCounter = 0;
      for(Team[] match : activeMatches) {
        Team teamA = match[0];
        Team teamB = match[1];
        int scoreA = 0;
        int scoreB = 0;
        //Simulates automatically without calling simulateMatch because round robin doesn't need a definitive winner.
        if(simType.equals("Automatic")) {
          scoreA = (int)(teamA.matchPower() * 10) + (int)(Math.random() * 10);
          scoreB = (int)(teamB.matchPower() * 10) + (int)(Math.random() * 10);
        } else {
          //Same method for manual mode though.
          scoreA = Integer.parseInt(roundFields.get(fieldCounter).getText().trim());
          scoreB = Integer.parseInt(roundFields.get(fieldCounter + 1).getText().trim());
          fieldCounter += 2;
        }
        //Allows for tracking of stats across the tournament to populate the table.
        if(scoreA > scoreB) {
          teamA.addLeagueResult(3, "WIN");
          teamB.addLeagueResult(0, "LOSS");
        } else if(scoreB > scoreA) {
          teamB.addLeagueResult(3, "WIN");
          teamA.addLeagueResult(0, "LOSS");
        } else {
          teamA.addLeagueResult(1, "DRAW");
          teamB.addLeagueResult(1, "DRAW");
        }
      }
      updateLeaderboard();
      this.currentRoundRobinRound++;
      if(this.currentRoundRobinRound < schedule.size()) {
        simButton.setText("SIMULATE ROUND " + (this.currentRoundRobinRound + 1));
        displayRobin(grid, schedule, simType, simButton);
      } else {
        simButton.setText("Round Robin Complete");
        simButton.setDisable(true);
        Alert completeAlert = new Alert(AlertType.INFORMATION, "All matches processed successfully! Press OK to view the leaderboard.");
        completeAlert.setTitle("Round Robin Simulation Complete");
        completeAlert.setHeaderText("We Have a Winner!!!");
        completeAlert.showAndWait();
      }
    });
  }

private Team simulateMatch(Team teamA, Team teamB) {
  //Simulates matches in automatic mode. Requires a winner.
    double powerA = teamA.matchPower();
    double powerB = teamB.matchPower();
    double totalPool = powerA + powerB;
    double teamAcutoff = powerA / totalPool;
    if(Math.random() < teamAcutoff) {
      return teamA;
    } else {
      return teamB;
    }
  }

  private javafx.scene.layout.HBox matchMaker(String title, Team teamA, Team teamB, Boolean isChampionship, Boolean firstRound, String simType) {
    VBox container = new VBox(5);
    container.setAlignment(Pos.CENTER);
    container.setPadding(new Insets(10));
    container.setPrefWidth(200);
    container.setMinWidth(200);
    //Creates match cards for each round of the tournament.
    if (isChampionship) {
      container.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1px; -fx-border-radius: 5px; -fx-background-color: #ffffff; -fx-background-radius: 5px;");
    } else {
      container.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1px; -fx-border-radius: 5px; -fx-background-color: #f8f9fa; -fx-background-radius: 5px;");
    }
    Label titleLabel = new Label(title);
    titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #7f8c8d; -fx-font-size: 11px;");
    container.getChildren().add(titleLabel);
    boolean isManual = simType.equals("Manual");
    javafx.scene.layout.HBox rowA = new javafx.scene.layout.HBox(10);
    rowA.setAlignment(Pos.CENTER_RIGHT);
    Label labelA = new Label();
    labelA.setStyle("-fx-font-size: 13px;");
    rowA.getChildren().add(labelA);
    //Creates the fields to allow for manual score entry.
    if(isManual) {
      labelA.setText(teamA.getTeamName());
      javafx.scene.control.TextField scoreFieldA = new javafx.scene.control.TextField("0");
      scoreFieldA.setPrefWidth(45);
      scoreFieldA.setUserData(teamA);
      rowA.getChildren().add(scoreFieldA);
    } else {
      labelA.setText(teamA.getTeamName() + " (" + String.format("%.1f", teamA.getWinChance()) + "%)");
    }
    Label vsLabel = new Label("vs");
    vsLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic; -fx-font-size: 11px;");
    javafx.scene.layout.HBox rowB = new javafx.scene.layout.HBox(10);
    rowB.setAlignment(Pos.CENTER_RIGHT);
    Label labelB = new Label();
    labelB.setStyle("-fx-font-size: 13px;");
    rowB.getChildren().add(labelB);
    if(isManual) {
      labelB.setText(teamB.getTeamName());
      javafx.scene.control.TextField scoreFieldB = new javafx.scene.control.TextField("0");
      scoreFieldB.setPrefWidth(45);
      scoreFieldB.setUserData(teamB);
      rowB.getChildren().add(scoreFieldB);
    } else {
      labelB.setText(teamB.getTeamName() + " (" + String.format("%.1f", teamB.getWinChance()) + "%)");
    }
    container.getChildren().addAll(rowA, vsLabel, rowB);
    //Creates a left facing tail to connect to the bracket forks for a complete design.
    javafx.scene.layout.Region leftTail = new javafx.scene.layout.Region();
    leftTail.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1px 0 0 0;");
    leftTail.setPrefWidth(35);
    leftTail.setMinWidth(35);
    javafx.scene.layout.HBox.setMargin(leftTail, new Insets(45, 0, 0, 0));
    javafx.scene.layout.HBox completeCardPackage = new javafx.scene.layout.HBox(0);
    completeCardPackage.setAlignment(Pos.CENTER_LEFT);
    if(firstRound || title.startsWith("Round")) {
      completeCardPackage.getChildren().addAll(container);
    } else {
      completeCardPackage.getChildren().addAll(leftTail, container);
    }
    return completeCardPackage;
  }

  private void drawBracketForks(GridPane grid, int lineCol, int rowA, int rowB, int targetRow) {
    int rowSpan = (rowB - rowA) + 1;
    //Draws an upside-down L to form the top half of the fork.
    javafx.scene.layout.Region topForkHalf = new javafx.scene.layout.Region();
    topForkHalf.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1px 1px 0 0;");
    topForkHalf.setMinWidth(35);
    topForkHalf.setPrefWidth(35);
    GridPane.setValignment(topForkHalf, javafx.geometry.VPos.CENTER);
    int topSpan = (targetRow - rowA) + 1;
    GridPane.setConstraints(topForkHalf, lineCol, rowA, 1, topSpan);
    grid.getChildren().add(topForkHalf);
    //Draws a backwards L to form the bottom half of the bracket fork.
    javafx.scene.layout.Region bottomForkHalf = new javafx.scene.layout.Region();
    bottomForkHalf.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 0 1px 1px 0;");
    bottomForkHalf.setMinWidth(35);
    bottomForkHalf.setPrefWidth(35);
    GridPane.setValignment(bottomForkHalf, javafx.geometry.VPos.CENTER);
    int bottomSpan = (rowB - targetRow) + 1;
    GridPane.setConstraints(bottomForkHalf, lineCol, targetRow, 1, bottomSpan);
    grid.getChildren().add(bottomForkHalf);
  }

  private void updateLeaderboard() {
    leaderboardSideBar.getChildren().clear();
    Label title = new Label("LIVE LEAGUE STANDINGS");
    title.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 13px; -fx-padding: 0 0 10 0;");
    leaderboardSideBar.getChildren().add(title);
    ArrayList<Team> sortedTeams = new ArrayList<>(tournamentTeams);
    //Compares teams to update the rankings.
    sortedTeams.sort((t1, t2) -> Integer.compare(t2.getLeaguePoints(), t1.getLeaguePoints()));
    int rank = 1;
    for(Team team : sortedTeams) {
      javafx.scene.layout.HBox rankRow = new javafx.scene.layout.HBox(10);
      rankRow.setPadding(new Insets(6, 10, 6, 10));
      rankRow.setAlignment(Pos.CENTER_LEFT);
      rankRow.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e1b12c; -fx-border-width: 0 0 1px 0; -fx-background-radius: 3;");
      //Displays team name.
      Label rankLabel = new Label("#" + rank + " - " + team.getTeamName());
      rankLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #353b48;");
      javafx.scene.layout.HBox.setHgrow(rankLabel, javafx.scene.layout.Priority.ALWAYS);
      //Displays the team's record.
      Label recordLabel = new Label(team.getRecordString());
      recordLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
      //Displays the number of points they have.
      Label pointsLabel = new Label(team.getLeaguePoints() + " pts");
      pointsLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60;");
      rankRow.getChildren().addAll(rankLabel, recordLabel, pointsLabel);
      leaderboardSideBar.getChildren().add(rankRow);
      rank++;
    }
  }

  private void harvestTextFields(javafx.scene.layout.HBox matchCard, ArrayList<javafx.scene.control.TextField> fieldList) {
    //Helper method to harvest text fields in manual mode.
    VBox innerBox = (VBox) matchCard.getChildren().get(matchCard.getChildren().size() - 1);
    for(javafx.scene.Node child : innerBox.getChildren()) {
      if(child instanceof javafx.scene.layout.HBox) {
        javafx.scene.layout.HBox matchRow = (javafx.scene.layout.HBox) child;
        if(matchRow.getChildren().size() > 1 && matchRow.getChildren().get(1) instanceof javafx.scene.control.TextField) {
          fieldList.add((javafx.scene.control.TextField) matchRow.getChildren().get(1));
        }
      }
    }
  }

  private ArrayList<Team> combineLosers(ArrayList<Team> survivors, ArrayList<Team> newLosers) {
    //Helper method to easily combine the losers in double elimination.
    ArrayList<Team> combined = new ArrayList<>();
    combined.addAll(survivors);
    combined.addAll(newLosers);
    return combined;
  }
}

