package com.example.myapplication;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.google.firebase.Timestamp;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * The following test file was written with the guidance of Gemini AI
 * Prompt: "Guide me with writing tests for Event" April 2, 2026
 * Extended with additional coverage for all fields and edge cases.
 */
public class EventsTest {

    private Event event;

    @Before
    public void setUp() {
        event = new Event();
    }

    // -------------------------------------------------------------------------
    // Constructor tests
    // -------------------------------------------------------------------------

    @Test
    public void testEmptyConstructor() {
        Event emptyEvent = new Event();
        assertNull("ID should be null on empty initialization", emptyEvent.getId());
    }

    @Test
    public void testParameterizedConstructor() {
        Timestamp start = new Timestamp(new Date());
        Timestamp end = new Timestamp(new Date(System.currentTimeMillis() + 10000));
        List<String> coOrgs = Arrays.asList("org1", "org2");
        List<String> invited = Arrays.asList("user1", "user2");

        Event fullEvent = new Event("123", "Test Title", "Desc", "Loc",
                start, end, "url", "qr", 100, "2026-04-10",
                "Organizer", "Music", 50, "device123", true, coOrgs, invited);

        assertEquals("123", fullEvent.getId());
        assertEquals("Test Title", fullEvent.getTitle());
        assertEquals("Desc", fullEvent.getDescription());
        assertEquals("Loc", fullEvent.getLocation());
        assertEquals(start, fullEvent.getRegStart());
        assertEquals(end, fullEvent.getRegEnd());
        assertEquals("url", fullEvent.getPosterURL());
        assertEquals("qr", fullEvent.getQrValue());
        assertEquals(100, fullEvent.getCapacity());
        assertEquals("2026-04-10", fullEvent.getDateEvent());
        assertEquals("Organizer", fullEvent.getOrganizerName());
        assertEquals("Music", fullEvent.getEventType());
        assertEquals(50, fullEvent.getMaxWaitingListSize());
        assertEquals("device123", fullEvent.getDeviceId());
        assertTrue(fullEvent.getPrivateEvent());
        assertEquals(2, fullEvent.getCoOrganizers().size());
        assertEquals(2, fullEvent.getInvitedUsers().size());
    }

    @Test
    public void testParameterizedConstructor_PrivateEventFalse() {
        Event e = new Event("1", "T", "D", "L", null, null,
                "url", "qr", 10, "2026-01-01",
                "Org", "Sports", 5, "dev1", false, null, null);
        assertFalse("privateEvent should be false", e.getPrivateEvent());
    }

    // -------------------------------------------------------------------------
    // Getter / Setter tests
    // -------------------------------------------------------------------------

    @Test
    public void testSetAndGetId() {
        event.setId("abc123");
        assertEquals("abc123", event.getId());
    }

    @Test
    public void testSetAndGetTitle() {
        event.setTitle("Mango Festival");
        assertEquals("Mango Festival", event.getTitle());
    }

    @Test
    public void testSetAndGetDescription() {
        event.setDescription("A great event about mangoes");
        assertEquals("A great event about mangoes", event.getDescription());
    }

    @Test
    public void testSetAndGetLocation() {
        event.setLocation("Edmonton, AB");
        assertEquals("Edmonton, AB", event.getLocation());
    }

    @Test
    public void testSetAndGetPosterURL() {
        event.setPosterURL("https://example.com/poster.png");
        assertEquals("https://example.com/poster.png", event.getPosterURL());
    }

    @Test
    public void testSetAndGetQrValue() {
        event.setQrValue("QR_XYZ_789");
        assertEquals("QR_XYZ_789", event.getQrValue());
    }

    @Test
    public void testSetAndGetCapacity() {
        event.setCapacity(500);
        assertEquals(500, event.getCapacity());
    }

    @Test
    public void testSetAndGetDateEvent() {
        event.setDateEvent("2026-12-25");
        assertEquals("2026-12-25", event.getDateEvent());
    }

    @Test
    public void testSetAndGetOrganizerName() {
        event.setOrganizerName("Sayuj");
        assertEquals("Sayuj", event.getOrganizerName());
    }

    @Test
    public void testSetAndGetEventType() {
        event.setEventType("Workshop");
        assertEquals("Workshop", event.getEventType());
    }

    @Test
    public void testSetAndGetMaxWaitingListSize() {
        event.setMaxWaitingListSize(25);
        assertEquals(25, event.getMaxWaitingListSize());
    }

    @Test
    public void testSetAndGetDeviceId() {
        event.setDeviceId("device-999");
        assertEquals("device-999", event.getDeviceId());
    }

    @Test
    public void testSetAndGetPrivateEvent_True() {
        event.setPrivateEvent(true);
        assertTrue(event.getPrivateEvent());
    }

    @Test
    public void testSetAndGetPrivateEvent_False() {
        event.setPrivateEvent(false);
        assertFalse(event.getPrivateEvent());
    }

    @Test
    public void testSetAndGetGeolocationRequired_True() {
        event.setGeolocationRequired(true);
        assertTrue("geolocationRequired should be true", event.getGeolocationRequired());
    }

    @Test
    public void testSetAndGetGeolocationRequired_False() {
        event.setGeolocationRequired(false);
        assertFalse("geolocationRequired should be false", event.getGeolocationRequired());
    }

    @Test
    public void testSetAndGetRegStart() {
        Timestamp ts = new Timestamp(new Date());
        event.setRegStart(ts);
        assertEquals(ts, event.getRegStart());
    }

    @Test
    public void testSetAndGetRegEnd() {
        Timestamp ts = new Timestamp(new Date());
        event.setRegEnd(ts);
        assertEquals(ts, event.getRegEnd());
    }

    @Test
    public void testInvitedUsersSetterAndGetter() {
        List<String> invited = Arrays.asList("Sayuj", "Sami", "Aditya");
        event.setInvitedUsers(invited);

        assertNotNull(event.getInvitedUsers());
        assertEquals(3, event.getInvitedUsers().size());
        assertTrue(event.getInvitedUsers().contains("Sami"));
    }

    @Test
    public void testCoOrganizersSetterAndGetter() {
        List<String> coOrgs = Arrays.asList("orgA", "orgB");
        event.setCoOrganizers(coOrgs);

        assertNotNull(event.getCoOrganizers());
        assertEquals(2, event.getCoOrganizers().size());
        assertTrue(event.getCoOrganizers().contains("orgA"));
    }

    // -------------------------------------------------------------------------
    // isRegistrationOpen() tests
    // -------------------------------------------------------------------------

    @Test
    public void testIsRegistrationOpen_WhenOpen() {
        Timestamp pastStart = new Timestamp(new Date(System.currentTimeMillis() - 3600000));
        Timestamp futureEnd = new Timestamp(new Date(System.currentTimeMillis() + 3600000));

        event.setRegStart(pastStart);
        event.setRegEnd(futureEnd);

        assertTrue("Registration should be open", event.isRegistrationOpen());
    }

    @Test
    public void testIsRegistrationOpen_WhenNotStartedYet() {
        Timestamp futureStart = new Timestamp(new Date(System.currentTimeMillis() + 3600000));
        Timestamp futureEnd = new Timestamp(new Date(System.currentTimeMillis() + 7200000));

        event.setRegStart(futureStart);
        event.setRegEnd(futureEnd);

        assertFalse("Registration should be closed (hasn't started)", event.isRegistrationOpen());
    }

    @Test
    public void testIsRegistrationOpen_WhenAlreadyClosed() {
        Timestamp pastStart = new Timestamp(new Date(System.currentTimeMillis() - 7200000));
        Timestamp pastEnd = new Timestamp(new Date(System.currentTimeMillis() - 3600000));

        event.setRegStart(pastStart);
        event.setRegEnd(pastEnd);

        assertFalse("Registration should be closed (already ended)", event.isRegistrationOpen());
    }

    @Test
    public void testIsRegistrationOpen_WithNullDates() {
        event.setRegStart(null);
        event.setRegEnd(null);
        assertFalse("Registration should default to closed if dates are missing", event.isRegistrationOpen());
    }

    @Test
    public void testIsRegistrationOpen_WithNullRegStart() {
        Timestamp futureEnd = new Timestamp(new Date(System.currentTimeMillis() + 3600000));
        event.setRegStart(null);
        event.setRegEnd(futureEnd);
        assertFalse("Registration should be closed if regStart is null", event.isRegistrationOpen());
    }

    @Test
    public void testIsRegistrationOpen_WithNullRegEnd() {
        Timestamp pastStart = new Timestamp(new Date(System.currentTimeMillis() - 3600000));
        event.setRegStart(pastStart);
        event.setRegEnd(null);
        assertFalse("Registration should be closed if regEnd is null", event.isRegistrationOpen());
    }

    @Test
    public void testIsRegistrationOpen_AtExactEndTime() {
        // regEnd set to ~1 second in the future to simulate "right at closing"
        // The boundary condition is: currentTime.compareTo(regEnd) <= 0 means still open AT end time
        Timestamp pastStart = new Timestamp(new Date(System.currentTimeMillis() - 3600000));
        Timestamp nearFutureEnd = new Timestamp(new Date(System.currentTimeMillis() + 1000));

        event.setRegStart(pastStart);
        event.setRegEnd(nearFutureEnd);

        assertTrue("Registration should still be open right at the end boundary", event.isRegistrationOpen());
    }
}