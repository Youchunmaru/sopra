package logic.game.map;


import enums.FieldType;
import enums.GameEntityType;
import enums.PlayerEnum;
import logic.game.GameInstance;

import logic.game.entity.unit.Unit;
import messages.configuration.PartyConfig;
import messages.configuration.ScenarioConfig;

import messages.util.Tile;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import static enums.FieldType.*;
import static java.util.logging.Level.INFO;

/**
 * Holds the current status of the map.
 *
 * @author Samuel Gröner, Janine Grimmer
 */
public class GameMap {
    private final PartyConfig partyConfig;
    private final ScenarioConfig scenarioConfig;
    private final int xSize;
    private final int ySize;
    //The actual GameMap
    private Field[][] gameField;


    private Logger logger = Logger.getLogger(GameMap.class.getName());

    /**
     * Constructor.
     *
     * @param partyConfig    the party configuration
     * @param scenarioConfig the scenario configuration
     */
    public GameMap(PartyConfig partyConfig, ScenarioConfig scenarioConfig) {
        this.partyConfig = partyConfig;
        this.scenarioConfig = scenarioConfig;

        xSize = scenarioConfig.getScenario().length;     //horizontal
        ySize = scenarioConfig.getScenario()[0].length;  //vertical
        generateEmptyMapFromScratch();
    }

    private void generateEmptyMapFromScratch() {
        gameField = new Field[xSize][ySize];
          for (int i = 0; i < xSize; i++) {              //horizontal
            for (int j = 0; j < ySize; j++) {          //vertical
                gameField[i][j] = new Field(i, j, scenarioConfig.getScenario()[i][j].getFieldType());
            }
        }
    }

    public Tile[][] generateTileFromFieldMap(GameInstance gameInstance) {
        Tile[][] tileMap = new Tile[xSize][ySize];
        for (int i = 0; i < xSize; i++) {              //horizontal
            for (int j = 0; j < ySize; j++) {          //vertical
                tileMap[i][j] = gameField[i][j].fieldToTile(gameInstance);
            }
        }
        return tileMap;
    }

    public int getXSize() {
        return xSize;
    }

    public int getYSize() {
        return ySize;
    }

    public Field[][] getGameField() {
        return gameField;
    }



    /**
     * @param xCoordinate the horizontal coordinate
     * @param yCoordinate the vertical coordinate
     * @return the field with coordinates (x, y) if existent or null if non-existent
     */
    public Field getField(int xCoordinate, int yCoordinate) {
        if (xCoordinate >= 0 && xCoordinate < getXSize() && yCoordinate >= 0
                && yCoordinate < getYSize()) {
            return gameField[xCoordinate][yCoordinate];
        }
        return null;
    }

    /**
     * Called in dune change phase to copy old map  into new game map
     *
     * @param oldMap {@link GameMap} gamemap of previous round
     * @author Janine Grimmer
     */
    public void copyGameMap(GameMap oldMap) {
        for (int i = 0; i < oldMap.getXSize(); i++) {
            for (int j = 0; j < oldMap.getYSize(); j++) {
                getGameField()[i][j] = new Field(i, j, oldMap.getGameField()[i][j].getFieldType());
                getGameField()[i][j].setGameEntities(oldMap.getGameField()[i][j].getGameEntities());
            }
        }
    }

    /**
     * Called to find a free field in order to put character there
     *
     * @param unit                  {@link Unit} unit to place on game map
     * @param xCoordinate           x coordinate of field
     * @param yCoordinate           y coordinate of field
     * @param randomNumberGenerator {@link Random}
     * @return {@link Field} free field to place unit on
     * @author Janine Grimmer
     */
    public Field getFreeField(Unit unit, int xCoordinate, int yCoordinate, Random randomNumberGenerator) {

        // possible coordinates: (xCoordinate-1), xCoordinate, (xCoordinate+1), (yCoordinate-1), yCoordinate, (yCoordinate+1)
        int newXCoordinate =
                randomNumberGenerator.nextInt(3) + (xCoordinate - 1);
        int newYCoordinate =
                randomNumberGenerator.nextInt(3) + (yCoordinate - 1);

        if (newXCoordinate < 0 || newYCoordinate < 0 ||
                newXCoordinate >= getXSize() ||
                newYCoordinate >= getYSize()
                || (newXCoordinate == xCoordinate && newYCoordinate == yCoordinate)) {
            return getFreeField(unit, xCoordinate, yCoordinate, randomNumberGenerator);
        }
        Field field = getGameField()[newXCoordinate][newYCoordinate];
        // check that this field is accessible,  if true put character on this field
        if (field.isAccessible() && unit.setField(field)) {
            unit.setIsDefeated(false);
            return field;

        } else { // try again
            getFreeField(unit, xCoordinate, yCoordinate, randomNumberGenerator);
        }
        return field;
    }

    /**
     * Called to get a free neighbouring field.
     *
     * @param field initial field
     * @param checkAgainst {@link logic.game.entity.GameEntity}
     * @param resultList list of new fields
     * @param previousNeighbours list of previously found neighbouring fields
     * @param visitedFields list of already visited fields
     * @return list of possible fields
     * @author Julian Korinth
     */
    public List<Field> getNearestFreeFieldsFromBlockedField(Field field, GameEntityType checkAgainst,List<Field> resultList, List<Field> previousNeighbours, List<Field> visitedFields){

        if(previousNeighbours.isEmpty()&&field!=null){
            //Search begins
            //The inital field is visited
            visitedFields.add(field);
            //Get current neighbours of initial position
            List<Field> currentNeighbourList = getNeighbouringFields(getField(field.getXCoordinate(),field.getYCoordinate()));
            //Get all eligible Fields
            resultList=getEligibleFields(currentNeighbourList,checkAgainst,resultList);
            //If no fields are found search your initial fields neighbours
            if(resultList.isEmpty()){
                return getNearestFreeFieldsFromBlockedField(null, checkAgainst,resultList,currentNeighbourList, visitedFields);
            }else{
                //If fields were found you are done
                return resultList;
            }
            //In previous search no fields were found
        }else if(!previousNeighbours.isEmpty()){
            //All neighbours of same grade of separation
            List<Field> allSameLevelNeighbours = new LinkedList<>();
            for(Field previousNeighBour : previousNeighbours){
                if(!visitedFields.contains(previousNeighBour)){
                    //The previous neighbour is visited
                    visitedFields.add(previousNeighBour);
                    List<Field> previousNeigboursCurrentNeighbours=getNeighbouringFields(previousNeighBour);
                    for(Field previousNeigboursCurrentNeighbour : previousNeigboursCurrentNeighbours){
                        if(!visitedFields.contains(previousNeigboursCurrentNeighbour)&&!allSameLevelNeighbours.contains(previousNeigboursCurrentNeighbour)){
                            allSameLevelNeighbours.add(previousNeigboursCurrentNeighbour);
                        }
                    }
                    resultList=getEligibleFields(previousNeigboursCurrentNeighbours, checkAgainst, resultList);
                }
            }
            if(resultList.isEmpty()){
                return getNearestFreeFieldsFromBlockedField(null,checkAgainst,resultList,allSameLevelNeighbours,visitedFields);
            }else{
                return resultList;
            }
        }else{
            return resultList;
        }
    }

    /**
     *Method to retrieve eligible fields.
     *
     * @param currentNeighbourList list with currently found neighbouring fields
     * @param checkAgainst {@link logic.game.entity.GameEntity} that shall be placed upon the new field
     * @param resultList currently list with possible fields
     * @return list of fields
     */
    private List<Field> getEligibleFields(List<Field> currentNeighbourList, GameEntityType checkAgainst, List<Field> resultList ){
        for(Field currentNeighbour : currentNeighbourList){
            //check all neighbours
            if(currentNeighbour.isAccessible()&&currentNeighbour.getGameEntity(checkAgainst)==null){
                //add all neighboura that are possible
                if(!resultList.contains(currentNeighbour)){
                    resultList.add(currentNeighbour);
                }
            }
        }
        return resultList;
    }


    /**
     * Reshapes the map after an atomics attack.
     *
     * @param x the vertical coordinate
     * @param y the horizontal coordinate
     */
    public void reshapeMapAfterAtomics(int x, int y) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                Field fieldIter = getField(x + i, y + j);
                if (fieldIter != null) {
                    if (fieldIter.getFieldType() == FieldType.MOUNTAINS) {
                        fieldIter.setFieldType(FieldType.PLATEAU);
                    } else if (fieldIter.getFieldType() == FieldType.DUNE) {
                        fieldIter.setFieldType(FieldType.FLAT_SAND);
                    }
                }
            }
        }
    }


    /**
     * Called when mechanism of excessively long game is activate. Sets rock fields to dune fields
     * so that Shai Hulud can move across the whole game map.
     *
     * @author Janine Grimmer
     */
    public void fillMapWithSand() {
        for (int i = 0; i < getXSize(); i++) {
            for (int j = 0; j < getYSize(); j++) {
                if (getField(i, j).getFieldType() == PLATEAU || getField(i, j).getFieldType() == MOUNTAINS
                || getField(i,j).getFieldType() == HELIPORT) {
                    getField(i, j).setFieldType(DUNE);
                }
            }
        }
    }


    public PartyConfig getPartyConfig() {
        return partyConfig;
    }


    public ScenarioConfig getScenarioConfiguration() {
        return scenarioConfig;
    }

    /**
     * Called to create a map of {@link Tile} to send a map change demand message.
     *
     * @param gameInstance game logic handling component
     * @return changed map as 2D array of {@link Tile}
     * @author Janine Grimmer
     */
    public Tile[][] createNewTileMap(GameInstance gameInstance) {
        Tile[][] newMap = new Tile[gameInstance.getGameMap().getXSize()][gameInstance.getGameMap()
                .getYSize()];
        for (int i = 0; i < gameInstance.getGameMap().getXSize(); i++) {
            for (int j = 0; j < gameInstance.getGameMap().getYSize(); j++) {

                Optional<Integer> clientID = Optional.empty();

                if (gameInstance.getGameMap().getField(i, j).getGameEntities().containsKey(GameEntityType.UNIT)) {
                    Unit unit = (Unit) gameInstance.getGameMap().getField(i, j)
                            .getGameEntity(GameEntityType.UNIT);
                    if (gameInstance.arePlayersPresent()) {
                        if (unit.getPlayer() == PlayerEnum.PLAYER_ONE) {
                            clientID = Optional.of(gameInstance.getPlayerOne().get().getClientID());
                        } else {
                            clientID = Optional.of(gameInstance.getPlayerTwo().get().getClientID());
                        }
                    } else {
                        Logger.getLogger(GameInstance.class.getName()).log(Level.SEVERE, "No players are present.");
                    }
                }

                boolean hasSpice = gameInstance.getGameMap().getField(i, j).getGameEntities().containsKey(GameEntityType.SPICE);
                boolean inSandstorm = checkIfFieldIsInSandstorm(gameInstance.getGameMap().getField(i, j), gameInstance);
                if (clientID.isPresent()) {
                    newMap[i][j] = new Tile(gameInstance.getGameMap().getField(i, j).getFieldType(),
                            clientID.orElse(null), hasSpice, inSandstorm);
                } else {
                    newMap[i][j] = new Tile(gameInstance.getGameMap().getField(i, j).getFieldType(),
                            hasSpice, inSandstorm);
                }
            }

        }
        return newMap;
    }

    /**
     * Called when a two dimensional Tile array shall be created to find out whether a field is in the
     * sandstorm.
     *
     * @param field        field under study
     * @param gameInstance {@link GameInstance}
     * @return true if field is in the 3x3 square of the sandstorm
     */
    public boolean checkIfFieldIsInSandstorm(Field field, GameInstance gameInstance) {
        Field sandstormEye = gameInstance.getSandstorm().getField();
        //logger.log(INFO, "Field of sandstorm: {0}", sandstormEye);
        for (int i = sandstormEye.getXCoordinate() - 1; i < sandstormEye.getXCoordinate() + 2; i++) {
            for (int j = sandstormEye.getYCoordinate() - 1; j < sandstormEye.getYCoordinate() + 2; j++) {
                if (field == gameInstance.getGameMap().getField(i, j)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Method to select neighbouring fields of a specific field
     * @param field Field from which neighbouring Fields are selected
     * @return list with all valid neighbouring fields
     */
    public List<Field> getNeighbouringFields(Field field){
        int x = field.getXCoordinate();
        int y = field.getYCoordinate();
        List<Field> neighbouringFields = new LinkedList<>();
        if(getField(x-1, y)!=null){
            neighbouringFields.add(getField(x-1, y));
        }
        if(getField(x, y-1)!=null){
            neighbouringFields.add(getField(x, y-1));
        }
        if(getField(x+1, y)!=null){
            neighbouringFields.add(getField(x+1, y));
        }
        if(getField(x, y+1)!=null){
            neighbouringFields.add(getField(x, y+1));
        }
        if(getField(x+1, y+1)!=null){
            neighbouringFields.add(getField(x+1, y+1));
        }
        if(getField(x-1, y-1)!=null){
            neighbouringFields.add(getField(x-1, y-1));
        }
        if(getField(x-1, y+1)!=null){
            neighbouringFields.add(getField(x-1, y+1));
        }
        if(getField(x+1, y-1)!=null){
            neighbouringFields.add(getField(x+1, y-1));
        }
        return neighbouringFields;
    }

    /**
     * Method to find out whether two fields are neighbours.
     * @param originField start field
     * @param potentialNeigbouringField potential neighour of originField
     * @return true if both fields are neighbours
     */
    public boolean isFieldNeighbouring(Field originField, Field potentialNeigbouringField){
        if(originField == null || potentialNeigbouringField == null) return false;
        int originX = originField.getXCoordinate();
        int originY = originField.getYCoordinate();
        int targetX = potentialNeigbouringField.getXCoordinate();
        int targetY = potentialNeigbouringField.getYCoordinate();
        return (Math.abs(originX-targetX)<=1&&Math.abs(originY-targetY)<=1);
    }

}
