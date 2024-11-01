package dev.jaqobb.message_editor.message;

public final class MessageData {
    
    private final String id;
    private final MessagePlace messagePlace;
    private final String message;
    private final boolean json;
    
    public MessageData(String id, MessagePlace messagePlace, String message, boolean json) {
        this.id = id;
        this.messagePlace = messagePlace;
        this.message = message;
        this.json = json;
    }
    
    public String getId() {
        return this.id;
    }
    
    public MessagePlace getMessagePlace() {
        return this.messagePlace;
    }
    
    public String getMessage() {
        return this.message;
    }
    
    public boolean isJson() {
        return this.json;
    }
}
