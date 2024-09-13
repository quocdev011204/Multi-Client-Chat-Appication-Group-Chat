/**
 *
 */

package objects;

/**
 * @author vaibhav
 *
 */
public enum MessageType {
	SEND_NAME,
	REQUEST_CLIENT_LIST,
	SERVER_PRIVATE_MESSAGE,
	SERVER_GLOBAL_MESSAGE,
	SEND_CLIENT_LIST,
	SEND_CLIENT_LIST_LEFT,
	CLIENT_PRIVATE_MESSAGE,
	CLIENT_GLOBAL_MESSAGE,
	CLIENT_FILE_TRANSFER, // Add new message type for file transfers
	SERVER_FILE_TRANSFER
}