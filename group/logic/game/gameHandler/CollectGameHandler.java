package logic.game.gameHandler;

import enums.ChangeReason;
import enums.GameEntityType;
import logic.game.GameInstance;
import logic.game.entity.unit.Unit;
import logic.game.map.Field;
import messages.Message;
import messages.gameplay.incoming.ActionRequest;
import messages.gameplay.outgoing.ActionMessage;
import messages.gameplay.outgoing.MapChangeMessage;
import messages.gameplay.outgoing.StrikeMessage;
import messages.util.Point;
import network.util.Client;
import java.util.List;

public class CollectGameHandler extends GameHandler<ActionRequest> {

  @Override
  public List<Message> handleGameRequest(GameInstance gameInstance, String version, Client client,
      ActionRequest gameRequest) {
    clearMessages();
    int x = gameRequest.specs.target.getX();
    int y = gameRequest.specs.target.getY();
    //check for right char
    Message message;
    if ((message = checkCurrentlyActive(gameInstance,version,client,gameRequest)) != null){
      messages.add(message);
      return messages;
    }
    Field field = gameInstance.getGameMap().getField(x, y);
    Unit unit = gameInstance.getCurrentlyActingUnit();
    //is in sandstorm?
    if ((message = checkIsInSandstorm(version, client, gameRequest, unit))
        != null) {
      messages.add(message);
      return messages;
    }
    //check for right target
    if ((message = checkForRightTarget(version,client,gameRequest,field,GameEntityType.SPICE)) != null){
      messages.add(message);
      return messages;
    }
    //check if standing on field
    if (!unit.getField().equals(field)) {
      client.increaseStrikeCounter();
      messages.add(new StrikeMessage(version, gameRequest.clientID,
          "Not in range!",
          (client.getStrikeCounter())));
      return messages;
    }
    //check for enough action points
    if (unit.getActionPoints() < 1){
        client.increaseStrikeCounter();
        messages.add(new StrikeMessage(version, gameRequest.clientID,
                "Not enough AP!",
                (client.getStrikeCounter())));
        return messages;
    }
    //check for enough space, if true -- then pickup
    if (!unit.pickupSpice(field)) {
      client.increaseStrikeCounter();
      messages.add(new StrikeMessage(version, gameRequest.clientID,
          "Not enough space!",
          (client.getStrikeCounter())));
      return messages;
    }
    //generate messages
    messages.add(new ActionMessage(version, gameRequest.clientID, gameRequest.characterID,
        gameRequest.action, gameRequest.specs));
    Point point = new Point(gameInstance.getSandstorm().getField().getXCoordinate(),
        gameInstance.getSandstorm().getField().getYCoordinate());
    messages.add(new MapChangeMessage(version, ChangeReason.SPICE_PICKUP,
        gameInstance.getGameMap().generateTileFromFieldMap(gameInstance), point));
    messages.add(
        getCharacterStatChangeMessage(version, gameRequest.clientID, unit.getCharacterID(),
            unit));
    return messages;
  }
}
