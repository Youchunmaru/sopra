package logic.game;

import enums.PlayerEnum;
import logic.game.entity.GameEntity;
import logic.game.entity.unit.Unit;
import logic.game.map.Field;
import logic.util.MathUtil;
import messages.configuration.PartyConfig;
import messages.gameplay.outgoing.SpawnMessage;
import messages.util.Point;
import messages.util.SpawnMessageCharacter;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that tries to clone defeated characters. Called from StateMachine during the Cloning Phase.
 * Spawns all characters with new ID for the character phase.
 *
 * @author Janine Grimmer, Samuel Gröner
 */
public class Cloning {

    /***
     * Tries to clone character.
     *
     * @return if unit is cloneable
     * @author Samuel Gröner
     */
    public boolean cloneable(Unit unit) {
        return !unit.isSwallowed() && unit.isDefeated();
    }


    /**
     * Called to try to clone characters of (own) house in order to revive them. Called every round.
     * Cloning of characters is only possible if character is not swallowed by sandworm or Shai Hulud.
     * If successfully cloned send Character Spawn Message to clients.
     *
     * @param unitList              list of all units
     * @param version               String, current version of standard document, needed to create SpawnMessage
     * @param partyConfig           {@link PartyConfig} needed to create SpawnMessage
     * @param id                    id for next to be respawned unit
     * @param gameInstance          {@link GameInstance}
     * @return list of {@link SpawnMessage} or empty
     * @author Janine Grimmer
     */
    public List<SpawnMessage> cloneCharacters(List<Unit> unitList,
                                              String version,  PartyConfig partyConfig, int id,
                                              GameInstance gameInstance) {
        List<SpawnMessage> messages = new LinkedList<>();
        SpawnMessage spawnMessage;

        if (!unitList.isEmpty()) {
            for (Unit unit : unitList) {
                if (cloneable(unit) && unit.cloneUnit()) {
                    // create new ID for this unit and put cloned unit on game map
                    unit.setCharacterID(id);
                    id++;
                    List<Field> freeFields= gameInstance.getGameMap().getNearestFreeFieldsFromBlockedField(unit.getCity().getField(),
                            unit.getEntityType(),new LinkedList<>(),new LinkedList<>(),new LinkedList<>());
                       Field placedAtField = freeFields.get(MathUtil.random.nextInt(freeFields.size()));
                    unit.setField(placedAtField);
                    spawnMessage = createSpawnMessage(unit, partyConfig, version, new Point(unit.getField().getXCoordinate(),
                            unit.getField().getYCoordinate()), gameInstance);
                    messages.add(spawnMessage);

                }
            }
        }
        return messages;
    }

    /**
     * Called to place a new character at a special postion.
     *
     * @param gameInstance game logic handling component
     * @param entity entity that needs a field
     * @param field start field from which a new field is found
     * @return always true
     */
    public boolean placeNewCharacterAtPosition(GameInstance gameInstance, GameEntity entity, Field field){
        List<Field> freeFields= gameInstance.getGameMap().getNearestFreeFieldsFromBlockedField(field,
                entity.getEntityType(),new LinkedList<Field>(),new LinkedList<Field>(),new LinkedList<Field>());

        Field placedAtField = freeFields.get(MathUtil.random.nextInt(freeFields.size()));
        GameInstance.LOGGER.info("Cloning(placeNewCharacterAtPosition) placed at Field: " + placedAtField);
        entity.setField(placedAtField);
        return true;
    }

    /**
     * Called in order to create new spawn message for a unit after successfull cloning.
     *
     * @param unit         {@link Unit} to be spawned on game map
     * @param partyConfig  {@link PartyConfig} needed to get maximum values for this unit type
     * @param version      String, current version of standard document
     * @param point        {@link Point} containts coordinates of unit's field
     * @param gameInstance {@link GameInstance}
     * @return {@link SpawnMessage} for this unit
     * @author Janine Grimmer
     */
    private SpawnMessage createSpawnMessage(Unit unit, PartyConfig partyConfig, String version,
                                            Point point, GameInstance gameInstance) {
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
        int clientID;
        if (gameInstance.arePlayersPresent()) {
            if (unit.getPlayer() == PlayerEnum.PLAYER_ONE) {
                clientID = gameInstance.getPlayerOne().get().getClientID();
            } else {
                clientID = gameInstance.getPlayerTwo().get().getClientID();
            }
        } else {
            clientID = -1;
        }
        return new SpawnMessage(version, clientID, unit.getCharacterID(), unit.getName(),
                point, attributes);

    }


}
