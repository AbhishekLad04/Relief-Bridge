package com.reliefbridge.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.reliefbridge.auth.entity.Role;
import com.reliefbridge.auth.entity.User;
import com.reliefbridge.auth.repository.UserRepository;
import com.reliefbridge.notification.dto.NotificationResponse;
import com.reliefbridge.notification.entity.Notification;
import com.reliefbridge.notification.entity.NotificationType;
import com.reliefbridge.notification.repository.NotificationRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User recipient;
    private Notification notification;

    @BeforeEach
    void setUp() {
        recipient = new User();
        recipient.setId(15L);
        recipient.setName("Citizen User");
        recipient.setRole(Role.CITIZEN);

        notification = new Notification();
        notification.setId(1001L);
        notification.setRecipient(recipient);
        notification.setTitle("Disaster Alert");
        notification.setMessage("Cyclone warning in coastal district.");
        notification.setType(NotificationType.DISASTER);
        notification.setRead(false);
    }

    @Test
    void testSendNotification_Success() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> {
            Notification n = i.getArgument(0);
            n.setId(1002L);
            return n;
        });

        Notification saved = notificationService.sendNotification(
                recipient, "Donation Successful", "Receipt generated", NotificationType.DONATION, "DONATION", 50L
        );

        assertNotNull(saved);
        assertEquals(1002L, saved.getId());
        assertEquals("Donation Successful", saved.getTitle());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void testMarkAsRead_Success() {
        when(notificationRepository.findById(1001L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        NotificationResponse response = notificationService.markAsRead(1001L, recipient);

        assertNotNull(response);
        assertTrue(response.isRead());
    }

    @Test
    void testMarkAsRead_OtherUser_ThrowsAccessDenied() {
        User otherUser = new User();
        otherUser.setId(99L);

        when(notificationRepository.findById(1001L)).thenReturn(Optional.of(notification));

        assertThrows(AccessDeniedException.class, () -> notificationService.markAsRead(1001L, otherUser));
    }

    @Test
    void testMarkAllAsRead_Success() {
        when(notificationRepository.markAllAsReadForUser(15L)).thenReturn(5);

        int count = notificationService.markAllAsRead(recipient);

        assertEquals(5, count);
        verify(notificationRepository).markAllAsReadForUser(15L);
    }
}
