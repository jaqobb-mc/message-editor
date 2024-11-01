package dev.jaqobb.message_editor.message;

import java.util.Objects;

public class MessageEditInfo {
    
    private final String messageBefore;
    private final MessagePlace messagePlace;
    
    public MessageEditInfo(String messageBefore, MessagePlace place) {
        this.messageBefore = messageBefore;
        this.messagePlace = place;
    }
    
    public String getMessageBefore() {
        return this.messageBefore;
    }
    
    public MessagePlace getMessagePlace() {
        return this.messagePlace;
    }
    
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        MessageEditInfo that = (MessageEditInfo) object;
        return Objects.equals(this.messageBefore, that.messageBefore) && this.messagePlace == that.messagePlace;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(this.messageBefore, this.messagePlace);
    }
}
