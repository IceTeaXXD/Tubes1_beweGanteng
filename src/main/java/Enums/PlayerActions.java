package Enums;

public enum PlayerActions {
  FORWARD(1),
  STOP(2),
  STARTAFTERBURNER(3),
  STOPAFTERBURNER(4),
  FIRETORPEDOES(5),
  FIRESUPERNOVA(6),
  DETONATESUPERNOVA(7),
  FIRETELEPORT(8),
  TELEPORT(9),
  ACTIVATESHIELD(10);

  public final Integer value;

  private PlayerActions(Integer value) {
    this.value = value;
  }

  public static PlayerActions valueOf(Integer value) {
    for (PlayerActions playerAction : PlayerActions.values()) {
      if (playerAction.value == value) return playerAction;
    }

    throw new IllegalArgumentException("Value not found");
  }
}
