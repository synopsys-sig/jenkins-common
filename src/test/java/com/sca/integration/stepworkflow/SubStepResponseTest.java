package com.sca.integration.stepworkflow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SubStepResponseTest {

    private final String data = "Test Data";
    private final Exception exception = new RuntimeException("Test exception");

    @Test
    // Tests calling constructor with data: True, valid data, valid exception
    public void testConstructorNoNulls() {
        SubStepResponse<String> subStepResponse = new SubStepResponse<>(true, data, exception);

        assertTrue(subStepResponse.isSuccess());
        assertFalse(subStepResponse.isFailure());

        assertTrue(subStepResponse.hasData());
        assertEquals(data, subStepResponse.getData());

        assertTrue(subStepResponse.hasException());
        assertNotNull(subStepResponse.getException());
        assertEquals(exception, subStepResponse.getException());
        assertEquals(exception.getMessage(), subStepResponse.getException().getMessage());
    }

    @Test
    // Tests calling constructor with data: True, null, null
    public void testSuccessNoData() {
        SubStepResponse<Object> subStepResponse = SubStepResponse.SUCCESS();

        assertTrue(subStepResponse.isSuccess());
        assertFalse(subStepResponse.isFailure());

        assertFalse(subStepResponse.hasData());
        assertNull(subStepResponse.getData());

        assertFalse(subStepResponse.hasException());
        assertNull(subStepResponse.getException());
    }

    @Test
    // Tests calling constructor with data: True, valid data, null
    public void testSuccessWithData() {
        SubStepResponse<String> subStepResponse = SubStepResponse.SUCCESS(data);

        assertTrue(subStepResponse.isSuccess());
        assertFalse(subStepResponse.isFailure());

        assertTrue(subStepResponse.hasData());
        assertEquals(data, subStepResponse.getData());

        assertFalse(subStepResponse.hasException());
        assertNull(subStepResponse.getException());
    }

    @Test
    // Tests calling constructor with data: False, null, null
    public void testFailureNullException() {
        SubStepResponse<String> subStepResponseInput = SubStepResponse.SUCCESS(data);
        SubStepResponse<Object> subStepResponse = SubStepResponse.FAILURE(subStepResponseInput);

        assertFalse(subStepResponse.isSuccess());
        assertTrue(subStepResponse.isFailure());

        assertFalse(subStepResponse.hasData());
        assertNull(subStepResponse.getData());

        assertFalse(subStepResponse.hasException());
        assertNull(subStepResponse.getException());
    }

    @Test
    // Tests calling constructor with data: False, null, valid exception
    public void testFailureValidException() {
        SubStepResponse<String> subStepResponseInput = new SubStepResponse<>(true, data, exception);
        SubStepResponse<Object> subStepResponse = SubStepResponse.FAILURE(subStepResponseInput);

        assertFalse(subStepResponse.isSuccess());
        assertTrue(subStepResponse.isFailure());

        assertFalse(subStepResponse.hasData());
        assertNull(subStepResponse.getData());

        assertTrue(subStepResponse.hasException());
        assertNotNull(subStepResponse.getException());
        assertEquals(exception, subStepResponse.getException());
        assertEquals(exception.getMessage(), subStepResponse.getException().getMessage());
    }
}
