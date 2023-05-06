package messages.exceptions;

import enums.MessageType;
import messages.Message;

/**
 *  * 006: unhandled MessageType
 *  * 007: unhandled ActionEnum
 *  * 008: strikeCounter exceeded
 *  * 009: ???
 *  * 010: wrong sequence
 *  * 011: client not registered
 *  * 777: Strike
 *  * 111: names of registered clients
 */

public class DebugMessage extends Message {
    public final int code;
    public final String explanation;

    public DebugMessage(String version,  int code, String explanation) {
        this.type = MessageType.DEBUG;
        this.version = version;
        this.code = code;
        this.explanation = explanation;
    }
}
