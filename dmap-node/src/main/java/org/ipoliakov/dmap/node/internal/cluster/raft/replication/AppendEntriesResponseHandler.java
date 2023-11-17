package org.ipoliakov.dmap.node.internal.cluster.raft.replication;

import org.ipoliakov.dmap.protocol.internal.AppendEntriesRes;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppendEntriesResponseHandler {

    public void handle(AppendEntriesRes res, int majorityNodesCount) {

    }
}
