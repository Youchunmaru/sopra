package enums;

/**
 * Enumeration for different server states during a game.
 *
 * @author Janine Grimmer
 */
public enum ServerState {
  RUNNING, //Der Server läuft wie gewünscht
  SHUTDOWN, // shutting server down
  PAUSED, // after pause request from human client
  ERROR, // if an error occurred
}
