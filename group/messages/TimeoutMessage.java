package messages;

import network.util.Client;

import static enums.MessageType.TIMEOUT;

/**
 * TimeoutMessage class to send a message if a client did not react to a message from the server in time,
 * especially during character phase.
 *
 * @author Janine Grimmer
 */
public class TimeoutMessage extends Message{

  private Client client;

  /**
   * Constructor for timeout message with type TIMEOUT.
   * Used internally only
   *
   * @param client {@link Client} who has not responded in time during character phase with a message
   */
  public TimeoutMessage(Client client) {
    this.type = TIMEOUT;
    this.client = client;
  }

  /**
   * Method is called when message content shall be printed on the console
   */
  @Override
  public String toString() {
    return "Message of type: " + type + ", clientID: " + client.getClientID();
  }


  /**
   * Used to get client from message.
   * @return {@link Client} who has not responded in time
   */
  public Client getClient() {
    return client;
  }

  /**
   * Used to set client who has not responded in time to server message.
   * @param client {@link Client}
   */
  public void setClient(Client client) {
    this.client = client;
  }
}
