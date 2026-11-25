package com.reliefbridge.notification.dto;

import java.time.LocalDateTime;

import com.reliefbridge.notification.entity.Notification;
import com.reliefbridge.notification.entity.NotificationType;

public class NotificationResponse {

    private Long id;
    private Long recipientId;
    private String recipientName;
    private String title;
    private String message;
    private NotificationType type;
    private String referenceType;
    private Long referenceId;
    private boolean isRead;
    private LocalDateTime createdAt;

    public NotificationResponse() {
    }

    public NotificationResponse(Notification notification) {
        this.id = notification.getId();
        this.recipientId = notification.getRecipient() != null ? notification.getRecipient().getId() : null;
        this.recipientName = notification.getRecipient() != null ? notification.getRecipient().getName() : null;
        this.title = notification.getTitle();
        this.message = notification.getMessage();
        this.type = notification.getType();
        this.referenceType = notification.getReferenceType();
        this.referenceId = notification.getReferenceId();
        this.isRead = notification.isRead();
        this.createdAt = notification.getCreatedAt();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
