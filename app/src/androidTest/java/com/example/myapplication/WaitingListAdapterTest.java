package com.example.myapplication;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * US 02.02.01 - Tests for WaitingListAdapter
 *
 * Tests that the adapter correctly:
 * - Reports the right item count (criteria #3)
 * - Binds entrant names to rows (criteria #2)
 * - Handles empty lists
 * - Reflects data changes (criteria #4 — adapter side)
 */
@RunWith(AndroidJUnit4.class)
public class WaitingListAdapterTest {

    private List<String> dummyNames;
    private WaitingListAdapter adapter;
    private RecyclerView recyclerView;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();

        // Dummy data simulating entrants on the waiting list
        dummyNames = new ArrayList<>();
        dummyNames.add("Oakley");
        dummyNames.add("Santan Dave");
        dummyNames.add("Julia");
        dummyNames.add("Chris");

        adapter = new WaitingListAdapter(dummyNames);

        // Create a RecyclerView and attach the adapter so ViewHolders can be created
        recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
    }

    // =====================================================
    // CRITERIA #3 — Count matches total entrant count
    // =====================================================

    /**
     * Test that getItemCount returns the correct number of entrants.
     */
    @Test
    public void testItemCountMatchesList() {
        assertEquals("Item count should match the number of entrants",
                4, adapter.getItemCount());
    }

    /**
     * Test that an empty list returns a count of 0.
     */
    @Test
    public void testEmptyListReturnsZeroCount() {
        List<String> emptyList = new ArrayList<>();
        WaitingListAdapter emptyAdapter = new WaitingListAdapter(emptyList);
        assertEquals("Empty list should return 0 count",
                0, emptyAdapter.getItemCount());
    }

    /**
     * Test that a single entrant returns a count of 1.
     */
    @Test
    public void testSingleEntrantCount() {
        List<String> singleList = new ArrayList<>();
        singleList.add("Solo Entrant");
        WaitingListAdapter singleAdapter = new WaitingListAdapter(singleList);
        assertEquals("Single entrant should return count of 1",
                1, singleAdapter.getItemCount());
    }

    // =====================================================
    // CRITERIA #2 — List shows entrant name
    // =====================================================

    /**
     * Test that the first entrant's name is correctly bound to the ViewHolder.
     */
    @Test
    public void testFirstEntrantNameBinding() {
        // Force the RecyclerView to layout so ViewHolders are created
        recyclerView.measure(
                View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.EXACTLY)
        );
        recyclerView.layout(0, 0, 1080, 1920);

        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(0);
        assertNotNull("ViewHolder at position 0 should exist", holder);

        TextView nameView = holder.itemView.findViewById(R.id.tvEntrantName);
        assertNotNull("tvEntrantName should exist in the item layout", nameView);
        assertEquals("First entrant name should be Oakley",
                "Oakley", nameView.getText().toString());
    }

    /**
     * Test that the second entrant's name is correctly bound.
     */
    @Test
    public void testSecondEntrantNameBinding() {
        recyclerView.measure(
                View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.EXACTLY)
        );
        recyclerView.layout(0, 0, 1080, 1920);

        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(1);
        assertNotNull("ViewHolder at position 1 should exist", holder);

        TextView nameView = holder.itemView.findViewById(R.id.tvEntrantName);
        assertEquals("Second entrant name should be Santan Dave",
                "Santan Dave", nameView.getText().toString());
    }

    /**
     * Test that the last entrant's name is correctly bound.
     */
    @Test
    public void testLastEntrantNameBinding() {
        recyclerView.measure(
                View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.EXACTLY)
        );
        recyclerView.layout(0, 0, 1080, 1920);

        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(3);
        assertNotNull("ViewHolder at position 3 should exist", holder);

        TextView nameView = holder.itemView.findViewById(R.id.tvEntrantName);
        assertEquals("Last entrant name should be Chris",
                "Chris", nameView.getText().toString());
    }

    // =====================================================
    // CRITERIA #4 — List updates when entrants join or leave
    //               (adapter side — data change reflected)
    // =====================================================

    /**
     * Test that adding an entrant increases the count.
     * Simulates a new entrant joining the waiting list.
     */
    @Test
    public void testCountUpdatesWhenEntrantJoins() {
        assertEquals(4, adapter.getItemCount());

        dummyNames.add("New Entrant");
        adapter.notifyDataSetChanged();

        assertEquals("Count should increase to 5 after adding an entrant",
                5, adapter.getItemCount());
    }

    /**
     * Test that removing an entrant decreases the count.
     * Simulates an entrant leaving the waiting list.
     */
    @Test
    public void testCountUpdatesWhenEntrantLeaves() {
        assertEquals(4, adapter.getItemCount());

        dummyNames.remove("Julia");
        adapter.notifyDataSetChanged();

        assertEquals("Count should decrease to 3 after removing an entrant",
                3, adapter.getItemCount());
    }

    /**
     * Test that clearing all entrants results in zero count.
     * Simulates all entrants leaving.
     */
    @Test
    public void testClearAllEntrants() {
        dummyNames.clear();
        adapter.notifyDataSetChanged();

        assertEquals("Count should be 0 after clearing all entrants",
                0, adapter.getItemCount());
    }

    /**
     * Test that rebuilding the list (clear + re-add) works correctly.
     * This is exactly what happens when the Firestore snapshot listener fires:
     * the Activity clears the list and rebuilds it from the new snapshot.
     */
    @Test
    public void testRebuildListSimulatesSnapshotUpdate() {
        // Simulate what the snapshot listener does: clear and rebuild
        dummyNames.clear();
        dummyNames.add("Alpha");
        dummyNames.add("Bravo");
        dummyNames.add("Charlie");
        adapter.notifyDataSetChanged();

        assertEquals("After rebuild, count should be 3",
                3, adapter.getItemCount());

        // Force layout to verify binding
        recyclerView.measure(
                View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.EXACTLY)
        );
        recyclerView.layout(0, 0, 1080, 1920);

        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(0);
        assertNotNull(holder);
        TextView nameView = holder.itemView.findViewById(R.id.tvEntrantName);
        assertEquals("First name after rebuild should be Alpha",
                "Alpha", nameView.getText().toString());
    }
}