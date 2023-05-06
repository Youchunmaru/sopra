package network.util;

import enums.GreatHouseEnum;
import enums.PlayerEnum;
import logic.game.entity.City;
import logic.game.entity.unit.Unit;

import java.util.LinkedList;
import java.util.List;

/**
 * Class of actively playing client.
 *
 * @author Janine Grimmer, Julian Korinth
 */
public class Player {

    private GreatHouseEnum greatHouse;
    private City city;
    private List<Unit> characterList = new LinkedList<>();

    public void setPlayerEnum(PlayerEnum playerEnum) {
        this.playerEnum = playerEnum;
    }

    private PlayerEnum playerEnum;
    private int clientID;
    private Client client;
    private boolean isShunned;
    // integer to collect sum of collected spice
    private int spiceSum = 0;

    private int atomicsCount = 3;

    // counter for winner metrics
    private int opponentsDefeated = 0;
    private int swallowedCharacters = 0;

    /**
     * Used for testing purpose only in shared logic tests.
     *
     * @param clientID unique integer, ID of client who plays this player
     */
    public Player(int clientID) {
        this.clientID = clientID;
    }

    /**
     * Used for testing purpose only in shared logic tests.
     *
     * @param client {@link Client}
     */
    public Player(Client client) {
        this.client = client;
        clientID = client.getClientID();
    }

    /**
     * Used for testing purpose only in shared logic tests.
     *
     * @param client     {@link Client} of this player
     * @param playerEnum {@link PlayerEnum}
     */
    public Player(Client client, PlayerEnum playerEnum) {
        this.client = client;
        clientID = client.getClientID();
        this.playerEnum = playerEnum;
    }

    /**
     * Called to get client of player.
     *
     * @return {@link Client}
     */
    public Client getClient() {
        return client;
    }


    /**
     * Method to get ID of playing client.
     *
     * @return positive integer
     */
    public int getClientID() {
        return clientID;
    }

    /**
     * Method to set client's chosen Great House
     *
     * @param greatHouse Great House that client has chosen to play
     */
    public void setGreatHouse(GreatHouseEnum greatHouse) {
        this.greatHouse = greatHouse;
    }

    /**
     * Method to get client's chosen Great House
     *
     * @return {@link GreatHouseEnum} Great House that client plays
     */
    public GreatHouseEnum getGreatHouse() {
        return greatHouse;
    }


    /**
     * Used to add all spice collected by the player's characters.
     *
     * @param spiceAmount positive integer, amount of spice to be added
     */
    public void increaseSpiceSum(int spiceAmount) {
        spiceSum += spiceAmount;
    }


    /**
     * Used to get sum of spice collected by this player.
     *
     * @return sum of spice collected by the player's characters
     */
    public int getSpiceTakenUp() {
        return spiceSum;
    }


    /**
     * Called to get own city.
     *
     * @return {@link City}
     */
    public City getCity() {
        return city;
    }

    /**
     * Used to set own city.
     *
     * @param city {@link City}
     */
    public void setCity(City city) {
        this.city = city;
    }

    /**
     * Used in EndphaseState after Shai Hulud has eaten all characters on the map to calculate winner.
     *
     * @return integer, number of defeated opponents
     */
    public int getOpponentsDefeated() {
        return opponentsDefeated;
    }

    /**
     * Called if a character defeats an opponent to increase the opponents defeated counter.
     * Used in EndphaseState as winner metrics.
     */
    public void increaseOpponentsDefeated() {
        opponentsDefeated++;
    }

    /**
     * Used in EndphaseState as winner metrics.
     *
     * @return integer, number of own characters that have been swallowed by a sandworm
     */
    public int getNumberOfSwallowedCharacters() {
        return swallowedCharacters;
    }

    /**
     * Called each time a character is swallowed by a sandworm. Increases this counter.
     */
    public void increaseSwallowedCharacters() {
        swallowedCharacters++;
    }

    public void addCharacter(Unit unit) {
        characterList.add(unit);
    }

    public int getAtomicsCount() {
        return atomicsCount;
    }
    public boolean isShunned(){
        return isShunned;
    }
    public void setIsShunned(boolean isShunned){
        this.isShunned=isShunned;
    }
    public void setAtomicsCount(int atomicsCount) {
        this.atomicsCount = atomicsCount;
    }

    public PlayerEnum getPlayerEnum() {
        return playerEnum;
    }

    public List<Unit> getCharacterList() {
        return characterList;
    }

}
