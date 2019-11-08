package com.ern.api.impl.core;

import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * MiniApp launch config that defines the custom configurations that can be used while starting a new MiniAppFragment to host a react native view component.
 */
public class LaunchConfig {
    public static final int ADD_TO_BACK_STACK = 0;
    public static final int DO_NOT_ADD_TO_BACK_STACK = 1;

    @IntDef({ADD_TO_BACK_STACK, DO_NOT_ADD_TO_BACK_STACK})
    @Retention(RetentionPolicy.SOURCE)
    @interface AddToBackStackState {
    }

    public static final int NONE = 0;

    /**
     * Pass a fragmentManager that should be used to start the new fragment.
     * If not passed the {@link AppCompatActivity#getSupportFragmentManager()} would be used to start the new fragment.
     */
    @Nullable
    FragmentManager fragmentManager;

    /**
     * Fragment class responsible for hosting the react native view.
     * <p>
     * A fragment class that can host a react view, one that has a proper implementation of {@link ElectrodeBaseFragmentDelegate}.
     */
    @Nullable
    Class<? extends Fragment> fragmentClass;

    /**
     * ViewGroup id to which the fragment needs to be loaded in your layout xml.
     * If not passed, the default fragmentContainerId provided by the activity would be used.
     */
    @IdRes
    int fragmentContainerId = NONE;

    /**
     * Optional props that you need to pass to a react native component as initial props.
     */
    Bundle initialProps = null;

    /**
     * Indicates that you want to load the react native component inside  a bottom sheet.
     * This will make sure that the fragment will not be added to the back stack, instead it will be presented as a dialog.
     */
    boolean isBottomSheet;

    /**
     * Set this to true if you want to force enable up navigation for component.
     */
    boolean forceUpEnabled;

    /**
     * Set this value to manage the fragment back stack
     */
    @AddToBackStackState
    int addToBackStack = ADD_TO_BACK_STACK;

    public LaunchConfig() {
    }

    @Nullable
    public FragmentManager getFragmentManager() {
        return fragmentManager;
    }

    public void setFragmentManager(@Nullable FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    @Nullable
    public Class<? extends Fragment> getFragmentClass() {
        return fragmentClass;
    }

    public void setFragmentClass(@Nullable Class<? extends Fragment> fragmentClass) {
        this.fragmentClass = fragmentClass;
    }

    public int getFragmentContainerId() {
        return fragmentContainerId;
    }

    public void setFragmentContainerId(@IdRes int fragmentContainerId) {
        this.fragmentContainerId = fragmentContainerId;
    }

    public Bundle getInitialProps() {
        return initialProps;
    }

    public void setInitialProps(@Nullable Bundle initialProps) {
        if (this.initialProps != null && initialProps != null) {
            initialProps.putAll(initialProps);
        } else {
            this.initialProps = initialProps;
        }
    }

    public boolean isBottomSheet() {
        return isBottomSheet;
    }

    public void setBottomSheet(boolean bottomSheet) {
        this.isBottomSheet = bottomSheet;
    }

    public int getAddToBackStack() {
        return addToBackStack;
    }

    public void setAddToBackStack(int addToBackStack) {
        this.addToBackStack = addToBackStack;
    }

    public void setForceUpEnabled(boolean forceUpEnabled) {
        this.forceUpEnabled = forceUpEnabled;
    }
}
