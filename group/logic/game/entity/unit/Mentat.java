package logic.game.entity.unit;

import enums.GameEntityType;
import enums.GreatHouseEnum;
import enums.PlayerEnum;
import enums.UnitType;
import logic.game.GameInstance;
import logic.game.map.Field;
import messages.util.UnitConfig;

/**
 * Holds all the information of a Mentat. A type of {@link Unit}.
 *
 * @author Samuel Gröner
 */
public class Mentat extends Unit {

  private final UnitConfig unitConfig;

  /**
   * Constructor.
   *
   * @see Unit
   */
  public Mentat(String name, PlayerEnum playerEnum, GreatHouseEnum affiliation, int characterID,
      GameInstance gameInstance) {
    super(name, UnitType.MENTAT, playerEnum, affiliation, characterID, gameInstance);
    unitConfig = gameInstance.getPartyConfig().getMentat();
    setAttributes();
  }

  /**
   * Performs the special action spice hording.
   *
   * @param unit null, ignore
   * @return if hording was successful
   * @implNote unit, is needed for kanley and bene
   */
  @Override
  public boolean doSpecialAction(Unit unit) {
    Field field = getField();
    int x = field.getXCoordinate();
    int y = field.getYCoordinate();
    int count = 0;
    int room = getMaxSpice() - getCurrentSpice();
    for (int i = x - 1; i <= x + 1; i++) {
      for (int j = y - 1; j <= y + 1; j++) {
        Field spiceField = getGameInstance().getGameMap().getField(i, j);
        if (spiceField != null && spiceField.getGameEntities().containsKey(GameEntityType.SPICE)
            && room > 0) {
          spiceField.removeGameEntity(GameEntityType.SPICE);
          count++;
          room--;
        }
      }
    }
    setCurrentSpice(getCurrentSpice() + count);
    getGameInstance().getPlayer(getPlayer()).increaseSpiceSum(count);
    setActionPoints(0);
    return count > 0;
  }

  @Override
  public void initializeUnit() {
//Intentionally Empty
  }

  @Override
  public boolean healHP() {
    if (getMovementPoints() == unitConfig.getMaxMP()) {
      setHealthPoints(Math.min(getHealthPoints() + getHealingRate(),
          unitConfig.getMaxHP()));
      return true;
    }
    return false;
  }

  @Override
  public UnitConfig getUnitConfig() {
    return unitConfig;
  }

  @Override
  public void setAttributes() {
    setDamage(unitConfig.getDamage());
    setHealthPoints(unitConfig.getMaxHP());
    setMovementPoints(unitConfig.getMaxMP());
    setActionPoints(unitConfig.getMaxAP());
    setMaxSpice(unitConfig.getInventorySize());
    setHealingRate(unitConfig.getHealingHP());
  }
}
