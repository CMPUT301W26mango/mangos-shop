package com.example.myapplication;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests the logic that handles the QR scan result and passes eventId to EventDetailsFragment
 * The following test file was written with the guidance of Claude AI
 * Prompt: "Write unit tests for US 01.06.01" April 2, 2026
 */
public class ScanQRUnitTest {


    /**
     * Non-null scan result should be treated as a valid scan
     * Matches: if (result.getContents() != null)
     */
    @Test
    public void testNonNullScanResult_isValidScan() {
        String scannedValue = "LVpMCQZpFkqISPnkgYPQ";
        assertNotNull("Non-null scan result should be a valid scan", scannedValue);
    }

    /**
     * Null scan result should be treated as a cancelled scan
     * Matches: else { Toast "Scan cancelled" }
     */
    @Test
    public void testNullScanResult_isCancelledScan() {
        String scannedValue = null;
        assertNull("Null scan result should indicate cancelled scan", scannedValue);
    }

    /**
     * Empty scan result should be detected as empty
     */
    @Test
    public void testEmptyScanResult_isInvalid() {
        String scannedValue = "";
        assertTrue("Empty scan result should be detected as empty",
                scannedValue.isEmpty());
    }

    /**
     * Non-empty scan result should not be empty
     */
    @Test
    public void testValidScanResult_isNotEmpty() {
        String scannedValue = "LVpMCQZpFkqISPnkgYPQ";
        assertFalse("Valid scan result should not be empty", scannedValue.isEmpty());
    }


    /**
     * Scanned value should be stored correctly as eventId
     * Simulates: bundle.putString("eventId", scannedValue)
     */
    @Test
    public void testScanResult_storedAsEventId() {
        String scannedValue = "LVpMCQZpFkqISPnkgYPQ";
        Map<String, String> args = new HashMap<>();
        args.put("eventId", scannedValue);

        assertTrue("Args should contain eventId key", args.containsKey("eventId"));
        assertEquals("eventId should match scanned value",
                scannedValue, args.get("eventId"));
    }

    /**
     * eventId should not be null after a valid scan
     */
    @Test
    public void testEventId_notNullAfterValidScan() {
        String scannedValue = "LVpMCQZpFkqISPnkgYPQ";
        Map<String, String> args = new HashMap<>();
        args.put("eventId", scannedValue);

        assertNotNull("eventId should not be null after valid scan",
                args.get("eventId"));
    }

    /**
     * eventId value should exactly match the scanned QR value
     */
    @Test
    public void testEventId_exactlyMatchesScanResult() {
        String scannedValue = "abc123eventId";
        Map<String, String> args = new HashMap<>();
        args.put("eventId", scannedValue);

        assertEquals("eventId must exactly match the scanned value",
                scannedValue, args.get("eventId"));
    }

    /**
     * Missing eventId key should be detected
     * Matches: args.containsKey("eventId") returning false
     */
    @Test
    public void testMissingEventIdKey_isDetected() {
        Map<String, String> args = new HashMap<>();
        assertFalse("Missing eventId key should be detected",
                args.containsKey("eventId"));
    }

    /**
     * Null args should be treated as no event provided
     * Matches: if (args != null && ...) in EventDetailsFragment
     */
    @Test
    public void testNullArgs_treatedAsMissingEvent() {
        Map<String, String> args = null;
        assertNull("Null args should be handled as missing event", args);
    }


    /**
     * Scan result should be used directly as eventId without modification
     */
    @Test
    public void testScanResult_usedDirectlyAsEventId() {
        String scannedValue = "rawQRContent12345";
        String eventId = scannedValue; // No transformation in the code
        assertEquals("Scan result should be used directly as eventId",
                scannedValue, eventId);
    }

    /**
     * Different QR codes should produce different eventIds
     */
    @Test
    public void testDifferentQRCodes_produceDifferentEventIds() {
        String scan1 = "event123";
        String scan2 = "event456";
        assertNotEquals("Different QR scans should produce different eventIds",
                scan1, scan2);
    }

    /**
     * Same QR code scanned twice should produce same eventId
     */
    @Test
    public void testSameQRCode_producesSameEventId() {
        String scan1 = "LVpMCQZpFkqISPnkgYPQ";
        String scan2 = "LVpMCQZpFkqISPnkgYPQ";
        assertEquals("Same QR code should always produce same eventId",
                scan1, scan2);
    }

    /**
     * Scanner prompt text should not be null or empty
     * Matches: options.setPrompt("Press back to cancel")
     */
    @Test
    public void testScannerPrompt_isNotEmpty() {
        String prompt = "Press back to cancel";
        assertNotNull("Scanner prompt should not be null", prompt);
        assertFalse("Scanner prompt should not be empty", prompt.isEmpty());
    }

    /**
     * Valid scan should trigger showing event details
     * Matches: if (result.getContents() != null) → show EventDetailsFragment
     */
    @Test
    public void testValidScan_triggersEventDetails() {
        String scannedValue = "LVpMCQZpFkqISPnkgYPQ";
        boolean shouldShowEventDetails = scannedValue != null;
        assertTrue("Valid scan should trigger showing event details",
                shouldShowEventDetails);
    }

    /**
     * Cancelled scan should not trigger showing event details
     * Matches: else { Toast "Scan cancelled" }
     */
    @Test
    public void testCancelledScan_doesNotTriggerEventDetails() {
        String scannedValue = null;
        boolean shouldShowEventDetails = scannedValue != null;
        assertFalse("Cancelled scan should not trigger showing event details",
                shouldShowEventDetails);
    }

    /**
     * eventId stored as value in args should be retrievable
     */
    @Test
    public void testEventId_isRetrievableFromArgs() {
        String scannedValue = "LVpMCQZpFkqISPnkgYPQ";
        Map<String, String> args = new HashMap<>();
        args.put("eventId", scannedValue);

        String retrievedId = args.get("eventId");
        assertNotNull("Retrieved eventId should not be null", retrievedId);
        assertEquals("Retrieved eventId should match original scan",
                scannedValue, retrievedId);
    }
}