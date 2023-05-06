package messages;

import enums.MessageType;

/**
 * The Message class, informs about the content of the Message object, as well as the application version.
 */
public class Message { 

    //Content type of the Message body
    public MessageType type;
    //Version of the game
    public String version;

    public Message(MessageType type, String version) {
        this.type = type;
        this.version = version;
    }
    public Message(){

    }

    /**
     * Returns type of message.
     * @return {@link MessageType}
     */
    public MessageType getType(){
        return type;
    }
}
