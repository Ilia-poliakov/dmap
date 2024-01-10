package org.ipoliakov.dmap.node.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(Class<?> entityType, String searchSubject) {
        super(entityType.getSimpleName() + " with " + searchSubject + " not found");
    }
}
