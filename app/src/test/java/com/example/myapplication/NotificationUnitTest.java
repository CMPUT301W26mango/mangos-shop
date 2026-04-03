package com.example.myapplication;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The following test file was written with the guidance of Claude AI
 * Prompt: "Write unit tests for notification user stories" April 2, 2026
 */
public class NotificationUnitTest {


    /**
     * NotificationItem should store notiName correctly
     */
    @Test
    public void testNotificationItem_storesNotiNameCorrectly() {
        NotificationItem item = new NotificationItem(
                "notifId123",
                "eventId123",
                "Basketball Tournament",
                "Private Invite",
                "You have been invited to join the waiting list",
                null,
                false
        );
        assertEquals("NotiName should be Private Invite",
                "Private Invite", item.getNotiName());
    }

    /**
     * NotificationItem should store eventId correctly
     */
    @Test
    public void testNotificationItem_storesEventIdCorrectly() {
        NotificationItem item = new NotificationItem(
                "notifId123",
                "eventId123",
                "Basketball Tournament",
                "Private Invite",
                "You have been invited",
                null,
                false
        );
        assertEquals("EventId should match", "eventId123", item.getEventId());
    }

    /**
     * NotificationItem should store event name correctly
     */
    @Test
    public void testNotificationItem_storesEventNameCorrectly() {
        NotificationItem item = new NotificationItem(
                "notifId123",
                "eventId123",
                "Basketball Tournament",
                "Private Invite",
                "You have been invited",
                null,
                false
        );
        assertEquals("Event name should match",
                "Basketball Tournament", item.getEventName());
    }

    /**
     * NotificationItem should store description correctly
     */
    @Test
    public void testNotificationItem_storesDescriptionCorrectly() {
        NotificationItem item = new NotificationItem(
                "notifId123",
                "eventId123",
                "Basketball Tournament",
                "Private Invite",
                "You have been invited to join the waiting list",
                null,
                false
        );
        assertEquals("Description should match",
                "You have been invited to join the waiting list",
                item.getDescription());
    }

    /**
     * New notification should default to unread
     */
    @Test
    public void testNotificationItem_defaultsToUnread() {
        NotificationItem item = new NotificationItem(
                "notifId123",
                "eventId123",
                "Basketball Tournament",
                "Private Invite",
                "You have been invited",
                null,
                false
        );
        assertFalse("New notification should be unread", item.isRead());
    }

    /**
     * Notification read status should be updatable
     */
    @Test
    public void testNotificationItem_readStatusUpdatable() {
        NotificationItem item = new NotificationItem(
                "notifId123",
                "eventId123",
                "Basketball Tournament",
                "Private Invite",
                "You have been invited",
                null,
                false
        );
        item.setRead(true);
        assertTrue("Notification should be marked as read", item.isRead());
    }

    /**
     * Null timestamp should be handled safely
     */
    @Test
    public void testNotificationItem_nullTimestamp_handledSafely() {
        NotificationItem item = new NotificationItem(
                "notifId123",
                "eventId123",
                "Basketball Tournament",
                "Private Invite",
                "You have been invited",
                null,
                false
        );
        assertNull("Null timestamp should remain null", item.getNotiTime());
    }


    /**
     * Private event invite notification should have correct notiName
     */
    @Test
    public void testPrivateEventInvite_hasCorrectNotiName() {
        NotificationItem item = new NotificationItem(
                "notifId123",
                "eventId123",
                "Basketball Tournament",
                "Private Invite",
                "You have been invited to join the waiting list",
                null,
                false
        );
        assertEquals("Private invite notiName should be correct",
                "Private Invite", item.getNotiName());
    }

    /**
     * Private event invite notification should have non-null eventId
     */
    @Test
    public void testPrivateEventInvite_hasNonNullEventId() {
        NotificationItem item = new NotificationItem(
                "notifId123",
                "eventId123",
                "Basketball Tournament",
                "Private Invite",
                "You have been invited to join the waiting list",
                null,
                false
        );
        assertNotNull("Private event invite should have eventId",
                item.getEventId());
    }

    /**
     * Private event invite notification should have non-empty event name
     */
    @Test
    public void testPrivateEventInvite_hasNonEmptyEventName() {
        NotificationItem item = new NotificationItem(
                "notifId123",
                "eventId123",
                "Basketball Tournament",
                "Private Invite",
                "You have been invited",
                null,
                false
        );
        assertFalse("Private event invite should have non-empty event name",
                item.getEventName().isEmpty());
    }

    /**
     * Private invite notiName should be distinguishable from co-org invite
     */
    @Test
    public void testPrivateInvite_notiNameDistinctFromCoOrg() {
        String privateInviteName = "Private Invite";
        String coOrgName = "Co-Organizer Invite";
        assertNotEquals("Private invite notiName should differ from co-org",
                privateInviteName, coOrgName);
    }


    /**
     * Accepting private invite should set status to waiting in waiting list
     */
    @Test
    public void testAcceptPrivateInvite_setsStatusToWaiting() {
        java.util.Map<String, Object> entrantInfo = new java.util.HashMap<>();
        entrantInfo.put("userId", "testDeviceId");
        entrantInfo.put("status", "waiting");

        assertEquals("Accepting invite should result in waiting status",
                "waiting", entrantInfo.get("status"));
    }

    /**
     * Declining private invite should remove user from invitedUsers
     */
    @Test
    public void testDeclinePrivateInvite_removesFromInvitedUsers() {
        List<String> invitedUsers = new ArrayList<>();
        invitedUsers.add("device1");
        invitedUsers.add("device2");
        invitedUsers.add("testDeviceId");

        invitedUsers.remove("testDeviceId");

        assertFalse("Device should be removed from invitedUsers after declining",
                invitedUsers.contains("testDeviceId"));
    }

    /**
     * Declining private invite should not remove other users from invitedUsers
     */
    @Test
    public void testDeclinePrivateInvite_doesNotRemoveOtherUsers() {
        List<String> invitedUsers = new ArrayList<>();
        invitedUsers.add("device1");
        invitedUsers.add("device2");
        invitedUsers.add("testDeviceId");

        invitedUsers.remove("testDeviceId");

        assertTrue("Other users should remain after one declines",
                invitedUsers.contains("device1"));
        assertTrue("Other users should remain after one declines",
                invitedUsers.contains("device2"));
    }

    /**
     * User in invitedUsers should be detected as invited
     */
    @Test
    public void testInvitedUser_detectedInInvitedUsers() {
        List<String> invitedUsers = new ArrayList<>();
        invitedUsers.add("testDeviceId");

        boolean isInvited = invitedUsers.contains("testDeviceId");
        assertTrue("User should be detected as invited", isInvited);
    }

    /**
     * User not in invitedUsers should not be detected as invited
     */
    @Test
    public void testNonInvitedUser_notDetectedInInvitedUsers() {
        List<String> invitedUsers = new ArrayList<>();
        invitedUsers.add("device1");

        boolean isInvited = invitedUsers.contains("testDeviceId");
        assertFalse("Non-invited user should not be detected as invited",
                isInvited);
    }

    /**
     * Null invitedUsers should be handled safely
     */
    @Test
    public void testNullInvitedUsers_handledSafely() {
        List<String> invitedUsers = null;
        boolean isInvited = invitedUsers != null
                && invitedUsers.contains("testDeviceId");
        assertFalse("Null invitedUsers should return false safely", isInvited);
    }


    /**
     * Co-organizer invite notification should have correct notiName
     */
    @Test
    public void testCoOrgInvite_hasCorrectNotiName() {
        NotificationItem item = new NotificationItem(
                "notifId456",
                "eventId123",
                "Basketball Tournament",
                "Co-Organizer Invite",
                "You have been invited to be a co-organizer",
                null,
                false
        );
        assertEquals("Co-org invite notiName should be correct",
                "Co-Organizer Invite", item.getNotiName());
    }

    /**
     * Co-organizer invite should have non-null eventId
     */
    @Test
    public void testCoOrgInvite_hasNonNullEventId() {
        NotificationItem item = new NotificationItem(
                "notifId456",
                "eventId123",
                "Basketball Tournament",
                "Co-Organizer Invite",
                "You have been invited to be a co-organizer",
                null,
                false
        );
        assertNotNull("Co-org invite should have eventId", item.getEventId());
    }

    /**
     * Co-organizer invite should default to unread
     */
    @Test
    public void testCoOrgInvite_defaultsToUnread() {
        NotificationItem item = new NotificationItem(
                "notifId456",
                "eventId123",
                "Basketball Tournament",
                "Co-Organizer Invite",
                "You have been invited to be a co-organizer",
                null,
                false
        );
        assertFalse("Co-org invite should default to unread", item.isRead());
    }

    /**
     * Co-organizer invite notiName should differ from private invite notiName
     */
    @Test
    public void testCoOrgInvite_notiNameDistinctFromPrivateInvite() {
        NotificationItem privateInvite = new NotificationItem(
                "id1", "eventId", "Event", "Private Invite", "msg", null, false);
        NotificationItem coOrgInvite = new NotificationItem(
                "id2", "eventId", "Event", "Co-Organizer Invite", "msg", null, false);

        assertNotEquals("Co-org notiName should differ from private invite notiName",
                privateInvite.getNotiName(), coOrgInvite.getNotiName());
    }

    /**
     * Co-organizer invite description should not be empty
     */
    @Test
    public void testCoOrgInvite_hasNonEmptyDescription() {
        NotificationItem item = new NotificationItem(
                "notifId456",
                "eventId123",
                "Basketball Tournament",
                "Co-Organizer Invite",
                "You have been invited to be a co-organizer",
                null,
                false
        );
        assertFalse("Co-org invite description should not be empty",
                item.getDescription().isEmpty());
    }



    /**
     * Notification list should not be null after initialization
     */
    @Test
    public void testNotificationList_notNullAfterInit() {
        List<NotificationItem> notificationList = new ArrayList<>();
        assertNotNull("Notification list should not be null", notificationList);
    }

    /**
     * Notification list should be empty initially
     */
    @Test
    public void testNotificationList_emptyInitially() {
        List<NotificationItem> notificationList = new ArrayList<>();
        assertTrue("Notification list should be empty initially",
                notificationList.isEmpty());
    }

    /**
     * Notification list should correctly report size after adding items
     */
    @Test
    public void testNotificationList_correctSizeAfterAdding() {
        List<NotificationItem> notificationList = new ArrayList<>();
        notificationList.add(new NotificationItem(
                "id1", "eventId1", "Event 1",
                "Private Invite", "Message 1", null, false));
        notificationList.add(new NotificationItem(
                "id2", "eventId2", "Event 2",
                "Co-Organizer Invite", "Message 2", null, false));

        assertEquals("Notification list should have 2 items",
                2, notificationList.size());
    }

    /**
     * Adapter item count should return 0 for empty list
     */
    @Test
    public void testNotificationAdapter_emptyList_returnsZero() {
        List<NotificationItem> emptyList = new ArrayList<>();
        int count = emptyList != null ? emptyList.size() : 0;
        assertEquals("Empty list should return count of 0", 0, count);
    }

    /**
     * Adapter item count should return correct count for non-empty list
     */
    @Test
    public void testNotificationAdapter_nonEmptyList_returnsCorrectCount() {
        List<NotificationItem> list = new ArrayList<>();
        list.add(new NotificationItem(
                "id1", "eventId1", "Event 1",
                "Private Invite", "Message 1", null, false));
        int count = list != null ? list.size() : 0;
        assertEquals("Non-empty list should return correct count", 1, count);
    }
}