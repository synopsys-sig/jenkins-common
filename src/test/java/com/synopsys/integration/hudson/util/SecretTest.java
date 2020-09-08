package com.synopsys.integration.hudson.util;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.junit.jupiter.api.Test;

import hudson.ProxyConfiguration;
import hudson.remoting.Base64;
import hudson.util.HistoricalSecrets;
import hudson.util.Secret;
import jenkins.security.ConfidentialKey;
import jenkins.security.ConfidentialStore;
import jenkins.security.CryptoConfidentialKey;
import jenkins.security.DefaultConfidentialStore;

public class SecretTest {
    @Test
    public void testSecretApi() throws IOException, BadPaddingException, IllegalBlockSizeException {
        Path jenkinsHomePath = Files.createTempDirectory("test-jenkins-home");
        File jenkinsHome = jenkinsHomePath.toFile();
        jenkinsHome.deleteOnExit();

        EncryptionHelp encryptionHelp = new EncryptionHelp(jenkinsHome);
        String valueToEncrypt = "silly test";

        // jenkins expects a magic string to be appended to the value to be kept a secret
        String firstLevelOfObfuscation = valueToEncrypt + encryptionHelp.magic;
        byte[] encrypted = encryptionHelp.confidentialKey.encrypt().doFinal(firstLevelOfObfuscation.getBytes());

        // store the encrypted secret, to be retrieved later
        encryptionHelp.storeSecret(encrypted);

        // when jenkins presents the secret, it base64 encodes the encrypted value
        String encodedAndEncrypted = Base64.encode(encrypted);

        ProxyConfiguration proxyConfiguration = new ProxyConfiguration("nothing", 0, "nothing", encodedAndEncrypted);
        assertEquals(valueToEncrypt, proxyConfiguration.getPassword());
    }

    private static class TestStore extends DefaultConfidentialStore {
        public TestStore(File jenkinsHome) throws IOException, InterruptedException {
            super(jenkinsHome);
        }

        public void storeIt(ConfidentialKey confidentialKey, byte[] payload) throws IOException {
            super.store(confidentialKey, payload);
        }
    }

    private static class EncryptionHelp {
        public TestStore confidentialStore;
        public CryptoConfidentialKey confidentialKey;
        public String magic;

        public EncryptionHelp(File jenkinsHome) {
            try {
                // jenkins method of injecting a test store
                confidentialStore = new TestStore(jenkinsHome);
                ThreadLocal<ConfidentialStore> confidentialStoreThreadLocal = new ThreadLocal<>();
                confidentialStoreThreadLocal.set(confidentialStore);

                Field storeField = ConfidentialStore.class.getDeclaredField("TEST");
                storeField.setAccessible(true);
                storeField.set(null, confidentialStoreThreadLocal);

                // we will use the default key
                Field keyField = Secret.class.getDeclaredField("KEY");
                keyField.setAccessible(true);
                confidentialKey = (CryptoConfidentialKey) keyField.get(null);

                Field magicField = HistoricalSecrets.class.getDeclaredField("MAGIC");
                magicField.setAccessible(true);
                magic = (String) magicField.get(null);
            } catch (InterruptedException | NoSuchFieldException | IllegalAccessException | IOException e) {
                throw new RuntimeException("It is likely the underlying assumptions are no longer valid for this test...");
            }
        }

        public void storeSecret(byte[] encrypted) throws IOException {
            confidentialStore.storeIt(confidentialKey, encrypted);
        }
    }
}
