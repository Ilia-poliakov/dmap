package org.ipoliakov.dmap.node.cluster.raft.state;

import org.ipoliakov.dmap.node.cluster.raft.Role;

public record RoleChangedEvent(Role newRole) {
}
