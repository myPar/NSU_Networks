package Net;

import Protocol.Data;
import Protocol.Message;

import java.net.InetAddress;
import java.util.List;

// 'wrap' represents channel for I/O message communication
public interface ChannelProvider {
    void sendMulticastInitMessage(List<Data.GamePlayer> players, Data.GameConfig config, boolean can_join) throws Exception;
    void sendPingMessage(InetAddress ip, int port, long sender_id, long receiver_id) throws Exception;
    void sendAcceptMessage(InetAddress ip, int port, long sender_id, long receiver_id) throws Exception;
    void sendJoinMessage(InetAddress ip, int port, String name, boolean onlyView, Data.PlayerType type) throws Exception;

    // receiving multicast messages
    AddressedMessage getMulticastMessage() throws Exception;
    // get other messages method
    AddressedMessage getUnicastMessage() throws Exception;
}
