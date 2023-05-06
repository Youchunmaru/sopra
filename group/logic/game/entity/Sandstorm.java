package logic.game.entity;

import enums.FieldType;
import enums.GameEntityType;
import logic.game.GameInstance;
import logic.game.entity.unit.Unit;
import logic.game.map.Field;
import logic.game.map.GameMap;
import logic.util.MathUtil;

import java.util.logging.Logger;

import static enums.FieldType.DUNE;

/**
 * Class for the sandstorm. Moves sandstorm each round one field further. A type of
 * {@link GameEntity}.
 *
 * @author Samuel Gröner, Janine Grimmer
 */
public class Sandstorm extends GameEntity {

    private Logger logger = Logger.getLogger(Sandstorm.class.getName());

    public Sandstorm(GameInstance gameInstance) {
        super(GameEntityType.SANDSTORM, gameInstance);
        selectEyeOfStorm(gameInstance.getGameMap());
    }

    /**
     * Called in first round only to select random field for eye of storm
     *
     * @param gameField the map of the game
     * @return {@link Field} field that is the eye of the storm
     */
    public Field selectEyeOfStorm(GameMap gameField) {
        int x = MathUtil.random.nextInt(gameField.getXSize());
        int y = MathUtil.random.nextInt(gameField.getYSize());
        setField(gameField.getField(x, y));
        return getField();
    }

    /**
     * Called each round to move sandstorm one random field further.
     *
     * @param field eye of sandstorm
     * @return if move has occurred successfully
     */
    @Override
    public boolean move(Field field) {
        GameMap gameMap = getGameInstance().getGameMap();
        boolean moved;
        do {
            // random move to left, right, top, bottom or one of the four corners
            int direction = MathUtil.random.nextInt(8);
            moved = moveSandstorm(gameMap, direction);
        } while (!moved);
        changeFieldHeight(gameMap);
        checkIfIsInSandstorm();
        return true;
    }

    /**
     * Called each round to move sandstorm one random field further.
     *
     * @param map       the game map
     * @param direction the direction to move
     * @return if move was successful
     */
    private boolean moveSandstorm(GameMap map, int direction) {

        int x = getField().getXCoordinate();
        int y = getField().getYCoordinate();
        /* moveDirection
         * 0: left upper corner
         * 1: top
         * 2: right upper corner
         * 3: left
         * 4: right
         * 5: left lower corner
         * 6: bottom
         * 7: right lower corner
         * */
        switch (direction) { // intentionally left out break or return statements in order to allow execution of following statements if field does not match the checks
            case 0:
                if (super.move(map.getField(x - 1, y - 1))) {
                    return true;
                }
            case 1:
                if (super.move(map.getField(x, y - 1))) {
                    return true;
                }
            case 2:
                if (super.move(map.getField(x + 1, y - 1))) {
                    return true;
                }
            case 3:
                if (super.move(map.getField(x - 1, y))) {
                    return true;
                }
            case 4:
                if (super.move(map.getField(x + 1, y))) {
                    return true;
                }
            case 5:
                if (super.move(map.getField(x - 1, y + 1))) {
                    return true;
                }
            case 6:
                if (super.move(map.getField(x, y + 1))) {
                    return true;
                }
            case 7:
                if (super.move(map.getField(x + 1, y + 1))) {
                    return true;
                }
            default:
                return false;
        }
    }

    /**
     * Called to change the height of the fields in the sandstorm.
     *
     * @param map current game map
     */
    private void changeFieldHeight(GameMap map) {
        Field field = getField();
        int x = field.getXCoordinate();
        int y = field.getYCoordinate();
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                Field fieldIter = map.getField(i, j);
                if (fieldIter != null &&
                        (fieldIter.getFieldType() == FieldType.FLAT_SAND || fieldIter.getFieldType() == DUNE)) {
                    int height = MathUtil.random.nextInt(2);
                    if (height == 1) {
                        fieldIter.setFieldType(DUNE);
                    } else {
                        fieldIter.setFieldType(FieldType.FLAT_SAND);
                    }
                }
            }
        }
    }

    /**
     * Used to check whether characters stand in the sandstorm square. If true they are marked.
     */
    private void checkIfIsInSandstorm() {

        for (Unit character : getGameInstance().getGameUnits()) {
            character.setInSandstorm(false);
            for (int i = getField().getXCoordinate() - 1; i <= getField().getXCoordinate() + 1;
                 i++) {
                for (int j = getField().getYCoordinate() - 1; j <= getField().getYCoordinate() + 1;
                     j++) {
                    if (character.getField() == (getGameInstance().getGameMap().getField(i, j))
                            && character.getField() != null) {
                        character.setInSandstorm(true);
                    }
                }
            }
        }
    }

}
