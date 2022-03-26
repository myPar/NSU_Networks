package Handlers;

import Attachments.CompleteAttachment;
import Exceptions.HandlerException;
import Exceptions.HandlerException.Classes;
import Exceptions.HandlerException.Types;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public class CloseChannelHandler implements Handler {
    @Override
    public void handle(SelectionKey key) throws HandlerException {
        try {
            assert key != null;
            assert key.selector() != null;

            CompleteAttachment attachment = (CompleteAttachment) key.attachment();

            if (attachment.getRemoteChannel() != null) {
                SelectionKey remoteKey = CompleteAttachment.getRemoteChannelKey(key);
                CompleteAttachment remoteKeyAttachment = (CompleteAttachment) remoteKey.attachment();
                // remove this channel from remote channel's field
                remoteKeyAttachment.removeRemoteChannel();
            }
            // cancel key and close channel
            key.cancel();
            key.channel().close();
        }
        catch (IOException e) {
            throw new HandlerException(Classes.CLOSE_CHANNEL, "can't close the channel", Types.IO);
        }
    }
}
