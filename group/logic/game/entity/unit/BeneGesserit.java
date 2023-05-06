package logic.game.entity.unit;

import enums.GreatHouseEnum;
import enums.PlayerEnum;
import enums.UnitType;
import logic.game.GameInstance;
import messages.util.UnitConfig;

/**
 * Holds all the information of a Bene Gesserit. A type of {@link Unit}.
 *
 * @author Samuel Gröner
 */
public class BeneGesserit extends Unit {

  private final UnitConfig unitConfig;

  /**
   * Constructor.
   *
   * @see Unit
   */
  public BeneGesserit(String name, PlayerEnum playerEnum, GreatHouseEnum affiliation,
      int characterID, GameInstance gameInstance) {
    super(name, UnitType.BENE_GESSERIT, playerEnum, affiliation, characterID, gameInstance);
    unitConfig = gameInstance.getPartyConfig().getBeneGesserit();
    setAttributes();
  }

  /**
   * Performs the special action voice.
   *
   * @param victim the unit to use voice on
   * @return if voice was successful
   */
  @Override
  public boolean doSpecialAction(Unit victim) {
    int amount = Math.min(getMaxSpice() - getCurrentSpice(), victim.getCurrentSpice());
    setCurrentSpice(getCurrentSpice() + amount);
    victim.setCurrentSpice(victim.getCurrentSpice() - amount);
    getGameInstance().getPlayer(getPlayer()).increaseSpiceSum(amount);
    victim.getGameInstance().getPlayer(getPlayer()).increaseSpiceSum(-amount);
    setActionPoints(0);
    return amount > 0;
  }

  @Override
  public void initializeUnit() {
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
