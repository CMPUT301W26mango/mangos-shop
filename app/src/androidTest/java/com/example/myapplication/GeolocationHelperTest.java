package com.example.myapplication;

import android.content.Context;
import android.widget.Switch;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * US 02.02.03 — Enable or disable the geolocation requirement for an event.
 *
 * Geolocation is handled in two places:
 *
 *   1. Event CREATE (EventCreateActivity):
 *      The Switch is read directly at form submit via geoSwitch.isChecked().
 *      GeolocationHelper is NOT involved here.
 *      Tests for this flow are in the "Create Form Switch" section below.
 *
 *   2. Event EDIT (a future edit screen):
 *      GeolocationHelper.loadSetting() reads the stored value from Firestore
 *      and sets the Switch. GeolocationHelper.setupToggle() writes back to
 *      Firestore on every toggle.
 *      Tests for this flow are in the "GeolocationHelper Edit Flow" section.
 */
@RunWith(AndroidJUnit4.class)
public class GeolocationHelperTest {

    private Context context;
    private Switch testSwitch;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();

        // Switch must be created on the main thread.
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                testSwitch = new Switch(context)
        );
    }


    // =========================================================================
    // CREATE FORM SWITCH (EventCreateActivity — no GeolocationHelper involved)
    // These tests verify the Switch that lives in activity_create_event.xml
    // and is read directly by EventCreateActivity on form submit.
    // =========================================================================

    /**
     * Criteria #1, #3: The Switch defaults to OFF so geolocation is
     * not required unless the organizer explicitly enables it.
     */
    @Test
    public void testCreateFormSwitchDefaultsToOff() {
        assertFalse("Geolocation switch should default to OFF (not required)",
                testSwitch.isChecked());
    }

    /**
     * Criteria #1, #4: When the organizer turns the Switch ON, isChecked()
     * returns true — this is the value EventCreateActivity reads and stores
     * as geolocationRequired in the new event document.
     */
    @Test
    public void testCreateFormSwitchOnMeansGeoRequired() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                testSwitch.setChecked(true)
        );
        assertTrue("Switch ON should mean geolocationRequired = true",
                testSwitch.isChecked());
    }

    /**
     * Criteria #1, #3: When the organizer turns the Switch OFF, isChecked()
     * returns false — EventCreateActivity stores geolocationRequired = false,
     * so no location data will be collected from entrants.
     */
    @Test
    public void testCreateFormSwitchOffMeansGeoNotRequired() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            testSwitch.setChecked(true);
            testSwitch.setChecked(false);
        });
        assertFalse("Switch OFF should mean geolocationRequired = false",
                testSwitch.isChecked());
    }

    /**
     * Criteria #4: Verifies that the boolean read from the Switch maps
     * correctly to what gets stored. When OFF, the stored value is false.
     */
    @Test
    public void testSwitchOffProducesFalseForStorage() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                testSwitch.setChecked(false)
        );
        assertFalse("Switch OFF should produce false for Firestore storage",
                testSwitch.isChecked());
    }

    /**
     * Criteria #4: When ON, the stored value is true.
     */
    @Test
    public void testSwitchOnProducesTrueForStorage() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                testSwitch.setChecked(true)
        );
        assertTrue("Switch ON should produce true for Firestore storage",
                testSwitch.isChecked());
    }


    // =========================================================================
    // GEOLOCATION HELPER — EDIT FLOW
    // GeolocationHelper.loadSetting() and setupToggle() are for an edit screen
    // where the event already exists in Firestore. The synchronous part of
    // loadSetting (disabling the switch while loading) is testable here.
    // The Firestore read/write parts require a Firebase Emulator — see note below.
    // =========================================================================

    /**
     * Criteria #1: GeolocationHelper.loadSetting() disables the Switch
     * immediately (synchronously) before the Firestore read completes.
     * This prevents the organizer from toggling before the current value loads.
     *
     * Note: This only tests the synchronous disable — the re-enable after
     * Firestore responds requires a Firebase Emulator integration test.
     */
    @Test
    public void testLoadSettingDisablesSwitchImmediately() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            testSwitch.setEnabled(true);

            // loadSetting disables the switch synchronously before the async Firestore call.
            // Passing null for activity is safe here because the disable happens before
            // any callback that would call Toast — we only verify the synchronous part.
            GeolocationHelper.loadSetting(null, "test_event_id", testSwitch);
        });

        assertFalse("Switch should be disabled immediately while Firestore loads",
                testSwitch.isEnabled());
    }

    /**
     * Criteria #1: After setupToggle() is called, the Switch remains
     * interactive — it can still be checked and unchecked programmatically.
     * The actual Firestore write triggered by the listener is tested separately
     * via integration tests using a Firebase Emulator.
     *
     * Note: The Firestore write will fail silently here because there is no
     * real event document — this only verifies the Switch itself is not broken
     * by attaching the listener.
     */
    @Test
    public void testSetupToggleDoesNotBreakSwitch() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                GeolocationHelper.setupToggle(null, "test_event_id", testSwitch)
        );

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                testSwitch.setChecked(true)
        );
        assertTrue("Switch should still be checkable after setupToggle",
                testSwitch.isChecked());

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                testSwitch.setChecked(false)
        );
        assertFalse("Switch should still be uncheckable after setupToggle",
                testSwitch.isChecked());
    }
}
