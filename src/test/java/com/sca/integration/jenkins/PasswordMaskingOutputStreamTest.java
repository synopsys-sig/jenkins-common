package com.sca.integration.jenkins;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PasswordMaskingOutputStreamTest {

    private static Stream<Arguments> populatePasswordMaskingTest() {
        return Stream.of(
            Arguments.of("password", "password", PasswordMaskingOutputStream.MASKED_PASSWORD),
            Arguments.of("password", "before password", "before " + PasswordMaskingOutputStream.MASKED_PASSWORD),
            Arguments.of("password", "password after", PasswordMaskingOutputStream.MASKED_PASSWORD + " after"),
            Arguments.of("password", "before password after", "before " + PasswordMaskingOutputStream.MASKED_PASSWORD + " after"),
            Arguments.of("password", "first line password\nsecond line password", "first line " + PasswordMaskingOutputStream.MASKED_PASSWORD + "\nsecond line " + PasswordMaskingOutputStream.MASKED_PASSWORD),
            Arguments.of("PASSWORD", "Some text", "Some text"),
            Arguments.of("", "Some text", "Some text"),
            Arguments.of(null, "Some text", "Some text"),
            Arguments.of("password", "", "")
        );
    }

    @ParameterizedTest
    @MethodSource("populatePasswordMaskingTest")
    public void testPasswordMaskingOutputStream(String password, String line, String expected) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bytes = line.getBytes();
        PasswordMaskingOutputStream passwordMaskingOutputStream = new PasswordMaskingOutputStream(byteArrayOutputStream, password);
        passwordMaskingOutputStream.eol(bytes, bytes.length);
        assertEquals(expected, byteArrayOutputStream.toString());
    }

}
