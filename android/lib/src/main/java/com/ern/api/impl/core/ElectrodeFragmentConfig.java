package com.ern.api.impl.core;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;

/**
 * Configuration used by {@link ElectrodeBaseFragmentDelegate} to host a react native view component.
 * This config can be used by a fragment to add custom Layouts, ReactNative view container view groups, tool bar, etc.
 */
public class ElectrodeFragmentConfig {

    public static final int NONE = 0;

    /***
     * The layout xml that will be used by the fragment to create the view.
     * This is the layout where you can place your toolbar(optional) and an empty view group to inflate the react native view.
     */
    @LayoutRes
    int fragmentLayoutId;

    /**
     * The container ViewGroup id to which a react native view can be added.
     */
    @IdRes
    int reactViewContainerId;

    /**
     * id of the toolbar if tool bar is part of the fragment layout, return NONE otherwise.
     */
    @IdRes
    int toolBarId;

    public ElectrodeFragmentConfig() {
        this.fragmentLayoutId = NONE;
        this.reactViewContainerId = NONE;
        this.toolBarId = NONE;
    }

    public int getFragmentLayoutId() {
        return fragmentLayoutId;
    }

    /***
     * The layout xml that will be used by the fragment to create the view.
     * This is the layout where you can place your toolbar(optional) and an empty view group to inflate the react native view.
     */
    public void setFragmentLayoutId(int fragmentLayoutId) {
        this.fragmentLayoutId = fragmentLayoutId;
    }

    public int getReactViewContainerId() {
        return reactViewContainerId;
    }

    /**
     * The container ViewGroup id to which a react native view can be added.
     */
    public void setReactViewContainerId(int reactViewContainerId) {
        this.reactViewContainerId = reactViewContainerId;
    }

    public int getToolBarId() {
        return toolBarId;
    }

    /**
     * Provide the id of the toolbar if tool bar is part of the fragment layout, return NONE otherwise.
     */
    public void setToolBarId(int toolBarId) {
        this.toolBarId = toolBarId;
    }
}
