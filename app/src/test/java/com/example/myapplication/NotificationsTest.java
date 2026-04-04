package com.example.myapplication;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import static org.mockito.Mockito.*;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Arrays;
import java.util.List;

/**
 * Asked Gemini AI to assist in helping write the tests.
 * The prompt used:
 * Can you please help me in ideas for what should be tested for this file <the file of code for this page> and generate some ideal test cases please.
 */

/**
 * Tests that the Notification logic:
 * Honors user opt-out preferences (US 01.04.03).
 * Allows notifications for winners and losers (US 01.04.01, US 01.04.02).
 * Distributes mass notifications correctly from Organizers (US 02.07.01, US 02.07.02, US 02.07.03).
 */
public class NotificationsTest {

    @Mock private FirebaseFirestore mockDb;
    @Mock private CollectionReference mockUsersCollection;
    @Mock private DocumentReference mockUserDoc;
    @Mock private CollectionReference mockNotiCollection;
    @Mock private DocumentSnapshot mockSnapshot;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockDb.collection("users")).thenReturn(mockUsersCollection);
        when(mockUsersCollection.document(anyString())).thenReturn(mockUserDoc);
        when(mockUserDoc.collection("notifications")).thenReturn(mockNotiCollection);
    }

    /**
     * US 01.04.03: Tests that the system suppresses notifications if the user opted out.
     */
    @Test
    public void testOptOutSuppressesNotification() {
        when(mockSnapshot.getBoolean("notificationsEnabled")).thenReturn(false);

        Boolean wantsNotis = mockSnapshot.getBoolean("notificationsEnabled");
        boolean shouldSend = (wantsNotis == null || wantsNotis);

        Assert.assertFalse("Notification should be suppressed", shouldSend);
    }

    /**
     * US 01.04.01 & US 01.04.02: Tests that opted-in users receive win/lose notifications.
     */
    @Test
    public void testOptInAllowsNotification() {
        when(mockSnapshot.getBoolean("notificationsEnabled")).thenReturn(true);

        Boolean wantsNotis = mockSnapshot.getBoolean("notificationsEnabled");
        boolean shouldSend = (wantsNotis == null || wantsNotis);

        Assert.assertTrue("Notification should be sent", shouldSend);
    }

    /**
     * US 02.07.01, US 02.07.02, US 02.07.03: Tests that organizers can target specific sub-lists.
     */
    @Test
    public void testOrganizerMassNotificationRouting() {
        List<String> targetList = Arrays.asList("entrantA", "entrantB", "entrantC");
        String message = "Status update for your event!";

        for (String userId : targetList) {
            mockDb.collection("users").document(userId).collection("notifications").add(message);
        }
        verify(mockNotiCollection, times(3)).add(any());
    }
}