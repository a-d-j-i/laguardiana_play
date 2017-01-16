package devices.device.event;

/**
 *
 */
public class DeviceEvent {

    public enum EventType {
        CANCEL,
        COLLECT,
        COUNT,
        ENVELOPE_DEPOSIT_START,
        PARSER_MESSAGE,
        PARSER_RESPONSE,
        OPEN_PORT,
        READ_TIMEOUT,
        STORE,
        WITHDRAW,
        RESET,
        STORING_ERROR_RESET,
        
    }
    final EventType type;

    public DeviceEvent(EventType type) {
        this.type = type;
    }

    public EventType getType() {
        return type;
    }

}
