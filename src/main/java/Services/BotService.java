package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class BotService {
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;
    public String botAction;
    private Integer tickCount = 0;
    private boolean isTeleported = false;
    private boolean teleportNear = false;
    private boolean teleportSmall = false;
    private Integer tickTeleport = 0;
    private Integer tickSuperNova = 0;
    private boolean superNovaTeleport = false;
    private boolean superNovaFired = false;
    public BotService() {
        this.playerAction = new PlayerAction();
        this.gameState = new GameState();
    }

    public GameObject getBot() {
        return this.bot;
    }

    public void setBot(GameObject bot) {
        this.bot = bot;
    }

    public PlayerAction getPlayerAction() {
        return this.playerAction;
    }

    public void setPlayerAction(PlayerAction playerAction) {
        this.playerAction = playerAction;
    }

    public void computeNextPlayerAction(PlayerAction playerAction) {
        if (!gameState.getGameObjects().isEmpty()) {
            if (tickCount != gameState.getWorld().getCurrentTick()){
                var worldRadius = gameState.getWorld().getRadius();
                var worldCenter = new GameObject(UUID.randomUUID(), 0, 0, 0, new Position(0, 0), null, 0, 0, 0, 0, 0);
                var foodList = gameState.getGameObjects()
                        .stream().filter(item -> (item.getGameObjectType() == ObjectTypes.FOOD || item.getGameObjectType() == ObjectTypes.SUPER_FOOD))
                        .sorted(Comparator
                                .comparing(item -> getDistanceBetween(bot, item)))
                        .collect(Collectors.toList());
                var torpedoList = gameState.getGameObjects()
                        .stream().filter(item -> item.getGameObjectType() == ObjectTypes.TORPEDO_SALVO)
                        .sorted(Comparator
                                .comparing(item -> getDistanceBetween(bot, item)))
                        .collect(Collectors.toList());
                var playerList = gameState.getPlayerGameObjects()
                        .stream().filter(item -> item.id != bot.id)
                        .sorted(Comparator
                                .comparing(item -> getDistanceBetween(bot, item)))
                        .collect(Collectors.toList());
                var smallestPlayer = gameState.getPlayerGameObjects()
                        .stream().filter(item -> item.id != bot.id)
                        .sorted(Comparator
                                .comparing(item -> item.getSize()))
                        .collect(Collectors.toList()).get(0);
                var biggestPlayer = gameState.getPlayerGameObjects()
                        .stream().filter(item -> item.id != bot.id)
                        .sorted(Comparator
                                .comparing(item -> item.getSize()))
                        .collect(Collectors.toList()).get(playerList.size() - 1);
                var obstacleList = gameState.getGameObjects()
                        .stream().filter(item -> ((item.getGameObjectType() == ObjectTypes.GAS_CLOUD) || item.getGameObjectType() == ObjectTypes.ASTEROID_FIELD) || item.getGameObjectType() == ObjectTypes.WORMHOLE)
                        .sorted(Comparator
                                .comparing(item -> getDistanceBetween(bot, item)))
                        .collect(Collectors.toList());
                var superNova = gameState.getGameObjects()
                        .stream().filter(item -> item.getGameObjectType() == ObjectTypes.SUPERNOVA_PICKUP)
                        .sorted(Comparator
                                .comparing(item -> getDistanceBetween(bot, item)))
                        .collect(Collectors.toList());
                var superNovaCloud = gameState.getGameObjects()
                        .stream().filter(item -> item.getGameObjectType() == ObjectTypes.SUPERNOVA_BOMB)
                        .sorted(Comparator
                                .comparing(item -> getDistanceBetween(bot, item)))
                        .collect(Collectors.toList());

                /* EATING */
                playerAction.action = PlayerActions.FORWARD;
                playerAction.heading = getHeadingBetween(worldCenter);
                if (foodList.size() > 0){
                    var nearestFood = foodList.get(0);
                    playerAction.heading = getHeadingBetween(nearestFood);
                    playerAction.action = PlayerActions.FORWARD;
                    botAction = "EATING FOOD";
                }

                /* ATTACKING */
                if (playerList.size() > 0){
                    var nearestPlayer = playerList.get(0);
                    if (
                        isBotInSight(obstacleList, nearestPlayer)
                        && bot.size > 30 
                        && getDistanceBetween(bot, nearestPlayer) - bot.size - nearestPlayer.size < 500 
                        && bot.torpedoSalvoCount > 0
                        ){
                        playerAction.heading = getHeadingBetween(nearestPlayer);
                        playerAction.action = PlayerActions.FIRETORPEDOES;
                        botAction = "FIRING TORPEDO";
                    }

                    if (
                        (bot.getSize() - 20) > nearestPlayer.getSize() 
                        && getDistanceBetween(bot, nearestPlayer) < 300 
                        && bot.shieldCount == 0 
                        && bot.torpedoSalvoCount == 0
                        ){
                        playerAction.heading = getHeadingBetween(nearestPlayer);
                        playerAction.action = PlayerActions.FORWARD;
                        botAction = "CHASING SMALLER PLAYER";
                    }
                    
                    if (
                        bot.size > 50 
                        && bot.size - 20 > nearestPlayer.size 
                        && bot.teleporterCount > 0 && !isTeleported 
                        && getDistanceBetween(bot, nearestPlayer) - bot.size - nearestPlayer.size < 600
                        ){
                        playerAction.heading = getHeadingBetween(nearestPlayer);
                        playerAction.action = PlayerActions.FIRETELEPORT;
                        botAction = "FIRE TELEPORTER";
                        isTeleported = true;
                        teleportNear = true;
                        teleportSmall = false;
                        tickTeleport = gameState.getWorld().getCurrentTick();
                    }

                    if (
                        bot.size > 50 
                        && bot.size - 20 > smallestPlayer.size 
                        && bot.teleporterCount > 0 && !isTeleported 
                        && getDistanceBetween(bot, smallestPlayer) - bot.size - nearestPlayer.size < 600
                        ){
                        playerAction.heading = getHeadingBetween(smallestPlayer);
                        playerAction.action = PlayerActions.FIRETELEPORT;
                        botAction = "FIRE TELEPORTER";
                        isTeleported = true;
                        teleportNear = false;
                        teleportSmall = true;
                        tickTeleport = gameState.getWorld().getCurrentTick();
                    }
                }

                /* DEFENSE */
                if (playerList.size() > 0){
                    var nearestPlayer = playerList.get(0);
                    if (bot.getSize() < 30 
                        && bot.getSize() < nearestPlayer.getSize() 
                        && getDistanceBetween(bot, nearestPlayer) - bot.getSize() - nearestPlayer.getSize() < 100 + bot.size
                        ){
                        playerAction.heading = (getHeadingBetween(nearestPlayer) + 180);
                        playerAction.action = PlayerActions.FORWARD;
                        botAction = "RUNNING FROM BIGGER PLAYER";
                    }
                }

                if (torpedoList.size() > 0) {
                    if (isTorpedoComing(torpedoList.get(0)) 
                        && getDistanceBetween(bot, torpedoList.get(0)) - bot.size < 100 
                        && bot.shieldCount > 0 && bot.size > 40
                        ){
                        playerAction.action = PlayerActions.ACTIVATESHIELD;
                        botAction = "ACTIVATING SHIELD";
                    }
                }

                if (getDistanceBetween(bot, worldCenter) + bot.size + 100 > worldRadius) {
                    System.out.println("AVOIDING EDGE");
                    playerAction.heading = getHeadingBetween(worldCenter);
                }

                /* TELEPORTING */
                if(playerList.size() > 0){
                    var nearestPlayer = playerList.get(0);
                    if (
                        bot.size > nearestPlayer.size 
                        && isTeleported 
                        && gameState.getWorld().getCurrentTick() - tickTeleport >= (getDistanceBetween(bot, nearestPlayer) - bot.size - nearestPlayer.size) / 20 
                        && teleportNear
                        ){
                        playerAction.heading = getHeadingBetween(nearestPlayer);
                        playerAction.action = PlayerActions.TELEPORT;
                        botAction = "TELEPORTING";
                        isTeleported = false;
                        teleportNear = false;
                        teleportSmall = false;
                        tickTeleport = gameState.getWorld().getCurrentTick();
                    }

                    if (
                        bot.size > smallestPlayer.size 
                        && isTeleported 
                        && gameState.getWorld().getCurrentTick() - tickTeleport >= (getDistanceBetween(bot, smallestPlayer) - bot.size - smallestPlayer.size) / 20 
                        && teleportSmall
                        ){
                        playerAction.heading = getHeadingBetween(smallestPlayer);
                        playerAction.action = PlayerActions.TELEPORT;
                        botAction = "TELEPORTING";
                        isTeleported = false;
                        teleportNear = false;
                        teleportSmall = false;
                        tickTeleport = gameState.getWorld().getCurrentTick();
                    }
                }

                /* SUPERNOVA */
                if (superNova.size() > 0){
                    System.out.println("SUPERNOVA DETECTED");
                    if (
                        bot.size > 50
                        && !isTeleported
                        && bot.teleporterCount > 0
                    ){
                        playerAction.heading = getHeadingBetween(superNova.get(0));
                        playerAction.action = PlayerActions.FIRETELEPORT;
                        botAction = "TELEPORT TO SUPERNOVA";
                        superNovaTeleport = true;
                        isTeleported = true;
                        tickTeleport = gameState.getWorld().getCurrentTick();
                    }   
                }

                if (superNovaTeleport && superNova.size() > 0){
                    if (
                        isTeleported 
                        && gameState.getWorld().getCurrentTick() - tickTeleport >= (getDistanceBetween(bot, superNova.get(0)) - bot.size - superNova.get(0).size) / 20
                        ){
                            playerAction.heading = getHeadingBetween(superNova.get(0));
                            playerAction.action = PlayerActions.TELEPORT;
                            botAction = "TELEPORTING TO SUPERNOVA";
                            isTeleported = false;
                            teleportNear = false;
                            teleportSmall = false;
                            superNovaTeleport = false;
                            tickTeleport = gameState.getWorld().getCurrentTick();
                        }
                }

                if (
                    superNovaFired
                    && gameState.getWorld().getCurrentTick() - tickSuperNova >= (getDistanceBetween(bot,biggestPlayer) - biggestPlayer.size - bot.size) / 20
                ){
                    playerAction.heading = getHeadingBetween(biggestPlayer);
                    playerAction.action = PlayerActions.DETONATESUPERNOVA;
                    botAction = "DETONATING SUPERNOVA";
                    superNovaFired = false;
                }

                if (
                    bot.superNovaAvailable == 1 
                    && getDistanceBetween(bot, biggestPlayer) - bot.size - biggestPlayer.size > 150){
                    playerAction.heading = getHeadingBetween(biggestPlayer);
                    playerAction.action = PlayerActions.FIRESUPERNOVA;
                    botAction = "FIRE SUPERNOVA";
                    superNovaFired = true;
                    tickSuperNova = gameState.getWorld().getCurrentTick();
                }

                if (superNovaCloud.size() > 0){
                    if (getDistanceBetween(bot, superNovaCloud.get(0)) - bot.size - superNovaCloud.get(0).size < 100){
                        playerAction.heading = getHeadingBetween(superNovaCloud.get(0)) + 180;
                        playerAction.action = PlayerActions.FORWARD;
                        botAction = "RUNNING FROM SUPERNOVA";
                    }
                }

                /* GAME STATE PRINT */
                System.out.println("====================================================================================");
                System.out.println("SUPERNOVA AVAILABLE : " + bot.superNovaAvailable);
                System.out.println("Enemies : " + playerList.size());
                System.out.println("Nearest player size: " + playerList.get(0).size);
                System.out.println("Distance from nearest player: " + (getDistanceBetween(bot, playerList.get(0)) - bot.size - playerList.get(0).size));
                System.out.println("Smallest player size: " + smallestPlayer.size);
                System.out.println("Distance from smallest player: " + (getDistanceBetween(bot, smallestPlayer) - bot.size - smallestPlayer.size));
                System.out.println("Bot size: " + bot.size);
                tickCount = gameState.getWorld().getCurrentTick();
                System.out.println("Current Tick: " + tickCount);
                System.out.println(botAction);
                System.out.println("====================================================================================");
                this.playerAction = playerAction;
            }
        }
    }

    public boolean isBotInSight(List<GameObject> obstacles, GameObject target){
        // check if bot is heading to target but there is an obstacle in the way
        for (var obstacle : obstacles){
            if (
                getDistanceBetween(bot, obstacle) < getDistanceBetween(bot, target)
                && getHeadingBetween(obstacle) == getHeadingBetween(target)
                ){
                return false;
            }
        }
        return true;
    } 

    public boolean isTorpedoComing(GameObject torpedo){
        if (torpedo.getCurrentHeading() >= 0 
            && torpedo.getCurrentHeading() <= 90
            && bot.getCurrentHeading() >= 180 
            && bot.getCurrentHeading() <= 270
            ){
            return true;
        }
        else if (
            torpedo.getCurrentHeading() >= 90 
            && torpedo.getCurrentHeading() <= 180 
            && bot.getCurrentHeading() >= 270 
            && bot.getCurrentHeading() <= 360
            ){
            return true;
        }
        else if (
            torpedo.getCurrentHeading() >= 180 
            && torpedo.getCurrentHeading() <= 270 
            && bot.getCurrentHeading() >= 0 
            && bot.getCurrentHeading() <= 90
            ){
            return true;
        }
        else if (
            torpedo.getCurrentHeading() >= 270 
            && torpedo.getCurrentHeading() <= 360 
            && bot.getCurrentHeading() >= 90 
            && bot.getCurrentHeading() <= 180
            ){
            return true;
        }
        else{
            return false;
        }
    }

    public GameState getGameState() {
        return this.gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        updateSelfState();
    }

    private void updateSelfState() {
        Optional<GameObject> optionalBot = gameState.getPlayerGameObjects().stream().filter(gameObject -> gameObject.id.equals(bot.id)).findAny();
        optionalBot.ifPresent(bot -> this.bot = bot);
    }

    private double getDistanceBetween(GameObject object1, GameObject object2) {
        var triangleX = Math.abs(object1.getPosition().x - object2.getPosition().x);
        var triangleY = Math.abs(object1.getPosition().y - object2.getPosition().y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    private int getHeadingBetween(GameObject otherObject) {
        var direction = toDegrees(Math.atan2(otherObject.getPosition().y - bot.getPosition().y,
                otherObject.getPosition().x - bot.getPosition().x));
        return (direction + 360) % 360;
    }

    private int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }
}