package Attachments;
import Core.Constants;

import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;

public class CompleteAttachment extends BaseAttachment {
    private ByteBuffer in;
    private ByteBuffer out;
    private SelectableChannel remoteChannel;

    public CompleteAttachment(KeyState state, boolean initBuffers) {
        super(state);
        if (initBuffers) {
            in = ByteBuffer.allocate(Constants.BUFFER_SIZE);
            out = ByteBuffer.allocate(Constants.BUFFER_SIZE);
        }
    }
    public void setRemoteChannel(SelectableChannel channel) {remoteChannel = channel;}
    public void setIn(ByteBuffer in) {this.in = in;}
    public void setOut(ByteBuffer out) {this.out = out;}
    public void setState(KeyState state) {this.state = state;}

    public SelectableChannel getRemoteChannel(){return remoteChannel;}
    public ByteBuffer getIn() {return in;}
    public ByteBuffer getOut() {return out;}
}
