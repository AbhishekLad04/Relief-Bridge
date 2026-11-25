package com.reliefbridge.notification.event;

import com.reliefbridge.auth.entity.User;
import com.reliefbridge.notification.entity.NotificationType;

public class ReliefBridgeNotificationEvent {

    private final User recipient;
    private final String title;
    private final String message;
    private final NotificationType type;
    private final String referenceType;
    private final Long referenceId;

    public ReliefBridgeNotificationEvent(
            User recipient,
            String title,
            String message,
            NotificationType type,
            String referenceType,
            Long referenceId) {
        this.recipient = recipient;
        this.title = title;
        this.message = message;
        this.type = type;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
    }

    public User getRecipient() {
        return recipient;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public NotificationType getType() {
        return type;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public Long getReferenceId() {
        return referenceId;
    }
}
