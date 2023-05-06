package logic.game.gameHandler;

import enums.ActionEnum;
import enums.ChangeReason;
import enums.UnitType;
import logic.game.GameInstance;
import logic.game.entity.Sandworm;
import logic.game.entity.unit.Noble;
import logic.game.entity.unit.Unit;
import logic.game.map.Field;
import messages.Message;
import messages.gameplay.incoming.ActionRequest;
import messages.gameplay.outgoing.ActionMessage;
import messages.gameplay.outgoing.AtomicsMessage;
import messages.gameplay.outgoing.MapChangeMessage;
import messages.gameplay.outgoing.StrikeMessage;
import messages.util.Point;
import messages.util.Tile;
import network.util.Client;

import java.util.List;
import network.util.Player;

public class FamilyAtomicsGameHandler extends GameHandler<ActionRequest>{
  // TODO increase opponentsDefeated in Player after every defeat
    @Override
    public List<Message> handleGameRequest(GameInstance gameInstance, String version, Client client, ActionRequest gameRequest) {
      boolean isshunned = (!gameInstance.getPlayers(client.getPlayerEnum()).get(0).isShunned()&& !gameInstance.getPlayers(client.getPlayerEnum()).get(1).isShunned());
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
      if (!unit.getUnitType().equals(UnitType.NOBLE)){
        client.increaseStrikeCounter();
        messages.add(new StrikeMessage(version, gameRequest.clientID,
            "Wrong unit type!",
            (client.getStrikeCounter())));
        return messages;
      }
      Noble noble = (Noble) unit;
      Player player = gameInstance.getPlayer(client.getPlayerEnum());
      if (player.getAtomicsCount() <= 0){
        client.increaseStrikeCounter();
        messages.add(new StrikeMessage(version, gameRequest.clientID,
            "Not enough bombs!",
            (client.getStrikeCounter())));
        return messages;
      }
      Field field = gameInstance.getGameMap().getField(gameRequest.specs.target.getX(),gameRequest.specs.target.getY());
      if (!noble.familyAtomics(field)){
        client.increaseStrikeCounter();
        messages.add(new StrikeMessage(version, gameRequest.clientID,
            "Not enough action points!",
            (client.getStrikeCounter())));
        return messages;
      }
      messages.add(new ActionMessage(version, client.getClientID(), unit.getCharacterID(), ActionEnum.FAMILY_ATOMICS, gameRequest.specs));
      messages.add(getCharacterStatChangeMessage(version, client.getClientID(), unit.getCharacterID(), unit));

      if (noble.checkVictimsOfFamilyAtomics(field, gameInstance.getGameMap(), gameInstance.getPlayer(client.getPlayerEnum()))) {
        if(isshunned) {
            gameInstance.getPlayer(client.getPlayerEnum()).setIsShunned(true);
            GameInstance.LOGGER.info("FamilyAtomicsGameHandler(handleGameRequest): client is set shunned"+ client.getPlayerEnum());
        }
        GameInstance.LOGGER.info("FamilyAtomicsGameHandler(handleGameRequest): the Bomb hit units.");
        for (Unit victim:noble.getHitByAtomics()) {
           changeMapAfterDefeat(victim, gameInstance, version,client);

          //Removing the Unit from the Map on both ends (Map:field and Unit:field)
          victim.setField(null);
        }
        GameInstance.LOGGER.info("FamilyAtomicsGameHandler(handleGameRequest): generated all characterStatChangeMessages.");
        if(gameInstance.getPlayer(client.getPlayerEnum()).isShunned()) {
          List<Unit> compensationUnits = gameInstance.getHouses().getAtomicsCompensationCharacters(unit, gameInstance);
          GameInstance.LOGGER.info("FamilyAtomicsGameHandler(handleGameRequest): got compensationUnits: " + compensationUnits.toString());
          for (Unit compensationUnit : compensationUnits) {
            if (gameInstance.getCloning().placeNewCharacterAtPosition(gameInstance, compensationUnit, compensationUnit.getCity().getField())) {
              gameInstance.addGameUnit(compensationUnit, compensationUnit.getPlayer());
              messages.add(createSpawnMessage(gameInstance, version, gameInstance.getPlayer(compensationUnit.getPlayer()).getClientID(), compensationUnit));
            }
            GameInstance.LOGGER.info("FamilyAtomicsGameHandler(handleGameRequest): compensationunit: " + compensationUnit.toString() + " is at: " + compensationUnit.getField().toString());
          }
        }

      }
      Sandworm sandworm;
      if ((sandworm = noble.getHitSandworm()) != null){
        GameInstance.LOGGER.info("FamilyAtomicsGameHandler(handleGameRequest): a Sandworm has been hit at position: " +sandworm.getField());
        messages.add(sandworm.removeSandworm(version));
      }
      Tile[][] tileMap = gameInstance.getGameMap().createNewTileMap(gameInstance);
      messages.add(new AtomicsMessage(version, gameRequest.clientID, gameInstance.getPlayer(client.getPlayerEnum()).isShunned(),
              gameInstance.getPlayer(unit.getPlayer()).getAtomicsCount()));
      messages.add(new MapChangeMessage(version, ChangeReason.FAMILY_ATOMICS,tileMap, new Point(
              gameInstance.getSandstorm().getField().getXCoordinate(),
              gameInstance.getSandstorm().getField().getYCoordinate())));
      return messages;
    }
}
