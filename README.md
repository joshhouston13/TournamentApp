Tournament Simulation Engine App  

A JavaFX application that simulates sports tournaments under these three formats: Single Elimination, Double Elimination, and Round Robin. Matches can be simulated automatically using custom skill metrics or managed via manual score entry windows.  

Requirments & System Prerequisites  
**Java Development Kit (JDK)  
**JavaFX SDK  

How to Run the Program  
-I was running on my windows platform, so I ended up using the following command within the terminal inside the project root folder:  
    javac --module-path "c:\javafx-sdk\javafx-sdk-25.0.3\lib" --add-modules javafx.controls,javafx.graphics TournamentApp.java FileManager.java Team.java Underdog.java Player.java  
to compile the files and then:  
    java --module-path "c:\javafx-sdk\javafx-sdk-25.0.3\lib" --add-modules javafx.controls,javafx.graphics TournamentApp  
to run the master program.  
-In the linux platform used through the majority of the class, the code can be run with the following command in the terminal inside the project root folder:  
    mvn javafx:run  
to run the javaFX program.  
-Alternatively, I made a run.bat file inside the project folder which can be used to run the program on my Windows platform.  

Required External Files  
**players.txt  
    -(Player Name; Rating; Team Name)  
**teams.txt  
    -(Seed, Team Name, Underdog(optional))  

GitHub Repository Link  
    https://github.com/joshhouston13/TournamentApp