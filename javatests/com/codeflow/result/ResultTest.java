package com.codeflow.result;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

@DisplayName("Result Type Specification")
public class ResultTest {

    @Test
    @DisplayName("Ok should store and allow extraction of a value")
    public void testOkExtraction() {
        Result<Integer, String> ok = new Result.Ok<>(100);

        // Java 25 Pattern Matching for switch
        int value = switch (ok) {
            case Result.Ok(var v) -> v;
            case Result.Err(var e) -> fail("Expected Ok, got Err: " + e);
        };

        assertEquals(100, value);
    }

    @Test
    @DisplayName("Err should store and allow extraction of an error")
    public void testErrExtraction() {
        Result<Integer, String> err = new Result.Err<>("Timeout");

        // Java 25 Pattern Matching with instanceof
        if (err instanceof Result.Err(var msg)) {
            assertEquals("Timeout", msg);
        } else {
            fail("Expected Err type");
        }
    }

    @Test
    @DisplayName("Ok should throw NullPointerException if value is null")
    public void testOkNullCheck() {
        assertThrows(NullPointerException.class, () -> new Result.Ok<String, String>(null),
                "Ok record should not accept null values");
    }

    @Test
    @DisplayName("Err should throw NullPointerException if error is null")
    public void testErrNullCheck() {
        var exception = assertThrows(NullPointerException.class,
                () -> new Result.Err<String, String>(null));

        assertEquals("Err value cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Sealed interface should support exhaustive switch")
    public void testExhaustiveness() {
        Result<Boolean, Boolean> result = new Result.Ok<>(true);

        // This works without a 'default' case because the interface is sealed
        boolean check = switch (result) {
            case Result.Ok(var v) -> v;
            case Result.Err(var e) -> !e;
        };

        assertTrue(check);
    }
}