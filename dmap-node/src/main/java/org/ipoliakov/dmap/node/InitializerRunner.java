package org.ipoliakov.dmap.node;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InitializerRunner {

    private final List<Initializer> initializers;

    public void run() {
        initializers.forEach(Initializer::initialize);
    }

}
