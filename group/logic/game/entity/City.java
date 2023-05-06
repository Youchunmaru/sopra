package logic.game.entity;

import enums.GameEntityType;
import enums.PlayerEnum;
import logic.game.GameInstance;
import logic.game.map.Field;

/**
 * Holds all information of a city. A type of {@link GameEntity}.
 *
 * @author Samuel Gröner, Janine Grimmer
 */
public class City extends GameEntity {

  private int spiceStock;
  private final PlayerEnum player;

  /**
   * Constructor.
   *
   * @param player the owner of the city
   */
  public City(PlayerEnum player, Field field, GameInstance gameInstance) {
    super(GameEntityType.CITY, gameInstance);
    this.player = player;
    setField(field);
  }

  public void increaseSpice(int spiceAmount) {
    spiceStock += spiceAmount;
  }

  public int getSpiceStock() {
    return spiceStock;
  }

  public PlayerEnum getPlayer() {
    return player;
  }

}
