package objects;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashSet;
import java.util.Set;

public class GroupChat {
    private MulticastSocket multicastSocket;
    private InetAddress group;
    private int port;
    private Set<String> members;

    public GroupChat(String groupName, int port) throws Exception {
        this.port = port;
        this.group = InetAddress.getByName(groupName);
        this.multicastSocket = new MulticastSocket(port);
        this.multicastSocket.joinGroup(group);
        this.members = new HashSet<>();
    }

    public void sendMessage(String message) {
        try {
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
            multicastSocket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void receiveMessages() {
        new Thread(() -> {
            try {
                byte[] buffer = new byte[256];
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    multicastSocket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength());
                    System.out.println("Received: " + message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void joinGroup(String member) {
        members.add(member);
    }

    public void leaveGroup(String member) {
        members.remove(member);
    }

    public void close() {
        try {
            multicastSocket.leaveGroup(group);
            multicastSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
