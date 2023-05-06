package logic.game.gameHandler;

import enums.ChangeReason;
import logic.game.GameInstance;
import logic.game.entity.unit.Unit;
import logic.game.map.Field;
import messages.Message;
import messages.gameplay.incoming.EndTurnRequest;
import messages.gameplay.outgoing.MapChangeMessage;
import messages.gameplay.outgoing.StrikeMessage;
import messages.util.Point;
import messages.util.Tile;
import network.util.Client;
import java.util.List;

public class EndTurnGameHandler extends GameHandler<EndTurnRequest>{
    @Override
    public List<Message> handleGameRequest(GameInstance gameInstance, String version, Client client, EndTurnRequest gameRequest) {
      clearMessages();
      //check for right char
      if (!gameInstance.isCurrentlyActingUnit(gameRequest.characterID)){
        client.increaseStrikeCounter();
        messages.add(new StrikeMessage(version, gameRequest.clientID,
            "Currently not turn of unit: " + gameRequest.characterID,
            (client.getStrikeCounter())));
        return messages;
      }
      Unit unit = gameInstance.getCurrentlyActingUnit();
      //Check if Spice Transfer to City automatically ensures
      Field endPoint = unit.getField();
      Field cityPoint = unit.getCity().getField();
      if(gameInstance.getGameMap().isFieldNeighbouring(endPoint,cityPoint)&&unit.getCurrentSpice()!=0){
          messages.add(unit.deliverSpiceToCity(version, client.getClientID()));
      }
      unit.onTurnEnd();

      messages.add(getCharacterStatChangeMessage(version,client.getClientID(),
          gameRequest.characterID, unit));
      return messages;
    }
}
