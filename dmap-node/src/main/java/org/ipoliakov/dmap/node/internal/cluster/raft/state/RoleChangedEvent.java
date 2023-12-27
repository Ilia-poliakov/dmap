package org.ipoliakov.dmap.node.internal.cluster.raft.state;

import org.ipoliakov.dmap.node.internal.cluster.raft.Role;

public record RoleChangedEvent(Role newRole) {
}
