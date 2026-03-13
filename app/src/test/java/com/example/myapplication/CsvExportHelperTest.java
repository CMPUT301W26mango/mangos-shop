package com.example.myapplication;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * US 02.06.05 - Tests for CsvExportHelper
 *
 * Tests the CSV generation logic against all acceptance criteria.
 * These are plain JUnit tests (not instrumented) because
 * generateCsvContent() and escapeCsv() are pure Java — no Android APIs.
 *
 */
public class CsvExportHelperTest {

    // =====================================================
    // CRITERIA #4 — Properly formatted with headers
    // =====================================================

    /**
     * Test that the CSV starts with the correct header row.
     */
    @Test
    public void testCsvHasHeaderRow() {
        List<EnrolledEntrant> entrants = new ArrayList<>();
        String csv = CsvExportHelper.generateCsvContent(entrants);

        String firstLine = csv.split("\n")[0];
        assertEquals("Header row should match expected format",
                "Name,Email,Phone,Enrolment Date", firstLine);
    }

    /**
     * Test that header row is present even with entrants.
     */
    @Test
    public void testHeaderRowAlwaysFirst() {
        List<EnrolledEntrant> entrants = new ArrayList<>();
        entrants.add(new EnrolledEntrant("Oakley", "oakley@test.com", "1234567890", "2025-01-15"));

        String csv = CsvExportHelper.generateCsvContent(entrants);
        assertTrue("CSV should start with the header row",
                csv.startsWith("Name,Email,Phone,Enrolment Date\n"));
    }

    // =====================================================
    // CRITERIA #5 — Works for 0 or more enrolled entrants
    // =====================================================

    /**
     * Test CSV output with zero entrants — should have header only.
     */
    @Test
    public void testEmptyListProducesHeaderOnly() {
        List<EnrolledEntrant> entrants = new ArrayList<>();
        String csv = CsvExportHelper.generateCsvContent(entrants);

        assertEquals("Empty list should produce header row only",
                "Name,Email,Phone,Enrolment Date\n", csv);
    }

    /**
     * Test CSV output with one entrant.
     */
    @Test
    public void testSingleEntrant() {
        List<EnrolledEntrant> entrants = new ArrayList<>();
        entrants.add(new EnrolledEntrant("Oakley", "oakley@test.com", "1234567890", "2025-01-15"));

        String csv = CsvExportHelper.generateCsvContent(entrants);
        String[] lines = csv.split("\n");

        assertEquals("Should have header + 1 data row", 2, lines.length);
        assertEquals("Data row should match entrant",
                "Oakley,oakley@test.com,1234567890,2025-01-15", lines[1]);
    }

    /**
     * Test CSV output with multiple entrants.
     */
    @Test
    public void testMultipleEntrants() {
        List<EnrolledEntrant> entrants = new ArrayList<>();
        entrants.add(new EnrolledEntrant("Oakley", "oakley@test.com", "1234567890", "2025-01-15"));
        entrants.add(new EnrolledEntrant("Santan Dave", "dave@test.com", "0987654321", "2025-01-16"));
        entrants.add(new EnrolledEntrant("Julia", "julia@test.com", "5555555555", "2025-01-17"));

        String csv = CsvExportHelper.generateCsvContent(entrants);
        String[] lines = csv.split("\n");

        assertEquals("Should have header + 3 data rows", 4, lines.length);
    }

    /**
     * Test that each entrant appears on its own line.
     */
    @Test
    public void testEachEntrantOnOwnLine() {
        List<EnrolledEntrant> entrants = new ArrayList<>();
        entrants.add(new EnrolledEntrant("Oakley", "oakley@test.com", "111", "2025-01-15"));
        entrants.add(new EnrolledEntrant("Julia", "julia@test.com", "222", "2025-01-16"));

        String csv = CsvExportHelper.generateCsvContent(entrants);
        String[] lines = csv.split("\n");

        assertEquals("Oakley,oakley@test.com,111,2025-01-15", lines[1]);
        assertEquals("Julia,julia@test.com,222,2025-01-16", lines[2]);
    }

    // =====================================================
    // CRITERIA #2 — CSV includes name, email, phone, date
    // =====================================================

    /**
     * Test that all four fields are present in the output.
     */
    @Test
    public void testAllFieldsPresent() {
        List<EnrolledEntrant> entrants = new ArrayList<>();
        entrants.add(new EnrolledEntrant("Chris", "chris@test.com", "9998887777", "2025-02-01"));

        String csv = CsvExportHelper.generateCsvContent(entrants);
        String dataLine = csv.split("\n")[1];

        assertTrue("Should contain name", dataLine.contains("Chris"));
        assertTrue("Should contain email", dataLine.contains("chris@test.com"));
        assertTrue("Should contain phone", dataLine.contains("9998887777"));
        assertTrue("Should contain date", dataLine.contains("2025-02-01"));
    }

    /**
     * Test that phone is optional — null phone produces empty field.
     */
    @Test
    public void testNullPhoneProducesEmptyField() {
        List<EnrolledEntrant> entrants = new ArrayList<>();
        entrants.add(new EnrolledEntrant("Oakley", "oakley@test.com", null, "2025-01-15"));

        String csv = CsvExportHelper.generateCsvContent(entrants);
        String dataLine = csv.split("\n")[1];

        // With null phone, we expect: Oakley,oakley@test.com,,2025-01-15
        assertEquals("Null phone should produce empty field between commas",
                "Oakley,oakley@test.com,,2025-01-15", dataLine);
    }

    /**
     * Test that empty string phone produces empty field.
     */
    @Test
    public void testEmptyPhoneProducesEmptyField() {
        List<EnrolledEntrant> entrants = new ArrayList<>();
        entrants.add(new EnrolledEntrant("Oakley", "oakley@test.com", "", "2025-01-15"));

        String csv = CsvExportHelper.generateCsvContent(entrants);
        String dataLine = csv.split("\n")[1];

        assertEquals("Empty phone should produce empty field",
                "Oakley,oakley@test.com,,2025-01-15", dataLine);
    }

    // =====================================================
    // CSV ESCAPING — Proper formatting edge cases
    // =====================================================

    /**
     * Test that commas in field values are handled correctly.
     * CSV standard: wrap in quotes.
     */
    @Test
    public void testEscapesCommasInValues() {
        List<EnrolledEntrant> entrants = new ArrayList<>();
        entrants.add(new EnrolledEntrant("Smith, John", "john@test.com", "111", "2025-01-15"));

        String csv = CsvExportHelper.generateCsvContent(entrants);
        String dataLine = csv.split("\n")[1];

        assertTrue("Name with comma should be wrapped in quotes",
                dataLine.startsWith("\"Smith, John\""));
    }

    /**
     * Test that quotes in field values are doubled (CSV standard).
     */
    @Test
    public void testEscapesQuotesInValues() {
        String result = CsvExportHelper.escapeCsv("He said \"hello\"");
        assertEquals("Quotes should be doubled and value wrapped",
                "\"He said \"\"hello\"\"\"", result);
    }

    /**
     * Test that normal values are not wrapped in quotes.
     */
    @Test
    public void testNormalValuesNotWrapped() {
        String result = CsvExportHelper.escapeCsv("Oakley");
        assertEquals("Normal value should not be wrapped",
                "Oakley", result);
    }

    /**
     * Test that null values return empty string.
     */
    @Test
    public void testNullReturnsEmptyString() {
        String result = CsvExportHelper.escapeCsv(null);
        assertEquals("Null should return empty string",
                "", result);
    }

    /**
     * Test that newlines in values are handled correctly.
     */
    @Test
    public void testEscapesNewlinesInValues() {
        String result = CsvExportHelper.escapeCsv("Line1\nLine2");
        assertEquals("Newlines should cause value to be wrapped in quotes",
                "\"Line1\nLine2\"", result);
    }

    // =====================================================
    // LARGE LIST — Performance sanity check
    // =====================================================

    /**
     * Test that a large list (100 entrants) produces correct row count.
     */
    @Test
    public void testLargeListRowCount() {
        List<EnrolledEntrant> entrants = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            entrants.add(new EnrolledEntrant(
                    "Entrant " + i,
                    "entrant" + i + "@test.com",
                    "555000" + String.format("%04d", i),
                    "2025-01-15"
            ));
        }

        String csv = CsvExportHelper.generateCsvContent(entrants);
        String[] lines = csv.split("\n");

        assertEquals("Should have header + 100 data rows",
                101, lines.length);
    }
}
