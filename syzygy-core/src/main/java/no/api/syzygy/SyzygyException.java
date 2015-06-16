package no.api.syzygy;

/**
 *
 */
public class SyzygyException extends RuntimeException {

    public SyzygyException(String msg) {
        super(msg);
    }

    public SyzygyException(String s, Exception cause) {
        super(s, cause);
    }
}
