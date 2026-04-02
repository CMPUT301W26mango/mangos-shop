package com.example.myapplication;

import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CommentTest {

    private Comment comment;
    private Timestamp mockTimestamp;

    /**
     * Sets up a fresh Comment object before every test to ensure test isolation.
     * This test was creating with the help of Google LLM "Gemini"
     * Prompt: Given the following getter and setter, recommend appropriate tests to test its functionalitys
     *  * Timestamp getter and setter and its functionality *
     */
    @Before
    public void setUp() {
        // Create a mock timestamp to use in our tests
        mockTimestamp = new Timestamp(new Date());
        comment = new Comment("Initial text", "InitialUser", "DeviceABC", mockTimestamp);
    }

    @Test
    public void testEmptyConstructor() {
        Comment emptyComment = new Comment();

        // Assert that all fields are null when using the empty constructor
        assertNull(emptyComment.getCommentText());
        assertNull(emptyComment.getUserName());
        assertNull(emptyComment.getDeviceId());
        assertNull(emptyComment.getTimestamp());
        assertNull(emptyComment.getCommentId());
    }

    @Test
    public void testParameterizedConstructor() {
        // Assert that the fields match the values passed in the setUp() method
        assertEquals("Initial text", comment.getCommentText());
        assertEquals("InitialUser", comment.getUserName());
        assertEquals("DeviceABC", comment.getDeviceId());
        assertEquals(mockTimestamp, comment.getTimestamp());

        // The parameterized constructor doesn't set the commentId, so it should be null
        assertNull(comment.getCommentId());
    }

    @Test
    public void testGetAndSetCommentText() {
        comment.setCommentText("Updated text");
        assertEquals("Updated text", comment.getCommentText());
    }

    @Test
    public void testGetAndSetUserName() {
        comment.setUserName("UpdatedUser");
        assertEquals("UpdatedUser", comment.getUserName());
    }

    @Test
    public void testGetAndSetDeviceId() {
        comment.setDeviceId("DeviceXYZ");
        assertEquals("DeviceXYZ", comment.getDeviceId());
    }

    @Test
    public void testGetAndSetTimestamp() {
        // Create a new distinct timestamp (e.g., exactly 1000 seconds after epoch)
        Timestamp newTimestamp = new Timestamp(1000, 0);
        comment.setTimestamp(newTimestamp);

        assertEquals(newTimestamp, comment.getTimestamp());
    }

    @Test
    public void testGetAndSetCommentId() {
        comment.setCommentId("CommentID_12345");
        assertEquals("CommentID_12345", comment.getCommentId());
    }
}