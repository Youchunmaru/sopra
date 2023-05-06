package logic.game.map;

import enums.ChangeReason;
import enums.FieldType;
import enums.GameEntityType;
import logic.game.GameInstance;
import logic.game.entity.Spice;
import logic.util.MathUtil;
import messages.gameplay.outgoing.MapChangeMessage;
import messages.util.Point;
import messages.util.Tile;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import static enums.ChangeReason.ROUND_PHASE;
import static enums.FieldType.*;
import static java.util.logging.Level.*;

/**
 * Class for the dune wandering during each round. Moves dune across game map and blows spice if necessary.
 *
 * @author Janine Grimmer
 */
public class Dune {

    // attributes for dune wandering and spice blow
    private int spiceMinimum;
    private String cellularAutomaton; // cellularAutomaton example:  B3/S23

    private long[] arraySurvive;
    private long[] arrayBorn;

    private Logger logger = Logger.getLogger(Dune.class.getName());

    private boolean isFirstRound;

    // list of fields used for spice blows
    LinkedList<Field> lookedAtFields = new LinkedList<>();
    List<Field> newFields = new LinkedList<>();

    /**
     * Constructor of dune. Sets cellular automaton for dune wandering to default value
     * from game configuration which prevents wandering.
     */
    public Dune() {
        cellularAutomaton = "";
        isFirstRound = true;
    }

    /**
     * Used to change height of desert fields and to let dune move across game map.
     *
     * @param gameMap       {@link GameMap}  current game map
     * @param version      string with current number of standard document
     * @param gameInstance {@link GameInstance} logic handling component
     * @return {@Link MapChangeMessage} message with changed game map
     */
    public MapChangeMessage changeDune(GameMap gameMap, String version, GameInstance gameInstance) {

        // generate copy of current gameMap to insert new map for next round
        GameMap nextGeneration = new GameMap(gameMap.getPartyConfig(), gameMap.getScenarioConfiguration());
        nextGeneration.copyGameMap(gameMap);
        try {
            if (isFirstRound) {
                checkAndSetTransitionRule();
                isFirstRound = false;
            }
            if (!cellularAutomaton.equals("")) {
                // change dune, generate and set changed game map
                gameInstance.setGameMap(fillNewMap(gameMap, nextGeneration, arrayBorn, arraySurvive, gameInstance));
            }
        } catch (NumberFormatException nfe) {
            logger.log(Level.SEVERE, "NumberFormatException: ", nfe);
        } catch (ArrayIndexOutOfBoundsException ae) {
            logger.log(Level.SEVERE, "Array index is out of bounds: ", ae);
        }

        return createGameMapChangeMessage(nextGeneration, ROUND_PHASE, version, gameInstance);
    }

    /**
     * Called in the first dune phase to check that transition rule is valid and
     * sets relevant parameters for dune wandering.
     */
    private void checkAndSetTransitionRule() {
        if (cellularAutomaton.length() > 19 || cellularAutomaton.equals("")) {
            logger.log(Level.WARNING,
                    "Default rule will be used.");
            // no dune wandering
            cellularAutomaton = "";
        } else {
            // split rule with regular expression and get numbers after S and B
            String[] rule = cellularAutomaton.split(
                    "[B/S]");
            int lengthBorn = rule[1].length();
            int lengthSurvive = rule[3].length();

            // select numbers of rule
            arraySurvive = new long[lengthSurvive];
            arraySurvive = selectNumbersOfRule(lengthSurvive, arraySurvive, rule[3]);
            arrayBorn = new long[lengthBorn];
            arrayBorn = selectNumbersOfRule(lengthBorn, arrayBorn, rule[1]);
        }
    }


    /***
     * Used to separate numbers in rule.
     *
     * @param length amount of numbers that need to be stored separately in array
     * @param array needed to save split numbers
     * @param bornSurvive String that contains the numbers
     * @return array with numbers, either numbers of living neighbors to revive a field or to let it survive
     */
    public long[] selectNumbersOfRule(int length, long[] array, String bornSurvive) {
        StringBuilder stringBuilder = new StringBuilder();
        // start with 1 and add further zeros
        stringBuilder.append("1");
        for (int i = 0; i < length - 1; i++) {
            stringBuilder.append('0');
        }

        for (int i = 0; i < length; i++) {
            // insert single number
            array[i] = Integer.valueOf(bornSurvive) / Integer.valueOf(stringBuilder.toString());
            // remove first number
            bornSurvive = bornSurvive.substring(1);
            // remove last number and replace
            String string = stringBuilder.toString().substring(0, stringBuilder.toString().length() - 1);
            stringBuilder.replace(0, length, string);
        }
        return array;
    }


    /**
     * Called to generate new map and to change flat sand and dune fields.
     *
     * @param gameMap        current {@link GameMap}
     * @param nextGeneration new {@link GameMap}
     * @param arrayBorn      array with integers, number of fields that must be alive in order to create a dune field
     * @param arraySurvive   array with integers, number of fields that must be alive in order to remain a dune
     * @return new {@link GameMap}
     */
    private GameMap fillNewMap(GameMap gameMap, GameMap nextGeneration, long[] arrayBorn,
                               long[] arraySurvive, GameInstance gameInstance) {
        for (int i = 0; i < gameMap.getXSize(); i++) {
            for (int j = 0; j < gameMap.getYSize(); j++) {
                // only change if field is of type FLAT_SAND or DUNE
                if (gameMap.getField(i, j).getFieldType() == FLAT_SAND
                        || gameMap.getField(i, j).getFieldType() == DUNE) {
                    // check for survival and rebirth
                    int alive = isAlive(gameMap, i, j, arrayBorn, arraySurvive, gameInstance);
                    // set new field type
                    if (alive == 0) { // dead
                        nextGeneration.getGameField()[i][j].setFieldType(FLAT_SAND);
                    } else { // born or survive
                        nextGeneration.getGameField()[i][j].setFieldType(DUNE);
                    }

                }
            }
        }
        return nextGeneration;
    }

    /**
     * Method to calculate whether a field is alive according to the transition rule (cellularAutomaton).
     *
     * @param gameField    {@Link GameMap} "old" game map
     * @param xCoordinate  integer, coordinate of x-axis of field under study
     * @param yCoordinate  integer, coordinate of y-axis of field under study
     * @param arrayBorn    array of longs, number of living cells needed to revive this field
     * @param arraySurvive array of longs, number of living cells needed to survive
     * @param gameInstance {@link GameInstance}
     * @return integer; 0 if field is dead or 1 if field is alive
     */
    private int isAlive(GameMap gameField, int xCoordinate, int yCoordinate, long[] arrayBorn,
                        long[] arraySurvive, GameInstance gameInstance) {
        // low field niveau = dead = plateau, heliport, flat sand; high field niveau = alive = city, mountains, dune
        int aliveCellCounter = 0;
        for (int i = xCoordinate - 1; i <= xCoordinate + 1; i++) {
            for (int j = yCoordinate - 1; j <= yCoordinate + 1; j++) {

                // jump over field under study and if x or y cross the edge (idea: edge is dead, so nothing to add)
                if (isValidButDifferentField(xCoordinate, yCoordinate, gameInstance, i, j) &&
                        (gameField.getField(i, j).getFieldType() == CITY
                                || gameField.getField(i, j).getFieldType() == DUNE
                                || gameField.getField(i, j).getFieldType() == MOUNTAINS)) {
                    aliveCellCounter++;

                }
            }
        }

        return checkBornOrSurvive(aliveCellCounter, gameField.getField(xCoordinate, yCoordinate).getFieldType(), arrayBorn, arraySurvive);

    }

    /**
     * Called to check whether counted number of alive fields can give birth to a dune field or can let a dune survive.
     *
     * @param aliveCellCounter integer, number of cells alive around the field under study
     * @param fieldType        {@Link FieldType} type of field under study
     * @param arrayBorn        array of longs, number of living cells needed to revive this field
     * @param arraySurvive     array of longs, number of living cells needed to survive as dune
     * @return 1 if alive or 0 if dead
     */
    private int checkBornOrSurvive(int aliveCellCounter, FieldType fieldType, long[] arrayBorn,
                                   long[] arraySurvive) {
        int alive = 0;

        // check survive
        if (fieldType == DUNE) {
            for (int i = 0; i < arraySurvive.length; i++) {
                if (aliveCellCounter == arraySurvive[i]) {
                    alive = 1;
                    break;
                }
            }
        }

        // check  born
        if (fieldType == FLAT_SAND) {
            for (int i = 0; i < arrayBorn.length; i++) {
                if (aliveCellCounter == arrayBorn[i]) {
                    alive = 1;
                    break;
                }
            }
        }

        return alive;
    }


    /**
     * Used to inform clients about changes in game map after dunes have moved.
     *
     * @param gameMap      {@link GameMap} changed game map
     * @param reason       {@link ChangeReason} for map change
     * @param version      String, current version of standard document
     * @param gameInstance {@link GameInstance} logic handling component
     * @return {@Link MapChangeMessage} message that contains the changed game map
     */
    public MapChangeMessage createGameMapChangeMessage(GameMap gameMap, ChangeReason reason, String version, GameInstance gameInstance) {
        Tile[][] tileMap = gameMap.createNewTileMap(gameInstance);
        Point stormEye = new Point(gameInstance.getSandstorm().getField().getXCoordinate(),
                gameInstance.getSandstorm().getField().getYCoordinate());
        return new MapChangeMessage(version, reason, tileMap, stormEye);
    }




    /**
     * Called to blow spice on map if too less spice is available.
     *
     * @param version      current version of standard document, String
     * @param gameInstance {@link GameInstance} logic handling component
     * @return {@link MapChangeMessage} contains changes on game map due to spice blow or null
     */
    public MapChangeMessage doSpiceBlow(String version, GameInstance gameInstance) {
        GameMap gameMap = gameInstance.getGameMap();
        // check if spice amount on map is lower than threshold in config
        int spiceAmount = calculateSpiceAmountOnMap(gameMap);
        if (spiceAmount < spiceMinimum) {
            //if true choose random dune field on map and random int from [3,6] for spice amount to be blown on map
            int[] xAndY = chooseRandomNumbers(gameMap);
            int numberOfNewSpice = MathUtil.random.nextInt(4) + 3;
            // change chosen field and all neighbouring desert fields to flat sand or dune (change height)
            changeFieldsInSpiceBlow(xAndY[0], xAndY[1], gameMap);
            // add one spice on this field
            gameMap.getField(xAndY[0], xAndY[1]).addGameEntity(new Spice(gameInstance));
            lookedAtFields.add(gameMap.getField(xAndY[0], xAndY[1]));
            blowSpice(gameInstance, xAndY[0], xAndY[1], numberOfNewSpice-1);

            return createGameMapChangeMessage(gameMap, ROUND_PHASE, version, gameInstance);
        }
        return null;
    }


    /***
     * Used to calculate current amount of spice objects on game map.
     * @param gameMap current game map
     * @return integer with number of spice collectible on game map
     */
    private int calculateSpiceAmountOnMap(GameMap gameMap) {
        int spiceAmount = 0;
        for (int i = 0; i < gameMap.getXSize(); i++) {
            for (int j = 0; j < gameMap.getYSize(); j++) {
                if (gameMap.getField(i, j).getGameEntity(GameEntityType.SPICE) != null) {
                    spiceAmount++;
                }
            }
        }
        return spiceAmount;
    }


    /**
     * Called during spice blow to get two random numbers for field coordinates.
     *
     * @param gameMap current {@link GameMap}
     * @return integer array with two values, first for x coordinate, second for y coordinate
     */
    private int[] chooseRandomNumbers(GameMap gameMap) {
        int[] randomNumbers = new int[2];
        randomNumbers[0] = MathUtil.random.nextInt(gameMap.getXSize());
        randomNumbers[1] = MathUtil.random.nextInt(gameMap.getYSize());
        if (gameMap.getField(randomNumbers[0], randomNumbers[1]).getFieldType() == DUNE
                || gameMap.getField(randomNumbers[0], randomNumbers[1]).getFieldType() == FLAT_SAND) {
            return randomNumbers;
        } else {
            chooseRandomNumbers(gameMap);
        }
        return randomNumbers;
    }


    /**
     * Called to change fields randomly into flat sand or dune if in spice blow.
     *
     * @param x       integer, x coordinate of spice blow center
     * @param y       integer, y coordinate of spice blow center
     * @param gameMap current {@link GameMap}
     */
    private void changeFieldsInSpiceBlow(int x, int y, GameMap gameMap) {
        // change chosen field and all neighbouring dune fields to flat sand or dune (change height)
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                if ((i >= 0) && (i < gameMap.getXSize()) && (j >= 0) && (j < gameMap.getYSize()
                        && (gameMap.getField(i, j).getFieldType() == DUNE
                        || gameMap.getField(i, j).getFieldType() == FLAT_SAND))) {
                    int randomChange = MathUtil.random.nextInt(2);
                    if (randomChange == 0) {
                        gameMap.getField(i, j).setFieldType(FLAT_SAND);
                    } else {
                        gameMap.getField(i, j).setFieldType(DUNE);
                    }
                }
            }
        }
    }


    /**
     * Method used to blow spice during spice blow .
     *
     * @param gameInstance     {@link GameInstance} logic handling component
     * @param xCoordinate      integer x coordinate of field in center
     * @param yCoordinate      integer y coordinate of field in center
     * @param numberOfNewSpice integer, number of spice to be spread on gameMap
     */
    public void blowSpice(GameInstance gameInstance, int xCoordinate, int yCoordinate, int numberOfNewSpice) {
        newFields.remove(gameInstance.getGameMap().getField(xCoordinate,yCoordinate));

        GameMap gameField = gameInstance.getGameMap();

        for (int i = xCoordinate - 1; i <= xCoordinate + 1; i++) {
            for (int j = yCoordinate - 1; j <= yCoordinate + 1; j++) {
                // jump over field under study and if x or y cross the edge (across edge no spice can be placed)
                if (isValidButDifferentField(xCoordinate, yCoordinate, gameInstance, i, j) &&
                        isDesertField(gameField.getField(i, j)) &&
                        !gameField.getField(i,j).getGameEntities().containsKey(GameEntityType.SANDWORM)
                        && !gameField.getField(i,j).getGameEntities().containsKey(GameEntityType.SPICE)) {
                    if (numberOfNewSpice <= 0) {
                        break;
                    }

                    gameField.getField(i, j).addGameEntity(new Spice(gameInstance));
                    numberOfNewSpice--;

                }

            }
        }

        if (numberOfNewSpice > 0) {
            findNewField(xCoordinate, yCoordinate, gameInstance, numberOfNewSpice, gameField);
        }
    }

    /**
     * Called to find a free field to blow spice.
     *
     * @param xCoordinate      x coordinate of spice blow center
     * @param yCoordinate      y coordinate of spice blow center
     * @param gameInstance     {@link GameInstance} game logic handling component
     * @param numberOfNewSpice number of spice to be blown
     * @param gameField        current game map
     * @return true if successful
     */
    private boolean findNewField(int xCoordinate, int yCoordinate, GameInstance gameInstance,
                              int numberOfNewSpice, GameMap gameField) {

        List<Field> neighbors = gameField.getNeighbouringFields(gameField.getField(xCoordinate,yCoordinate));
        for (Field field: neighbors) {
            if(!lookedAtFields.contains(field) && !newFields.contains(field)){

                newFields.add(field);
            }
        }

       try {
           int randomIndex = MathUtil.random.nextInt(newFields.size());
           lookedAtFields.addFirst(newFields.get(randomIndex));
           blowSpice(gameInstance, lookedAtFields.getFirst().getXCoordinate(), lookedAtFields.getFirst().getYCoordinate(), numberOfNewSpice);
       }catch (IllegalArgumentException iae){
           logger.log(WARNING, "No free field found, searching on whole game map: ", iae);
           for (int i = 0; i < gameField.getXSize(); i++) {
               for (int j = 0; j < gameField.getYSize(); j++) {
                   if(isDesertField(gameField.getField(i,j)) &&
                           !gameField.getField(i,j).getGameEntities().containsKey(GameEntityType.SPICE)){
                       newFields.add(gameField.getField(i,j));
                   }
               }
           }
           if(!newFields.isEmpty()){
               int randomIndex = MathUtil.random.nextInt(newFields.size());
               lookedAtFields.addFirst(newFields.get(randomIndex));
               blowSpice(gameInstance, lookedAtFields.getFirst().getXCoordinate(), lookedAtFields.getFirst().getYCoordinate(), numberOfNewSpice);
               return true;
           } else {
               logger.log(WARNING, "No free field found, all desert fields contain a spice! ");
           return false;
        }
       }
        return true;

    }

    /**
     * Method to check that field is within the game map borders and that the currently looked at field
     * is not the same as the field in the spice blow center.
     *
     * @param xCoordinate  integer x coordinate of field in center
     * @param yCoordinate  integer y coordinate of field in center
     * @param gameInstance {@link GameInstance}
     * @param i            x coordinate of field under study
     * @param j            y coordinate of field under study
     * @return true if coordinates are valid and differ from the ones of the spice blow center
     */
    public boolean isValidButDifferentField(int xCoordinate, int yCoordinate, GameInstance gameInstance, int i, int j) {
        return (i >= 0) && (i < gameInstance.getGameMap().getXSize()) && (j >= 0)
                && (j < gameInstance.getGameMap().getYSize()) && !(i == xCoordinate && j == yCoordinate);
    }


    /**
     * Method to check type of field.
     *
     * @param field {@link Field} under study
     * @return true if desert field
     */
    private boolean isDesertField(Field field) {
        return (field.getFieldType() == DUNE
                || field.getFieldType() == FLAT_SAND);
    }


    /**
     * Called to get minimum of spice on game map in order to be able to do a spice blow.
     *
     * @return positive integer >= 0
     */
    public int getSpiceMinimum() {
        return spiceMinimum;
    }

    /**
     * Called to set minimum of spice in order to be able to do a spice blow.
     *
     * @param spiceMinimum positive integer >= 0
     */
    public void setSpiceMinimum(int spiceMinimum) {
        this.spiceMinimum = Math.abs(spiceMinimum);
    }

    /**
     * Called to get the rules for dune wandering.
     *
     * @return string with numbers and letters that are the rules for dune wandering
     */
    public String getCellularAutomaton() {
        return cellularAutomaton;
    }

    /**
     * Called to set the rules for dune wandering.
     *
     * @param cellularAutomaton string with numbers and letters, transition rule
     */
    public void setCellularAutomaton(String cellularAutomaton) {
        this.cellularAutomaton = cellularAutomaton;
    }

    /**
     * Called for testing purpose only to check whether cellularAutomaton
     * rule has already been saved.
     *
     * @return true if never called changeDune() before
     */
    public boolean isFirstRound() {
        return isFirstRound;
    }

}
