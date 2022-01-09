package data;

public enum ChannelState {
    INIT_REQUEST,
    INIT_RESPONSE_SUCCESS,
    INIT_RESPONSE_FAILED,
    CONNECTION_REQUEST,
    CONNECTION_RESPONSE_SUCCESS,
    CONNECTION_RESPONSE_FAILED,
    DATA_TRANSFER,
    NONE}
