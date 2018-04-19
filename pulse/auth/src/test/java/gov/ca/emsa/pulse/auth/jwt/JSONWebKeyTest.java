package gov.ca.emsa.pulse.auth.jwt;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.ca.emsa.pulse.auth.TestConfig.class
})
public class JSONWebKeyTest {

    @Autowired
    Environment env;
    @Autowired
    JSONWebKey jsonWebKey;

    @Before
    public void init() {
        jsonWebKey.createOrLoadKey();
    }

    @Test
    public void testSaveKey() throws ClassNotFoundException, IOException {
        jsonWebKey.saveKey(env.getProperty("keyLocation"));
        PublicKey originalPublicKey = jsonWebKey.getPublicKey();
        PrivateKey originalPrivateKey = jsonWebKey.getPrivateKey();

        jsonWebKey.loadSavedKey(env.getProperty("keyLocation"));
        assertEquals(originalPublicKey, jsonWebKey.getPublicKey());
        assertEquals(originalPrivateKey, jsonWebKey.getPrivateKey());
    }

    @Test
    public void testLoadSavedKey() throws ClassNotFoundException, IOException {
        jsonWebKey.saveKey(env.getProperty("keyLocation"));
        PublicKey originalPublicKey = jsonWebKey.getPublicKey();
        PrivateKey originalPrivateKey = jsonWebKey.getPrivateKey();

        jsonWebKey.loadSavedKey(env.getProperty("keyLocation"));
        assertEquals(originalPublicKey, jsonWebKey.getPublicKey());
        assertEquals(originalPrivateKey, jsonWebKey.getPrivateKey());
    }

    @Test
    public void testCreateOrLoadKey() {
        jsonWebKey.createOrLoadKey();
    }
}
