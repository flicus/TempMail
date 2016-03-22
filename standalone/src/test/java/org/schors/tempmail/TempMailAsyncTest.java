package org.schors.tempmail;

import junit.framework.TestCase;
import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TempMailAsyncTest extends TestCase {

    private TempMailClient client;
    private CountDownLatch lock = new CountDownLatch(1);
    private Result<JSONObject> result;

    @Override
    public void setUp() throws Exception {
        client = TempMailClient.create();
    }

    public void testGetDomains() throws Exception {

        client.getSupportedDomains(event -> {
            result = event;
        });


        lock.await(5000, TimeUnit.MILLISECONDS);

        assertNotNull(result);
        assertTrue(result.success());
        assertNotNull(result.result());
        assertNotNull(result.result().getJSONArray("result"));
        assertTrue(result.result().getJSONArray("result").length() > 0);

    }
}
