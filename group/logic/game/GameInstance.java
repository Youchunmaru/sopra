package logic.game;

import enums.GameHandlerType;
import enums.MessageType;
import enums.PlayerEnum;
import logic.config.Houses;
import logic.game.entity.City;
import logic.game.entity.Sandstorm;
import logic.game.entity.Sandworm;
import logic.game.entity.unit.Unit;
import logic.game.gameHandler.*;
import logic.game.map.Dune;
import logic.game.map.GameMap;
import messages.Message;
import messages.configuration.PartyConfig;
import messages.configuration.ScenarioConfig;
import messages.exceptions.ErrorMessage;
import messages.gameplay.incoming.ActionRequest;
import messages.gameplay.outgoing.GameEndMessage;
import messages.gameplay.outgoing.HouseOfferMessage;
import network.util.Client;
import network.util.Player;

import java.util.*;
import java.util.logging.Logger;

import static enums.FieldType.CITY;


/**
 * Class of logic handling component.
 *
 * @author Julian Korinth
 */
public class GameInstance {
    public static final Logger LOGGER = Logger.getLogger(GameInstance.class.getName());
    private Unit currentlyActingUnit;
    private PartyConfig partyConfig;
    private GameMap gameMap;
    private List<Unit> gameUnits = new LinkedList<>();
    private Player firstToDropTheBomb;
    private Optional<Player> playerOne = Optional.empty();
    private Optional<Player> playerTwo = Optional.empty();
    private ScenarioConfig scenarioConfig;
    private Map<GameHandlerType, GameHandler<?>> gameHandlers = new EnumMap<>(GameHandlerType.class);
    private int idCounter = 0;
    public Houses getHouses() {
        return houses;
    }

    public void setHouses(Houses houses) {
        this.houses = houses;
    }

    private Houses houses;
    private List<HouseOfferMessage> houseOffers;

    // required for game phases
    private Sandstorm sandstorm;
    private Dune dune;
    private Cloning cloning;
    private Sandworm sandworm;

    public GameInstance(PartyConfig partyConfig, ScenarioConfig scenarioConfig, Houses houses) {
        this.partyConfig = partyConfig;
        this.scenarioConfig = scenarioConfig;
        this.houses = houses;
        this.gameMap = new GameMap(partyConfig, scenarioConfig);
        registerGameHandlers();
    }
    public void addGameUnit(Unit unit, PlayerEnum player){
        if(unit == null) return;
        if(!gameUnits.contains(unit)){
            gameUnits.add(unit);
        }
        if(!getPlayer(player).getCharacterList().contains(unit)){
            getPlayer(player).addCharacter(unit);
        }
    }
    /**
     * Constructor for statemachine. All variables are set during the setup state phase.
     */
    public GameInstance() {
        registerGameHandlers();
    }

    /**
     * Method to register game handlers in order to handle incoming requests from players.
     */
    public void registerGameHandlers() {
        gameHandlers.put(GameHandlerType.ATTACK, new AttackGameHandler());
        gameHandlers.put(GameHandlerType.COLLECT, new CollectGameHandler());
        gameHandlers.put(GameHandlerType.KANLY, new KanlyGameHandler());
        gameHandlers.put(GameHandlerType.FAMILY_ATOMICS, new FamilyAtomicsGameHandler());
        gameHandlers.put(GameHandlerType.SPICE_HOARDING, new SpiceHoardingGameHandler());
        gameHandlers.put(GameHandlerType.VOICE, new VoiceGameHandler());
        gameHandlers.put(GameHandlerType.SWORDSPIN, new SwordSpinGameHandler());
        gameHandlers.put(GameHandlerType.MOVEMENT_REQUEST, new MovementGameHandler());
        gameHandlers.put(GameHandlerType.TRANSFER_REQUEST, new TransferGameHandler());
        gameHandlers.put(GameHandlerType.END_TURN_REQUEST, new EndTurnGameHandler());
        gameHandlers.putIfAbsent(GameHandlerType.HOUSE_REQUEST, new HouseGameHandler());
        gameHandlers.putIfAbsent(GameHandlerType.HELI_REQUEST, new HeliGameHandler());
    }

    /**
     * Called to get an ID number for the next unit.
     *
     * @return integer >=0
     */
    public int getIdCounter() {
        return idCounter;
    }

    /**
     * Method to set counter for IDs to another number. Must be greater than before.
     *
     * @param idCounter integer >=0
     */
    public void setIdCounter(int idCounter) {
        this.idCounter = idCounter;
    }

    public int generateCharacterID(){
        idCounter += 1;
        return idCounter;
    }

    //TODO if cast MessageType to GamehandlerType necessary
    private <T extends Message> List<Message> queueGameRequest(Client client, String version, T gameRequest) {
        LOGGER.info("GameInstance(queueGameRequest): A Request of Type: " + gameRequest.type + " has arrived.");
        GameHandler<T> handler;
        /*todo: do it this way:
         * return gameHandlers.get(GameHandlerType.valueOf(((ActionRequest) gameRequest).action.name())).handleGameRequest(...);
         * */// TODO: 04.07.2022 shouldn't gameHandlers be used and not a new handler?
        if (gameRequest.type == MessageType.ACTION_REQUEST) {

            handler = (GameHandler<T>) this.gameHandlers.get(GameHandlerType.valueOf(((ActionRequest) gameRequest).action.name()));
            return handler.handleGameRequest(this, version, client, gameRequest);
        }


        handler = (GameHandler<T>) this.gameHandlers.get(GameHandlerType.valueOf(gameRequest.type.name()));
        return handler.handleGameRequest(this, version, client, gameRequest);
    }

    /**
     * Method which has for input desired game changes, e.g. a MovementRequest message
     * and as return the resulting changes to the game state as a message, e.g. as a Movement-Message.
     *
     * @return list of messages
     */
    public List<Message> handleGameChange(Client client, String version, Message request) {
        LOGGER.info("GameInstance(handleGameChange): A Request of Type: " + request.type + " has arrived.");
        List<Message> messages = new LinkedList<>();
        switch (request.type) {
            case ACTION_REQUEST:
            case MOVEMENT_REQUEST:
            case TRANSFER_REQUEST:
            case HOUSE_REQUEST:
            case END_TURN_REQUEST:
            case HELI_REQUEST:
                return queueGameRequest(client, version, request);
            default:
                messages.add(new ErrorMessage(version, 006, "Unhandled message type."));
                return messages;
        }
    }

    /**
     * Called from statemachine during default state after registration of two players.
     * Registers clients as players and sets player enums.
     *
     * @param clientOne first player
     * @param clientTwo second player
     */
    public void registerPlayerClients(Client clientOne, Client clientTwo) {
        playerOne = Optional.of(new Player(clientOne, PlayerEnum.PLAYER_ONE));
        clientOne.setPlayerEnum(PlayerEnum.PLAYER_ONE);
        playerTwo = Optional.of(new Player(clientTwo, PlayerEnum.PLAYER_TWO));
        clientTwo.setPlayerEnum(PlayerEnum.PLAYER_TWO);
    }

    public List<Unit> getGameUnits() {
        return gameUnits;
    }



    /**
     * Called from statemachine during character phase to shuffle order of characters.
     *
     * @return list of units in a new order
     */
    public void shuffleUnitOrder() {
        Collections.shuffle(gameUnits);
    }

    /**
     * Called to find out whether IDs of the currently acting unit and the received
     * character ID are identical.
     *
     * @param requestCharacterID ID of unit from incoming request
     * @return true if IDs match
     */
    public boolean isCurrentlyActingUnit(int requestCharacterID) {
        return currentlyActingUnit.getCharacterID() == requestCharacterID;
    }

    /**
     * Called from statemachine when game is over to determine the game's winner.
     *
     * @param hasCausedTooManyStrikes boolean, true if a client has lost the game because he caused more strikes than allowed
     * @param version                 version of standard, needed to send messages
     * @author Janine Grimmer
     */
    public GameEndMessage determineWinner(boolean hasCausedTooManyStrikes, String version) {
        Winner winner = new Winner();
        if (getSandworm().checkGameTermination(this) && !getSandworm().getIsShaiHulud()) {
            winner.getWinner(getSandworm().getLoser(), this);
            return winner.showGameResult(version);
        }
        // excessively long
        if (getSandworm().getIsShaiHulud()) {
            winner.calculateWinner(this);
            return winner.showGameResult(version);
        }
        // won due to many strikes of a client
        if (hasCausedTooManyStrikes ) {
            if (arePlayersPresent() && getPlayerOne().get().getClient().getStrikeCounter() >= partyConfig.maxStrikes) {
                winner.setWinnerID(getPlayerOne().get().getClientID());
                winner.setLoserID(getPlayerTwo().get().getClientID());
                return winner.showGameResult(version);
            } else if (arePlayersPresent() && getPlayerTwo().get().getClient().getStrikeCounter() >= partyConfig.maxStrikes) {
                winner.setWinnerID(getPlayerTwo().get().getClientID());
                winner.setLoserID(getPlayerOne().get().getClientID());
                return winner.showGameResult(version);
            }
        }
        // won because one player lost connection; only winner ID can be shown
        if (getPlayerOne().isPresent() && !getPlayerTwo().isPresent()) {
            winner.setWinnerID(getPlayerOne().get().getClientID());
            return winner.showGameResult(version);
        } else if (!getPlayerOne().isPresent() && getPlayerTwo().isPresent()) {
            winner.setWinnerID(getPlayerTwo().get().getClientID());
            return winner.showGameResult(version);
        }  return null;
    }


    /**
     * Called during setup state to allocate each player one city.
     */
    public void matchCityToPlayer() {
        boolean isFirstCity = true;
        if(arePlayersPresent()){
            for (int i = 0; i < gameMap.getXSize(); i++) {
                for (int j = 0; j < gameMap.getYSize(); j++) {
                    if (gameMap.getField(i, j).getFieldType() == CITY) {
                        // create new city
                        City city;
                        if (isFirstCity) {
                            city = new City(PlayerEnum.PLAYER_ONE, getGameMap().getField(i, j), this);
                            playerOne.get().setCity(city);
                            isFirstCity = false;
                        } else {
                            city = new City(PlayerEnum.PLAYER_TWO, getGameMap().getField(i, j), this);
                            playerTwo.get().setCity(city);
                        }
                        gameMap.getField(i, j).setGameEntity(city.getEntityType(), city);
                        city.setField(gameMap.getField(i, j));
                    }
                }
            }
        }
    }

    /**
     * Checks whether players are set and present.
     *
     * @return true, if both players are not empty, false otherwise
     * @author Janine Grimmer
     */
    public boolean arePlayersPresent() {
        return getPlayerOne().isPresent() && getPlayerTwo().isPresent();
    }

    /**
     * Returns the unit with the specified id
     *
     * @param id the id of the unit
     * @return the unit if present or null
     */
    public Unit getUnitById(int id) {
        for (Unit unit : gameUnits) {
            if (unit.getCharacterID() == id) {
                return unit;
            }
        }
        return null;
    }

    public GameMap getGameMap() {
        return gameMap;
    }

    public void setGameMap(GameMap gameMap) {
        this.gameMap = gameMap;
    }

    public Sandstorm getSandstorm() {
        return sandstorm;
    }

    public void setSandstorm(Sandstorm sandstorm) {
        this.sandstorm = sandstorm;
    }

    public Dune getDune() {
        return dune;
    }

    public void setDune(Dune dune) {
        this.dune = dune;
    }

    public Optional<Player> getPlayerOne() {
        return playerOne;
    }

    // for testing purpose only
    public void setPlayerOne(Optional<Player> playerOne) {
        this.playerOne = playerOne;
    }

    public Optional<Player> getPlayerTwo() {
        return playerTwo;
    }

    // for testing purpose only
    public void setPlayerTwo(Optional<Player> playerTwo) {
        this.playerTwo = playerTwo;
    }

    public Player getPlayer(PlayerEnum player) {
        if (playerOne.isPresent() && playerTwo.isPresent()) {
            return playerOne.get().getPlayerEnum() == player ? playerOne.get() : playerTwo.get();
        }
        return null;
    }
    public List<Player> getPlayers(PlayerEnum playerEnum){
        List<Player> players = new LinkedList<>();
        if(playerOne.get().getPlayerEnum()==playerEnum){
            players.add(playerOne.get());
            players.add(playerTwo.get());
        }else{
            players.add(playerTwo.get());
            players.add(playerOne.get());
        }
        return players;
    }
    public Player getFirstToDropTheBomb() {
        return firstToDropTheBomb;
    }

    public void setFirstToDropTheBomb(Player firstToDropTheBomb) {
        this.firstToDropTheBomb = firstToDropTheBomb;
    }

    public Cloning getCloning() {
        return cloning;
    }

    public void setCloning(Cloning cloning) {
        this.cloning = cloning;
    }

    public PartyConfig getPartyConfig() {
        return partyConfig;
    }

    public void setPartyConfig(PartyConfig partyConfig) {
        this.partyConfig = partyConfig;
    }

    public Sandworm getSandworm() {
        return sandworm;
    }

    public void setSandworm(Sandworm sandworm) {
        this.sandworm = sandworm;
    }

    public void setScenarioConfig(ScenarioConfig scenarioConfig) {
        this.scenarioConfig = scenarioConfig;
    }

    public ScenarioConfig getScenarioConfig() {
        return scenarioConfig;
    }

    public Unit getCurrentlyActingUnit() {
        return currentlyActingUnit;
    }

    public void setCurrentlyActingUnit(Unit currentlyActingUnit) {
        this.currentlyActingUnit = currentlyActingUnit;
    }

    public List<HouseOfferMessage> getHouseOffers() {
        return houseOffers;
    }

    public void setHouseOffers(List<HouseOfferMessage> houseOffers) {
        this.houseOffers = houseOffers;
    }
}
