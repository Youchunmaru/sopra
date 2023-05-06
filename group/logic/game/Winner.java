package logic.game;

import enums.PlayerEnum;
import logic.game.entity.unit.Unit;
import messages.gameplay.outgoing.GameEndMessage;
import network.util.Statistics;

/**
 * Class to calculate winner of the game.
 *
 * @author Janine Grimmer
 */
public class Winner {
    // integer for client IDs
    private int winnerID;
    private int loserID;

    /**
     * Constructor of winner class.
     * Sets IDs to -1 to distinguish from real winner and loser IDs.
     */
    public Winner() {
        winnerID = -1;
        loserID = -1;
    }

    /**
     * Method to get winner of the current game and to set the winner's ID.
     *
     * @param loserID      integer, ID of losing client
     * @param gameInstance {@link GameInstance}
     */
    public void getWinner(int loserID, GameInstance gameInstance) {
        this.loserID = loserID;
        // call getLoser the other way round to get ID of winnerID
        if (gameInstance.getPlayerOne().isPresent() && gameInstance.getPlayerTwo().isPresent()) {
            if (gameInstance.getPlayerOne().get().getClientID() == loserID) {
                winnerID = gameInstance.getPlayerTwo().get().getClientID();
            } else if (gameInstance.getPlayerTwo().get().getClientID() == loserID) {
                winnerID = gameInstance.getPlayerOne().get().getClientID();
            } else {
                winnerID = -1;
            }
        }
    }

    /**
     * Called to find out which player has won after excessively long game.
     * Sets ID of winner and loser.
     *
     * @param gameInstance {@link GameInstance}
     */
    public void calculateWinner(GameInstance gameInstance) {
        // use victory measures to determine winnerID, start with comparison of spice amount in cities
        winnerID = compareSpiceAmount(gameInstance);
        if (winnerID == -1) {
            // if both players have the same amount of spice in town compare the total amount of spice taken up during the game
            winnerID = compareSpiceTakenUp(gameInstance);
            if (winnerID == -1) {
                // if standoff, compare total amount of defeated opossing characters
                winnerID = compareOpponentsDefeated(gameInstance);
                if (winnerID == -1) {
                    // if standoff, compare total number of characters swallowed by a "normal" sandworm
                    winnerID = compareNumberOfSwallowedCharacters(gameInstance);
                }
                if (winnerID == -1) {
                    // finally if still standoff, get the last character swallowed by Shai-Hulud
                    winnerID = getLastCharacterStanding(gameInstance);
                }
            }
        }
        // get ID of losing client
        loserID = getLoser(winnerID, gameInstance);
    }

    /**
     * Called to get loser of game with help of clientID from winner
     *
     * @param winnerID     ID of winning client
     * @param gameInstance {@link GameInstance}
     * @return integer, ID of losing client or -1 if players are not present
     */
    private int getLoser(int winnerID, GameInstance gameInstance) {
        this.winnerID = winnerID;
        if (gameInstance.arePlayersPresent()) {

            if (winnerID == gameInstance.getPlayerOne().get().getClientID()) {
                loserID = gameInstance.getPlayerTwo().get().getClientID();
            } else {
                loserID = gameInstance.getPlayerOne().get().getClientID();
            }
            return loserID;
        }
        return -1;
    }

    /**
     * Method to find out which player's unit was the last to stand on the game map before being swallowed by Shai Hulud.
     *
     * @param gameInstance {@link GameInstance}
     * @return integer, ID of client who plays the last character standing on game map
     */
    private int getLastCharacterStanding(GameInstance gameInstance) {
        Unit lastCharacter = gameInstance.getSandworm().getLastCharacterStanding();
        return getClientID(lastCharacter, gameInstance);
    }

    /**
     * Method to get ID of client that plays the unit.
     *
     * @param unit {@link Unit}
     * @return positive integer, ID of client or -1 if players are not set
     */
    public int getClientID(Unit unit, GameInstance gameInstance) {
        if (gameInstance.arePlayersPresent()) {
            if (unit.getPlayer() == PlayerEnum.PLAYER_ONE) {
                return gameInstance.getPlayerOne().get().getClientID();
            } else if (unit.getPlayer() == PlayerEnum.PLAYER_TWO) {
                return gameInstance.getPlayerTwo().get().getClientID();
            }
        }
        return -1;
    }


    /**
     * Called to compare number of swallowed characters of both players.
     *
     * @param gameInstance {@link GameInstance}
     * @return integer; either ID of client who has less characters swallowed by sandworm or -1 if players are not present
     */
    private int compareNumberOfSwallowedCharacters(GameInstance gameInstance) {
        if (gameInstance.arePlayersPresent()) {
            if (gameInstance.getPlayerOne().get().getNumberOfSwallowedCharacters() < gameInstance.getPlayerTwo().get().getNumberOfSwallowedCharacters()) {
                return gameInstance.getPlayerOne().get().getClientID();
            } else if (gameInstance.getPlayerOne().get().getNumberOfSwallowedCharacters() > gameInstance.getPlayerTwo().get().getNumberOfSwallowedCharacters()) {
                return gameInstance.getPlayerTwo().get().getClientID();
            } else return -1;
        }
        return -1;
    }

    /**
     * Compares number of defeated units by both players.
     *
     * @param gameInstance {@link GameInstance}
     * @return integer, either ID of client who has less opponent defeated or -1 if players are not present
     */
    private int compareOpponentsDefeated(GameInstance gameInstance) {
        if (gameInstance.arePlayersPresent()) {
            if (gameInstance.getPlayerOne().get().getOpponentsDefeated() > gameInstance.getPlayerTwo().get().getOpponentsDefeated()) {
                return gameInstance.getPlayerOne().get().getClientID();
            } else if (gameInstance.getPlayerOne().get().getOpponentsDefeated() < gameInstance.getPlayerTwo().get().getOpponentsDefeated()) {
                return gameInstance.getPlayerTwo().get().getClientID();
            } else return -1;
        }
        return -1;
    }


    /**
     * Compares amount of spice taken up during the game by both players.
     *
     * @param gameInstance {@link GameInstance}
     * @return integer, either ID of client who has taken up more spice or -1 if players are not present
     */
    private int compareSpiceTakenUp(GameInstance gameInstance) {
        if (gameInstance.arePlayersPresent()) {
            if (gameInstance.getPlayerOne().get().getSpiceTakenUp() > gameInstance.getPlayerTwo().get().getSpiceTakenUp()) {
                return gameInstance.getPlayerOne().get().getClientID();
            } else if (gameInstance.getPlayerOne().get().getSpiceTakenUp() < gameInstance.getPlayerTwo().get().getSpiceTakenUp()) {
                return gameInstance.getPlayerTwo().get().getClientID();
            } else return -1;
        }
        return -1;
    }

    /**
     * Called to compare amount of spice in city of both players.
     *
     * @param gameInstance {@link GameInstance}
     * @return integer, either ID of client who has more spice delivered to its city or -1 if players are not present
     */
    private int compareSpiceAmount(GameInstance gameInstance) {
        if (gameInstance.arePlayersPresent()) {
            if (gameInstance.getPlayerOne().get().getCity().getSpiceStock() > gameInstance.getPlayerTwo().get().getCity().getSpiceStock()) {
                return gameInstance.getPlayerOne().get().getClientID();
            } else if (gameInstance.getPlayerOne().get().getCity().getSpiceStock() < gameInstance.getPlayerTwo().get().getCity().getSpiceStock()) {
                return gameInstance.getPlayerTwo().get().getClientID();
            } else return -1;
        }
        return -1;
    }

    /**
     * Called to create a game end message.
     *
     * @param version string, current version of standard document
     * @return {@link GameEndMessage}
     */
    public GameEndMessage showGameResult(String version) {
        // create empty stats object
        Statistics[] statistics = new Statistics[]{new Statistics("")};
        return new GameEndMessage(version, winnerID, loserID, statistics);
    }

    /**
     * Called to access ID of winner.
     *
     * @return ID of winner client, integer
     */
    public int getWinnerID() {
        return winnerID;
    }

    /**
     * Used to set ID of winner client.
     *
     * @param winnerID integer, ID of {@link network.util.Client}
     */
    public void setWinnerID(int winnerID) {
        this.winnerID = winnerID;
    }

    /**
     * Called to access ID of winner.
     *
     * @return ID of loser, integer
     */
    public int getLoserID() {
        return loserID;
    }

    /**
     * Used to set ID of loser
     *
     * @param loserID integer, ID of {@link network.util.Client}
     */
    public void setLoserID(int loserID) {
        this.loserID = loserID;
    }


}
