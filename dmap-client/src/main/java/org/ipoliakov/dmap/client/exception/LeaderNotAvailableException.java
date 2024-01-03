package org.ipoliakov.dmap.client.exception;

import org.ipoliakov.dmap.client.internal.Endpoint;

public class LeaderNotAvailableException extends RuntimeException {

    public LeaderNotAvailableException(Endpoint endpoint) {
        super("Unknown leader endpoint " + endpoint);
    }
}
