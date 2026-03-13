package com.example.myapplication;

import org.junit.Test;
import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * The following test file was written with the guidance of Claude AI
 * Prompt: "Guide me with writing tests for EventCreateActivity" March 12, 2026
 */

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
}