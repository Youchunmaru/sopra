package logic.game.gameHandler;

import enums.ChangeReason;
import enums.GameEntityType;
import logic.game.GameInstance;
import logic.game.entity.unit.Unit;
import logic.game.map.Field;
import messages.Message;
import messages.configuration.PartyConfig;
import messages.gameplay.incoming.ActionRequest;
import messages.gameplay.outgoing.CharacterStatChangeMessage;
import messages.gameplay.outgoing.MapChangeMessage;
import messages.gameplay.outgoing.SpawnMessage;
import messages.gameplay.outgoing.StrikeMessage;
import messages.util.Point;
import messages.util.SpawnMessageCharacter;
import messages.util.Tile;
import messages.util.UnitStatChange;
import network.util.Client;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handels everything game related.
 *
 * @author Samuel Gröner, Julian Korinth
 */
public abstract class GameHandler<T extends Message> {

  protected List<Message> messages = new LinkedList<>();
  /**
   * Handels the incoming game request and returns a list of messages that show what changed in the
   * game.
   *
   * @param gameInstance the currently running game
   * @param gameRequest  the request
   * @param version      the version of the request
   * @param client       the client requesting
   */
  public abstract List<Message> handleGameRequest(GameInstance gameInstance, String version,
      Client client, T gameRequest);
  protected Message checkCurrentlyActive(GameInstance gameInstance, String version, Client client, ActionRequest gameRequest){
    if (!gameInstance.isCurrentlyActingUnit(gameRequest.characterID)){
      client.increaseStrikeCounter();
      return new StrikeMessage(version, gameRequest.clientID,
          "Currently not turn of unit: " + gameRequest.characterID,
          (client.getStrikeCounter()));
    }
    return null;
  }
  protected Message checkIsInSandstorm(String version, Client client, ActionRequest gameRequest, Unit unit){
    if (unit.isInSandstorm()){
      client.increaseStrikeCounter();
      return new StrikeMessage(version, gameRequest.clientID,
          "Currently not able to this action!",
          (client.getStrikeCounter()));
    }
    return null;
  }
  protected Message checkForRightTarget(String version, Client client, ActionRequest gameRequest, Field field, GameEntityType target){
    if (field == null || !field.getGameEntities().containsKey(target)){
      client.increaseStrikeCounter();
      return new StrikeMessage(version, gameRequest.clientID,
          "There is no target!",
          (client.getStrikeCounter()));
    }
    return null;
  }
  protected Message checkForRightAffiliation(String version, Client client, ActionRequest gameRequest, Unit victim){
    if (client.getPlayerEnum() == victim.getPlayer()){
      client.increaseStrikeCounter();
      return new StrikeMessage(version, gameRequest.clientID,
          "You cant attack your own units!",
          (client.getStrikeCounter()));
    }
    return null;
  }
  protected Message checkIfInRange(String version, Client client, ActionRequest gameRequest, Unit attacker, Unit victim){
    if (!((Math.abs(attacker.getField().getXCoordinate() - victim.getField().getXCoordinate()) <= 1) && (
        Math.abs(attacker.getField().getYCoordinate() - victim.getField().getYCoordinate()) <= 1))){
      client.increaseStrikeCounter();
      return new StrikeMessage(version, gameRequest.clientID,
          "Target not in range!",
          (client.getStrikeCounter()));
    }
    return null;
  }
  protected Message getCharacterStatChangeMessage(String version, int clientID, int characterID, Unit unit){
    return new CharacterStatChangeMessage(version, clientID,
        characterID,
        new UnitStatChange(unit.getHealthPoints(), unit.getActionPoints(),
            unit.getMovementPoints(), unit.getCurrentSpice(), unit.isLoud(),
            unit.isSwallowed()));
  }
  protected void clearMessages(){
    messages = new LinkedList<>();
  }

  public List<Message> getMessages() {
        return messages;
    }

  public MapChangeMessage creatMapChangeMessage(ChangeReason reason, String version, GameInstance gameInstance) {
    Tile[][] tileMap = gameInstance.getGameMap().createNewTileMap(gameInstance);
    Point stormEye = new Point(gameInstance.getSandstorm().getField().getXCoordinate(),
            gameInstance.getSandstorm().getField().getYCoordinate());
    return new MapChangeMessage(version, reason, tileMap, stormEye);
  }
  /**
   * Taken from janine
   */
  protected SpawnMessage createSpawnMessage(GameInstance gameInstance, String version,
                                          int clientID,Unit unit) {
    PartyConfig partyConfig = gameInstance.getPartyConfig();
    SpawnMessageCharacter attributes;
    switch (unit.getUnitType()) {
      // current and max values are the same as characters are spawned for the next round
      case NOBLE:
        attributes = new SpawnMessageCharacter(unit.getUnitType(),
                partyConfig.noble.getMaxHP(), unit.getHealthPoints(), partyConfig.noble.getHealingHP(),
                partyConfig.noble.getMaxMP(), partyConfig.noble.getMaxMP(), partyConfig.noble.getMaxAP(),
                partyConfig.noble.getMaxAP(), partyConfig.noble.getDamage(),
                partyConfig.noble.getInventorySize(), unit.getCurrentSpice(), false, false);
        break;
      case BENE_GESSERIT:
        attributes = new SpawnMessageCharacter(unit.getUnitType(),
                partyConfig.beneGesserit.getMaxHP(), unit.getHealthPoints(), partyConfig.beneGesserit.getHealingHP(),
                partyConfig.beneGesserit.getMaxMP(), partyConfig.beneGesserit.getMaxMP(),
                partyConfig.beneGesserit.getMaxAP(),
                partyConfig.beneGesserit.getMaxAP(), partyConfig.beneGesserit.getDamage(),
                partyConfig.beneGesserit.getInventorySize(), unit.getCurrentSpice(), false, false);
        break;
      case MENTAT:
        attributes = new SpawnMessageCharacter(unit.getUnitType(),
                partyConfig.mentat.getMaxHP(), unit.getHealthPoints(), partyConfig.mentat.getHealingHP(),
                partyConfig.mentat.getMaxMP(), partyConfig.mentat.getMaxMP(), partyConfig.mentat.getMaxAP(),
                partyConfig.mentat.getMaxAP(), partyConfig.mentat.getDamage(),
                partyConfig.mentat.getInventorySize(), unit.getCurrentSpice(), false, false);
        break;
      case FIGHTER:
        attributes = new SpawnMessageCharacter(unit.getUnitType(),
                partyConfig.fighter.getMaxHP(), unit.getHealthPoints(), partyConfig.fighter.getHealingHP(),
                partyConfig.fighter.getMaxMP(), partyConfig.fighter.getMaxMP(), partyConfig.fighter.getMaxAP(),
                partyConfig.fighter.getMaxAP(), partyConfig.fighter.getDamage(),
                partyConfig.fighter.getInventorySize(), unit.getCurrentSpice(), false, false);
        break;
      default:
        Logger.getLogger(GameInstance.class.getName()).log(Level.SEVERE, "Unknown unit type.: {0}.", unit.getUnitType());
        return null;
    }
    return new SpawnMessage(version, clientID, unit.getCharacterID(), unit.getName(),
            new Point(unit.getField().getXCoordinate(),unit.getField().getYCoordinate()), attributes);
  }


  protected List<Message> changeMapAfterDefeat(Unit victim, GameInstance gameInstance, String version, Client client){

    victim.blowSpiceAfterDefeat(victim.getField().getXCoordinate(), victim.getField().getYCoordinate(), victim.getCurrentSpice());
    victim.setCurrentSpice(0);
    messages.add(getCharacterStatChangeMessage(version,client.getClientID(),
            victim.getCharacterID(), victim));
    Point stormEye = new Point(gameInstance.getSandstorm().getField().getXCoordinate(),
            gameInstance.getSandstorm().getField().getYCoordinate());
    Tile[][] newMap = gameInstance.getGameMap().createNewTileMap(gameInstance);
    messages.add(new MapChangeMessage(version, ChangeReason.ROUND_PHASE, newMap,
            stormEye));
    return messages;
  }
}