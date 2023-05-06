package logic.game.gameHandler;

import enums.ChangeReason;
import enums.UnitType;
import logic.game.GameInstance;
import logic.game.entity.unit.Unit;
import messages.Message;
import messages.gameplay.incoming.ActionRequest;
import messages.gameplay.outgoing.ActionMessage;
import messages.gameplay.outgoing.MapChangeMessage;
import messages.gameplay.outgoing.StrikeMessage;
import messages.util.Point;
import network.util.Client;
import java.util.List;

public class SpiceHoardingGameHandler extends GameHandler<ActionRequest> {

    @Override
    public List<Message> handleGameRequest(GameInstance gameInstance, String version, Client client, ActionRequest gameRequest) {
      clearMessages();
      //check for right char
      Message message;
      if ((message = checkCurrentlyActive(gameInstance,version,client,gameRequest)) != null){
        messages.add(message);
        return messages;
      }
      Unit unit = gameInstance.getCurrentlyActingUnit();
      if ((message = checkIsInSandstorm(version, client, gameRequest, unit))
          != null) {
        messages.add(message);
        return messages;
      }
      if (!unit.getUnitType().equals(UnitType.MENTAT)){
        client.increaseStrikeCounter();
        messages.add(new StrikeMessage(version, gameRequest.clientID,
            "Wrong unit type!",
            (client.getStrikeCounter())));
        return messages;
      }
      if (unit.getActionPoints() != unit.getUnitConfig().getMaxAP()){
        client.increaseStrikeCounter();
        messages.add(new StrikeMessage(version, gameRequest.clientID,
            "Not enough action points!",
            (client.getStrikeCounter())));
        return messages;
      }
      boolean pickup = unit.doSpecialAction(null);
      messages.add(new ActionMessage(version, gameRequest.clientID, gameRequest.characterID,
          gameRequest.action, gameRequest.specs));
      if(pickup) {
        Point point = new Point(gameInstance.getSandstorm().getField().getXCoordinate(),
            gameInstance.getSandstorm().getField().getYCoordinate());
        messages.add(new MapChangeMessage(version, ChangeReason.SPICE_PICKUP,
            gameInstance.getGameMap().generateTileFromFieldMap(gameInstance), point));
      }
      messages.add(
          getCharacterStatChangeMessage(version, gameRequest.clientID, unit.getCharacterID(),
              unit));
      return messages;
    }

}
