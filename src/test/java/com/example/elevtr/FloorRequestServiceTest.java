package com.example.elevtr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.elevtr.service.FloorRequestService;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FloorRequestServiceTest — unit tests for FloorRequestService.
 *
 * UNIT TEST EXPLAINED:
 * --------------------
 * A unit test tests ONE class in complete isolation.
 * No Spring context is loaded — no @SpringBootTest annotation.
 * We just create the class directly with new and test its behaviour.
 * This makes tests fast — they run in milliseconds.
 *
 * WHY NO @SpringBootTest HERE?
 * -----------------------------
 * @SpringBootTest starts the ENTIRE Spring application context —
 * Tomcat, all beans, all config. That takes several seconds.
 * For a simple service class we do not need any of that.
 * We just need the class itself. Keep tests as lightweight as possible.
 *
 * JUNIT 5 ANNOTATIONS:
 * --------------------
 * @Test           — marks a method as a test case
 * @BeforeEach     — runs before EVERY test method — used to reset state
 * @DisplayName    — human readable test description shown in test reports
 *
 * ASSERTION STYLE — AssertJ vs JUnit:
 * -------------------------------------
 * JUnit 5 includes basic assertions (assertEquals, assertTrue etc.)
 * We use these here because they are built in and simple enough
 * for what we need. For more complex assertions AssertJ is available
 * via spring-boot-starter-test and reads more like plain English:
 *   assertThat(result).isEqualTo(5).isGreaterThan(3)
 */
class FloorRequestServiceTest {

    /**
     * The class under test.
     * Created fresh before each test in @BeforeEach so tests
     * never share state — one test's add() cannot affect another test.
     */
    private FloorRequestService floorRequestService;

    /**
     * @BeforeEach runs before every single @Test method.
     * Creates a fresh FloorRequestService so each test
     * starts with an empty pending requests set.
     *
     * Without this, a floor added in test 1 would still be
     * present when test 2 runs — making tests order-dependent
     * and unreliable.
     */
    @BeforeEach
    void setUp() {
        floorRequestService = new FloorRequestService();
    }

    // ----------------------------------------------------------------
    // addRequest tests
    // ----------------------------------------------------------------

    @Test
    @DisplayName("Adding a floor request should make it retrievable as pending")
    void addRequest_shouldAddFloorToPendingRequests() {
        // ARRANGE — nothing to set up, fresh service from @BeforeEach

        // ACT — perform the action being tested
        floorRequestService.addRequest(5);

        // ASSERT — verify the expected outcome
        assertTrue(floorRequestService.hasPendingRequests(5),
                "Floor 5 should be pending after being added");
    }

    @Test
    @DisplayName("Adding the same floor twice should not create duplicates")
    void addRequest_duplicateFloor_shouldNotCreateDuplicate() {
        // ARRANGE + ACT
        floorRequestService.addRequest(3);
        floorRequestService.addRequest(3);  // same floor again

        // ASSERT — Set deduplication means only one entry
        Set<Integer> pending = floorRequestService.getAllpendingRequests();
        assertEquals(1, pending.size(),
                "Duplicate floor request should not increase the set size");
    }

    @Test
    @DisplayName("Adding multiple different floors should track all of them")
    void addRequest_multipleFloors_shouldTrackAll() {
        // ARRANGE + ACT
        floorRequestService.addRequest(1);
        floorRequestService.addRequest(4);
        floorRequestService.addRequest(7);

        // ASSERT
        assertEquals(3, floorRequestService.getAllpendingRequests().size(),
                "All three floors should be tracked");
        assertTrue(floorRequestService.hasPendingRequests(1));
        assertTrue(floorRequestService.hasPendingRequests(4));
        assertTrue(floorRequestService.hasPendingRequests(7));
    }

    // ----------------------------------------------------------------
    // removeRequest tests
    // ----------------------------------------------------------------

    @Test
    @DisplayName("Removing a pending floor should clear it from the set")
    void removeRequest_shouldRemoveFloorFromPendingRequests() {
        // ARRANGE
        floorRequestService.addRequest(5);

        // ACT
        floorRequestService.removeRequest(5);

        // ASSERT
        assertFalse(floorRequestService.hasPendingRequests(5),
                "Floor 5 should no longer be pending after removal");
    }

    @Test
    @DisplayName("Removing a floor that was never requested should not throw")
    void removeRequest_floorNotPresent_shouldNotThrow() {
        // ARRANGE — floor 9 was never added

        // ACT + ASSERT — assertDoesNotThrow verifies no exception is thrown
        assertDoesNotThrow(() -> floorRequestService.removeRequest(9),
                "Removing a non-existent floor should be silently ignored");
    }

    @Test
    @DisplayName("Removing one floor should not affect other pending requests")
    void removeRequest_shouldOnlyRemoveSpecifiedFloor() {
        // ARRANGE
        floorRequestService.addRequest(2);
        floorRequestService.addRequest(6);

        // ACT — remove only floor 2
        floorRequestService.removeRequest(2);

        // ASSERT — floor 6 must still be pending
        assertFalse(floorRequestService.hasPendingRequests(2),
                "Floor 2 should be removed");
        assertTrue(floorRequestService.hasPendingRequests(6),
                "Floor 6 should still be pending");
    }

    // ----------------------------------------------------------------
    // hasPendingRequest tests
    // ----------------------------------------------------------------

    @Test
    @DisplayName("hasPendingRequest should return false for a floor never requested")
    void hasPendingRequest_floorNeverRequested_shouldReturnFalse() {
        assertFalse(floorRequestService.hasPendingRequests(5),
                "Floor 5 was never requested so should not be pending");
    }

    @Test
    @DisplayName("hasPendingRequest should return true after floor is added")
    void hasPendingRequest_afterAdd_shouldReturnTrue() {
        floorRequestService.addRequest(8);
        assertTrue(floorRequestService.hasPendingRequests(8));
    }

    // ----------------------------------------------------------------
    // isEmpty tests
    // ----------------------------------------------------------------

    @Test
    @DisplayName("isEmpty should return true when no requests are pending")
    void isEmpty_noRequests_shouldReturnTrue() {
        assertTrue(floorRequestService.isEmpty(),
                "Freshly created service should have no pending requests");
    }

    @Test
    @DisplayName("isEmpty should return false when requests are pending")
    void isEmpty_withRequests_shouldReturnFalse() {
        floorRequestService.addRequest(3);
        assertFalse(floorRequestService.isEmpty(),
                "Service should not be empty after a request is added");
    }

    @Test
    @DisplayName("isEmpty should return true after all requests are removed")
    void isEmpty_afterAllRequestsRemoved_shouldReturnTrue() {
        // ARRANGE
        floorRequestService.addRequest(1);
        floorRequestService.addRequest(2);

        // ACT
        floorRequestService.removeRequest(1);
        floorRequestService.removeRequest(2);

        // ASSERT
        assertTrue(floorRequestService.isEmpty(),
                "Service should be empty after all requests are removed");
    }

    // ----------------------------------------------------------------
    // getAllPendingRequests tests
    // ----------------------------------------------------------------

    @Test
    @DisplayName("getAllPendingRequests should return unmodifiable set")
    void getAllPendingRequests_shouldReturnUnmodifiableSet() {
        // ARRANGE
        floorRequestService.addRequest(4);

        // ACT
        Set<Integer> pending = floorRequestService.getAllpendingRequests();

        // ASSERT — trying to modify the returned set should throw
        assertThrows(UnsupportedOperationException.class,
                () -> pending.add(99),
                "Returned set should be unmodifiable");
    }
}