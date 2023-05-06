package logic.game.gameHandler;

import enums.ChangeReason;
import enums.FieldType;
import enums.GameEntityType;
import logic.game.GameInstance;
import logic.game.entity.unit.Unit;
import logic.game.map.Field;
import logic.util.DoublePoint;
import logic.util.MathUtil;
import messages.Message;
import messages.gameplay.incoming.HeliRequest;
import messages.gameplay.outgoing.HeliMessage;
import messages.gameplay.outgoing.StrikeMessage;
import messages.util.Point;
import network.util.Client;
import java.util.LinkedList;
import java.util.List;

public class HeliGameHandler extends GameHandler<HeliRequest> {

    MathUtil mathUtil;
    public HeliGameHandler(){
      mathUtil = new MathUtil();
    }
    public List<Message> handleGameRequest(GameInstance gameInstance, String version, Client client,
                                           HeliRequest heliRequest) {
        clearMessages();
        GameInstance.LOGGER.info("HeliGameHandler(handleGameRequest): Message of type: " + heliRequest.getType());
        //Checks if requested unit is the currently acting unit
        if (!gameInstance.isCurrentlyActingUnit(heliRequest.characterID)) {
            client.increaseStrikeCounter();
            messages.add(new StrikeMessage(version, client.getClientID(),
                    "Currently not turn of unit: " + heliRequest.characterID, client.getStrikeCounter()));
            return messages;
        }
        GameInstance.LOGGER.info("HeliGameHandler(handleGameRequest): passed currentlyActingUnitTest");
        Unit unit = gameInstance.getCurrentlyActingUnit();
        //Checks if Unit is on correct Field
        if(unit.getField().getFieldType() != FieldType.HELIPORT){
            client.increaseStrikeCounter();
            messages.add(new StrikeMessage(version, client.getClientID(), "Unit is not on a Heliport",
                    client.getStrikeCounter()));
            return messages;
        }
        GameInstance.LOGGER.info("HeliGameHandler(handleGameRequest): passed correct Field test");

        //Checks if targetField has HeliPort
        if(gameInstance.getGameMap().getField(heliRequest.target.getX(),heliRequest.target.getY()).getFieldType()!=FieldType.HELIPORT){
            if(unit.getField().getFieldType() != FieldType.HELIPORT){
                client.increaseStrikeCounter();
                messages.add(new StrikeMessage(version, client.getClientID(), "Target has no Heliport",
                        client.getStrikeCounter()));
                return messages;
            }
        }
        GameInstance.LOGGER.info("HeliGameHandler(handleGameRequest): passed target has heliport test");

        Field heliEndField = gameInstance.getGameMap().getField(heliRequest.target.getX(),heliRequest.target.getY());
        Field crashField = null;
        Field targetField = heliEndField;
        //Checks if the line of transport intersects a sandstorm
        GameInstance.LOGGER.info("HeliGameHandler(handleGameRequest): Trying to get intersecting coord");
        List<DoublePoint> intersectingSandstormCoord=mathUtil.getIntersectingFieldCoordinates(unit.getField(),heliEndField,gameInstance);
        GameInstance.LOGGER.info("HeliGameHandler(handleGameRequest): Intersecting coordinates are: " + intersectingSandstormCoord.toString());
        if(!intersectingSandstormCoord.isEmpty()){
            GameInstance.LOGGER.info("HeliGameHandler(handleGameRequest): Intersections found.");
            double crash = gameInstance.getPartyConfig().crashProbability;
            GameInstance.LOGGER.info("HeliGameHandler(handleGameRequest): Crashprobability: " +crash*100.0d + "%.");
            if(MathUtil.random.nextDouble()<=crash){
                //CRASH
                DoublePoint rndStIF = intersectingSandstormCoord.get(MathUtil.random.nextInt(intersectingSandstormCoord.size()));
                GameInstance.LOGGER.info("HeliGameHandler(handleGameRequest): Heli crashed at randomly chosen field: " + rndStIF);
                crashField = gameInstance.getGameMap().getField((int)Math.ceil(rndStIF.x),(int)Math.ceil(rndStIF.y));
            }
        }
        if(crashField!=null){
            List<Field> allEligibleFields=gameInstance.getGameMap().getNearestFreeFieldsFromBlockedField(crashField, GameEntityType.UNIT, new LinkedList<Field>(),new LinkedList<Field>(),new LinkedList<Field>());
            if(!crashField.getGameEntities().containsKey(GameEntityType.UNIT)) allEligibleFields.add(crashField);
            targetField = allEligibleFields.get(MathUtil.random.nextInt(allEligibleFields.size()));
            if(unit.getCurrentSpice()!=0){
                unit.blowSpiceAfterDefeat(targetField.getXCoordinate(), targetField.getYCoordinate(),unit.getCurrentSpice());
                unit.setCurrentSpice(0);
            }
            messages.add(getCharacterStatChangeMessage(version,client.getClientID(),unit.getCharacterID(),unit));
            messages.add(creatMapChangeMessage(ChangeReason.HELI_CRASH,version,gameInstance));
        }

        //Actually moves the unit
        boolean endPointHasUnit = heliEndField.getGameEntity(GameEntityType.UNIT)!=null;
        if(endPointHasUnit&&targetField==heliEndField){
            List<Field> allEligibleFields=gameInstance.getGameMap().getNearestFreeFieldsFromBlockedField(heliEndField, GameEntityType.UNIT, new LinkedList<Field>(),new LinkedList<Field>(),new LinkedList<Field>());
            targetField = allEligibleFields.get(MathUtil.random.nextInt(allEligibleFields.size()));
        }
        unit.setField(targetField);
        messages.add(new HeliMessage(version, client.getClientID(), unit.getCharacterID(),
                new Point(targetField.getXCoordinate(), targetField.getYCoordinate()),targetField!=heliEndField));

        return messages;
    }
}
