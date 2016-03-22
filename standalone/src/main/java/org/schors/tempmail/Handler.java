package org.schors.tempmail;

@FunctionalInterface
public interface Handler<Result> {
    void handle(Result result);
}
