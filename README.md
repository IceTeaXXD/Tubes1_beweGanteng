# Entelect 2021 Challenge - Galaxio
>Tugas Besar IF2211 Strategi Algortima
## General Information 
This repository contains the implementation of Entelect 2021 Challenge - Galaxio Bot using Greedy Algorithm.
## Technology Used
- Java Version 11
- Apache Maven (Compiler)
- .dotnet version 3.1
## Contributors (beweGanteng)
| NIM | Nama |
| :---: | :---: |
| 13521005 | Kelvin Rayhan Alkarim |
| 13521023 | Kenny Benaya Nathan  |
| 13521024 | Ahmad Nadil |
## Strategy
The bot will use Greedy Algorithm to determine the best strategy
1. Create a list of objects in game, which are foods, players, torpedoes, obstacles, supernova
2. Then, we determine the best move
3. Action that will be determined using a set of unique priorities
4. Priorities:
- Food<br>
Determining the closest food and moving to it
- Attack<br>
Chasing smaller player, firing torpedoes, teleporting, firing supernova bomb
- Defense<br>
Run away from bigger player, use shield
- Movement<br>
Avoiding edge of map by moving towards the center
## Project Structure
```bash
.
│   README.md
│   Dockerfile
│   pom.xml
│
├───src                    # Source code
│    └──main
│       └──java
│            ├─ Enums
│            │    ├── ObjectTypes.java
│            │    └── PlayerActions.java
│            ├─ Models
│            │    ├── GameObject.java
│            │    ├── GameState.java
│            │    ├── GameStateDto.java
│            │    ├── PlayerAction.java
│            │    ├── Position.java
│            │    └── World.java
│            │    
│            ├─ Services
│            │     └──BotService.java
│            └──Main.java
│            
│            
├───target                # bytecode         
│    ├─ classes
│    │   ├─ Enums
│    │   │    ├── ObjectTypes.class
│    │   │    └── PlayerActions.class
│    │   ├─ Models
│    │   │    ├── GameObject.class
│    │   │    ├── GameState.class
│    │   │    ├── GameStateDto.class
│    │   │    ├── PlayerAction.class
│    │   │    ├── Position.class
│    │   │    └── World.class
│    │   │    
│    │   ├─ Services
│    │   │     └──BotService.class
│    │   └──Main.class                          
│    │ 
│    ├─ generated-sources
│    ├─ libs
│    ├─ maven-archiver
│    ├─ maven-status
│    └──JavaBot.jar 
│    
│    
└───doc                    # Documentation
```

## Local Setup
1. [Click here to Download Galaxio Starter Pack](https://github.com/EntelectChallenge/2021-Galaxio/releases/tag/2021.3.2) 

2. Clone this repo to the `starter-bots` folder in the starter pack by using this link below

`git clone https://github.com/IceTeaXXD/Tubes1_beweGanteng.git`

3. Compile the program using maven in this bot directory

`mvn clean package`

4. Edit the `run.bat` file to run the bot, for example, you can use this command:

`java -jar ../starter-bots/Tubes1_beweGanteng/target/JavaBot.jar`

5. Execute the `run.bat` '

6. Run the visualizer to see watch the match

7. More instructions please head to
[Galaxio-2021 GitHub Repository](https://github.com/EntelectChallenge/2021-Galaxio)

## Documentation
![This is us!](https://raw.githubusercontent.com/IceTeaXXD/Tubes1_beweGanteng/main/doc/Foto-kelompok.jpg?token=GHSAT0AAAAAAB5KSVCQQEM6ORA3EM6YXAE4Y7PWBXA)