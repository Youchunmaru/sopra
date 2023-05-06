package enums;

/**
 * Enumeration for different message types. First argument in every message.
 * Sorted according to order in network standard document version 1.0.
 * Added MessageType need to be registered in Message and Requesthandler Lists in CommunicationHandler class
 * @author Janine Grimmer
 */
public enum MessageType {
  DEBUG,
  ERROR,
  JOIN,
  REJOIN,
  JOINACCEPTED,
  GAMECFG,
  HOUSE_OFFER,
  HOUSE_REQUEST,
  HOUSE_ACKNOWLEDGEMENT,
  TURN_DEMAND,
  MOVEMENT_REQUEST,
  HELI_REQUEST,
  ACTION_REQUEST,
  TRANSFER_REQUEST,
  MOVEMENT_DEMAND,
  HELI_DEMAND,
  ACTION_DEMAND,
  TRANSFER_DEMAND,
  CHARACTER_STAT_CHANGE_DEMAND,
  MAP_CHANGE_DEMAND,
  ATOMICS_UPDATE_DEMAND,
  SPAWN_CHARACTER_DEMAND,
  CHANGE_PLAYER_SPICE_DEMAND,
  SANDWORM_SPAWN_DEMAND,
  SANDWORM_MOVE_DEMAND,
  SANDWORM_DESPAWN_DEMAND,
  GAME_PAUSE_DEMAND,
  UNPAUSE_GAME_OFFER,
  END_TURN_REQUEST,
  ENDGAME,
  GAME_END,
  GAMESTATE_REQUEST, // attention: GAMESTATE_REQUEST and REQUEST_GAMESTATE in standard document
  REQUEST_GAMESTATE,
  GAMESTATE,
  STRIKE,
  PAUSE_REQUEST,
  TIMEOUT // for server communication only, indicates that one client has not reacted during one turn
}
