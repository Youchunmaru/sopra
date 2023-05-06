package logic.game.entity;

import enums.GameEntityType;
import logic.game.GameInstance;

/**
 * A type of {@link GameEntity}.
 *
 * @author Samuel Gröner
 */
public class Spice extends GameEntity {

  public Spice(GameInstance gameInstance) {
    super(GameEntityType.SPICE, gameInstance);
  }

  /**
   * Cannot move!
   *
   * @return false
   */
}
