package com.reliefbridge.notification.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.reliefbridge.auth.entity.Role;
import com.reliefbridge.auth.entity.User;
import com.reliefbridge.auth.repository.UserRepository;
import com.reliefbridge.notification.dto.NotificationResponse;
import com.reliefbridge.notification.entity.Notification;
import com.reliefbridge.notification.entity.NotificationType;
import com.reliefbridge.notification.event.ReliefBridgeNotificationEvent;
import com.reliefbridge.notification.repository.NotificationRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Notification sendNotification(
            User recipient,
            String title,
            String message,
            NotificationType type,
            String referenceType,
            Long referenceId) {

        if (recipient == null) {
            return null;
        }

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type != null ? type : NotificationType.SYSTEM);
        notification.setReferenceType(referenceType);
        notification.setReferenceId(referenceId);
        notification.setRead(false);

        return notificationRepository.save(notification);
    }

    @EventListener
    public void handleNotificationEvent(ReliefBridgeNotificationEvent event) {
        try {
            sendNotification(
                    event.getRecipient(),
                    event.getTitle(),
                    event.getMessage(),
                    event.getType(),
                    event.getReferenceType(),
                    event.getReferenceId()
            );
        } catch (Exception ex) {
            System.err.println("Failed to dispatch notification: " + ex.getMessage());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyRole(
            Role role,
            String title,
            String message,
            NotificationType type,
            String referenceType,
            Long referenceId) {

        List<User> users = userRepository.findAll().stream()
                .filter(u -> u.getRole() == role)
                .collect(Collectors.toList());

        for (User user : users) {
            try {
                sendNotification(user, title, message, type, referenceType, referenceId);
            } catch (Exception ex) {
                // Keep processing remaining recipients
            }
        }
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(User currentUser, Pageable pageable) {
        if (currentUser == null) {
            throw new AccessDeniedException("Authentication required.");
        }
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(currentUser.getId(), pageable)
                .map(NotificationResponse::new);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(User currentUser) {
        if (currentUser == null) {
            throw new AccessDeniedException("Authentication required.");
        }
        return notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(NotificationResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(User currentUser) {
        if (currentUser == null) {
            return 0;
        }
        return notificationRepository.countByRecipientIdAndIsReadFalse(currentUser.getId());
    }

    @Transactional
    public NotificationResponse markAsRead(Long notificationId, User currentUser) {
        if (currentUser == null) {
            throw new AccessDeniedException("Authentication required.");
        }

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with ID: " + notificationId));

        if (!notification.getRecipient().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to access this notification.");
        }

        notification.setRead(true);
        Notification updated = notificationRepository.save(notification);
        return new NotificationResponse(updated);
    }

    @Transactional
    public int markAllAsRead(User currentUser) {
        if (currentUser == null) {
            throw new AccessDeniedException("Authentication required.");
        }
        return notificationRepository.markAllAsReadForUser(currentUser.getId());
    }

    @Transactional
    public void deleteNotification(Long notificationId, User currentUser) {
        if (currentUser == null) {
            throw new AccessDeniedException("Authentication required.");
        }

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with ID: " + notificationId));

        if (!notification.getRecipient().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to delete this notification.");
        }

        notificationRepository.delete(notification);
    }
}
