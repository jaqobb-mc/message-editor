package dev.jaqobb.message_editor.message;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SerializableAs("MessageEdit")
public final class MessageEdit implements ConfigurationSerializable {
    
    private final Pattern messageBeforePattern;
    private final MessagePlace messageBeforePlace;
    private final String messageAfter;
    private final MessagePlace messageAfterPlace;
    
    public MessageEdit(String messageBeforePattern, MessagePlace messageBeforePlace, String messageAfter, MessagePlace messageAfterPlace) {
        this.messageBeforePattern = Pattern.compile(messageBeforePattern);
        this.messageBeforePlace = messageBeforePlace;
        this.messageAfter = messageAfter;
        this.messageAfterPlace = messageAfterPlace;
    }
    
    public Pattern getMessageBeforePattern() {
        return this.messageBeforePattern;
    }
    
    public MessagePlace getMessageBeforePlace() {
        return this.messageBeforePlace;
    }
    
    public String getMessageBefore() {
        return this.messageBeforePattern.pattern();
    }
    
    public String getMessageAfter() {
        return this.messageAfter;
    }
    
    public MessagePlace getMessageAfterPlace() {
        return this.messageAfterPlace;
    }
    
    public Matcher getMatcher(String messageBefore) {
        Matcher matcher = this.messageBeforePattern.matcher(messageBefore);
        if (!matcher.matches()) {
            return null;
        }
        return matcher;
    }
    
    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> data = new LinkedHashMap<>(4, 1.0F);
        data.put("message-before-pattern", this.messageBeforePattern.pattern());
        if (this.messageBeforePlace != null) {
            data.put("message-before-place", this.messageBeforePlace.name());
        }
        data.put("message-after", this.messageAfter);
        if (this.messageAfterPlace != null) {
            data.put("message-after-place", this.messageAfterPlace.name());
        }
        return data;
    }
    
    public static MessageEdit deserialize(Map<String, Object> data) {
        String messageBeforePattern = (String) data.get("message-before-pattern");
        MessagePlace messageBeforePlace = null;
        if (data.containsKey("message-before-place")) {
            messageBeforePlace = MessagePlace.fromName((String) data.get("message-before-place"));
        }
        String messageAfter = (String) data.get("message-after");
        MessagePlace messageAfterPlace = null;
        if (data.containsKey("message-after-place")) {
            messageAfterPlace = MessagePlace.fromName((String) data.get("message-after-place"));
        }
        return new MessageEdit(messageBeforePattern, messageBeforePlace, messageAfter, messageAfterPlace);
    }
}
