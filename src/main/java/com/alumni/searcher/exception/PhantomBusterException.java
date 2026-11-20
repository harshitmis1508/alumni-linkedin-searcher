package com.alumni.searcher.exception;

/** Raised when the PhantomBuster API fails or returns something unusable. */
public class PhantomBusterException extends RuntimeException {

    public PhantomBusterException(String message) {
        super(message);
    }

    public PhantomBusterException(String message, Throwable cause) {
        super(message, cause);
    }
}
