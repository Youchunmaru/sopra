package logic.game.entity.unit;

import enums.GameEntityType;
import enums.GreatHouseEnum;
import enums.PlayerEnum;
import enums.UnitType;
import java.util.ArrayList;
import java.util.List;
import logic.game.GameInstance;
import logic.game.map.Field;
import messages.util.UnitConfig;

/**
 * Holds all the information of a Fighter. A type of {@link Unit}.
 *
 * @author Samuel Gröner
 */
public class Fighter extends Unit {

  private final UnitConfig unitConfig;
  private List<Unit> victims = new ArrayList<>();

  /**
   * Constructor.
   *
   * @see Unit
   */
  public Fighter(String name, PlayerEnum playerEnum, GreatHouseEnum affiliation, int characterID,
      GameInstance gameInstance) {
    super(name, UnitType.FIGHTER, playerEnum, affiliation, characterID, gameInstance);
    unitConfig = gameInstance.getPartyConfig().getFighter();
    setAttributes();
  }


  /**
   * Performs the special action sword spin.
   *
   * @param unit null, ignore
   * @return if spin was successful
   * @implNote unit, is needed for kanley and bene
   */
  @Override
  public boolean doSpecialAction(Unit unit) {
    Field field = getField();
    List<Unit> victimsList = new ArrayList<>();
    int x = field.getXCoordinate();
    int y = field.getYCoordinate();
    for (int i = x - 1; i <= x + 1; i++) {
      for (int j = y - 1; j <= y + 1; j++) {
        Field attackField = getGameInstance().getGameMap().getField(i, j);
        if (attackField != null && attackField.getGameEntities().containsKey(GameEntityType.UNIT) && !(field.equals(attackField))
                && ((Unit)attackField.getGameEntities().get(GameEntityType.UNIT)).getPlayer()!=this.getPlayer()
                && !((Unit)attackField.getGameEntity(GameEntityType.UNIT)).isInSandstorm()) {
          Unit victim = (Unit) attackField.getGameEntity(GameEntityType.UNIT);
          attack(victim);
          victimsList.add(victim);
        }
      }
    }
    victims = victimsList;
    setActionPoints(0);
    return !victimsList.isEmpty();
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

  public List<Unit> getVictims() {
    return victims;
  }
}
