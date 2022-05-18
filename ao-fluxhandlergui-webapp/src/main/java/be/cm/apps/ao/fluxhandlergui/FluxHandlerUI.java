/*
 * Copyright 2003-2009 LCM-ANMC, Inc. All rights reserved.
 * This source code is the property of LCM-ANMC, Direction
 * Informatique and cannot be copied or distributed without
 * the formal permission of LCM-ANMC.
 */
package be.cm.apps.ao.fluxhandlergui;

import be.cm.apps.ao.fluxhandlergui.common.util.ResourceUtils;
import be.cm.apps.ao.fluxhandlergui.fluxmanagement.WinFluxCreation;
import be.cm.apps.ao.fluxhandlergui.fluxmanagement.WinFluxesManagement;
import be.cm.apps.supportgui.bdo.orgref.EmployeeBDO;
import be.cm.apps.supportgui.vaadin.ui.helper.UserInfoMPU;
import be.cm.comps.logman.CMLogFactory;
import be.cm.comps.logman.Logger;
import be.cm.comps.parametermanager.ParameterManager;
import be.cm.comps.parametermanager.ParameterManagerFactory;
import be.cm.comps.profilemanager.ProfileManager;
import be.cm.comps.profilemanager.UserProfile;
import be.cm.comps.profilemanager.exceptions.ProfileManagerException;
import be.cm.comps.vaadin.util.AutoVaadin;
import be.cm.comps.vaadin.util.HistoryService;
import be.cm.comps.vaadin.util.VaadinUI;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.BaseTheme;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static be.cm.comps.vaadin.util.TranslationUtil.getActiveLocale;
import static be.cm.comps.vaadin.util.TranslationUtil.translate;

public class FluxHandlerUI extends VaadinUI implements ViewChangeListener /*, Refreshable, ChangeInTodoListNotifier */ {

    private static final Logger LOGGER = CMLogFactory.getLog(FluxHandlerUI.class);
    public static final String LANGUAGE_COOKIE = "USER_LOCALE";

    private UserInfoMPU userInfoMPU;

    private ComponentContainer mnuFluxHandler;
    private Button mnuSearchFlux;
    private Button mnuCreateFlux;
    private boolean isMenuVisible = true;


    private String initMemberIdentifier = null;

    private Map<Class<? extends PageBase>, Button> menuItemsByPage = new HashMap<Class<? extends PageBase>, Button>();

    private static final String SELECTED_ITEM_MENU_CSS = "selectedItem";

    public static FluxHandlerUI getInstance() {
        return (FluxHandlerUI) UI.getCurrent();
    }

    @Override
    protected void init(VaadinRequest request) {
        super.init(request);
        String language = getActiveLocale().getLanguage();
        Cookie cookie = getCookieByName(LANGUAGE_COOKIE);
        if (cookie != null) {
            language = cookie.getValue();
            cookie.setMaxAge(Integer.MAX_VALUE);
            cookie.setPath(VaadinService.getCurrentRequest().getContextPath());
            VaadinService.getCurrentResponse().addCookie(cookie);
        }
        Locale locale;
        if (language.equalsIgnoreCase("fr")) {
            locale = new Locale("fr", "BE");
        } else {
            locale = new Locale("nl", "BE");
        }
        getUI().getSession().setLocale(locale);
        VaadinSession.getCurrent().getSession().setAttribute("USER_LOCALE", locale);
        VaadinSession.getCurrent().setErrorHandler(new FluxHandlerErrorHandler());

        HistoryService handler = HistoryService.getInstance();
        handler.register(WinFluxesManagement.class);
        handler.register(WinFluxCreation.class);
        handler.registerErrorView(WinFluxesManagement.class);
        handler.removeViewChangeListener(this);
        handler.addViewChangeListener(this);

        mnuFluxHandler = createMenu();
        createSubmenuTitle(mnuFluxHandler, translate(this, "fluxManagement.caption"));
        mnuSearchFlux = createMenuItem(mnuFluxHandler, WinFluxesManagement.class);
        mnuSearchFlux.addStyleName("submenu");
        mnuCreateFlux = createMenuItem(mnuFluxHandler, WinFluxCreation.class);
        mnuCreateFlux.setVisible(false);
        mnuCreateFlux.addStyleName("submenu");

        if (isUserAdmin()) {
            mnuCreateFlux.setVisible(true);
        }

        createSubmenuTitle(mnuFluxHandler, "");

        AutoVaadin.doWindow(this);
        userInfoMPU = getUserInfo();
        final CssLayout cmMenu = (CssLayout) mnuFluxHandler.getParent();
        cmMenu.setWidth(230, Unit.PIXELS);
        final Button showHideMenu = new Button();
        showHideMenu.setStyleName(BaseTheme.BUTTON_LINK);
        showHideMenu.addStyleName("hide-menu");
        cmMenu.addComponentAsFirst(showHideMenu);

        showHideMenu.addClickListener(event -> {
            if (isMenuVisible) {
                cmMenu.setWidth(20, Unit.PIXELS);
                for (Component component : cmMenu) {
                    if (component != showHideMenu) {
                        component.setVisible(false);
                    }
                }
            } else {
                cmMenu.setWidth(230, Unit.PIXELS);
                for (Component component : cmMenu) {
                    component.setVisible(true);
                }
            }
            isMenuVisible = !isMenuVisible;
        });


        createSubmenuTitle(mnuFluxHandler, "");
        menuItemsByPage.put(WinFluxesManagement.class, mnuSearchFlux);
    }

    @Override
    public boolean beforeViewChange(ViewChangeEvent viewChangeEvent) {
        return true;
    }

    @Override
    public void afterViewChange(ViewChangeEvent event) {
        Class<? extends View> classObject = event.getNewView().getClass();

        for (Button btn : menuItemsByPage.values()) {
            btn.removeStyleName(SELECTED_ITEM_MENU_CSS);
        }
        if (menuItemsByPage.containsKey(classObject)) {
            menuItemsByPage.get(classObject).addStyleName(SELECTED_ITEM_MENU_CSS);
        }
    }

    private Cookie getCookieByName(String name) {
        // Fetch all cookies from the request
        Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();

        // Iterate to find cookie by its name
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie;
            }
        }
        return null;
    }

    @Override
    protected Component createBreadcrumb() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        return layout;
    }

    public String getInitMemberIdentifier() {
        return initMemberIdentifier;
    }

    @Override
    public void logout() {
        this.getSession().getSession().invalidate();
        this.getSession().close();
        // Close HTTP session
        VaadinRequest request = VaadinService.getCurrentRequest();
        if (request instanceof HttpServletRequest) {
            ((HttpServletRequest) request).getSession().invalidate();
        }
        String url = "";
        this.getPage().setLocation(url);
    }

    private static boolean isUserAdmin() {
        return VaadinService.getCurrentRequest().isUserInRole("fluxhandlergui-admin");
    }

    private static boolean isUserCommonUser() {
        return VaadinService.getCurrentRequest().isUserInRole("fluxhandler-user");
    }

    private Button createMenuItemExternalLink(ComponentContainer menu) {
        Button btnItem = new Button("(unnamed)");
        btnItem.setStyleName(BaseTheme.BUTTON_LINK + " cmMenuItem");
        btnItem.setWidth("100%");
        menu.addComponent(btnItem);
        return btnItem;
    }

    @SuppressWarnings("deprecation")
    private UserInfoMPU getUserInfo() {
        UserInfoMPU userInfo = null;
        try {
            userInfo = new UserInfoMPU(getUserLogon());

            try {
                ResourceUtils resourceUtils = ResourceUtils.getInstance();
                userInfo.setRoles(resourceUtils.getUserOrgRefRoles(getUserLogon()));
                Map<String, List<String>> rolesAnsMedicaresMap = resourceUtils.getEmployeeRolesAndAssignedMedicare(getUserLogon());
                userInfo.setRolesByMedicare(rolesAnsMedicaresMap.get("roles"));
                userInfo.getListOfAssignedMedicare().addAll(rolesAnsMedicaresMap.get("medicares"));
                EmployeeBDO employeeBDO = resourceUtils.getEmployeeInfoByID(getUserLogon());
                userInfo.setMedicareId(employeeBDO.getTodayPrimaryMedicareIdentifier());
            } catch (Exception e) {
                // No Roles assigned to this employee
                LOGGER.warn(String.format("Unable to determine user roles [%s]: %s", getUserLogon(), e.getMessage()));
            }

            String userId = getUserLogon();
            if (!StringUtils.isEmpty(userId)) {
                UserProfile userProfile = ProfileManager.getUserProfile(userId);
                userInfo.setUserProfile(userProfile);
            }
        } catch (ProfileManagerException e1) {
            LOGGER.warn(e1);
        }
        return userInfo;
    }

    private static String getParameter(String key) {
        try {
            ParameterManager pm = ParameterManagerFactory.getManager(FluxHandlerUI.class);
            return pm.getString(key);
        } catch (Exception e) {
            return "Could not find Parameter: " + key + ": " + e.getMessage();
        }
    }

    public UserInfoMPU getUserInfoMPU() {
        return this.userInfoMPU;
    }
}
