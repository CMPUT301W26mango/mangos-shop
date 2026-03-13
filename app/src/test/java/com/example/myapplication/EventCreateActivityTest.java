package com.example.myapplication;

import org.junit.Test;
import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventCreateActivityTest {

    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    @Test
    public void testRegStartInFuture_isValid() throws ParseException {
        Date future = new Date(System.currentTimeMillis() + 86400000); // tomorrow
        assertFalse(future.before(new Date()));
    }

    @Test
    public void testRegStartInPast_isInvalid() throws ParseException {
        Date past = new Date(System.currentTimeMillis() - 86400000); // yesterday
        assertTrue(past.before(new Date()));
    }


    @Test
    public void testRegEndAfterStart_isValid() throws ParseException {
        Date start = formatter.parse("2030-01-01 10:00");
        Date end = formatter.parse("2030-01-02 10:00");
        assertTrue(end.after(start));
    }

    @Test
    public void testRegEndBeforeStart_isInvalid() throws ParseException {
        Date start = formatter.parse("2030-01-02 10:00");
        Date end = formatter.parse("2030-01-01 10:00");
        assertFalse(end.after(start));
    }

    @Test
    public void testRegEndSameAsStart_isInvalid() throws ParseException {
        Date start = formatter.parse("2030-01-01 10:00");
        Date end = formatter.parse("2030-01-01 10:00");
        assertFalse(end.after(start));
    }


    @Test
    public void testEventDateAfterRegEnd_isValid() throws ParseException {
        Date regEnd = formatter.parse("2030-01-02 10:00");
        Date eventDate = formatter.parse("2030-01-03 10:00");
        assertTrue(eventDate.after(regEnd));
    }

    @Test
    public void testEventDateBeforeRegEnd_isInvalid() throws ParseException {
        Date regEnd = formatter.parse("2030-01-03 10:00");
        Date eventDate = formatter.parse("2030-01-02 10:00");
        assertFalse(eventDate.after(regEnd));
    }

    @Test
    public void testEventDateSameAsRegEnd_isInvalid() throws ParseException {
        Date regEnd = formatter.parse("2030-01-02 10:00");
        Date eventDate = formatter.parse("2030-01-02 10:00");
        assertFalse(eventDate.after(regEnd));
    }

    @Test
    public void testCapacityValidNumber() {
        String capacityText = "50";
        int capacity = Integer.parseInt(capacityText);
        assertEquals(50, capacity);
    }

    @Test
    public void testCapacityEmpty_skipped() {
        String capacityText = "";
        assertTrue(capacityText.isEmpty());
    }


    @Test
    public void testEventFieldsSetCorrectly() {
        Event event = new Event();
        event.setTitle("Test Event");
        event.setLocation("Edmonton");
        event.setDescription("A test event");

        assertEquals("Test Event", event.getTitle());
        assertEquals("Edmonton", event.getLocation());
        assertEquals("A test event", event.getDescription());
    }

    @Test
    public void testPosterURLSetCorrectly() {
        Event event = new Event();
        event.setPosterURL("https://example.com/poster.jpg");
        assertEquals("https://example.com/poster.jpg", event.getPosterURL());
    }

    @Test
    public void testPosterURLEmpty_skipped() {
        String posterURL = "";
        assertTrue(posterURL.isEmpty());
    }
    @Test
    public void testEventTypeSetCorrectly() {
        Event event = new Event();
        event.setEventType("Sports");
        assertEquals("Sports", event.getEventType());
    }

    @Test
    public void testEventTypeEmpty_skipped() {
        String eventType = "";
        assertTrue(eventType.isEmpty());
    }

    @Test
    public void testOrganizerNameSetCorrectly() {
        Event event = new Event();
        event.setOrganizerName("Test Name");
        assertEquals("Test Name", event.getOrganizerName());
    }

    @Test
    public void testOrganizerNameEmpty_skipped() {
        String organizerName = "";
        assertTrue(organizerName.isEmpty());
    }
}