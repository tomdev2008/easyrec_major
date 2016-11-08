package org.easyrec.service.core.exception;

/**
 * @author: Fabian Salcher
 * @version: 2013-11-15
 */
public class ItemNotFoundException extends RuntimeException {
    private static final long serialVersionUID = -5870516773440232907L;

    public ItemNotFoundException(String message) {
        super(message);
    }
}
