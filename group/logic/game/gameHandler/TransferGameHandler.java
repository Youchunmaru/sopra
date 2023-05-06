package logic.game.gameHandler;

import logic.game.GameInstance;
import logic.game.entity.unit.Unit;
import messages.Message;
import messages.gameplay.incoming.TransferRequest;
import messages.gameplay.outgoing.CharacterStatChangeMessage;
import messages.gameplay.outgoing.StrikeMessage;
import messages.gameplay.outgoing.TransferMessage;
import messages.util.UnitStatChange;
import network.util.Client;
import java.util.List;

public class TransferGameHandler extends GameHandler<TransferRequest>{
    @Override
    public List<Message> handleGameRequest(GameInstance gameInstance, String version, Client client, TransferRequest gameRequest) {
      clearMessages();
      GameInstance.LOGGER.info("TransferGameHandler(handleGameRequest): Request arrived and is of Type: " + gameRequest.getType());
      //check for right char
      if (!gameInstance.isCurrentlyActingUnit(gameRequest.characterID)){
        client.increaseStrikeCounter();
        messages.add(new StrikeMessage(version, gameRequest.clientID,
            "Currently not turn of unit: " + gameRequest.characterID,
            (client.getStrikeCounter())));
        return messages;
      }
      Unit deliverer = gameInstance.getCurrentlyActingUnit();
      if (deliverer.isInSandstorm()){
        client.increaseStrikeCounter();
        messages.add(new StrikeMessage(version, gameRequest.clientID,
            "Currently not able to move!",
            (client.getStrikeCounter())));
      }
      Unit receiver = gameInstance.getUnitById(gameRequest.targetID);
      if (receiver == null){
        client.increaseStrikeCounter();
        messages.add(new StrikeMessage(version, gameRequest.clientID, "Wrong target id!",client.getStrikeCounter()));
        return messages;
      }
      //check for right affiliation
      if (client.getPlayerEnum() != receiver.getPlayer()){
        client.increaseStrikeCounter();
        messages.add(new StrikeMessage(version, gameRequest.clientID,
            "You cant transfer to your opponent!",
            (client.getStrikeCounter())));
        return messages;
      }
      //check if in range
      if (!((Math.abs(deliverer.getField().getXCoordinate() - receiver.getField().getXCoordinate()) <= 1) && (
          Math.abs(deliverer.getField().getYCoordinate() - receiver.getField().getYCoordinate()) <= 1))){
        client.increaseStrikeCounter();
        messages.add(new StrikeMessage(version, gameRequest.clientID,
            "Target not in range!",
            (client.getStrikeCounter())));
        return messages;
      }
      if (!deliverer.transferSpice(receiver,gameRequest.amount)){
        client.increaseStrikeCounter();
        messages.add(new StrikeMessage(version, gameRequest.clientID,
            "Wrong amount!",
            (client.getStrikeCounter())));
        return messages;
      }
      deliverer.transferSpice(receiver, gameRequest.amount);

      messages.add(new TransferMessage(version, gameRequest.clientID, gameRequest.characterID,
          gameRequest.targetID));
      
      UnitStatChange delivererStats = new UnitStatChange(deliverer.getHealthPoints(), deliverer.getActionPoints(), deliverer.getMovementPoints(), deliverer.getCurrentSpice(), deliverer.isLoud(), deliverer.isSwallowed());
      UnitStatChange receiverStats = new UnitStatChange(receiver.getHealthPoints(), receiver.getActionPoints(), receiver.getMovementPoints(), receiver.getCurrentSpice(), receiver.isLoud(), receiver.isSwallowed());
      
      messages.add(new CharacterStatChangeMessage(version, gameRequest.clientID, deliverer.getCharacterID(), delivererStats));
      messages.add(new CharacterStatChangeMessage(version, gameRequest.clientID, receiver.getCharacterID(), receiverStats));
      return messages;
    }
}
