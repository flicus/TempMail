package org.schors.tempmail;

public interface Result<T> {
    boolean success();

    T result();
}
