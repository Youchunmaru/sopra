package logic.game.entity;

import enums.ChangeReason;
import enums.GameEntityType;
import enums.PlayerEnum;
import logic.game.GameInstance;
import logic.game.entity.unit.Unit;
import logic.game.map.Field;
import logic.game.map.FieldSearch;
import logic.util.MathUtil;
import messages.Message;
import messages.gameplay.outgoing.*;
import messages.util.Point;
import messages.util.Tile;
import messages.util.UnitStatChange;
import network.util.Player;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static enums.FieldType.*;
import static java.util.logging.Level.INFO;


/**
 * Holds all information of a sandworm. A type of {@link GameEntity}.
 *
 * @author Janine Grimmer, Samuel Gröner
 */
public class Sandworm extends GameEntity {

    private Unit currentMeal;
    private Unit lastCharacterStanding;
    private final int sandWormSpawnDistance;
    private final int sandWormSpeed;
    private boolean targetFound;
    private int loser = -1;
    private final FieldSearch fieldSearch;
    // flag for shai-hulud in stateMachine
    private boolean isShaiHulud;
    // flag for visibility of sandworm
    private boolean isVisible;

    private final Logger LOGGER = Logger.getLogger(Sandworm.class.getName());

    /**
     * Constructor for a sandworm. Sets all necessary boolean attributes to false.
     */
    public Sandworm(GameInstance gameInstance, int sandWormSpawnDistance, int sandWormSpeed) {
        super(GameEntityType.SANDWORM, gameInstance);
        isVisible = false;
        isShaiHulud = false;
        targetFound = false;
        fieldSearch = new FieldSearch();
        if (sandWormSpawnDistance >= 2) {
            // prevent negative distance with absolute value
            this.sandWormSpawnDistance = Math.abs(sandWormSpawnDistance);
        } else { // set to default value
            this.sandWormSpawnDistance = 2;
        }
        this.sandWormSpeed = Math.abs(sandWormSpeed);
    }


    public Unit getLastCharacterStanding() {
        return lastCharacterStanding;
    }

    public void setLastCharacterStanding(Unit unit) {
        lastCharacterStanding = unit;
    }

    /**
     * Called when sandworm is on the same field as a unit.
     *
     * @param character {@link Unit} that is swallowed by the sandworm
     */
    public void eat(Unit character) {
        // mark character as swallowed
        character.setIsSwallowed(true);
        character.setHealthPoints(0);
    }

    /**
     * Method to move sandworm towards target
     *
     * @param pathToMove list of Fields, path to target
     * @return boolean, true if target is reached and swallowed in this turn
     * @author Janine Grimmer
     */
    public boolean move(List<Field> pathToMove) {

        int numberOfFieldsToMove = getSmallerNumber(pathToMove);
        for (int i = 0; i < numberOfFieldsToMove; i++) {

            this.setField(pathToMove.get(i));
            //if sandworm "meets" target, swallow it & set character state to dead (non-cloneable)
            if (currentMeal.getField().equals(getField())) {
                return true;
            }
            // flatten dune fields that are part of sandworm's path
            getField().setFieldType(FLAT_SAND);
        }
        return false;
    }

    /**
     * Called to check if target is still available.
     *
     * @return {@link SandwormDespawnMessage} or null if target exists
     * @author Janine Grimmer
     */
    public SandwormDespawnMessage checkTargetExistence(String version) {

        // check target: swallowed, defeated in fight or moved onto a plateau field --> no longer target
        if (currentMeal.isSwallowed() || currentMeal.isDefeated()
                || currentMeal.getField().getFieldType() == PLATEAU) {
            //reset target and finding flag
            currentMeal = null;
            targetFound = false;

            // let sandworm disappear with message
            return removeSandworm(version);
        } else{
            return null;
        }
    }

    /**
     * Called if sandworm does not already exist, tries to find a new target and makes sandworm
     * visible.
     *
     * @param version String of standard version
     * @param gameUnits list of all units in gameInstance
     * @param gameInstance logic handling component
     * @author Janine Grimmer
     */
    public SandwormSpawnMessage findSandwormTarget(String version, List<Unit> gameUnits, GameInstance gameInstance) {
        if (!isVisible) {

            List<Unit> loudCharacterList = new LinkedList<>();
            for (Unit character : gameUnits) {
                if (character.isLoud()) {
                    loudCharacterList.add(character);
                }
            }
            // check if at least one noisy character exists
            if (!loudCharacterList.isEmpty()) {
                LOGGER.log(INFO,"Target list is not empty, spawn.");
                return selectNoisyUnit(loudCharacterList, version, gameInstance);
            }else {
                LOGGER.log(INFO,"Target list is empty, do not spawn.");
            }
        }
        return null;
    }

    /**
     * Called from findSandwormTarget method if at least one character has been marked as loud in order
     * to select the current meal as target.
     *
     * @param loudCharacterList list of {@link Unit}
     * @param version           String, current standard version
     * @param gameInstance logic handling component
     * @return {@link SandwormSpawnMessage} to show sandworm on game map
     */
    private SandwormSpawnMessage selectNoisyUnit(List<Unit> loudCharacterList, String version, GameInstance gameInstance) {
        // select randomly one noisy character and set as target
        int index = MathUtil.random.nextInt(loudCharacterList.size());
        currentMeal = loudCharacterList.get(index);
        LOGGER.log(INFO, "Current meal has ID {0} and type {1}", new Object[]{
                currentMeal.getCharacterID(), currentMeal.getUnitType()  });

        searchRandomSandwormStartField(currentMeal.getField(),gameInstance);
        // create and broadcast sandworm spawn message
        int clientID;
        if (gameInstance.arePlayersPresent()) {
            clientID = gameInstance.getPlayerTwo().get().getClientID();
            if (currentMeal.player == PlayerEnum.PLAYER_ONE) {
                clientID = gameInstance.getPlayerOne().get().getClientID();
            }
        } else {
            clientID = -1;
        }
        Point point = new Point(getField().getXCoordinate(), getField().getYCoordinate());
        SandwormSpawnMessage spawnMessage = new SandwormSpawnMessage(version, clientID, currentMeal.getCharacterID(),
                point);
        setVisible(true);
        targetFound = true;
        return spawnMessage;
    }

    /**
     * Used to search for a random field where sandworm can start moving.
     *
     * @param targetField field of target or (if recursively called) random field
     * @param gameInstance logic handling component
     * @author Janine Grimmer
     */
    private void searchRandomSandwormStartField(Field targetField, GameInstance gameInstance) {
        List<Field> sandwormField = findFieldForSandworm(targetField, gameInstance);

        // if at least one field is a desert field and free
        if (!sandwormField.isEmpty()) {
            // choose a random field and place sandworm on it
            int randomField = MathUtil.random.nextInt(sandwormField.size());
            this.setField(sandwormField.get(randomField));
        } else {
            // do new search on a random field, use sandWormSpawnDistance
            Field newTargetField = fieldSearch.doNewFieldSearch(targetField, gameInstance);
            // start new search
            searchRandomSandwormStartField(newTargetField, gameInstance);
        }
    }

    /**
     * Called when a start field for the sandworm is searched for.
     *
     * @param targetField field of target or (if recursively called) random field
     * @return
     */
    private List<Field> findFieldForSandworm(Field targetField, GameInstance gameInstance) {
        List<Field> sandwormField = new LinkedList<>();
        for (int i = targetField.getXCoordinate() - sandWormSpawnDistance;
             i <= targetField.getXCoordinate() + sandWormSpawnDistance; i++) {
            for (int j = targetField.getYCoordinate() - sandWormSpawnDistance;
                 j <= targetField.getYCoordinate() + sandWormSpawnDistance; j++) {
                Field field = gameInstance.getGameMap().getField(i, j);
                // jump over fields directly around the target and append field if type of desert field
                if (isValidField(i, j) && !(Math.abs(currentMeal.getField().getXCoordinate() - i) <= 1 &&
                        Math.abs(currentMeal.getField().getYCoordinate() - j) <= 1) &&
                        (field.getFieldType() == FLAT_SAND || field.getFieldType() == DUNE)
                        && !field.getGameEntities().containsKey(GameEntityType.UNIT)) {
                    sandwormField.add(field);
                }
            }
        }
        return sandwormField;
    }


    /**
     * Called to find shortest way from sandworm to target Method uses A* algorithm with Chebyshev
     * distance d=max(|xd​|,|yd​|) xd​=xgoal​−xcellposition​ yd=ygoal−ycellposition g: movement cost
     * from start to current field (+1 if accessible) h: heuristic estimated movement cost from
     * current field to target field f: = g+h (total movement cost)
     *
     * @param version String with current number of standard document
     * @return list of messages that are generated due to sandworm's move
     * @param gameInstance logic handling component
     * @author Janine Grimmer
     */
    private LinkedList<Message> aStarAlgorithm(String version, GameInstance gameInstance) {

        List<Field> openList = new LinkedList<>();
        List<Field> closedList = new LinkedList<>();
        openList.add(getField());
        int lowestCost = 1000;
        int indexWithLowestCost = -1;
        expand(getField(), closedList, openList, gameInstance);
        closedList.add(getField());

        while (!openList.isEmpty()) {

            indexWithLowestCost = findMinCostField(openList, lowestCost);
            Field currentField = openList.get(indexWithLowestCost);
            if (currentField == currentMeal.getField()) {
                return targetFoundStartingMoving(closedList, currentField, gameInstance, version);
            }

            expand(currentField, closedList, openList, gameInstance);
            closedList.add(currentField);
            openList.remove(currentField);

        }

        LinkedList<Message> messages = new LinkedList<>();
       if (openList.isEmpty()) {

            //search new start field, move ends here
            searchRandomSandwormStartField(getField(), gameInstance);
           Message message = removeSandworm(version);
           Point position = new Point(currentMeal.getField().getXCoordinate(),
                   currentMeal.getField().getYCoordinate());
           SandwormSpawnMessage sandwormSpawn = new SandwormSpawnMessage(version,
                   getClientID(currentMeal, gameInstance),
                   currentMeal.getCharacterID(), position);
           messages.add(message);
           messages.add(sandwormSpawn);
        }
        return messages;
    }

    /**
     * Called to start moving of sandworm after path has been calculated.
     *
     * @param closedList List with sandworm path
     * @param currentField field of target
     * @param gameInstance logic handling component
     * @param version version of standard document, string
     * @return list of messages
     */
    private LinkedList<Message> targetFoundStartingMoving(List<Field> closedList, Field currentField, GameInstance gameInstance, String version) {

        closedList.add(currentField);
        LinkedList<Field> parents = new LinkedList<>();
        parents.add(currentMeal.getField());
        Field parent = currentMeal.getField().getParentNode();
        parents.addFirst(parent);
        while (parent != getField()) {
            parent = parent.getParentNode();
            parents.addFirst(parent);
        }
        parents.removeFirst(); // remove sandworm field

        // copy path
        List<Field> pathToMove = new LinkedList<>();
        for (Field field : parents) {
            pathToMove.add(field);
        }

        // now move with sandworm speed = number of fields during one move and return whether target has been eaten
        boolean hasEaten = move(parents);
        LinkedList<Message> messages = new LinkedList<>();
        if (hasEaten) {
            swallowAndDespawn(messages, gameInstance, version);
        }
        return moveAndCheckForUnits(messages, pathToMove, version, gameInstance);
    }

    /**
     * Final step in a* algorithm: Creates movement messages and swallows units in the path of the sandworm.
     *
     * @param messages     list of {@link Message} that shall be broadcast
     * @param pathToMove   list of {@link Field} sandworm path
     * @param version      String, current version of standard document
     * @param gameInstance {@link GameInstance}
     * @return list of {@link Message}
     */
    private LinkedList<Message> moveAndCheckForUnits(LinkedList<Message> messages, List<Field> pathToMove, String version,
                                               GameInstance gameInstance) {

        Point[] pathInPoints = new Point[sandWormSpeed];
        int numberOfFieldsToMove = getSmallerNumber(pathToMove);

        // swallow all other characters that are on fields that the sandworm passes

        for (int i = 0; i < numberOfFieldsToMove; i++) {

            Field field = (pathToMove.get(i));
            pathInPoints[i] = new Point(field.getXCoordinate(), field.getYCoordinate());
            List<Message> swallowMessages = checkForCharactersInSandwormWay(field, gameInstance, version);
            messages.addAll(swallowMessages);

        }

        SandwormMovementMessage sandwormMove = new SandwormMovementMessage(version,
                pathInPoints);
        messages.addFirst(sandwormMove);
        MapChangeMessage mapChange = createMapChangeMessage(gameInstance, version);
        messages.add(mapChange);
        return messages;
    }

    /**
     * Called to get the smaller number of sandworm path and sandworm speed.
     *
     * @param pathToMove list of {@link Field}, path of sandworm
     * @return integer, number of fields that the sandworm will move
     */
    private int getSmallerNumber(List<Field> pathToMove) {

        if (pathToMove.size() < sandWormSpeed) {
            return pathToMove.size();
        } else return sandWormSpeed;
    }

    /**
     * Called to swallow currentMeal and to let sandworm disappear. Creates required messages.
     *
     * @param messages     list of {@link Message} to be returned
     * @param gameInstance {@link GameInstance}
     * @param version      String, current version of standard document
     * @return true
     */
    private boolean swallowAndDespawn(LinkedList<Message> messages, GameInstance gameInstance, String version) {

        LinkedList<Message> swallowMessages = swallowCharacter(currentMeal, gameInstance, version);
        for (Message message : swallowMessages) {
            messages.add(message);
        }

        // target is swallowed: send map change and character stats change messages
        SandwormDespawnMessage despawnMessage = removeSandworm(version);
        messages.addLast(despawnMessage);
        targetFound = false;
        return true;
    }

    /**
     * Called to create a map change message.
     *
     * @param gameInstance {@link GameInstance}
     * @param version      String, current version of standard document
     * @return {@link MapChangeMessage}
     */
    private MapChangeMessage createMapChangeMessage(GameInstance gameInstance, String version) {

        Point stormEye = new Point(gameInstance.getSandstorm().getField().getXCoordinate(),
                gameInstance.getSandstorm().getField().getYCoordinate());
        Tile[][] newMap = gameInstance.getGameMap().createNewTileMap(gameInstance);

        return new MapChangeMessage(version, ChangeReason.ROUND_PHASE, newMap,
                stormEye);
    }


    /**
     * Called to get field from open list with lowest cost.
     *
     * @param openList   list of fields that must be looked at
     * @param lowestCost current lowest cost
     * @return positive integer, index in open list that points to the field with the lowest cost
     * value
     * @author Janine Grimmer
     */
    private int findMinCostField(List<Field> openList, int lowestCost) {
        int indexWithLowestCost = -1;
        for (Field field : openList) {
            //retrieve field with minimal cost
            if (field.getMoveCost() < lowestCost) {
                // replace cost value
                lowestCost = field.getMoveCost();
                // save index of this field
                indexWithLowestCost = openList.indexOf(field);
            }
        }
        return indexWithLowestCost;
    }


    /**
     * Method in A* search to add 8 neighbours of current field to list.
     *
     * @param currentField field that is currently looked at in order to find the shortest sandworm
     *                     path
     * @param closedList   list with fields that have already been checked
     * @param openList     list of fields that have not yet been checked
     * @param gameInstance {@link GameInstance}
     * @author Janine Grimmer
     */
    private void expand(Field currentField, List<Field> closedList, List<Field> openList,
                        GameInstance gameInstance) {

        for (int i = currentField.getXCoordinate() - 1; i < currentField.getXCoordinate() + 2; i++) {
            for (int j = currentField.getYCoordinate() - 1; j < currentField.getYCoordinate() + 2; j++) {

                // add to list if field is valid, desert field and not already added to pathToMove list
                if (isValidField(i, j) && (!(i == currentField.getXCoordinate() && j == currentField.getYCoordinate()))) {
                     Field field = gameInstance.getGameMap().getField(i, j );
                   // add to list if field is valid, desert field and not already added to pathToMove list
                    if (isAccessible(field) && !closedList.contains(field)) {
                        updateOrSetField(field, gameInstance, i, j, openList, currentField);
                    }

                }

            }

        }

    }

    /**
     * Called when search for path is expanded. Calculates cost of field and either adds it to possible path list or
     * updates it.
     *
     * @param field        {@link Field} expanded field from currentField
     * @param gameInstance {@link GameInstance}
     * @param i            x coordinate of field
     * @param j            y coordinate of field
     * @param openList     list of fields that have not yet been checked
     * @param currentField field that is currently looked at in order to find the shortest sandworm
     *                     path
     */
    private void updateOrSetField(Field field, GameInstance gameInstance, int i, int j, List<Field> openList,
                                  Field currentField) {

        // calculate cost of neighbouring field  = f and set field to possiblePath or update it there
        int cost = field.getMoveCost() + 1 + calculateDistance(currentMeal.getField());
        if (openList.contains(field)) {

            if (field.getMoveCost() > cost) {
                // if previously calculated cost is higher replace it with new lower cost and update parent
                gameInstance.getGameMap().getField(i, j).setMoveCost(cost);
                gameInstance.getGameMap().getField(i, j).setParentNode(currentField);
                openList.add(gameInstance.getGameMap().getField(i, j));
            }

        } else { // not in list: set cost and parent
            gameInstance.getGameMap().getField(i, j).setMoveCost(cost);
            gameInstance.getGameMap().getField(i, j).setParentNode(currentField);
            openList.add(gameInstance.getGameMap().getField(i, j));

        }
    }


    /**
     * Called ever round if sandworm exists to move sandworm n fields towards target on shortest path
     * (sandworm can only move across desert fields).
     *
     * @param version String with current version of standard document
     * @return list of {@link Message} containing sandworm spawn, despawn, movement, character stat change
     * or map change messages
     * @author Janine Grimmer
     */
    public LinkedList<Message> moveSandworm(String version, GameInstance gameInstance) {
        return aStarAlgorithm(version, gameInstance);
    }


    /**
     * Called to check whether sandwom has crossed a field with a character on it. If true, character
     * is swallowed.
     *
     * @param field        current position of sandworm
     * @param gameInstance {@link GameInstance}
     * @param version      String with current version of standard document
     * @return list of {@link Message}
     * @author Janine Grimmer
     */
    private LinkedList<Message> checkForCharactersInSandwormWay(Field field, GameInstance gameInstance, String version) {
        LinkedList<Message> messages = new LinkedList<>();
        int i = 0;
        for (Unit character : gameInstance.getGameUnits()) {
            if (field == gameInstance.getGameUnits().get(i).getField()) {
                LinkedList<Message> swallowMessages = swallowCharacter(character, gameInstance, version);
                messages.addAll(swallowMessages);
            }
            i++;
        }
        return messages;
    }


    /**
     * Called to calculate heuristic distance between sandworm's and target's position
     *
     * @param targetField {@Link Field} sandworm's destination: field of target
     * @return integer, maximum distance between this field and the target's field
     * @author Janine Grimmer
     */
    private int calculateDistance(Field targetField) {
        /* Chebyshev distance d=max(|xd​|,|yd​|)
        xd​=xgoal​−xcellposition​
        yd=ygoal−ycellposition
        source: https://learn-udacity.top/udrb552921/Robotics%20Software%20Engineer%20v5.0.0/Part%2007-Module%2001-Lesson%2003_Lab%20Path%20Planning/06.%20A%20Shortest%20Path.html
         */
        int xDistance = Math.abs(targetField.getXCoordinate() - this.getField().getXCoordinate());
        int yDistance = Math.abs(targetField.getYCoordinate() - this.getField().getYCoordinate());
        return Math.max(xDistance, yDistance);
    }


    /**
     * Called to check whether sandworm can move across a field.
     *
     * @param field {@Link Field} possibel next field in path
     * @return true if desert field
     * @author Janine Grimmer
     */
    private boolean isAccessible(Field field) {

        return (field.getFieldType() == FLAT_SAND || field.getFieldType() == DUNE);
    }


    /**
     * Method to find ID of client where character belongs to.
     *
     * @param unit         Character who's client is searched for
     * @param gameInstance {@link GameInstance}
     * @return positive integer, ID of client or -1 if players are not empty
     * @author Janine Grimmer
     */
    public int getClientID(Unit unit, GameInstance gameInstance) {

        if (gameInstance.arePlayersPresent()) {
            if (unit.getPlayer() == PlayerEnum.PLAYER_ONE) {
                return gameInstance.getPlayerOne().get().getClientID();
            } else if (unit.getPlayer() == PlayerEnum.PLAYER_TWO) {
                return gameInstance.getPlayerTwo().get().getClientID();
            }
        }
        return -1;
    }

    /**
     * Method to swallow character. Increases swallowed characters counter.
     *
     * @param character    {@link Unit} character to be swallowed by sandworm
     * @param gameInstance {@link GameInstance}
     * @param version      String with current version of standard document
     * @return list of {@link Message}: CharacterStatChangeMessage and MapChangeMessage
     * @author Janine Grimmer
     */
    public LinkedList<Message> swallowCharacter(Unit character, GameInstance gameInstance, String version) {

        eat(character);
        for (Unit unit: gameInstance.getGameUnits()) {
            if(unit == character){
                gameInstance.getGameUnits().remove(character);
                break;
            }

        }
        LinkedList<Message> messages = new LinkedList<>();
        int id = getClientID(character, gameInstance);
        if (gameInstance.arePlayersPresent()) {
            if (id == gameInstance.getPlayerOne().get().getClientID()) {
                gameInstance.getPlayerOne().get().increaseSwallowedCharacters();
            } else {
                gameInstance.getPlayerTwo().get().increaseSwallowedCharacters();
            }
        } else {
           LOGGER.log(Level.SEVERE, "Players are not present.");
        }

        if (character.getCurrentSpice() > 0) {
            LOGGER.log(INFO, "Current inventory size of swallowed unit is {0}", character.getCurrentSpice());
            blowSpiceAfterDefeat(character.getField().getXCoordinate(),
                    character.getField().getYCoordinate(), character.getCurrentSpice());
            character.setCurrentSpice(0);
            MapChangeMessage mapChangeMessage = createMapChangeMessage(gameInstance, version);
            messages.add(mapChangeMessage);
        }

        UnitStatChange targetChange = new UnitStatChange((int) character.getHealthPoints(),
                character.getActionPoints(),
                character.getMovementPoints(), character.getCurrentSpice(), character.isLoud(),
                character.isSwallowed());

        CharacterStatChangeMessage characterChange = new CharacterStatChangeMessage(version,
                getClientID(character, gameInstance), character.getCharacterID(), targetChange);
        messages.add(characterChange);


        return messages;
    }


    /**
     * Called to remove normal sandworm (e.g. before Shai-Hulud is introduced)
     *
     * @param version String with current version of standard document
     * @return {@link SandwormDespawnMessage}
     * @author Janine Grimmer
     */
    public SandwormDespawnMessage removeSandworm(String version) {

        if (isVisible()) {
            SandwormDespawnMessage sandwormDespawn = new SandwormDespawnMessage(
                    version);

            setVisible(false);
            getField().removeGameEntity(getEntityType());
            return sandwormDespawn;
        }
        else {
            return null;}
    }

    /**
     * Method for Shai Hulud to select randomly a character to swallow
     *
     * @param characters list of all characters
     * @return {@Link Unit} character to be swallowed now
     * @author Janine Grimmer
     */
    public Unit selectCharacter(List<Unit> characters) {

        int randomNumber = MathUtil.random.nextInt(characters.size());
        Unit character = characters.get(randomNumber);
        if (character.isDefeated() || character.isSwallowed()) {
            // keep on searching for a character that stands on map
            selectCharacter(characters);
        }
        return character;
    }

    /**
     * Method of Shai Hulud to attack and swallow selected character.
     *
     * @param character    {@Link Unit} character that is going to be swallowed by Shai-Hulud
     * @param gameInstance {@link GameInstance}
     * @param version      String with current version of standard document
     * @author Janine Grimmer
     */
    public List<Message> attackAndSwallow(Unit character, GameInstance gameInstance, String version) {
        List<Message> messages = new LinkedList<>();
        // Show Shai Hulud on field
        Point position = new Point(character.getField().getXCoordinate(),
                character.getField().getYCoordinate());
        SandwormSpawnMessage shaiHuludSpawn = new SandwormSpawnMessage(version,
                getClientID(character, gameInstance),
                character.getCharacterID(), position);
        messages.add(shaiHuludSpawn);
        // change character's state to isSwallowed
        List<Message> swallowMessages = swallowCharacter(character, gameInstance, version);
        for (Message message : swallowMessages) {
            messages.add(message);
        }
        // create map change and character stats change messages
        Point stormEye = new Point(gameInstance.getSandstorm().getField().getXCoordinate(),
                gameInstance.getSandstorm().getField().getYCoordinate());
        Tile[][] newMap = gameInstance.getGameMap().createNewTileMap(gameInstance);
        MapChangeMessage mapChangeMessage = new MapChangeMessage(version, ChangeReason.ROUND_PHASE, newMap, stormEye);

        messages.add(mapChangeMessage);

        return messages;

    }


    /**
     * Method to check termination: shai hulud phase ends when no character is left on the game map
     *
     * @param characters list that contains all characters of the game
     * @return boolean, true if all characters on the map are swallowed, false if at least one
     * character is standing on the map
     * @author Janine Grimmer
     */
    public boolean checkShaiHuludTermination(List<Unit> characters) {
        List<Unit> alive = new LinkedList<>();
        for (Unit unit : characters) {
            // if character is not swallowed by Shai-Hulud and stands on map, add it to the list
            if (!unit.isSwallowed() && !unit.isDefeated()) {
                alive.add(unit);
            }
        }

        if (alive.size() == 1) {
            // set last character standing if list contains only one character
            setLastCharacterStanding(alive.get(0));
        }
        return alive.isEmpty();
    }

    /**
     * Called after each "normal" sandworm phase to figure out if game is over.
     *
     * @param gameInstance {@link GameInstance}
     * @return boolean, true if game is over
     * @author Janine Grimmer
     */
    public boolean checkGameTermination(GameInstance gameInstance) {
        List<Unit> greatHouse0 = new LinkedList<>();
        List<Unit> greatHouse1 = new LinkedList<>();
        if (gameInstance.arePlayersPresent()) {

            for (Unit character : gameInstance.getGameUnits()) {
                if (!character.isSwallowed()) {

                    if (character.getAffiliation() == gameInstance.getPlayerOne().get().getGreatHouse()) {
                        greatHouse0.add(character);
                    } else {
                        greatHouse1.add(character);
                    }
                }
            }

            // if one list is empty, game is over
            if (greatHouse0.isEmpty()) {
                Player tmpPlayer = gameInstance.getPlayerOne().get();
                loser = tmpPlayer.getClientID();
                return true;
            } else if (greatHouse1.isEmpty()) {
                loser = gameInstance.getPlayerTwo().get().getClientID();
                return true;
            } else {
                return false;
            }
        }
       LOGGER.log(Level.SEVERE, "Players are empty.");
        return false;
    }

    /**
     * Called to get ID of loosing player.
     *
     * @return integer, ID of loser
     */
    public int getLoser() {
        return loser;
    }

    /**
     * Method to set ID of loser.
     *
     * @param loser integer >= 0
     */
    public void setLoser(int loser) {
        this.loser = loser;
    }

    // getter and setter
    public int getSandWormSpawnDistance() {
        return sandWormSpawnDistance;
    }

    public int getSandWormSpeed() {
        return sandWormSpeed;
    }

    public Unit getCurrentMeal() {
        return currentMeal;
    }

    public void setCurrentMeal(Unit currentMeal) {
        this.currentMeal = currentMeal;
    }

    public boolean getIsShaiHulud() {
        return isShaiHulud;
    }

    public void setIsShaiHulud(boolean shaiHulud) {
        isShaiHulud = shaiHulud;
    }

    public Sandworm getSandworm() {
        return this;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public boolean isTargetFound() {
        return targetFound;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }



}

