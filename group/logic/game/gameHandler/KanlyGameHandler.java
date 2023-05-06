package logic.game.gameHandler;

import enums.ChangeReason;
import enums.GameEntityType;
import enums.UnitType;
import logic.game.GameInstance;
import logic.game.entity.unit.Unit;
import logic.game.map.Field;
import messages.Message;
import messages.gameplay.incoming.ActionRequest;
import messages.gameplay.outgoing.ActionMessage;
import messages.gameplay.outgoing.MapChangeMessage;
import messages.gameplay.outgoing.StrikeMessage;
import messages.util.Point;
import messages.util.Tile;
import network.util.Client;
import java.util.List;

public class KanlyGameHandler extends GameHandler<ActionRequest> {

  @Override
  public List<Message> handleGameRequest(GameInstance gameInstance, String version, Client client,
      ActionRequest gameRequest) {
    clearMessages();
    int x = gameRequest.specs.target.getX();
    int y = gameRequest.specs.target.getY();
    //check for right char
    Message message;
    if ((message = checkCurrentlyActive(gameInstance, version, client, gameRequest)) != null) {
      messages.add(message);
      return messages;
    }
    Field field = gameInstance.getGameMap().getField(x, y);
    Unit attacker = gameInstance.getCurrentlyActingUnit();
    //if in sandstorm
    if ((message = checkIsInSandstorm(version, client, gameRequest, attacker))
        != null) {
      messages.add(message);
      return messages;
    }
    //check for right target
    if ((message = checkForRightTarget(version, client, gameRequest, field, GameEntityType.UNIT))
        != null) {
      messages.add(message);
      return messages;
    }
    Unit victim = (Unit) field.getGameEntity(GameEntityType.UNIT);
    //check for right affiliation
    if ((message = checkForRightAffiliation(version, client, gameRequest, victim)) != null) {
      messages.add(message);
      return messages;
    }
    //right type of target
    if (!attacker.getUnitType().equals(UnitType.NOBLE) || !victim.getUnitType()
        .equals(UnitType.NOBLE)) {
      client.increaseStrikeCounter();
      messages.add(new StrikeMessage(version, gameRequest.clientID,
          "Both have to be a noble!",
          (client.getStrikeCounter())));
      return messages;
    }
    //check if in range
    if ((message = checkIfInRange(version, client, gameRequest, attacker, victim)) != null) {
      messages.add(message);
      return messages;
    }
    //check for enough action points
    if (attacker.getActionPoints() != attacker.getUnitConfig().getMaxAP()) {
      client.increaseStrikeCounter();
      messages.add(new StrikeMessage(version, gameRequest.clientID, "Not enough action points",
          client.getStrikeCounter()));
      return messages;
    }
    boolean hit = attacker.doSpecialAction(victim);
    if(victim.isDefeated() && victim.getCurrentSpice() > 0){
      changeMapAfterDefeat(victim, gameInstance, version,client);

    }
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
