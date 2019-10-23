package com.walmartlabs.moviesreloaded;

import androidx.annotation.NonNull;

import com.ern.api.impl.navigation.ElectrodeReactFragmentNavDelegate;
import com.ern.api.impl.navigation.MiniAppNavFragment;

public class ChildFragment extends MiniAppNavFragment {

    @NonNull
    @Override
    protected ElectrodeReactFragmentNavDelegate createFragmentDelegate() {
        return new CustomFragmentDelegate(this);
    }
}
