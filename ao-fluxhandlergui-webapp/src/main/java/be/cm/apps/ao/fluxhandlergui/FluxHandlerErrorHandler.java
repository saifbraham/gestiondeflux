/*
 * Copyright 2003-2009 LCM-ANMC, Inc. All rights reserved.
 * This source code is the property of LCM-ANMC, Direction
 * Informatique and cannot be copied or distributed without
 * the formal permission of LCM-ANMC.
 */
package be.cm.apps.ao.fluxhandlergui;

import be.cm.apps.ao.fluxhandlergui.error.CmpTechnicalErrorWindow;
import be.cm.comps.logman.CMLogFactory;
import be.cm.comps.logman.Logger;
import be.cm.comps.vaadin.util.VaadinUI;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.ErrorEvent;
import com.vaadin.ui.AbstractComponent;

public class FluxHandlerErrorHandler extends DefaultErrorHandler {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = CMLogFactory.getLog(FluxHandlerErrorHandler.class);

    @Override
    public void error(ErrorEvent event) {
        // Do the default error handling (optional)
        doDefault(event);
        // do not display the error message on component
        AbstractComponent component = findAbstractComponent(event);
        if (component != null) {
            component.setComponentError(null);
        }
        // Log error.
        java.util.UUID uuid = java.util.UUID.randomUUID();
        LOGGER.error(uuid.toString() + " : " + event.getThrowable().toString(), event.getThrowable());
        VaadinUI.getCurrent().addWindow(new CmpTechnicalErrorWindow(event, uuid));
    }
}
