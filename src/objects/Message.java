package objects;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

public class Message implements Serializable {

	private static final long serialVersionUID = 1L;
	private String message;
	private String person;
	private ArrayList<String> clients;
	private MessageType mType;
	private byte[] fileData; // File data
	private String fileName; // File name

	// Constructor cho tin nhắn văn bản
	public Message() {
		this.mType = MessageType.REQUEST_CLIENT_LIST;
	}

	public Message(String message, String person) {
		this.mType = MessageType.CLIENT_PRIVATE_MESSAGE;
		this.message = message;
		this.person = person;
	}

	public Message(String message, MessageType mType) {
		this.mType = mType;
		this.message = message;
	}

	public Message(ArrayList<String> clients) {
		this.mType = MessageType.SEND_CLIENT_LIST;
		this.clients = clients;
	}

	public Message(String message, String person, MessageType mType) {
		this.mType = mType;
		this.message = message;
		this.person = person;
	}

	public Message(String person, byte[] fileData, String fileName) {
		this.mType = MessageType.CLIENT_FILE_TRANSFER; // Update the message type
		this.person = person;
		this.fileData = fileData;
		this.fileName = fileName;
	}


	public String getMessage() {
		return message;
	}

	public String getPerson() {
		return person;
	}

	public ArrayList<String> getClients() {
		return clients;
	}

	public MessageType getmType() {
		return mType;
	}

	public byte[] getFileData() {
		return fileData;
	}

	public String getFileName() {
		return fileName;
	}
}
