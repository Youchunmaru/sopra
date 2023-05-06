package messages.exceptions;

import enums.MessageType;
import messages.Message;

/**
 * errorCodes:
 * 000: no Error
 * 001: Bad format.
 * 002: isActive and isCpu do not match.
 * 003: Already two players registered.
 * 004: Unknown clientSecret.
 * 005: An unknown error occured.
 */
public class ErrorMessage extends Message {
    public int errorCode;
    public String errorDescription;

    /**
     *
     * @param version
     * @param errorCode
     * @param errorDescription
     */
    public ErrorMessage(String version, int errorCode, String errorDescription){
        this.type = MessageType.ERROR;
        this.version = version;
        this.errorCode=errorCode;
        this.errorDescription=errorDescription;
    }
}
