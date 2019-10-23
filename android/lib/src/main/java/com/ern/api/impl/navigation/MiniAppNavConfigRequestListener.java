package com.ern.api.impl.navigation;

import androidx.annotation.NonNull;

import com.ern.api.impl.core.ElectrodeReactFragmentActivityDelegate;

/**
 * Interface that exposes a way to provide more configuration while starting a MiniApp fragment.
 */
public interface MiniAppNavConfigRequestListener extends MiniAppNavRequestListener {

    /**
     * starts a new fragment and inflate it with the given react component.
     *
     * @param componentName react view component name.
     */
    void startMiniAppFragment(@NonNull String componentName, @NonNull ElectrodeReactFragmentActivityDelegate.StartMiniAppConfig startMiniAppConfig);
}
