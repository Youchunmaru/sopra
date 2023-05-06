package logic.game.gameHandler;

import enums.ChangeReason;
import enums.UnitType;
import logic.game.GameInstance;
import logic.game.entity.unit.Fighter;
import logic.game.entity.unit.Unit;
import messages.Message;
import messages.gameplay.incoming.ActionRequest;
import messages.gameplay.outgoing.ActionMessage;
import messages.gameplay.outgoing.MapChangeMessage;
import messages.gameplay.outgoing.StrikeMessage;
import messages.util.Point;
import messages.util.Tile;
import network.util.Client;
import java.util.List;

public class SwordSpinGameHandler extends GameHandler<ActionRequest>{
    @Override
    public List<Message> handleGameRequest(GameInstance gameInstance, String version, Client client, ActionRequest gameRequest) {
      clearMessages();
      //check for right char
      Message message;
      if ((message = checkCurrentlyActive(gameInstance,version,client,gameRequest)) != null){
        messages.add(message);
        return messages;
      }
      Unit attacker = gameInstance.getCurrentlyActingUnit();
      if ((message = checkIsInSandstorm(version, client, gameRequest, attacker))
          != null) {
        messages.add(message);
        return messages;
      }
      if (attacker.getActionPoints() != attacker.getUnitConfig().getMaxAP()){
        client.increaseStrikeCounter();
        messages.add(new StrikeMessage(version, gameRequest.clientID,
            "Not enough action points!",
            (client.getStrikeCounter())));
        return messages;
      }
      if (!attacker.getUnitType().equals(UnitType.FIGHTER)){
        client.increaseStrikeCounter();
        messages.add(new StrikeMessage(version, gameRequest.clientID,
            "Not the right unit type!",
            (client.getStrikeCounter())));
        return messages;
      }
      boolean hit = attacker.doSpecialAction(null);
      messages.add(new ActionMessage(version, gameRequest.clientID, gameRequest.characterID,
          gameRequest.action, gameRequest.specs));
      messages.add(
          getCharacterStatChangeMessage(version, gameRequest.clientID, attacker.getCharacterID(),
              attacker));
      if (hit){
        Fighter fighter = (Fighter) attacker;
        List<Unit> victims = fighter.getVictims();
        for (Unit victim:victims) {
          messages.add(
              getCharacterStatChangeMessage(version, gameRequest.clientID, victim.getCharacterID(),
                  victim));
          if(victim.isDefeated() && victim.getCurrentSpice() > 0){
              changeMapAfterDefeat(victim, gameInstance, version,client);

        }
      }
      }
      return messages;
    }
}
