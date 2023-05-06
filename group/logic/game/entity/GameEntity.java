package logic.game.entity;

import enums.GameEntityType;
import logic.game.GameInstance;
import logic.game.map.Field;
import logic.game.map.GameMap;
import logic.util.MathUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import static java.util.logging.Level.*;

/**
 * An entity of the game.
 *
 * @author Samuel Gröner, Janine Grimmer
 * @see enums.GameEntityType
 */
public abstract class GameEntity {

  private final GameEntityType entityType;
  private Field field;
  private final GameInstance gameInstance;
  protected final Logger LOGGER = Logger.getLogger(GameEntity.class.getName());
  LinkedList<Field> lookedAtFields = new LinkedList<>();

  /**
   * Constructor.
   *
   * @param type the type of the entity
   * @see enums.GameEntityType
   */
  protected GameEntity(GameEntityType type, GameInstance gameInstance) {
    entityType = type;
    this.gameInstance = gameInstance;
  }

  /**
   * Moves the entity on the specified field.
   *
   * @param field the field to move to
   * @return if the entity has moved
   */
  protected boolean move(Field field) {
    if (field != null && isValidField(field.getXCoordinate(), field.getYCoordinate())) {
      return setField(field);
    }
    return false;
  }

  /**
   * Method to check that coordinate of possible field are within the game map borders
   *
   * @param xCoordinate integer, x coordinate of new field
   * @param yCoordinate integer, y coordinate of new field
   * @return boolean, true if field is within borders
   * @author Janine Grimmer
   */
  public boolean isValidField(int xCoordinate, int yCoordinate) {
    //  true if x and y are in range of game map
    return ((xCoordinate >= 0) && (xCoordinate < gameInstance.getGameMap().getXSize()) && (
        yCoordinate >= 0)
        && (yCoordinate < gameInstance.getGameMap().getYSize()));
  }

  /**
   * Sets the field of this to the specified field.
   * An entity only can occupy a field if it isn't already openFields by an entity of the same type.
   *
   * @param field the field to set to
   * @return if it could set the field
   * */
  public boolean setField(Field field) {
    if(field==null&&this.field!=null){
      //Remove from Map
      gameInstance.getGameMap().getField(this.getField().getXCoordinate(),this.getField().getYCoordinate()).removeGameEntity(this.entityType);
      //Remove reference in object
      this.field=null;
      return true;
    }
    if (field != null && isValidField(field.getXCoordinate(), field.getYCoordinate())) {
      if (field.addGameEntity(this)) {
        if (getField() != null){
        getField().removeGameEntity(getEntityType());}
        this.field = field;
        return true;
      }
    }
    return false;
  }

  List<Field> openFields = new LinkedList<>();
    /**
     * Method used to blow spice if character with spice in inventory is swallowed by sandworm
     * or defeated.
     *
     * @param xCoordinate      integer x coordinate of field
     * @param yCoordinate      integer y coordinate of field
     * @param numberOfNewSpice integer, number of spice to be spread on gameMap
     */
    public void blowSpiceAfterDefeat( int xCoordinate, int yCoordinate, int numberOfNewSpice){
      openFields.remove(gameInstance.getGameMap().getField(xCoordinate,yCoordinate));
      LOGGER.log(INFO, "Amount of spice to be blown due to defeat is {0}.", numberOfNewSpice);
      GameMap gameField = gameInstance.getGameMap();
      for (int i = xCoordinate - 1; i <= xCoordinate + 1; i++) {
        for (int j = yCoordinate - 1; j <= yCoordinate + 1; j++) {
          // jump over field under study and if x or y cross the edge (across edge no spice can be placed)
          if (isValidField(i, j) && (i != xCoordinate && j != yCoordinate) && gameField.getField(i, j).isAccessible() &&
                  !gameField.getField(i, j).getGameEntities().containsKey(GameEntityType.SANDWORM)
                  && !gameField.getField(i, j).getGameEntities().containsKey(GameEntityType.SPICE)) {
            if (numberOfNewSpice <= 0) {
              LOGGER.log(INFO,"stopping blowSpiceAfterDefeat in GameEntity");
              lookedAtFields.clear();
              break;
            }
            gameField.getField(i, j).addGameEntity(new Spice(gameInstance));
            numberOfNewSpice--;
            LOGGER.log(INFO, "Spice laid down on map, new spice number is {0}", numberOfNewSpice);

          }

        }
      }
      // if spice is still left over but no free field: blow spice on non free field
      if (numberOfNewSpice > 0){
        try{
        Field field = getAnotherFieldForSpice( xCoordinate, yCoordinate);
        blowSpiceAfterDefeat(field.getXCoordinate(), field.getYCoordinate(), numberOfNewSpice);
        }catch (NullPointerException npe){
          LOGGER.log(SEVERE, "Null pointer for field search: ", npe);
        }
      }

    }



  /**
   * Called to find another free field where spice can be added.
   *

   * @param xCoordinate integer x coordinate of previous field
   * @param yCoordinate integer y coordinate of previous field
   * @return new field
   */
    private Field getAnotherFieldForSpice(int xCoordinate, int yCoordinate ){
      List<Field> neighbors = gameInstance.getGameMap().getNeighbouringFields(gameInstance.getGameMap().getField(xCoordinate,yCoordinate));
      for (Field field: neighbors) {
        if(!lookedAtFields.contains(field) && !openFields.contains(field)){
          LOGGER.log(INFO, "Adding neighbours for spice blwoe.");
          openFields.add(field);
        }
      }
       try {
        int randomIndex = MathUtil.random.nextInt(openFields.size());
        lookedAtFields.addFirst(openFields.get(randomIndex));
        return lookedAtFields.getFirst();
      }catch (IllegalArgumentException iae){
        LOGGER.log(WARNING, "No free field found, searching on whole game map for a free field: ", iae);
        for (int i = 0; i < gameInstance.getGameMap().getXSize(); i++) {
          for (int j = 0; j < gameInstance.getGameMap().getYSize(); j++) {
            if(isValidField(i,j) &&
                    !gameInstance.getGameMap().getField(i,j).getGameEntities().containsKey(GameEntityType.SPICE)){
              openFields.add(gameInstance.getGameMap().getField(i,j));
            }
          }
        }
        if(!openFields.isEmpty()){
          int randomIndex = MathUtil.random.nextInt(openFields.size());
          lookedAtFields.addFirst(openFields.get(randomIndex));
           return lookedAtFields.getFirst();
        } else {
          LOGGER.log(WARNING, "No free field found, all desert fields contain a spice!");
          return null;
        }
      }
    }


  public GameEntityType getEntityType() {
    return entityType;
  }


  /**
   * Called to get the field of this game entity.
   * @return {@link Field}
   */
  public Field getField() {
      if(field==null){
        return field;
      }else return gameInstance.getGameMap().getField(field.getXCoordinate(),field.getYCoordinate());
  }

  public GameInstance getGameInstance() {
    return gameInstance;
  }

  @Override
  public String toString(){
      return getEntityType().toString();
  }

}
