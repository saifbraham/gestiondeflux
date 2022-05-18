/*
 * Copyright 2003-2009 LCM-ANMC, Inc. All rights reserved.
 * This source code is the property of LCM-ANMC, Direction
 * Informatique and cannot be copied or distributed without
 * the formal permission of LCM-ANMC.
 */
package be.cm.apps.ao.fluxhandlergui;

import be.cm.apps.ao.fluxhandlergui.model.fluxes.FluxBDO;

public interface FluxCreatedListener {

    void onAllowancePeriodCreated(FluxBDO allowancePeriodCreated);

    void onAllowanceAttestCreated(FluxBDO allowanceAttestCreated);
}
