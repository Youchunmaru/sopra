package logic.game.map;

import logic.game.GameInstance;
import logic.util.MathUtil;


/**
 * Class to do a field search if the current field is not accessible.
 *
 * @author Janine Grimmer
 */
public class FieldSearch {


    /**
     * Called to find new start field for a sandworm or a unit to be spawned.
     *
     * @param field                 {@link Field} field from which the search for another field begins
     * @param gameInstance          {@link GameInstance}
     * @return {@link Field} new field to try to start
     */
    public Field doNewFieldSearch(Field field, GameInstance gameInstance) {
        int neighbour = MathUtil.random.nextInt(8);
        /* 0 1 2
           3 x 4
           5 6 7 */

        if (field == null) {
            return null;
        }
        switch (neighbour) { // intentionally left out break or return statements in order to allow execution of following statements if field does not match the checks

            case 0:
                if (field.getXCoordinate() - 1 > -1 && field.getYCoordinate() - 1 > -1) {
                    field = gameInstance.getGameMap()
                            .getField(field.getXCoordinate() - 1, field.getYCoordinate() - 1);
                    break;
                }
            case 1:
                if (field.getXCoordinate() - 1 > -1) {
                    field = gameInstance.getGameMap()
                            .getField(field.getXCoordinate() - 1, field.getYCoordinate());
                    break;
                }
            case 2:
                if (field.getXCoordinate() - 1 > -1
                        && field.getYCoordinate() + 1 < gameInstance.getGameMap().getYSize()) {
                    field = gameInstance.getGameMap()
                            .getField(field.getXCoordinate() - 1, field.getYCoordinate() + 1);
                    break;
                }

            case 3:
                if (field.getYCoordinate() - 1 > -1) {
                    field = gameInstance.getGameMap()
                            .getField(field.getXCoordinate(), field.getYCoordinate() - 1);
                    break;
                }

            case 4:
                if (field.getYCoordinate() + 1 < gameInstance.getGameMap().getXSize()) {
                    field = gameInstance.getGameMap()
                            .getField(field.getXCoordinate(), field.getYCoordinate() + 1);
                    break;
                }

            case 5:
                if (field.getXCoordinate() + 1 < gameInstance.getGameMap().getXSize()
                        && field.getYCoordinate() - 1 > -1) {
                    field = gameInstance.getGameMap()
                            .getField(field.getXCoordinate() + 1, field.getYCoordinate() - 1);
                    break;
                }

            case 6:
                if (field.getXCoordinate() + 1 < gameInstance.getGameMap().getXSize()) {
                    field = gameInstance.getGameMap()
                            .getField(field.getXCoordinate() + 1, field.getYCoordinate());
                    break;
                }

            case 7:
                if (field.getXCoordinate() + 1 < gameInstance.getGameMap().getXSize()
                        && field.getYCoordinate() + 1 < gameInstance.getGameMap().getYSize()) {
                    field = gameInstance.getGameMap()
                            .getField(field.getXCoordinate() + 1, field.getYCoordinate() + 1);
                    break;
                } else {
                    field = doNewFieldSearch(field, gameInstance);
                }
                break;
            default:
                return null;
        }
        return field;
    }
}
