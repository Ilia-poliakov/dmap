package org.ipoliakov.dmap.client.internal;

import java.net.InetSocketAddress;

public record Endpoint(String host, int port) {

    public boolean isTheSame(InetSocketAddress socketAddress) {
        return socketAddress.getPort() == port && host.equals(socketAddress.getHostString());
    }
}
