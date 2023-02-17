package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

import com.azure.core.annotation.Head;

public class BotService {
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;
    public GameObject worldCenter = new GameObject(UUID.randomUUID(), 0, 0, 0, new Position(0, 0), null, 0, 0, 0, 0, 0);
    public String botAction;
    public Integer teleportTick;
    public boolean isTeleporting;
    // public boolean isShielding;
    // public boolean isSuperNovaing;
    // public boolean isTorpedoing;
    // public boolean isWarping;
    // public boolean isBoosting;
    // public boolean isTurning;
    // public boolean isMoving;

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

// ======================================================================================================================================
    public void computeNextPlayerAction(PlayerAction playerAction) {
        // playerAction.heading = new Random().nextInt(360);
        if (!gameState.getGameObjects().isEmpty()) {
            var foodList = gameState.getGameObjects()
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.FOOD)
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());
            var superFoodList = gameState.getGameObjects()
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.SUPERFOOD)
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());
            var torpedoList = gameState.getGameObjects()
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.TORPEDOSALVO)
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());
            var playerList = gameState.getPlayerGameObjects()
                    .stream().filter(item -> item.getId() != bot.getId())
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());
            var gasCloud = gameState.getGameObjects()
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.GASCLOUD)
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());

            var worldRadius = gameState.getWorld().getRadius();
            var distanceFromWorldCenter = getDistanceBetween(bot, worldCenter) + bot.getSize();
            var nearestFood = getDistanceBetween(bot, foodList.get(0)) + bot.getSize();
            var nearestSuperFood = getDistanceBetween(bot, superFoodList.get(0)) + bot.getSize();
            var nearestPlayer = getDistanceBetween(bot, playerList.get(0)) + bot.getSize();
            var nearestGasCloud = getDistanceBetween(bot, gasCloud.get(0)) + bot.getSize();

            int value = 0;

            if (bot.size <= 30) {
                if (nearestPlayer < 100 && bot.getSize() > playerList.get(0).getSize()) {
                    if (bot.torpedoSalvoCount > 0 && bot.getSize() > 20) {
                        playerAction.heading = getHeadingBetween(playerList.get(0));
                        playerAction.action = PlayerActions.FIRETORPEDOES;
                        System.out.println("1_FIRE LITTLE");
                        value++;
                    } else {
                        playerAction.heading = getHeadingBetween(playerList.get(0));
                        playerAction.action = PlayerActions.FORWARD;
                        System.out.println("1_CHASING LITTLE");
                        value++;
                    }
                    
                }

                
                if (nearestFood < nearestSuperFood) {
                    playerAction.heading = getHeadingBetween(foodList.get(0));
                    playerAction.action = PlayerActions.FORWARD;
                    System.out.println("1_FOOD!");
                    value++;
                } else {
                    playerAction.heading = getHeadingBetween(superFoodList.get(0));
                    playerAction.action = PlayerActions.FORWARD;
                    System.out.println("1_SUPER FOOD!");
                    value++;
                }
                

                if (nearestPlayer < 150 && nearestPlayer > 100 && bot.getSize() < playerList.get(0).getSize()) {
                    if (bot.torpedoSalvoCount > 5 && bot.getSize() > 20) {
                        playerAction.heading = getHeadingBetween(playerList.get(0));
                        playerAction.action = PlayerActions.FIRETORPEDOES;
                        System.out.println("1_FIRE BIG");
                        value++;
                    } else {
                        playerAction.heading = getHeadingBetween(playerList.get(0)) + 180;
                        playerAction.action = PlayerActions.FORWARD;
                        System.out.println("1_KABURR");
                        value++;
                    }
                }

                if (nearestGasCloud < 80) {                                                     // Avoid gas cloud
                    playerAction.heading = (getHeadingBetween(gasCloud.get(0)) + 95) % 360;
                    playerAction.action = PlayerActions.FORWARD;
                    System.out.println("1_Avoiding gas cloud");
                    value++;
                }

                if (torpedoList.size() > 0) {
                    for (int i = 0; i < torpedoList.size(); i++) {
                        if (botInSight(torpedoList.get(i)))
                        {
                            playerAction.heading = (getHeadingBetween(torpedoList.get(i)) + 90) % 360;
                            playerAction.action = PlayerActions.FORWARD;
                            System.out.println("1_EVADE");
                            value++;
                            break;
                        }
                    }
                }

                if (distanceFromWorldCenter + 125 > worldRadius) {
                    System.out.println("1_AVOIDING EDGE");
                    playerAction.heading = getHeadingBetween(worldCenter);
                    playerAction.action = PlayerActions.FORWARD;
                    value++;
                }
                
                // if (distanceFromWorldCenter + (2 * bot.size) > gameState.world.getRadius()) {
                //     playerAction.heading = getHeadingBetween(worldCenter);
                //     playerAction.action = PlayerActions.FORWARD;
                //     System.out.println("Heading to center");
                // }

                
            } else if (bot.getSize() > 30 && bot.getSize() <= 80) {
                if (nearestFood < nearestSuperFood) {
                    playerAction.heading = getHeadingBetween(foodList.get(0));
                    playerAction.action = PlayerActions.FORWARD;
                    System.out.println("2_FOOD!");
                    value++;
                } else {
                    playerAction.heading = getHeadingBetween(superFoodList.get(0));
                    playerAction.action = PlayerActions.FORWARD;
                    System.out.println("2_SUPER FOOD!");
                    value++;
                }

                if (nearestPlayer < 150) {
                    if (bot.size > playerList.get(0).size) {
                        if (bot.torpedoSalvoCount > 0) {
                            playerAction.heading = getHeadingBetween(playerList.get(0));
                            playerAction.action = PlayerActions.FIRETORPEDOES;
                            System.out.println("2_FIRE LITTLE");
                            value++;
                        } else {
                            playerAction.heading = getHeadingBetween(playerList.get(0));
                            playerAction.action = PlayerActions.FORWARD;
                            System.out.println("2_CHASING LITTLE");
                            value++;
                        }
                    } else {
                        playerAction.heading = (getHeadingBetween(playerList.get(0)) + 180) % 360;
                        playerAction.action = PlayerActions.FORWARD;
                        System.out.println("2_KABURR");
                        value++;
                    }   
                } else {
                    if (bot.torpedoSalvoCount > 0) {
                        playerAction.heading = getHeadingBetween(playerList.get(0));
                        playerAction.action = PlayerActions.FIRETORPEDOES;
                        System.out.println("2_FIRE");
                        value++;
                    }
                }

                if (nearestGasCloud < 50) {                                                     // Avoid gas cloud
                    playerAction.heading = (getHeadingBetween(gasCloud.get(0)) + 95) % 360;
                    playerAction.action = PlayerActions.FORWARD;
                    System.out.println("2_Avoiding gas cloud");
                    value++;
                }

                // if (distanceFromWorldCenter + (5 * bot.size) >= gameState.world.getRadius()) {
                //     playerAction.heading = getHeadingBetween(worldCenter);
                //     playerAction.action = PlayerActions.FORWARD;
                //     System.out.println("Heading to center");
                // }

                if (torpedoList.size() > 0) {
                    for (int i = 0; i < torpedoList.size(); i++) {
                        if (botInSight(torpedoList.get(i)) && getDistanceBetween(torpedoList.get(i), bot) - bot.size < 30)
                        {
                            if (bot.shieldCount > 0)
                            {
                                playerAction.action = PlayerActions.ACTIVATESHIELD;
                                System.out.println("2_SHIELD");
                                value++;
                            }
                            else
                            {
                                playerAction.heading = (getHeadingBetween(torpedoList.get(i)) + 90) % 360;
                                playerAction.action = PlayerActions.FORWARD;
                                System.out.println("2_EVADE");
                                value++;
                            }
                            break;
                        }
                    }
                }

                if (distanceFromWorldCenter + 200 > worldRadius) {
                    playerAction.heading = getHeadingBetween(worldCenter);
                    playerAction.action = PlayerActions.FORWARD;
                    System.out.println("2_AVOIDING EDGE");
                    value++;
                }
 
            } else if (bot.getSize() > 80 && bot.getSize() <= 150) {
                if (nearestFood < nearestSuperFood) {
                    playerAction.heading = getHeadingBetween(foodList.get(0));
                    playerAction.action = PlayerActions.FORWARD;
                    System.out.println("3_FOOD!");
                    value++;
                } else {
                    playerAction.heading = getHeadingBetween(superFoodList.get(0));
                    playerAction.action = PlayerActions.FORWARD;
                    System.out.println("3_SUPER FOOD!");
                    value++;
                }

                if (nearestPlayer < 400) {
                    if (bot.getSize() > playerList.get(0).getSize()) {
                        if (bot.torpedoSalvoCount > 0) {
                            playerAction.heading = getHeadingBetween(playerList.get(0));
                            playerAction.action = PlayerActions.FIRETORPEDOES;
                            System.out.println("3_FIRE LITTLE");
                            value++;
                        } else {
                            playerAction.heading = getHeadingBetween(playerList.get(0));
                            playerAction.action = PlayerActions.FORWARD;
                            System.out.println("3_CHASING LITTLE");
                            value++;
                        }
                    } else {
                        if (bot.torpedoSalvoCount > 0) {
                            playerAction.heading = getHeadingBetween(playerList.get(0));
                            playerAction.action = PlayerActions.FIRETORPEDOES;
                            System.out.println("3_FIRE BIG");
                            value++;
                        } else {
                            playerAction.heading = getHeadingBetween(playerList.get(0));
                            playerAction.action = PlayerActions.FORWARD;
                            System.out.println("3_LAWAN COK");
                            value++;
                        }
                    }   
                }

                if (nearestGasCloud < 80) {                                                     // Avoid gas cloud
                    playerAction.heading = (getHeadingBetween(gasCloud.get(0)) + 95) % 360;
                    playerAction.action = PlayerActions.FORWARD;
                    System.out.println("3_Avoiding gas cloud");
                    value++;
                }

                // if (distanceFromWorldCenter + (7 * bot.size) > gameState.world.getRadius()) {
                //     playerAction.heading = getHeadingBetween(worldCenter);
                //     playerAction.action = PlayerActions.FORWARD;
                //     System.out.println("Heading to center");
                // }

                if (distanceFromWorldCenter + 200 > worldRadius) {
                    playerAction.heading = getHeadingBetween(worldCenter);
                    playerAction.action = PlayerActions.FORWARD;
                    System.out.println("3_AVOIDING EDGE");
                    value++;
                }

                if (torpedoList.size() > 0) {
                    for (int i = 0; i < torpedoList.size(); i++) {
                        if (botInSight(torpedoList.get(i)) && getDistanceBetween(torpedoList.get(i), bot) - bot.getSize() < 50)
                        {
                            if (bot.shieldCount > 0)
                            {
                                playerAction.action = PlayerActions.ACTIVATESHIELD;
                                System.out.println("3_SHIELD");
                            } 
                            // else if (bot.torpedoSalvoCount > 5) {
                            //     playerAction.heading = getHeadingBetween(torpedoList.get(i));
                            //     playerAction.action = PlayerActions.FIRETORPEDOES;
                            //     System.out.println("FIRE");
                            else
                            {
                                playerAction.heading = (getHeadingBetween(torpedoList.get(i)) + 90) % 360;
                                playerAction.action = PlayerActions.FORWARD;
                                System.out.println("3_EVADE");
                            }
                            value++;
                        } 
                    }   
                }    
            } else if (bot.getSize() > 150) {
                if (nearestFood < nearestSuperFood) {
                    playerAction.heading = getHeadingBetween(foodList.get(0));
                    playerAction.action = PlayerActions.FORWARD;
                    System.out.println("3_FOOD!");
                    value++;
                } else {
                    playerAction.heading = getHeadingBetween(superFoodList.get(0));
                    playerAction.action = PlayerActions.FORWARD;
                    System.out.println("3_SUPER FOOD!");
                    value++;
                }

                if (nearestPlayer < 400) {
                    if (bot.getSize() > playerList.get(0).getSize()) {
                        if (bot.torpedoSalvoCount > 0) {
                            playerAction.heading = getHeadingBetween(playerList.get(0));
                            playerAction.action = PlayerActions.FIRETORPEDOES;
                            System.out.println("3_FIRE LITTLE");
                            value++;
                        } else {
                            playerAction.heading = getHeadingBetween(playerList.get(0));
                            playerAction.action = PlayerActions.FORWARD;
                            System.out.println("3_CHASING LITTLE");
                            value++;
                        }
                    } else {
                        if (bot.torpedoSalvoCount > 0) {
                            playerAction.heading = getHeadingBetween(playerList.get(0));
                            playerAction.action = PlayerActions.FIRETORPEDOES;
                            System.out.println("3_FIRE BIG");
                            value++;
                        } else {
                            playerAction.heading = getHeadingBetween(playerList.get(0));
                            playerAction.action = PlayerActions.FORWARD;
                            System.out.println("3_LAWAN COK");
                            value++;
                        }
                    }   
                }

                if (nearestGasCloud < 100) {                                                     // Avoid gas cloud
                    playerAction.heading = (getHeadingBetween(gasCloud.get(0)) + 95) % 360;
                    playerAction.action = PlayerActions.FORWARD;
                    System.out.println("3_Avoiding gas cloud");
                    value++;
                }

                // if (distanceFromWorldCenter + (7 * bot.size) > gameState.world.getRadius()) {
                //     playerAction.heading = getHeadingBetween(worldCenter);
                //     playerAction.action = PlayerActions.FORWARD;
                //     System.out.println("Heading to center");
                // }

                if (distanceFromWorldCenter + 250 > worldRadius) {
                    playerAction.heading = getHeadingBetween(worldCenter);
                    playerAction.action = PlayerActions.FORWARD;
                    System.out.println("3_AVOIDING EDGE");
                    value++;
                }

                if (torpedoList.size() > 0) {
                    for (int i = 0; i < torpedoList.size(); i++) {
                        if (botInSight(torpedoList.get(i)) && getDistanceBetween(torpedoList.get(i), bot) - bot.getSize() < 50)
                        {
                            if (bot.shieldCount > 0)
                            {
                                playerAction.action = PlayerActions.ACTIVATESHIELD;
                                System.out.println("3_SHIELD");
                            } 
                            // else if (bot.torpedoSalvoCount > 5) {
                            //     playerAction.heading = getHeadingBetween(torpedoList.get(i));
                            //     playerAction.action = PlayerActions.FIRETORPEDOES;
                            //     System.out.println("FIRE");
                            else
                            {
                                playerAction.heading = (getHeadingBetween(torpedoList.get(i)) + 90) % 360;
                                playerAction.action = PlayerActions.FORWARD;
                                System.out.println("3_EVADE");
                            }
                            value++;
                        } 
                    }   
                }    
            }

            if (value == 0) {
                playerAction.heading = getHeadingBetween(worldCenter);
                playerAction.action = PlayerActions.FORWARD;
                System.out.println("0_DEFAULT");
            }

            this.playerAction = playerAction;
            System.out.println("current size: " + bot.size);
            System.out.println("current VALUE = " + value);
            System.out.println("distance from center: " + distanceFromWorldCenter);
            System.out.println("world radius = " + worldRadius);
            System.out.println("nearest player: " + nearestPlayer);
            System.out.println("gas cloud distance: " + nearestGasCloud);
            if (torpedoList.size() > 0) {
                System.out.println("====================================================================");
                System.out.println("torpedo distance: " + getDistanceBetween(torpedoList.get(0), bot));
                System.out.println("heading to bot: " + getHeadingToBot(torpedoList.get(0)));
                System.out.println("torpedo heading: " + getHeadingBetween(torpedoList.get(0)));
                System.out.println("true or false? " + botInSight(torpedoList.get(0)));
                System.out.println("====================================================================");
            }
        }
    }        


//======================================================================================================================================

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

    private int getHeadingToBot(GameObject otherObject) {
        var direction = toDegrees(Math.atan2(bot.getPosition().y - otherObject.getPosition().y,
                bot.getPosition().x - otherObject.getPosition().x));
        return (direction + 360) % 360;
    }

    private boolean botInSight(GameObject otherObject) {
        for (int i = 0; i < 90; i++) {
            if (getHeadingToBot(otherObject) + i == otherObject.getCurrentHeading() || getHeadingToBot(otherObject) - i == otherObject.getCurrentHeading()) {
                return true;
            }
        }
        return false;
    }
    private int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }
}