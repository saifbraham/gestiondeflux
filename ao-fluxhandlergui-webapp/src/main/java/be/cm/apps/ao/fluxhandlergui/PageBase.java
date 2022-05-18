/*
 * Copyright 2003-2009 LCM-ANMC, Inc. All rights reserved.
 * This source code is the property of LCM-ANMC, Direction
 * Informatique and cannot be copied or distributed without
 * the formal permission of LCM-ANMC.
 */
package be.cm.apps.ao.fluxhandlergui;

import be.cm.commons.exceptions.ApplicationException;
import be.cm.comps.logman.CMLogFactory;
import be.cm.comps.logman.Logger;
import be.cm.comps.metrics.MetricRegistryProvider;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import io.micrometer.core.instrument.Timer;

import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public abstract class PageBase extends CustomComponent implements ScreenNumberAware, View {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = CMLogFactory.getInstance().getLogger(PageBase.class);

    private String screenNumber;
    private final VerticalLayout mainLayout = new VerticalLayout();

    @EJB
    private MetricRegistryProvider metricRegistryProvider;

    protected PageBase(String screenNumber) {
        this.screenNumber = screenNumber;
        this.mainLayout.setSizeFull();
        this.mainLayout.setSpacing(true);
        this.mainLayout.setMargin(true);
        this.setCompositionRoot(this.mainLayout);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Timer.Sample sample = Timer.start(this.locateMetricRegistry().getMeterRegistry());
        try {
            enterView(event);
        } finally {
            sample.stop(Timer.builder("gui")
                    .description(null)
                    .tags("view", event.getNewView().getClass().getSimpleName())
                    .tags("user", getUserId() == null ? "" : getUserId())
                    .register(this.locateMetricRegistry().getMeterRegistry()));
        }
    }

    private MetricRegistryProvider locateMetricRegistry() {
        if (this.metricRegistryProvider == null) {
            try {
                InitialContext ctx = new InitialContext();
                this.metricRegistryProvider = (MetricRegistryProvider)ctx.lookup("java:comp/env/ejb/metricRegistryProvider");
            } catch (NamingException var2) {
                return null;
            }
        }
        return this.metricRegistryProvider;
    }

    protected void handleApplicationException(ApplicationException exception) {
        LOGGER.error("Exception caught: " + exception);
        Notification.show(exception.toString(), Notification.Type.ERROR_MESSAGE);
    }

    @Override
    public String getScreenNumber() {
        return this.screenNumber;
    }

    public void setScreenNumber(String screenNumber) {
        this.screenNumber = screenNumber;
    }

    protected VerticalLayout getMainLayout() {
        return this.mainLayout;
    }

    private String getUserId() {
        return FluxHandlerUI.getInstance().getUserInfoMPU() == null ? null : FluxHandlerUI.getInstance().getUserInfoMPU().getUserId();
    }

    public abstract void enterView(ViewChangeListener.ViewChangeEvent event);
}
