package logic.game.map;

import enums.FieldType;
import enums.GameEntityType;
import logic.game.GameInstance;
import logic.game.entity.City;
import logic.game.entity.GameEntity;
import logic.game.entity.unit.Unit;
import messages.util.Tile;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Describes the field and its properties. Part of the {@link GameMap}.
 *
 * @author Samuel Gröner
 */
public class Field {

  //Position on the grid
  private final int x;
  private final int y;
  //Attributed of the Terrain
  private FieldType fieldType;
  private boolean isAccessible;
  private boolean isHighTerrain;
  //Content of the Field
  private Map<GameEntityType, GameEntity> gameEntities = new EnumMap<>(GameEntityType.class);
  //Cost of the edges in the path finding graph
  private int moveCost;
  //Previously considered node in the path finding algorithm
  private Field parentNode;
  // return field type as string
  /*
  @Override
  public String toString() {
    String str = "{" +x  +", " + y +"}";
    str+= " FieldType: " +fieldType;
    str+= " isAccessible,isHighTerrain: (" + isAccessible + ", " + isHighTerrain +")";
    str+= "\n GameEntities: {";
    for(GameEntity entity : gameEntities.values()){
      str+=entity.toString() +", ";
    }
    str+="}";

    return str;
  }
  */
  @Override
  public String toString(){
    return fieldType.toString();
  }
  /**
   * Constructor.
   *
   * @param x the horizontal coordinate
   * @param y the vertical coordinate
   */
  public Field(int x, int y, FieldType fieldType) {
    moveCost = 0;
    parentNode = null;
    this.x = x;
    this.y = y;
    this.fieldType = fieldType;
    setIsHighTerrain(checkForHighTerrain());
    setIsAccessible(checkForAccessibility());
  }

  /**
   * Checks whether a field contains a city, sandworm or is of type mountains.
   * @return true if none is true
   */
  private boolean checkForAccessibility() {
    return !gameEntities.containsKey(GameEntityType.CITY) && !gameEntities.containsKey(
        GameEntityType.SANDWORM) && fieldType != FieldType.MOUNTAINS;

  }

  /**
   * Checks whether field is high terrain
   * @return true if field is city, dune or mountains
   */
  private boolean checkForHighTerrain() {
    return fieldType == FieldType.CITY || fieldType == FieldType.MOUNTAINS
        || fieldType == FieldType.DUNE;
  }

  /**
   * Adds a gameEntity to this field, if it isn't already taken.
   *
   * @param entity the entity to add
   * @return if it was successful
   */
  public boolean addGameEntity(GameEntity entity) {
    if (!gameEntities.containsKey(entity.getEntityType())) {
      gameEntities.put(entity.getEntityType(), entity);
      setIsAccessible(checkForAccessibility());
      return true;
    } else {
      return false;
    }
  }

  /**
   * Removes the specified entity.
   *
   * @param entityType the type of the entity to remove
   * @return the removed entity if present or null if it doesn't exist
   */
  public GameEntity removeGameEntity(GameEntityType entityType) {
    GameEntity oldEntity;
    oldEntity = gameEntities.remove(entityType);
    setIsAccessible(checkForAccessibility());
    return oldEntity;
  }

  /**
   * Searches for the type of entity specified and returns it.
   *
   * @param type the type of entity you want
   * @return the entity or a default entity if not present
   */
  public GameEntity getGameEntity(GameEntityType type) {
    return gameEntities.get(type);
  }

  /**
   * Writes the new entity over the old.
   *
   * @param oldEntityType the type of the old entity
   * @param newEntity     the new entity to save
   * @return if it was successful
   * @implNote Deletes the entry if new is null.
   */
  public boolean setGameEntity(GameEntityType oldEntityType, GameEntity newEntity) {
    if (oldEntityType != null) {
      if (newEntity == null) {
        gameEntities.remove(oldEntityType);
      } else {
        gameEntities.put(oldEntityType, newEntity);
      }
      setIsAccessible(checkForAccessibility());
      return true;
    }
    return false;
  }

  public boolean isHighTerrain() {
    return isHighTerrain;
  }

  public void setIsHighTerrain(boolean isHighTerrain) {
    this.isHighTerrain = isHighTerrain;
  }

  public int getMoveCost() {
    return moveCost;
  }

  public void setMoveCost(int moveCost) {
    this.moveCost = moveCost;
  }

  public Field getParentNode() {
    return parentNode;
  }

  public void setParentNode(Field parentNode) {
    this.parentNode = parentNode;
  }

  public Map<GameEntityType, GameEntity> getGameEntities() {
    return gameEntities;
  }

  public void setGameEntities(Map<GameEntityType, GameEntity> gameEntities) {
    this.gameEntities = gameEntities;
  }

  public boolean isAccessible() {
    return isAccessible;
  }

  public void setIsAccessible(boolean isAccessible) {
    this.isAccessible = isAccessible;
  }

  public int getXCoordinate() {
    return x;
  }

  public int getYCoordinate() {
    return y;
  }

  public FieldType getFieldType() {
    return fieldType;
  }

  /**
   * Gives this field a new type.
   *
   * @param fieldType the new type of the field
   */
  public void setFieldType(FieldType fieldType) {
    this.fieldType = fieldType;
    setIsAccessible(checkForAccessibility());
    setIsHighTerrain(checkForHighTerrain());
  }



  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Field field = (Field) o;

    if (x != field.x) {
      return false;
    }
    if (y != field.y) {
      return false;
    }
    return fieldType == field.fieldType;
  }

  public Tile fieldToTile(GameInstance gameInstance) {
    boolean inSandstorm = false;
    boolean hasSpice = false;
    int clientID = -1;
    //Check if a unit is present and if in sandstorm
    if (gameEntities.containsKey(GameEntityType.UNIT)) {
      Unit unit = (Unit) gameInstance.getGameMap().getField(x, y)
          .getGameEntity(GameEntityType.UNIT);
      inSandstorm = unit.isInSandstorm();
    }
    //Check if spice is present
    if (gameEntities.containsKey(GameEntityType.SPICE)) {
      hasSpice = true;
    }
    //Check if city is present and getting clientID
    if (gameEntities.containsKey(GameEntityType.CITY)) {
      clientID = gameInstance.getPlayer(
          ((City) (gameEntities.get(GameEntityType.CITY))).getPlayer()).getClient().getClientID();
    }
    //Check if is in Sandstorm
    List<Field> neigbours = gameInstance.getGameMap().getNeighbouringFields(this);
    for(Field field : neigbours){
      if(field.gameEntities.containsKey((GameEntityType.SANDSTORM))){
        inSandstorm=true;
      }
    }
    if(gameEntities.containsKey((GameEntityType.SANDSTORM))){
      inSandstorm=true;
    }
    //Choosing the correct constructor based on the presence of a city entity
    if (clientID != -1) {
      return new Tile(fieldType,
          clientID, hasSpice, inSandstorm);
    } else {
      return new Tile(fieldType,
          hasSpice, inSandstorm);
    }

  }


}
