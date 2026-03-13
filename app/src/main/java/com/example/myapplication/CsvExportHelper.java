package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import java.io.OutputStream;
import java.util.List;

/**
 * US 02.06.05 — Export enrolled entrants list to CSV.
 *
 * Uses ACTION_CREATE_DOCUMENT so the user picks where to save.
 * No FileProvider, no manifest changes, no extra XML files needed.
 *
 */
public class CsvExportHelper {

    private static final String CSV_HEADER = "Name,Email,Phone,Enrolment Date";

    /**
     * Generates CSV content as a String.
     * This is the core logic — separated from file I/O so it can be unit tested.
     *
     * Criteria #4: Properly formatted with headers.
     * Criteria #5: Works for 0 or more enrolled entrants.
     * Criteria #2: Includes name, email, phone, enrolment date.
     *
     * @param entrants list of enrolled entrants (can be empty)
     * @return complete CSV string with header row and data rows
     */
    public static String generateCsvContent(List<EnrolledEntrant> entrants) {
        StringBuilder csv = new StringBuilder();

        // Header row (criteria #4)
        csv.append(CSV_HEADER).append("\n");

        // Data rows
        for (EnrolledEntrant entrant : entrants) {
            csv.append(escapeCsv(entrant.getName())).append(",");
            csv.append(escapeCsv(entrant.getEmail())).append(",");
            csv.append(escapeCsv(entrant.getPhone())).append(",");
            csv.append(escapeCsv(entrant.getEnrolmentDate())).append("\n");
        }

        return csv.toString();
    }

    /**
     * Escapes a single CSV field value.
     * - Null values become empty string
     * - Values containing commas, quotes, or newlines are wrapped in quotes
     * - Existing quotes are doubled (CSV standard)
     *
     * @param value the field value to escape
     * @return escaped CSV field
     */
    static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }

        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }

        return value;
    }

    /**
     * Creates an intent that opens Android's file picker for saving.
     * The user picks where to save the CSV file.
     *
     * Criteria #3: File downloads to device.
     *
     * @param eventName name of the event (used in the default filename)
     * @return intent to launch with startActivityForResult
     */
    public static Intent createExportIntent(String eventName) {
        String safeEventName = eventName.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        String fileName = "enrolled_" + safeEventName + ".csv";

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, fileName);

        return intent;
    }

    /**
     * Writes the CSV content to the URI the user chose in the file picker.
     * Call this from onActivityResult after the user picks a save location.
     *
     * @param context   the Activity context
     * @param uri       the URI returned by the file picker
     * @param entrants  list of enrolled entrants
     */
    public static void writeCsvToUri(Context context, Uri uri, List<EnrolledEntrant> entrants) {
        String csvContent = generateCsvContent(entrants);

        try {
            OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
            if (outputStream != null) {
                outputStream.write(csvContent.getBytes());
                outputStream.flush();
                outputStream.close();
                Toast.makeText(context, "CSV exported successfully.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, "Error exporting CSV.", Toast.LENGTH_SHORT).show();
        }
    }
}
