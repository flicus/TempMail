package org.schors.tempmail.impl;

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
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.schors.tempmail.Handler;
import org.schors.tempmail.Result;
import org.schors.tempmail.TempMailClient;
import org.schors.tempmail.TempMailOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class TempMailClientImpl implements TempMailClient {

    private static final Logger log = Logger.getLogger(TempMailClientImpl.class);

    private static final String getMessagesURL = "http://api.temp-mail.ru/request/mail/id/%s/format/json";
    private static final String getDomainsURL = "http://api.temp-mail.ru/request/domains/format/json";
    private static final String getSourcesURL = "http://api.temp-mail.ru/request/source/id/%s/format/json";
    private static final String deleteMessageURL = "http://api.temp-mail.ru/request/delete/id/%s/";

    private CloseableHttpClient httpclient = HttpClientBuilder.create().setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private ScheduledExecutorService scheduledExexutor = Executors.newSingleThreadScheduledExecutor();

    private Map<String, Wrapper> handlers = new ConcurrentHashMap<>();
    private TempMailOptions options;

    public TempMailClientImpl(TempMailOptions options) {
        this.options = options;
        scheduledExexutor.schedule(new MailChecker(), options.getCheckPeriod(), TimeUnit.MILLISECONDS);
    }


    @Override
    public JSONObject getSupportedDomains() {
        log.debug("getSupportedDomainsSync::");
        JSONObject result = null;
        Future<JSONObject> f = executorService.submit(() -> doGenericRequest(getDomainsURL));
        try {
            result = f.get();
        } catch (InterruptedException e) {
            log.warn(e, e);
        } catch (ExecutionException e) {
            log.warn(e, e);
        }
        return result;
    }

    @Override
    public void getSupportedDomains(final Handler<Result<JSONObject>> handler) {
        log.debug("getSupportedDomainsAsync::");
        executorService.submit(() -> {

            final JSONObject res = doGenericRequest(getDomainsURL);
            handler.handle(new Result<JSONObject>() {
                @Override
                public boolean success() {
                    return res != null;
                }

                @Override
                public JSONObject result() {
                    return res;
                }
            });

        });
    }

    @Override
    public JSONObject getMessages(String email) {
        log.debug("getMessagesSync::" + email);
        JSONObject result = null;
        Future<JSONObject> f = executorService.submit(() -> doGenericRequest(String.format(getMessagesURL, DigestUtils.md5Hex(email))));
        try {
            result = f.get();
        } catch (InterruptedException e) {
            log.warn(e, e);
        } catch (ExecutionException e) {
            log.warn(e, e);
        }
        return result;
    }

    @Override
    public void getMessages(String email, Handler<Result<JSONObject>> handler) {
        log.debug("getMessagesAsync::" + email);
        executorService.submit(() -> {

            final JSONObject res = doGenericRequest(String.format(getMessagesURL, DigestUtils.md5Hex(email)));
            handler.handle(new Result<JSONObject>() {
                @Override
                public boolean success() {
                    return res != null;
                }

                @Override
                public JSONObject result() {
                    return res;
                }
            });

        });
    }

    @Override
    public void addMailListener(String email, Handler<Result<JSONObject>> handler) {
        log.debug("addMailListener::" + email);
        Wrapper wrapper = handlers.get(email);
        if (wrapper == null) {
            wrapper = new Wrapper(email, handler);
            handlers.put(email, wrapper);
        } else {
            wrapper.addHandler(handler);
        }
    }

    @Override
    public void removeMailListener(String email) {
        log.debug("removeMailListener::" + email);
        handlers.remove(email);
    }

    @Override
    public JSONObject getSources(String email) {
        log.debug("getSourcesSync::" + email);
        JSONObject result = null;
        Future<JSONObject> f = executorService.submit(() -> doGenericRequest(String.format(getSourcesURL, DigestUtils.md5Hex(email))));
        try {
            result = f.get();
        } catch (InterruptedException e) {
            log.warn(e, e);
        } catch (ExecutionException e) {
            log.warn(e, e);
        }
        return result;
    }

    @Override
    public void getSources(String email, Handler<Result<JSONObject>> handler) {
        log.debug("getSourcesAsync::" + email);
        executorService.submit(() -> {

            final JSONObject res = doGenericRequest(String.format(getSourcesURL, DigestUtils.md5Hex(email)));
            handler.handle(new Result<JSONObject>() {
                @Override
                public boolean success() {
                    return res != null;
                }

                @Override
                public JSONObject result() {
                    return res;
                }
            });

        });
    }

    @Override
    public JSONObject deleteMessage(String messageId) {
        log.debug("deleteMessageSync::" + messageId);
        JSONObject result = null;
        Future<JSONObject> f = executorService.submit(() -> doGenericRequest(String.format(deleteMessageURL, DigestUtils.md5Hex(messageId))));
        try {
            result = f.get();
        } catch (InterruptedException e) {
            log.warn(e, e);
        } catch (ExecutionException e) {
            log.warn(e, e);
        }
        return result;
    }

    @Override
    public void deleteMessage(String messageId, Handler<Result<JSONObject>> handler) {
        log.debug("deleteMessageAsync::" + messageId);
        executorService.submit(() -> {

            final JSONObject res = doGenericRequest(String.format(deleteMessageURL, DigestUtils.md5Hex(messageId)));
            handler.handle(new Result<JSONObject>() {
                @Override
                public boolean success() {
                    return res != null;
                }

                @Override
                public JSONObject result() {
                    return res;
                }
            });

        });
    }

    private JSONObject doGenericRequest(String url) {
        JSONObject res = null;

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
                JSONArray jsonArray = new JSONArray(responseContent);
                res = new JSONObject();
                res.put("result", jsonArray);
            }
        } catch (Exception e) {
            log.warn(e, e);
        }
        return res;
    }

    private class MailChecker implements Runnable {

        @Override
        public void run() {


            log.debug("MailChecker::run::");
            for (Map.Entry<String, Wrapper> entry : handlers.entrySet()) {
                getMessages(entry.getKey(), res -> {
                    if (res.success()) {
                        try {
                            JSONObject result = res.result();
                            if (result != null) {
                                JSONArray allMessages = result.getJSONArray("result");
                                log.debug("MailChecker::run::allMessages" + allMessages);
                                if (allMessages.length() > entry.getValue().getCount()) {
                                    JSONArray newMessages = new JSONArray();
                                    int start = allMessages.length() - entry.getValue().getCount();
                                    for (int i = start; i <= allMessages.length(); i++) {
                                        newMessages.put(allMessages.get(i - 1));
                                    }
                                    result.put("result", newMessages);
                                    result.put("email", entry.getKey());
                                    log.debug("MailChecker::run::result" + result);
                                    for (Handler<Result<JSONObject>> handler : entry.getValue().getHandlers()) {
                                        handler.handle(new Result<JSONObject>() {
                                            @Override
                                            public boolean success() {
                                                return true;
                                            }

                                            @Override
                                            public JSONObject result() {
                                                return result;
                                            }
                                        });
                                    }
                                }
                                entry.getValue().setCount(allMessages.length());
                            }
                        } catch (Exception e) {
                            log.warn(e, e);
                        }
                    }
                    scheduledExexutor.schedule(new MailChecker(), options.getCheckPeriod(), TimeUnit.MILLISECONDS);
                });
            }
        }
    }

    private class Wrapper {
        private String email;
        private int count;
        private List<Handler<Result<JSONObject>>> handlers = new ArrayList<>();

        public Wrapper() {
            this.count = 0;
        }

        public Wrapper(String email, Handler<Result<JSONObject>> handler) {
            this.email = email;
            this.handlers.add(handler);
            this.count = 0;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public List<Handler<Result<JSONObject>>> getHandlers() {
            return handlers;
        }

        public void addHandler(Handler<Result<JSONObject>> handler) {
            this.handlers.add(handler);
        }

        @Override
        public String toString() {
            return "Wrapper{" +
                    "email='" + email + '\'' +
                    ", count=" + count +
                    '}';
        }
    }
}
