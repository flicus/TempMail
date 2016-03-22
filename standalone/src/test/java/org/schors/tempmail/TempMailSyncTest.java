package org.schors.tempmail;

import junit.framework.TestCase;
import org.json.JSONObject;

public class TempMailSyncTest extends TestCase {


    private TempMailClient client;

    @Override
    public void setUp() throws Exception {
        client = TempMailClient.create();
    }

    public void testGetDomains() throws Exception {

        JSONObject res = client.getSupportedDomains();
        assertNotNull(res);
        assertNotNull(res.getJSONArray("result"));
        assertTrue(res.getJSONArray("result").length() > 0);

    }

    public void testMailCheck() throws Exception {
        client.addMailListener("pipka@shotmail.ru", event -> {
            if (event.success()) {
                System.out.print(event.result());
            }
        });
        //todo send mail and check

    }
}
