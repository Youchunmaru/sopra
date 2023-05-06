package logic.game.gameHandler;

import enums.GameEntityType;
import enums.UnitType;
import logic.game.GameInstance;
import logic.game.entity.unit.Unit;
import logic.game.map.Field;
import messages.Message;
import messages.gameplay.incoming.ActionRequest;
import messages.gameplay.outgoing.ActionMessage;
import messages.gameplay.outgoing.StrikeMessage;
import network.util.Client;
import java.util.List;

public class VoiceGameHandler extends GameHandler<ActionRequest> {

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
    Unit attacker = gameInstance.getCurrentlyActingUnit();
    if ((message = checkIsInSandstorm(version, client, gameRequest, attacker))
        != null) {
      messages.add(message);
      return messages;
    }
    //check for right target
    if ((message = checkForRightTarget(version,client,gameRequest,field,GameEntityType.UNIT)) != null){
      messages.add(message);
      return messages;
    }
    Unit victim = (Unit) field.getGameEntity(GameEntityType.UNIT);
    if (!attacker.getUnitType().equals(UnitType.BENE_GESSERIT)) {
      client.increaseStrikeCounter();
      messages.add(new StrikeMessage(version, gameRequest.clientID,
          "Wrong unit type!",
          (client.getStrikeCounter())));
      return messages;
    }
    //check if in range
    if ((message = checkIfInRange(version,client,gameRequest,attacker,victim)) != null){
      messages.add(message);
      return messages;
    }
    boolean hit = attacker.doSpecialAction(victim);
    messages.add(new ActionMessage(version, gameRequest.clientID, gameRequest.characterID,
        gameRequest.action, gameRequest.specs));
    messages.add(
        getCharacterStatChangeMessage(version, gameRequest.clientID, attacker.getCharacterID(),
            attacker));
    if (hit) {
      messages.add(
          getCharacterStatChangeMessage(version, gameRequest.clientID, victim.getCharacterID(),
              victim));
    }
    return messages;
  }
}
