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

package org.schors.tempmail;

import org.json.JSONObject;
import org.schors.tempmail.impl.TempMailClientImpl;

public interface TempMailClient {

    static TempMailClient create() {
        return new TempMailClientImpl(new TempMailOptions());
    }

    static TempMailClient create(TempMailOptions options) {
        return new TempMailClientImpl(options);
    }


    JSONObject getSupportedDomains();

    void getSupportedDomains(Handler<Result<JSONObject>> handler);

    JSONObject getMessages(String email);

    void getMessages(String email, Handler<Result<JSONObject>> handler);

    void addMailListener(String email, Handler<Result<JSONObject>> handler);

    void removeMailListener(String email);

    JSONObject getSources(String email);

    void getSources(String email, Handler<Result<JSONObject>> handler);

    JSONObject deleteMessage(String messageId);

    void deleteMessage(String messageId, Handler<Result<JSONObject>> handler);
}
