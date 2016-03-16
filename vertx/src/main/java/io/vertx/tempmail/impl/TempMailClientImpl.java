/*
 *   The MIT License (MIT)
 *
 *   Copyright (c) 2016 schors
 *
 *    Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *    The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 *
 */

package io.vertx.tempmail.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.tempmail.TempMailClient;
import io.vertx.tempmail.TempMailOptions;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TempMailClientImpl implements TempMailClient {

    private static final String getMessagesURL = "http://api.temp-mail.ru/request/mail/id/%s/format/json";
    private static final String getDomainsURL = "http://api.temp-mail.ru/request/domains/format/json";
    private static final String getSourcesURL = "http://api.temp-mail.ru/request/source/id/%s/format/json";
    private static final String deleteMessageURL = "http://api.temp-mail.ru/request/delete/id/%s/";

    //todo replace with vertx HttpClient after it will support proxy
    private CloseableHttpClient httpclient = HttpClientBuilder.create().setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
    private Map<String, Wrapper> handlers = new ConcurrentHashMap<>();
    private Vertx vertx;
    private TempMailOptions options;

    public TempMailClientImpl(Vertx vertx, TempMailOptions options) {
        this.vertx = vertx;
        this.options = options;
        vertx.setTimer(options.getCheckPeriod(), new MailChecker());
    }

    @Override
    public void getSupportedDomains(Handler<AsyncResult<JsonObject>> handler) {
        doGenericRequest(getDomainsURL, handler);
    }

    @Override
    public void getMessages(String email, Handler<AsyncResult<JsonObject>> handler) {
        doGenericRequest(String.format(getMessagesURL, DigestUtils.md5Hex(email)), handler);
    }

    @Override
    public void createMailListener(String email, Handler<AsyncResult<JsonObject>> handler) {
        handlers.put(email, new Wrapper(email, handler));
    }

    @Override
    public void removeMailListener(String email) {
        handlers.remove(email);
    }

    @Override
    public void getSources(String email, Handler<AsyncResult<JsonObject>> handler) {
        doGenericRequest(String.format(getSourcesURL, DigestUtils.md5Hex(email)), handler);
    }

    @Override
    public void deleteMessage(String messageId, Handler<AsyncResult<JsonObject>> handler) {
        doGenericRequest(String.format(deleteMessageURL, messageId), handler);
    }

    private void doGenericRequest(String url, Handler<AsyncResult<JsonObject>> handler) {
        vertx.executeBlocking(future -> {
            HttpGet httpGet = new HttpGet(url);
            if (options.getProxy() != null) {
                RequestConfig response = RequestConfig.custom().setProxy(options.getProxy()).build();
                httpGet.setConfig(response);
            }
            try {
                CloseableHttpResponse response = httpclient.execute(httpGet);
                if (response.getStatusLine().getStatusCode() == 200) {
                    HttpEntity ht = response.getEntity();
                    BufferedHttpEntity buf = new BufferedHttpEntity(ht);
                    String responseContent = EntityUtils.toString(buf, "UTF-8");
                    JsonArray jsonArray = new JsonArray(responseContent);
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.put("result", jsonArray);
                    future.complete(jsonObject);
                }
            } catch (Exception e) {
                future.fail(e);
            }
        }, res -> {
            handler.handle(new AsyncResult<JsonObject>() {
                @Override
                public JsonObject result() {
                    return res.succeeded() ? (JsonObject) res.result() : null;
                }

                @Override
                public Throwable cause() {
                    return res.succeeded() ? null : res.cause();
                }

                @Override
                public boolean succeeded() {
                    return res.succeeded();
                }

                @Override
                public boolean failed() {
                    return res.failed();
                }
            });
        });
    }

    private class MailChecker implements Handler<Long> {

        @Override
        public void handle(Long event) {
            for (Map.Entry<String, Wrapper> entry : handlers.entrySet()) {
                getMessages(entry.getKey(), res -> {
                    if (res.succeeded()) {
                        JsonObject messages = res.result();
                        if (messages != null) {
                            if (messages.size() > entry.getValue().getCount()) {
                                entry.getValue().getHandler().handle(new AsyncResult<JsonObject>() {
                                    @Override
                                    public JsonObject result() {
                                        return messages;
                                    }

                                    @Override
                                    public Throwable cause() {
                                        return null;
                                    }

                                    @Override
                                    public boolean succeeded() {
                                        return true;
                                    }

                                    @Override
                                    public boolean failed() {
                                        return false;
                                    }
                                });
                            }
                            entry.getValue().setCount(messages.size());
                        }
                    }
                });
            }
        }
    }

    private class Wrapper {
        private String email;
        private long count;
        private Handler<AsyncResult<JsonObject>> handler;

        public Wrapper() {
            this.count = 0;
        }

        public Wrapper(String email, Handler<AsyncResult<JsonObject>> handler) {
            this.email = email;
            this.handler = handler;
            this.count = 0;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public Handler<AsyncResult<JsonObject>> getHandler() {
            return handler;
        }

        public void setHandler(Handler<AsyncResult<JsonObject>> handler) {
            this.handler = handler;
        }

        @Override
        public String toString() {
            return "Wrapper{" +
                    "email='" + email + '\'' +
                    ", count=" + count +
                    ", handler=" + handler +
                    '}';
        }
    }
}
