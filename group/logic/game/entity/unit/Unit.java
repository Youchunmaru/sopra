package logic.game.entity.unit;

import enums.FieldType;
import enums.GameEntityType;
import enums.GreatHouseEnum;
import enums.PlayerEnum;
import enums.UnitType;
import java.util.ArrayList;
import java.util.List;
import logic.game.GameInstance;
import logic.game.entity.City;
import logic.game.entity.GameEntity;
import logic.game.map.Field;
import java.util.logging.Level;

import logic.util.MathUtil;
import messages.gameplay.outgoing.ChangeSpiceMessage;
import messages.util.Point;
import messages.util.UnitConfig;

/**
 * Holds all the information of a unit. A type of {@link GameEntity}.
 *
 * @author Samuel Gröner, Janine Grimmer
 * @see enums.UnitType
 */
public abstract class Unit extends GameEntity {

  //type and player
  private final UnitType unitType;
  public final PlayerEnum player;

  //name, great house and city
  private final GreatHouseEnum affiliation;
  private final String name;
  private final City city;

  //Stats:
  private float damage;
  private float healthPoints;
  private int movementPoints;
  private int actionPoints;
  private int maxSpice; // max inventory size
  private float healingRate;
  private int currentSpice; // spice amount
  private final float cloneProbability;

  //meta data
  private int characterID;
  private boolean isSwallowed;
  private boolean isLoud;
  private boolean isDefeated;
  private boolean isInSandstorm;
  private boolean steppedOnSand;
  private List<Unit> pushedUnits;

  /**
   * Constructor. Is instanced from a subclass.
   *
   * @param name         the name of the unit
   * @param type         the type of the unit
   * @param player       the owner of the unit
   * @param affiliation  the house it is affiliated with
   * @param characterID  the id of the unit
   * @param gameInstance the game it is currently playing in
   */
  protected Unit(String name, UnitType type, PlayerEnum player, GreatHouseEnum affiliation,
      int characterID, GameInstance gameInstance) {
    super(GameEntityType.UNIT, gameInstance);
    this.name = name;
    this.unitType = type;
    this.player = player;
    this.affiliation = affiliation;
    city = gameInstance.getPlayer(player).getCity();
    this.characterID = characterID;
    currentSpice = 0;
    cloneProbability = gameInstance.getPartyConfig().getCloneProbability();
    isDefeated = false;
    isLoud = false;
    isSwallowed = false;
    isInSandstorm = false;
    steppedOnSand = false;
    initializeUnit();
  }
  public boolean onTurnEnd() {
    steppedOnSand = false;
    if (healthPoints <= 0) {
      setIsDefeated(true);
      if(getCurrentSpice()>0){
        blowSpiceAfterDefeat(getField().getXCoordinate(), getField().getYCoordinate(),
                getCurrentSpice());
        setCurrentSpice(0);
      }
      getField().removeGameEntity(GameEntityType.UNIT);
      return true;
    }
    if (getMovementPoints() == getUnitConfig().getMaxMP()) {
      healHP();
    }
    movementPoints = getUnitConfig().getMaxMP();
    actionPoints = getUnitConfig().getMaxAP();
    return true;
  }

  /**
   * Performs the special action of the charakter.
   *
   * @param unit the target of the special action, null if no target
   * @return if hording was successful
   * @implNote unit, is needed only for kanley and bene
   */
  public abstract boolean doSpecialAction(Unit unit);

  public abstract void initializeUnit();

  /**
   * Attacks the specified opponent.
   *
   * @param victim the unit to attack
   * @return if attack was successful
   */
  public boolean attack(Unit victim) {
    Field field = getField();
    //execute attack
    GameInstance.LOGGER.info("Unit(Attack): attacking Unit: " + this.toString() + ", defending Unit: " + victim.toString());
    GameInstance.LOGGER.info("Unit(Attack): originField: " + getField().toString() + ", targetField: " + victim.getField().toString());
    if (field.isHighTerrain() == victim.getField().isHighTerrain()) {
      normalAttack(victim);
    } else {
      if (field.isHighTerrain()) {
        advantageousAttack(victim);
      } else {
        disadvantageousAttack(victim);
      }
    }
    if (victim.getHealthPoints() <= 0) {
      victim.setIsDefeated(true);
      if(victim.getCurrentSpice()>0){
        victim.blowSpiceAfterDefeat(victim.getField().getXCoordinate(), victim.getField().getYCoordinate(),
                victim.getCurrentSpice());
        victim.setCurrentSpice(0);
      }
      getGameInstance().getPlayer(getPlayer()).increaseOpponentsDefeated();
    }
    actionPoints--;
    return true;
  }

  private void normalAttack(Unit victim) {
    victim.setHealthPoints(victim.getHealthPoints() - damage);
  }

  private void advantageousAttack(Unit victim) {
    GameInstance.LOGGER.info("Test" + victim.getHealthPoints() + "- (" + damage + " * " + getGameInstance().getPartyConfig()
        .getHighGroundBonusRatio() + ")");
    victim.setHealthPoints(victim.getHealthPoints() - (damage * getGameInstance().getPartyConfig()
        .getHighGroundBonusRatio()));
    GameInstance.LOGGER.info("Test" + victim.getHealthPoints());
  }

  private void disadvantageousAttack(Unit victim) {
    victim.setHealthPoints(
        victim.getHealthPoints() - (damage * getGameInstance().getPartyConfig().getLowGroundMalusRatio()));
  }

  /**
   * Moves the unit to through the specified path.
   *
   * @param path the path to move on
   * @return if it was successful
   */
  public boolean moveUnit(Point[] path) {// TODO: 04.07.2022 update isInSandstorm if walked in it
    List<Unit> pushedUnitsList = new ArrayList<>();
    setSteppedOnSand(false);
    for (Point point : path) {
      Field field = getGameInstance().getGameMap().getField(point.getX(), point.getY());
      if (field.getGameEntities().containsKey(GameEntityType.UNIT)) {
        //If a unit is on targetfield get the Unit
        Unit pushedUnit = (Unit) field.getGameEntity(GameEntityType.UNIT);
        //Add it to pushedUnits
        pushedUnitsList.add(pushedUnit);
        //Remove the pushed Unit from the field
        field.removeGameEntity(GameEntityType.UNIT);
        //Get the Field at the start of the movement
        Field oldField = getField();
        //Move the Unit to the TargetField
        move(field);
        //Reduce MP by 1
        movementPoints--;
        //Move the pushed Unit to the StartField
        pushedUnit.move(oldField);
      } else {
        //If no Unit is on targetField move the Unit to the targetField
        move(field);
        //Reduce MP by 1
        movementPoints--;
      }
      //If the Unit stepped on Sand it is loud
      if (field.getFieldType().equals(FieldType.DUNE) || field.getFieldType()
          .equals(FieldType.FLAT_SAND)) {
        if (isSteppedOnSand()) {
          setIsLoud(true);
          LOGGER.log(Level.INFO, "Unit is loud: {0}", this.isLoud());
        } else {
          setSteppedOnSand(true);
        }
      }
    }
    pushedUnits = pushedUnitsList;
    return !pushedUnits.isEmpty();
  }

  /**
   * Picks up spice from the specified filed.
   *
   * @param field the filed to pick up spice from
   * @return if it was successful
   */
  public boolean pickupSpice(Field field) {
    if (currentSpice < maxSpice) {
      field.setGameEntity(GameEntityType.SPICE, null);
      currentSpice++;
      getGameInstance().getPlayer(getPlayer()).increaseSpiceSum(1);
      actionPoints--;
      setIsLoud(true);
      return true;
    }
    return false;
  }

  /**
   * Adds the specified amount of spices to the specified unit and removes it of this unit.
   *
   * @param unit   the unit to transfer to
   * @param amount the amount to transfer
   * @return if it was successful
   */
  public boolean transferSpice(Unit unit, int amount) {
    if (currentSpice >= amount && (unit.getMaxSpice() - unit.getCurrentSpice()) > amount) {
      currentSpice = currentSpice - amount;
      unit.setCurrentSpice(unit.getCurrentSpice() + amount);
      actionPoints--;
      return true;
    }
    return false;
  }

  /**
   *
   * @param version
   * @param clientID
   * @return
   */
  // TODO: 16.06.2022 where handler?
  public ChangeSpiceMessage deliverSpiceToCity(String version, int clientID) {
      city.increaseSpice(currentSpice);
      setCurrentSpice(0);
      return new ChangeSpiceMessage(version, clientID,city.getSpiceStock());
  }

  /**
   * Used in Clone Phase. Tries to revive this character if knocked-out. Not possible if character
   * has been swallowed by a sandworm.
   *
   * @return true if alive, false otherwise
   */
  public boolean cloneUnit() {
    // cloning successful if random number is lower than cloneProbability (because this equals the cloneProbability)
    if (MathUtil.random.nextInt(101) <= cloneProbability * 100) {
      // reset is defeated to revive character
      isDefeated = false;
      setAttributes();
      return true;
    } else {
      return false;
    }
  }

  /**
   * Heals the hp of this unit.
   *
   * @return if heal was successful
   */
  public abstract boolean healHP();

  public abstract UnitConfig getUnitConfig();

  /**
   * Sets the attributes of this unit.
   */
  public abstract void setAttributes();

  public UnitType getUnitType() {
    return unitType;
  }

  public int getMaxSpice() {
    return maxSpice;
  }

  public int getCurrentSpice() {
    return currentSpice;
  }

  public void setCurrentSpice(int currentSpice) {
    this.currentSpice = currentSpice;
  }

  // setter and getter for all attributes
  public void setIsSwallowed(boolean swallowed) {
    isSwallowed = swallowed;
  }

  public boolean isSwallowed() {
    return isSwallowed;
  }

  public boolean isLoud() {
    return isLoud;
  }

  public void setIsLoud(boolean loud) {// TODO: 04.07.2022 reset this at the beginning of each Turn
    isLoud = loud;
  }

  public boolean isDefeated() {
    return isDefeated;
  }

  public void setIsDefeated(boolean defeated) {
    isDefeated = defeated;
  }

  public GreatHouseEnum getAffiliation() {
    return affiliation;
  }

  public City getCity() {
    return city;
  }

  public int getCharacterID() {
    return characterID;
  }

  public String getName() {
    return name;
  }

  public float getHealthPoints() {
    return healthPoints;
  }

  public void setHealthPoints(float healthPoints) {
    this.healthPoints = healthPoints;
  }

  public int getMovementPoints() {
    return movementPoints;
  }

  public void setMovementPoints(int movementPoints) {
    this.movementPoints = movementPoints;
  }

  public int getActionPoints() {
    return actionPoints;
  }

  public void setActionPoints(int actionPoints) {
    this.actionPoints = actionPoints;
  }

  public float getDamage() {
    return damage;
  }

  public float getHealingRate() {
    return healingRate;
  }

  public void setInSandstorm(boolean inSandstorm) {
    this.isInSandstorm = inSandstorm;
  }

  public boolean isInSandstorm() {
    return isInSandstorm;
  }

  public void setDamage(float damage) {
    this.damage = damage;
  }

  public void setMaxSpice(int maxSpice) {
    this.maxSpice = maxSpice;
  }

  public void setHealingRate(float healingRate) {
    this.healingRate = healingRate;
  }

  public PlayerEnum getPlayer() {
    return player;
  }

  public boolean isSteppedOnSand() {
    return steppedOnSand;
  }

  public void setSteppedOnSand(boolean steppedOnSand) {
    this.steppedOnSand = steppedOnSand;
  }

  public List<Unit> getPushedUnits() {
    return pushedUnits;
  }

  @Override
  public String toString() {
    return "{Unit:" + unitType + ", " + name + "}";
  }

  public void setCharacterID(int characterID) {
    this.characterID = characterID;
  }

}
