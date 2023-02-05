package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class BotService {
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;
    public GameObject worldCenter = new GameObject(UUID.randomUUID(), 0, 0, 0, new Position(0, 0), null, 0, 0, 0, 0, 0);
    public String botAction;
    public Integer teleportTick;
    public boolean isTeleporting;
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
            System.out.println(bot.getTeleporterCount());
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
            var nearestPlayer = gameState.getPlayerGameObjects()
                    .stream().filter(item -> item.id != bot.id)
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList()).get(0);
            var nearestFood = getHeadingBetween(foodList.get(0));
            var nearestGasCloud = gameState.getGameObjects()
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.GAS_CLOUD)
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList()).get(0);
            var nearestAsteroid = gameState.getGameObjects()
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.ASTEROID_FIELD)
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList()).get(0);
            var nearestSuperFood = getHeadingBetween(superFoodList.get(0));
            var distanceFromWorldCenter = getDistanceBetween(bot, worldCenter);
            var distanceFromGasCloud = getDistanceBetween(bot, nearestGasCloud);
            var distanceFromAsteroid = getDistanceBetween(bot, nearestAsteroid);
            // var torpedoList = gameState.getGameObjects()
            //         .stream().filter(item -> item.getGameObjectType() == ObjectTypes.TORPEDO_SALVO)
            //         .sorted(Comparator
            //                 .comparing(item -> getDistanceBetween(bot, item)))
            //         .collect(Collectors.toList());

            System.out.println("Nearest player size: " + nearestPlayer.size);
            System.out.println("Bot size: " + bot.size);
            System.out.println("Distance from nearest player: " + getDistanceBetween(bot, nearestPlayer));

            if (nearestFood < nearestSuperFood) {
                playerAction.heading = nearestFood;
                playerAction.action = PlayerActions.FORWARD;
                botAction = "Food!";
            }else if (nearestSuperFood < nearestFood){
                playerAction.heading = nearestSuperFood;
                playerAction.action = PlayerActions.FORWARD;
                botAction = "Super food!";
            }
            if (nearestPlayer.size > bot.size && bot.size > 30) {
                if (getDistanceBetween(bot, nearestPlayer) < 150){
                    playerAction.heading = (getHeadingBetween(nearestPlayer) + 90) % 360;
                    playerAction.action = PlayerActions.FORWARD;
                    botAction = "Avoiding bigger player!";
                }else{
                    playerAction.heading = getHeadingBetween(nearestPlayer);
                    playerAction.action = PlayerActions.FIRETORPEDOES;
                    botAction = "Attacking bigger player!";
                }
            }else if (nearestPlayer.size < bot.size && bot.size > 30) {
                if (bot.torpedoSalvoCount > 0){
                    playerAction.heading = getHeadingBetween(nearestPlayer);
                    playerAction.action = PlayerActions.FIRETORPEDOES;
                    botAction = "Attacking smaller player!";
                }
                if (getDistanceBetween(bot, nearestPlayer) < 200){
                    playerAction.heading = getHeadingBetween(nearestPlayer);
                    playerAction.action = PlayerActions.FORWARD;
                    botAction = "Chasing smaller player!";
                    if (teleportTick != null){
                        if (gameState.getWorld().getCurrentTick() - teleportTick > 10){
                            playerAction.action = PlayerActions.TELEPORT;
                            teleportTick = null;
                        }
                    }   
                    if (bot.getTeleporterCount() > 0 && teleportTick == null && nearestPlayer.size < bot.size + 40){
                        playerAction.action = PlayerActions.FIRETELEPORT;
                        botAction = "Teleporting!TeleportingTeleportingTeleportingTeleportingTeleportingTeleportingTeleportingTeleporting";
                        teleportTick = gameState.getWorld().getCurrentTick();
                    }
                }
            }

            // if (bot.shieldCount > 0 && getDistanceBetween(bot, torpedoList.get(0))) {
            //     playerAction.action = PlayerActions.ACTIVATESHIELD;
            //     System.out.println("Shield!");
            // }

            if (distanceFromAsteroid < 10) {
                playerAction.heading = (getHeadingBetween(nearestAsteroid) + 90) % 360;
                playerAction.action = PlayerActions.FORWARD;
                System.out.println("Avoiding asteroid!");
            }

            if (distanceFromGasCloud < 10) {
                playerAction.heading = (getHeadingBetween(nearestGasCloud) + 90) % 360;
                playerAction.action = PlayerActions.FORWARD;
                System.out.println("Avoiding gas cloud!");
            }


            if (distanceFromWorldCenter + (1.5 * bot.size) > gameState.world.getRadius()) { // avoid edge of world
                playerAction.heading = getHeadingBetween(worldCenter);
                playerAction.action = PlayerActions.FORWARD;
                botAction = "Going for center!";
            }
            
            
        }
        System.out.println(botAction);
        this.playerAction =  playerAction;
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