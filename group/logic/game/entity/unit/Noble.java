package logic.game.entity.unit;

import enums.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import logic.game.GameInstance;
import logic.game.entity.Sandworm;
import logic.game.map.Field;
import logic.game.map.GameMap;
import messages.util.UnitConfig;
import network.util.Player;

/**
 * Holds all the information of a noble. A type of {@link Unit}.
 *
 * @author Samuel Gröner
 */
public class Noble extends Unit {

  private final float successRate;
  private final UnitConfig unitConfig;
  private Sandworm hitSandworm;
  private List<Unit> hitByAtomics;

  /**
   * Constructor.
   *
   * @see Unit
   */
  public Noble(String name, PlayerEnum playerEnum, GreatHouseEnum affiliation, int characterID,
      GameInstance gameInstance) {
    super(name, UnitType.NOBLE, playerEnum, affiliation, characterID, gameInstance);
    unitConfig = gameInstance.getPartyConfig().getNoble();
    setAttributes();
    successRate = gameInstance.getPartyConfig().getKanlySuccessProbability();
  }

  /**
   * Performs the special action kanley.
   *
   * @param unit the unit to attack
   * @return if attack was successful
   */
  @Override
  public boolean doSpecialAction(Unit unit) {
	  setActionPoints(0);
    if (Math.random() <= successRate) {
      unit.setHealthPoints(0);
      unit.setIsDefeated(true);
      getGameInstance().getPlayer(getPlayer()).increaseOpponentsDefeated();
      return true;
    }
    return false;
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

  /**
   * Performs the atomics action.
   *
   * @param field the target field
   * @return if atomics was successful
   */
  public boolean familyAtomics(Field field) {
    GameMap map = getGameInstance().getGameMap();
    Player attacker = getGameInstance().getPlayer(getPlayer());
    if (getActionPoints() == unitConfig.getMaxAP() && attacker.getAtomicsCount() > 0) {
      int x = field.getXCoordinate();
      int y = field.getYCoordinate();
      setActionPoints(0);
      map.reshapeMapAfterAtomics(x, y);
      attacker.setAtomicsCount(attacker.getAtomicsCount() - 1);
      GameInstance.LOGGER.info("Noble(familyAtomics): atomics count is now: " + attacker.getAtomicsCount());
      return true;
    }
    return false;
  }

  public boolean checkVictimsOfFamilyAtomics(Field attackCenter, GameMap map, Player attacker) {
    GameInstance.LOGGER.info("Noble(checkVictimsOfFamilyAtomics): Noble checks for victims");
    int x = attackCenter.getXCoordinate();
    int y = attackCenter.getYCoordinate();
    hitByAtomics = new ArrayList<>();
    boolean hasAttackedPlayer = false;
    for (int i = -1; i <= 1; i++) {
      for (int j = -1; j <= 1; j++) {
        if(map.getField(x + i, y + j) == null){
          continue;
        }
        Field fieldIter = map.getField(x + i, y + j);
        GameInstance.LOGGER.info("Noble(checkVictimsOfFamilyAtomics): Noble checks for victims at field: " + fieldIter.toString());
        if (fieldIter.getGameEntities().containsKey(GameEntityType.SANDWORM)) {
          GameInstance.LOGGER.info("Noble(checkVictimsOfFamilyAtomics): Noble found sandworm at field: " + fieldIter.toString());
          hitSandworm = (Sandworm) fieldIter.getGameEntity(GameEntityType.SANDWORM);
          GameInstance.LOGGER.info("Noble(checkVictimsOfFamilyAtomics): hitSandworm: " + hitSandworm.toString());
        }
        if (fieldIter.getGameEntities().containsKey(GameEntityType.UNIT)) {
          GameInstance.LOGGER.info("Noble(checkVictimsOfFamilyAtomics): Noble found Unit at field: " + fieldIter.toString());
          Unit unit = (Unit) fieldIter.getGameEntity(GameEntityType.UNIT);
          unit.setIsDefeated(true);
          unit.setHealthPoints(0);
          hitByAtomics.add(unit);
          // set flag that atomics rule is no longer valid
          if (!unit.getPlayer().equals(attacker.getPlayerEnum())) {
            getGameInstance().setFirstToDropTheBomb(attacker);
            hasAttackedPlayer = true;
            getGameInstance().getPlayer(getPlayer()).increaseOpponentsDefeated();
          }
        }
        if (fieldIter.getGameEntities().containsKey(GameEntityType.SPICE)) {
          fieldIter.removeGameEntity(GameEntityType.SPICE);
        }
      }
    }
    return hasAttackedPlayer;
  }

  public float getSuccessRate() {
    return successRate;
  }

  public Sandworm getHitSandworm() {
    Sandworm copy = hitSandworm;
    GameInstance.LOGGER.info("Noble(getHitSandworm): " + hitSandworm);
    hitSandworm = null;
    LOGGER.log(Level.INFO, "Sandworm copy is set to null");
    return copy;
  }

  public List<Unit> getHitByAtomics() {
    return hitByAtomics;
  }
}
