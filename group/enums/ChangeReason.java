package enums;

/**
 * Enumeration for possible map change reasons.
 * Needed in MAP_CHANGE_DEMAND message.
 *
 * @author Janine Grimmer
 * * */
public enum ChangeReason {
    SANDSTORM,
    ROUND_PHASE,
    ENDGAME,
    FAMILY_ATOMICS,
    SPICE_PICKUP,
    HELI_CRASH
}
