package logic.game.gameHandler;

import enums.ClientRole;
import enums.GreatHouseEnum;
import enums.PlayerEnum;
import logic.game.GameInstance;
import messages.Message;
import messages.gameplay.incoming.HouseSelectionRequest;
import messages.gameplay.outgoing.HouseSelectionAckMessage;
import messages.gameplay.outgoing.StrikeMessage;
import network.util.Client;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HouseGameHandler extends GameHandler<HouseSelectionRequest> {

    private static final Logger LOGGER = Logger.getLogger(HouseGameHandler.class.getName());

    @Override
    public List<Message> handleGameRequest(GameInstance gameInstance, String version,
                                           Client client, HouseSelectionRequest gameRequest) {
        LOGGER.log(Level.INFO,"HouseGameHandler(handleGameRequest): A Request of Type: {0} has arrived with house: {1}.", new Object[]{gameRequest.type, gameRequest.houseName});
        clearMessages();
        GreatHouseEnum house = gameRequest.houseName;
        LOGGER.log(Level.INFO,"HouseGameHandler(handleGameRequest): house field is: {0}",  house.toString());
        if (isActiveClientRole(client) && client.getPlayerEnum() == PlayerEnum.PLAYER_TWO) {
            if (house == gameInstance.getHouseOffers().get(0).houses[0].houseName||
            house == gameInstance.getHouseOffers().get(0).houses[1].houseName) {
                messages.add(
                        new HouseSelectionAckMessage(version, client.getClientID(), house));
                gameInstance.getPlayer(PlayerEnum.PLAYER_TWO).setGreatHouse(house);
            }
        } else if (isActiveClientRole(client) && client.getPlayerEnum() == PlayerEnum.PLAYER_ONE) {
            if (house == gameInstance.getHouseOffers().get(1).houses[0].houseName||
            house == gameInstance.getHouseOffers().get(1).houses[1].houseName) {
                messages.add(
                        new HouseSelectionAckMessage(version, client.getClientID(), house));
                gameInstance.getPlayer(PlayerEnum.PLAYER_ONE).setGreatHouse(house);
            }
        } else if (!isActiveClientRole(client)){
            client.increaseStrikeCounter();
            messages.add(new StrikeMessage(version, client.getClientID(), "Wrong client to send house selection request!",
                    client.getStrikeCounter()));
            return messages;
        }
        LOGGER.log(Level.INFO,"HouseGameHandler(handleGameRequest): Messages returned have count {0}.", messages.size());
        if(!messages.isEmpty())LOGGER.log(Level.INFO,"HouseGameHandler(handleGameRequest): message[0] of type houseAck has houseName: {0}.",
                ((HouseSelectionAckMessage)messages.get(0)).houseName.toString());
        return messages;
    }

    /**
     * Called to check role of client.
     *
     * @param client client that has sent the HouseSelectionRequest
     * @return true if client plays actively, either as player or as ai
     */
    private boolean isActiveClientRole(Client client) {
        return client.getRole() == ClientRole.PLAYER || client.getRole() == ClientRole.AI;
    }
}

