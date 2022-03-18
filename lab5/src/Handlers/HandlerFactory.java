package Handlers;

import Attachments.BaseAttachment;

import java.nio.channels.SelectionKey;

// getting key handler instance depending on its state
// all handlers instances are singleton, so keys with the same state are handled by single handler

public class HandlerFactory {
    private static AcceptHandler acceptHandler;
    private static InitRequestHandler initRequestHandler;
    private static InitResponseHandler initResponseHandler;

    public static Handler getHandler(SelectionKey key) {
        BaseAttachment attachment = (BaseAttachment) key.attachment();
        Handler result = null;

        switch (attachment.getState()) {
            case ACCEPT:
                if (acceptHandler == null) {
                    acceptHandler = new AcceptHandler();
                }
                result = acceptHandler;
                break;
            case INIT_REQUEST:
                if (initRequestHandler == null) {
                    initRequestHandler = new InitRequestHandler();
                }
                result = initRequestHandler;
                break;
            case INIT_RESPONSE_FAILED:
            case INIT_RESPONSE_SUCCESS:
                if (initResponseHandler == null) {
                    initResponseHandler = new InitResponseHandler();
                }
                result = initResponseHandler;
            default:
                assert false;
        }
        return result;
    }
}
