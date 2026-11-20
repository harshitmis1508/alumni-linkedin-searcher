package com.alumni.searcher.exception;

/** Raised when the PhantomBuster agent does not finish within the configured timeout. */
public class PhantomBusterTimeoutException extends PhantomBusterException {

    public PhantomBusterTimeoutException(String message) {
        super(message);
    }
}
