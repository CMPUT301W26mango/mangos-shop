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
This test file was generated using Claude AI
Prompt: Generate test cases for the WaitingListAdapater file.

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

    private List<EnrolledEntrant> dummyEntrants;
    private WaitingListAdapter adapter;
    private RecyclerView recyclerView;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();

        dummyEntrants = new ArrayList<>();
        dummyEntrants.add(new EnrolledEntrant("Oakley", "oakley@test.com", "111", "2025-01-15"));
        dummyEntrants.add(new EnrolledEntrant("Santan Dave", "dave@test.com", "222", "2025-01-16"));
        dummyEntrants.add(new EnrolledEntrant("Julia", "julia@test.com", null, "2025-01-17"));
        dummyEntrants.add(new EnrolledEntrant("Chris", "chris@test.com", "444", "2025-01-18"));

        adapter = new WaitingListAdapter(dummyEntrants);

        recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
    }

    // =====================================================
    // CRITERIA #3 — Count matches total entrant count
    // =====================================================

    @Test
    public void testItemCountMatchesList() {
        assertEquals("Item count should match the number of entrants",
                4, adapter.getItemCount());
    }

    @Test
    public void testEmptyListReturnsZeroCount() {
        List<EnrolledEntrant> emptyList = new ArrayList<>();
        WaitingListAdapter emptyAdapter = new WaitingListAdapter(emptyList);
        assertEquals("Empty list should return 0 count",
                0, emptyAdapter.getItemCount());
    }

    @Test
    public void testSingleEntrantCount() {
        List<EnrolledEntrant> singleList = new ArrayList<>();
        singleList.add(new EnrolledEntrant("Solo", "solo@test.com", null, "2025-01-15"));
        WaitingListAdapter singleAdapter = new WaitingListAdapter(singleList);
        assertEquals("Single entrant should return count of 1",
                1, singleAdapter.getItemCount());
    }

    // =====================================================
    // CRITERIA #2 — List shows entrant name
    // =====================================================

    @Test
    public void testFirstEntrantNameBinding() {
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

    /**
     * Test that only name is displayed — not email, phone, or date.
     */
    @Test
    public void testOnlyNameIsDisplayed() {
        recyclerView.measure(
                View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.EXACTLY)
        );
        recyclerView.layout(0, 0, 1080, 1920);

        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(0);
        assertNotNull(holder);

        TextView nameView = holder.itemView.findViewById(R.id.tvEntrantName);
        String displayed = nameView.getText().toString();

        assertEquals("Only name should be displayed", "Oakley", displayed);
        assertFalse("Email should not be displayed", displayed.contains("oakley@test.com"));
        assertFalse("Phone should not be displayed", displayed.contains("111"));
        assertFalse("Date should not be displayed", displayed.contains("2025-01-15"));
    }

    // =====================================================
    // CRITERIA #4 — List updates when entrants join or leave
    // =====================================================

    @Test
    public void testCountUpdatesWhenEntrantJoins() {
        assertEquals(4, adapter.getItemCount());

        dummyEntrants.add(new EnrolledEntrant("New", "new@test.com", null, "2025-01-20"));
        adapter.notifyDataSetChanged();

        assertEquals("Count should increase to 5 after adding an entrant",
                5, adapter.getItemCount());
    }

    @Test
    public void testCountUpdatesWhenEntrantLeaves() {
        assertEquals(4, adapter.getItemCount());

        dummyEntrants.remove(2); // Remove Julia
        adapter.notifyDataSetChanged();

        assertEquals("Count should decrease to 3 after removing an entrant",
                3, adapter.getItemCount());
    }

    @Test
    public void testClearAllEntrants() {
        dummyEntrants.clear();
        adapter.notifyDataSetChanged();

        assertEquals("Count should be 0 after clearing all entrants",
                0, adapter.getItemCount());
    }

    @Test
    public void testRebuildListSimulatesSnapshotUpdate() {
        dummyEntrants.clear();
        dummyEntrants.add(new EnrolledEntrant("Alpha", "alpha@test.com", "999", "2025-02-01"));
        dummyEntrants.add(new EnrolledEntrant("Bravo", "bravo@test.com", null, "2025-02-02"));
        dummyEntrants.add(new EnrolledEntrant("Charlie", "charlie@test.com", "777", "2025-02-03"));
        adapter.notifyDataSetChanged();

        assertEquals("After rebuild, count should be 3",
                3, adapter.getItemCount());

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
