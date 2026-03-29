package com.example.myapplication;

/**
 * Unit tests for OrganizerEventAdapter.
 * The following test file was written with the guidance of Claude AI
 * Prompt: "Guide me with writing tests for Event" March 24, 2026
 */

import static org.junit.Assert.*;

import android.content.Context;
import android.widget.FrameLayout;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class OrganizerEventAdapterTest {

    private final OrganizerEventAdapter.OnEventClickListener noOpListener = event -> {};

    private Event makeEvent(String title, String location, int capacity) {
        Event e = new Event();
        e.setTitle(title);
        e.setLocation(location);
        e.setCapacity(capacity);
        return e;
    }

    @Test
    public void getItemCount_returnsCorrectSize() {
        List<Event> events = Arrays.asList(
                makeEvent("Event A", "Room 1", 50),
                makeEvent("Event B", "Room 2", 100)
        );
        OrganizerEventAdapter adapter = new OrganizerEventAdapter(events, noOpListener);
        assertEquals(2, adapter.getItemCount());
    }

    @Test
    public void getItemCount_returnsZeroForEmptyList() {
        OrganizerEventAdapter adapter = new OrganizerEventAdapter(new ArrayList<>(), noOpListener);
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void getItemCount_returnsZeroForNullList() {
        OrganizerEventAdapter adapter = new OrganizerEventAdapter(null, noOpListener);
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void getItemCount_singleItem() {
        OrganizerEventAdapter adapter = new OrganizerEventAdapter(
                Arrays.asList(makeEvent("Solo Event", "Hall A", 200)), noOpListener
        );
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void onBindViewHolder_setsTitle() {
        OrganizerEventAdapter adapter = new OrganizerEventAdapter(
                Arrays.asList(makeEvent("Tech Talk", "Lab 3", 30)), noOpListener
        );
        Context ctx = ApplicationProvider.getApplicationContext();
        OrganizerEventAdapter.EventViewHolder holder =
                adapter.onCreateViewHolder(new FrameLayout(ctx), 0);
        adapter.onBindViewHolder(holder, 0);
        assertEquals("Tech Talk", holder.titleText.getText().toString());
    }

    @Test
    public void onBindViewHolder_setsLocation() {
        OrganizerEventAdapter adapter = new OrganizerEventAdapter(
                Arrays.asList(makeEvent("Tech Talk", "Lab 3", 30)), noOpListener
        );
        Context ctx = ApplicationProvider.getApplicationContext();
        OrganizerEventAdapter.EventViewHolder holder =
                adapter.onCreateViewHolder(new FrameLayout(ctx), 0);
        adapter.onBindViewHolder(holder, 0);
        assertEquals("Location: Lab 3", holder.locationText.getText().toString());
    }

    @Test
    public void onBindViewHolder_setsCapacity() {
        OrganizerEventAdapter adapter = new OrganizerEventAdapter(
                Arrays.asList(makeEvent("Tech Talk", "Lab 3", 30)), noOpListener
        );
        Context ctx = ApplicationProvider.getApplicationContext();
        OrganizerEventAdapter.EventViewHolder holder =
                adapter.onCreateViewHolder(new FrameLayout(ctx), 0);
        adapter.onBindViewHolder(holder, 0);
        assertEquals("Capacity: 30", holder.capacityText.getText().toString());
    }

    @Test
    public void onBindViewHolder_secondItemHasCorrectData() {
        List<Event> events = Arrays.asList(
                makeEvent("First Event", "Room A", 10),
                makeEvent("Second Event", "Room B", 99)
        );
        OrganizerEventAdapter adapter = new OrganizerEventAdapter(events, noOpListener);
        Context ctx = ApplicationProvider.getApplicationContext();
        OrganizerEventAdapter.EventViewHolder holder =
                adapter.onCreateViewHolder(new FrameLayout(ctx), 0);
        adapter.onBindViewHolder(holder, 1);
        assertEquals("Second Event", holder.titleText.getText().toString());
        assertEquals("Location: Room B", holder.locationText.getText().toString());
        assertEquals("Capacity: 99", holder.capacityText.getText().toString());
    }

    @Test
    public void clickListener_isTriggeredOnItemClick() {
        Event targetEvent = makeEvent("Clickable Event", "Room X", 20);
        final Event[] clickedEvent = {null};

        OrganizerEventAdapter adapter = new OrganizerEventAdapter(
                Arrays.asList(targetEvent), event -> clickedEvent[0] = event
        );
        Context ctx = ApplicationProvider.getApplicationContext();
        OrganizerEventAdapter.EventViewHolder holder =
                adapter.onCreateViewHolder(new FrameLayout(ctx), 0);
        adapter.onBindViewHolder(holder, 0);
        holder.itemView.performClick();

        assertEquals(targetEvent, clickedEvent[0]);
    }
}