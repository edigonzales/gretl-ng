package ch.so.agi.gretl.util;

public class NotAllowedSqlExpressionException extends GretlException {

    public NotAllowedSqlExpressionException(String message) {
        super(message);
    }

    public NotAllowedSqlExpressionException(String message, Throwable cause) {
        super(message, cause);
    }
}
