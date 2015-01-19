package no.api.syzygy;

import no.api.pantheon.exception.PantheonException;

/**
 *
 */
public class SyzygyException extends PantheonException {

    public SyzygyException(String msg) {
        super(msg);
    }

    public SyzygyException(String s, Exception cause) {
        super(s, cause);
    }
}
