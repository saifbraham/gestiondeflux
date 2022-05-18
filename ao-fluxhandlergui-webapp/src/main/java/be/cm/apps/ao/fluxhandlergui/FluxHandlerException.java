/*
 * Copyright 2003-2009 LCM-ANMC, Inc. All rights reserved.
 * This source code is the property of LCM-ANMC, Direction
 * Informatique and cannot be copied or distributed without
 * the formal permission of LCM-ANMC.
 */
package be.cm.apps.ao.fluxhandlergui;

public class FluxHandlerException extends RuntimeException {

    public FluxHandlerException() {
        super();
    }

    public FluxHandlerException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public FluxHandlerException(String message) {
        super(message);
    }

    public FluxHandlerException(Throwable throwable) {
        super(throwable);
    }
}
