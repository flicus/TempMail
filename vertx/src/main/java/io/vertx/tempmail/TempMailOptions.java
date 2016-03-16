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

package io.vertx.tempmail;

import io.vertx.core.json.JsonObject;
import org.apache.http.HttpHost;

public class TempMailOptions {

    private static final String PROXY_HOST = "proxyHost";
    private static final String PROXY_PORT = "proxyPort";
    private static final String PROXY_TYPE = "proxyType";
    private static final String CHECK_PERIOD = "checkPeriod";

    private JsonObject jsonObject = new JsonObject();

    public TempMailOptions() {
        jsonObject.put(CHECK_PERIOD, new Long(30000));
    }

    public TempMailOptions setProxy(String host, Integer port, String type) {
        jsonObject.put(PROXY_HOST, host);
        jsonObject.put(PROXY_PORT, port);
        jsonObject.put(PROXY_TYPE, type);
        return this;
    }

    public String getProxyHost() {
        return jsonObject.getString(PROXY_HOST);
    }

    public Integer getProxyPort() {
        return jsonObject.getInteger(PROXY_PORT);
    }

    public String getProxyType() {
        return jsonObject.getString(PROXY_TYPE);
    }

    public HttpHost getProxy() {
        return new HttpHost(getProxyHost(), getProxyPort(), getProxyType());
    }

    public Long getCheckPeriod() {
        return jsonObject.getLong(CHECK_PERIOD);
    }

    public TempMailOptions setCheckPeriod(Long period) {
        jsonObject.put(CHECK_PERIOD, period);
        return this;
    }
}
