package com.walmartlabs.moviesreloaded.demo.bottomsheet.modal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.ern.api.impl.core.ElectrodeBaseActivityDelegate;
import com.ern.api.impl.core.LaunchConfig;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ModalBottomSheetActivityDelegate extends ElectrodeBaseActivityDelegate {

    /**
     * @param activity            Hosting activity
     * @param rootComponentName   First react native component to be launched.
     * @param defaultLaunchConfig : {@link LaunchConfig} that acts as the the initial configuration to load the rootComponent as well as the default launch config for subsequent navigation flows.
     *                            This configuration will also be used as a default configuration when the root component tries to navigate to a new pages if a proper launch config is passed inside {@link #startMiniAppFragment(String, LaunchConfig)}.
     */
    public ModalBottomSheetActivityDelegate(@NonNull FragmentActivity activity, @Nullable String rootComponentName, @NonNull LaunchConfig defaultLaunchConfig) {
        super(activity, rootComponentName, defaultLaunchConfig);
    }

    @Override
    protected void showBottomSheetDialogFragment(Fragment fragment, FragmentManager fragmentManager, String tag) {
        if (fragment instanceof BottomSheetDialogFragment) {
            ((BottomSheetDialogFragment) fragment).show(fragmentManager, tag);
        } else {
            throw new RuntimeException("Should never reach here");
        }
    }
}
