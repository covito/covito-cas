package org.covito.cas.client.exception;

public class TicketValidationException extends RuntimeException {

    private static final long serialVersionUID = -7036248720402711806L;

    public TicketValidationException(final String string) {
        super(string);
    }

    public TicketValidationException(final String string, final Throwable throwable) {
        super(string, throwable);
    }

    public TicketValidationException(final Throwable throwable) {
        super(throwable);
    }
}
