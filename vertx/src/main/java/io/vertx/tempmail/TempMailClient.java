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


import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.tempmail.impl.TempMailClientImpl;

public interface TempMailClient {

    static TempMailClient create(Vertx vertx) {
        return new TempMailClientImpl(vertx, new TempMailOptions());
    }

    static TempMailClient create(Vertx vertx, TempMailOptions options) {
        return new TempMailClientImpl(vertx, options);
    }

    /**
     * @param handler
     */
    void getSupportedDomains(Handler<AsyncResult<JsonArray>> handler);

    /**
     * @param email
     * @param handler
     */
    void getMessages(String email, Handler<AsyncResult<JsonArray>> handler);

    /**
     * @param email
     * @param handler
     */
    void createMailListener(String email, Handler<AsyncResult<JsonArray>> handler);

    /**
     * @param email
     * @param handler
     */
    void removeMailListener(String email);

    /**
     * @param email
     * @param handler
     */
    void getSources(String email, Handler<AsyncResult<JsonArray>> handler);

    /**
     * @param id
     * @param handler
     */
    void deleteMessage(String messageId, Handler<AsyncResult<JsonArray>> handler);
}
