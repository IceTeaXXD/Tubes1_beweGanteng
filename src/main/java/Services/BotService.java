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
    private boolean isFired = false;

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
        // playerAction.action = PlayerActions.FORWARD;
        if (!gameState.getGameObjects().isEmpty()) {
            if (tickCount != gameState.getWorld().getCurrentTick()){
                var foodList = gameState.getGameObjects()
                        .stream().filter(item -> item.getGameObjectType() == ObjectTypes.FOOD)
                        .sorted(Comparator
                                .comparing(item -> getDistanceBetween(bot, item)))
                        .collect(Collectors.toList());
                var superFoodList = gameState.getGameObjects()
                        .stream().filter(item -> item.getGameObjectType() == ObjectTypes.SUPER_FOOD)
                        .sorted(Comparator
                                .comparing(item -> getDistanceBetween(bot, item)))
                        .collect(Collectors.toList());
                var worldRadius = gameState.getWorld().getRadius();
                var worldCenter = new GameObject(UUID.randomUUID(), 0, 0, 0, new Position(0, 0), null, 0, 0, 0, 0, 0);
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
                var asteroidList = gameState.getGameObjects()
                        .stream().filter(item -> item.getGameObjectType() == ObjectTypes.ASTEROID_FIELD)
                        .sorted(Comparator
                                .comparing(item -> getDistanceBetween(bot, item)))
                        .collect(Collectors.toList());
                var gasCloudList = gameState.getGameObjects()
                        .stream().filter(item -> item.getGameObjectType() == ObjectTypes.GAS_CLOUD)
                        .sorted(Comparator
                                .comparing(item -> getDistanceBetween(bot, item)))
                        .collect(Collectors.toList());

                /* EATING */
                if (foodList.size() > 0){
                    var nearestFood = getHeadingBetween(foodList.get(0));
                    var nearestSuperFood = getHeadingBetween(superFoodList.get(0));
                    if (nearestFood < nearestSuperFood) {
                        playerAction.heading = nearestFood;
                        playerAction.action = PlayerActions.FORWARD;
                        botAction = "EATING FOOD";
                    }
                    else {
                        playerAction.heading = nearestSuperFood;
                        playerAction.action = PlayerActions.FORWARD;
                        botAction = "EATING SUPERFOOD";
                    }
                }

                /* ATTACKING */
                if (playerList.size() > 0){
                    var nearestPlayer = playerList.get(0);
                    if (bot.size >= 20 && getDistanceBetween(bot, nearestPlayer) - bot.size - nearestPlayer.size < 300 && bot.torpedoSalvoCount > 0){
                        playerAction.heading = getHeadingBetween(nearestPlayer);
                        playerAction.action = PlayerActions.FIRETORPEDOES;
                        botAction = "FIRING TORPEDO";
                        isFired = true;
                    }
                    // if (bot.size > 30 && bot.size > nearestPlayer.size && bot.teleporterCount && getDistanceBetween(bot, nearestPlayer) < 150){
                    //     playerAction.heading = getHeadingBetween(nearestPlayer);
                    //     playerAction.action = PlayerActions.FIRE_TELEPORTER;
                    // }
                    else if ((bot.getSize() - 20) > nearestPlayer.getSize() && getDistanceBetween(bot, nearestPlayer)< 300){
                        playerAction.heading = getHeadingBetween(nearestPlayer);
                        playerAction.action = PlayerActions.FORWARD;
                        botAction = "CHASING SMALLER PLAYER";
                    }
                }

                /* DEFENSE */
                if (torpedoList.size() > 0) {
                    System.out.print("TORPEDO DISTANCEEEEEEEEEEEEEEEEEEEEEE : ");
                    System.out.println(getDistanceBetween(bot, torpedoList.get(0)) - bot.getSize());
                    if (!isFired && getDistanceBetween(bot, torpedoList.get(0)) - bot.getSize() < 100 && bot.shieldCount > 0 && bot.size > 40){
                        playerAction.heading = (getHeadingBetween(torpedoList.get(0)) + 180);
                        playerAction.action = PlayerActions.ACTIVATESHIELD;
                        botAction = "ACTIVATING SHIELD";
                    }
                }
                
                if (playerList.size() > 0){
                    var nearestPlayer = playerList.get(0);
                    if (bot.getSize() < nearestPlayer.getSize() && getDistanceBetween(bot, nearestPlayer) - bot.getSize() - nearestPlayer.getSize() < 50 + bot.size && bot.shieldCount == 0){
                        playerAction.heading = (getHeadingBetween(nearestPlayer) + 180);
                        playerAction.action = PlayerActions.FORWARD;
                        botAction = "RUNNING FROM BIGGER PLAYER";
                    }
                }

                /* MOVEMENT */
                if (asteroidList.size() > 0){
                    var nearestAsteroid = asteroidList.get(0);
                    if ((getDistanceBetween(bot, nearestAsteroid) - bot.size) < 50){
                        playerAction.heading = (getHeadingBetween(nearestAsteroid) + 180);
                        playerAction.action = PlayerActions.FORWARD;
                        botAction = "AVOIDING ASTEROID";
                    }
                }

                if (gasCloudList.size() > 0){
                    var nearestGasCloud = gasCloudList.get(0);
                    if (getDistanceBetween(bot, nearestGasCloud) - bot.size < 50){
                        playerAction.heading = (getHeadingBetween(nearestGasCloud) + 180);
                        playerAction.action = PlayerActions.FORWARD;
                        botAction = "AVOIDING GAS CLOUD";
                    }
                }

                if (getDistanceBetween(bot, worldCenter) + bot.size + 100 > worldRadius) {
                    System.out.println("AVOIDING EDGE");
                    playerAction.heading = getHeadingBetween(worldCenter);
                }

                /* CRITICAL */
                // if (foodList.size() > 0 && bot.size < 20){
                //     var nearestFood = getDistanceBetween(bot, foodList.get(0));
                //     var nearestSuperFood = getDistanceBetween(bot, superFoodList.get(0)) * 0.99;
                //     if (nearestFood < nearestSuperFood) {
                //         playerAction.heading = getHeadingBetween(foodList.get(0));
                //         botAction = "EATING FOOD";
                //     }
                //     else {
                //         playerAction.heading = getHeadingBetween(superFoodList.get(0));
                //         botAction = "EATING SUPERFOOD";
                //     }
                // }
                if (torpedoList.size() > 0) {
                    System.out.print("TORPEDO DISTANCEEEEEEEEEEEEEEEEEEEEEE : ");
                    System.out.println(getDistanceBetween(bot, torpedoList.get(0)) - bot.getSize());
                    if (!isFired && getDistanceBetween(bot, torpedoList.get(0)) - bot.getSize() < 100 && bot.shieldCount > 0 && bot.size > 40){
                        playerAction.heading = (getHeadingBetween(torpedoList.get(0)) + 180);
                        playerAction.action = PlayerActions.ACTIVATESHIELD;
                        botAction = "ACTIVATING SHIELD";
                    }
                }

                if (botAction != "FIRING TORPEDO" && isFired){
                    isFired = false;
                }
                System.out.println("Nearest player size: " + playerList.get(0).size);
                System.out.println("Bot size: " + bot.size);
                System.out.println("Distance from nearest player: " + getDistanceBetween(bot, playerList.get(0)));
                tickCount = gameState.getWorld().getCurrentTick();
                // System.out.println("Tick count: " + tickCount);
                // System.out.println("Current tick: " + gameState.getWorld().getCurrentTick());
                System.out.println(botAction);
                this.playerAction = playerAction;
            }
        }
        // System.out.println("Tick count: " + tickCount);
        // System.out.println("Current tick: " + gameState.getWorld().getCurrentTick());
        // System.out.println(botAction);
        // this.playerAction = playerAction;
    }

    private int getHeadingToBot(GameObject otherObject) {
        var direction = toDegrees(Math.atan2(bot.getPosition().y - otherObject.getPosition().y,
                bot.getPosition().x - otherObject.getPosition().x));
        return (direction + 360) % 360;
    }

    private boolean botInSight(GameObject otherObject) {
        for (int i = 0; i < 45; i++) {
            if (getHeadingToBot(otherObject) + i == otherObject.getCurrentHeading() || getHeadingToBot(otherObject) - i == otherObject.getCurrentHeading()) {
                return true;
            }
        }
        return false;
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