package com.example.myapplication;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.firebase.Timestamp;
import java.util.Date;

public class EventsTest {

    @Test
    public void testEventTitleSetCorrectly() {
        Event event = new Event();
        event.setTitle("Swimming Lessons");
        assertEquals("Swimming Lessons", event.getTitle());
    }

    @Test
    public void testEventDescriptionSetCorrectly() {
        Event event = new Event();
        event.setDescription("A fun swimming event");
        assertEquals("A fun swimming event", event.getDescription());
    }

    @Test
    public void testEventLocationSetCorrectly() {
        Event event = new Event();
        event.setLocation("Edmonton Rec Centre");
        assertEquals("Edmonton Rec Centre", event.getLocation());
    }

    @Test
    public void testEventCapacitySetCorrectly() {
        Event event = new Event();
        event.setCapacity(100);
        assertEquals(100, event.getCapacity());
    }

    @Test
    public void testEventOrganizerNameSetCorrectly() {
        Event event = new Event();
        event.setOrganizerName("John Doe");
        assertEquals("John Doe", event.getOrganizerName());
    }

    @Test
    public void testEventQrValueSetCorrectly() {
        Event event = new Event();
        event.setQrValue("abc123");
        assertEquals("abc123", event.getQrValue());
    }

    @Test
    public void testEventPosterURLSetCorrectly() {
        Event event = new Event();
        event.setPosterURL("https://example.com/poster.jpg");
        assertEquals("https://example.com/poster.jpg", event.getPosterURL());
    }

    @Test
    public void testEventRegStartSetCorrectly() {
        Timestamp timestamp = new Timestamp(new Date());
        Event event = new Event();
        event.setRegStart(timestamp);
        assertEquals(timestamp, event.getRegStart());
    }

    @Test
    public void testEventRegEndSetCorrectly() {
        Timestamp timestamp = new Timestamp(new Date());
        Event event = new Event();
        event.setRegEnd(timestamp);
        assertEquals(timestamp, event.getRegEnd());
    }

    @Test
    public void testEventRegEndAfterRegStart_isValid() {
        Timestamp start = new Timestamp(new Date(1000000));
        Timestamp end = new Timestamp(new Date(2000000));
        assertTrue(end.compareTo(start) > 0);
    }

    @Test
    public void testEventRegEndBeforeRegStart_isInvalid() {
        Timestamp start = new Timestamp(new Date(2000000));
        Timestamp end = new Timestamp(new Date(1000000));
        assertFalse(end.compareTo(start) > 0);
    }


    @Test
    public void testEventTitleNull_isEmpty() {
        Event event = new Event();
        assertNull(event.getTitle());
    }

    @Test
    public void testEventCapacityZero_isValid() {
        Event event = new Event();
        event.setCapacity(0);
        assertEquals(0, event.getCapacity());
    }

    @Test
    public void testFullEventObject() {
        Event event = new Event();
        event.setTitle("Test Event");
        event.setLocation("Edmonton");
        event.setDescription("Description");
        event.setCapacity(50);
        event.setOrganizerName("Jane");
        event.setQrValue("qr123");

        assertNotNull(event.getTitle());
        assertNotNull(event.getLocation());
        assertNotNull(event.getDescription());
        assertEquals(50, event.getCapacity());
        assertNotNull(event.getOrganizerName());
        assertNotNull(event.getQrValue());
    }

    @Test
    public void testGeolocationRequiredDefaultsFalse() {
        Event event = new Event();
        assertFalse(event.getGeolocationRequired());
    }

    @Test
    public void testGeolocationRequiredSetTrue() {
        Event event = new Event();
        event.setGeolocationRequired(true);
        assertTrue(event.getGeolocationRequired());
    }

    @Test
    public void testGeolocationRequiredSetFalse() {
        Event event = new Event();
        event.setGeolocationRequired(true);
        event.setGeolocationRequired(false);
        assertFalse(event.getGeolocationRequired());
    }
}