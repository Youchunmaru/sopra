package enums;

/**
 * Enumeration for different stages of a game. Sorted according to order of phases during a game.
 * Additionally added stages are paused, endgamephase, default, and setup.
 *
 * @author Janine Grimmer
 */
public enum GameState {
  DUNEPHASE,
  SANDSTORMPHASE,
  SANDWORMPHASE,
  CLONEPHASE,
  CHARACTERPHASE,
  ENDGAMEPHASE, // if mechanism for excessively long games is started
  ENDPHASE, // if one player wins
  DEFAULT,
  SETUP,
  PAUSED,
  ERROR,
  FAILED // if one player gets disconnected
}
