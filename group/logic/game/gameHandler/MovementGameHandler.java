package logic.game.gameHandler;

import enums.GameEntityType;
import logic.game.GameInstance;
import logic.game.entity.unit.Unit;
import logic.game.map.Field;
import messages.Message;
import messages.gameplay.incoming.MovementRequest;
import messages.gameplay.outgoing.MovementMessage;
import messages.gameplay.outgoing.StrikeMessage;
import messages.util.PathConfig;
import messages.util.Point;
import network.util.Client;
import java.util.List;

/**
 * On successful Movement sends back one MovementDemand with the walked path plus one
 * CharacterStateChangeDemand
 */
public class MovementGameHandler extends GameHandler<MovementRequest> {

    public List<Message> handleGameRequest(GameInstance gameInstance, String version, Client client,
                                           MovementRequest gameRequest) {
        clearMessages();
        //Checks if requested unit is the currently acting unit
        if (!gameInstance.isCurrentlyActingUnit(gameRequest.characterID)) {
            client.increaseStrikeCounter();
            messages.add(new StrikeMessage(version, client.getClientID(),
                    "Currently not turn of unit: " + gameRequest.characterID, client.getStrikeCounter()));
            return messages;
        }
        Unit unit = gameInstance.getCurrentlyActingUnit();
        //Checks if requested unit has enough movement points
        if (unit.getMovementPoints() < gameRequest.specs.path.length) {
            client.increaseStrikeCounter();
            messages.add(new StrikeMessage(version, client.getClientID(), "Not enough movement points",
                    client.getStrikeCounter()));
        }

        //Checks if the whole path is traversable
        for (Point point : gameRequest.specs.path) {
            Field field = gameInstance.getGameMap().getField(point.getX(), point.getY());
            //&& !field.getGameEntities().containsKey(GameEntityType.UNIT)
            if (field != null  && !field.isAccessible()) {
                client.increaseStrikeCounter();
                messages.add(new StrikeMessage(version, client.getClientID(), "Inaccessible field on path!",
                        client.getStrikeCounter()));
                return messages;
            }
        }

        //Actually moves the unit
        unit.moveUnit(gameRequest.specs.path);
        GameInstance.LOGGER.info("MovementGameHandler(handleGameRequest): trying to move the unit with path[0]: " + gameRequest.specs.path[0]);
         messages.add(new MovementMessage(version, client.getClientID(), unit.getCharacterID(),
                new PathConfig(gameRequest.specs.path)));
            for (Unit pushedUnit : unit.getPushedUnits()) {
                Field field = pushedUnit.getField();
                Point[] switchPoint = new Point[]{
                        new Point(field.getXCoordinate(), field.getYCoordinate())};
                messages.add(new MovementMessage(version, client.getClientID(), pushedUnit.getCharacterID(),
                        new PathConfig(switchPoint)));
            }

         // check if unit has moved into or out of sandstorm
         if( gameInstance.getGameMap().checkIfFieldIsInSandstorm( unit.getField(), gameInstance)){
             unit.setInSandstorm(true);
         } else {
             unit.setInSandstorm(false);
         }

        messages.add(
                getCharacterStatChangeMessage(version, gameRequest.clientID, unit.getCharacterID(),
                        unit));
        return messages;
    }
}
