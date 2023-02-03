package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class BotService {
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;

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
        // playerAction.heading = new Random().nextInt(360);
        if (!gameState.getGameObjects().isEmpty()) {
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
            var nearestPlayer = gameState.getGameObjects()
                    .stream().filter(item -> item.getId() != bot.getId())
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList()).get(0);
                
            var nearestFood = getHeadingBetween(foodList.get(0));
            var nearestSuperFood = getHeadingBetween(superFoodList.get(0));
            var distanceFromWorldCenter = getDistanceBetween(bot, new GameObject(UUID.randomUUID(), 0, 0, 0, new Position(0, 0), ObjectTypes.PLAYER));
            
            if (distanceFromWorldCenter + (1.5 * bot.size) > gameState.world.getRadius()) { // avoid edge of world
                playerAction.heading = getHeadingBetween(new GameObject(UUID.randomUUID(), 0, 0, 0, new Position(0, 0), ObjectTypes.PLAYER));
                playerAction.action = PlayerActions.FORWARD;
                System.out.println("Going for center!");
            }
            else if (nearestFood < nearestSuperFood) {
                playerAction.heading = nearestFood;
                playerAction.action = PlayerActions.FORWARD;
                System.out.println("Food!");
            }else if (nearestSuperFood < nearestFood){
                playerAction.heading = nearestSuperFood;
                playerAction.action = PlayerActions.FORWARD;
                System.out.println("Superfood!");
            }
            if (nearestPlayer.size > bot.size && getDistanceBetween(bot, nearestPlayer) < 10) {
                playerAction.heading = getHeadingBetween(nearestPlayer) + 180;
                playerAction.action = PlayerActions.FORWARD;
                System.out.println("Run away!");
            }
        }
        this.playerAction = playerAction;
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