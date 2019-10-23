package com.walmartlabs.moviesreloaded;

import androidx.annotation.NonNull;

import com.ern.api.impl.navigation.ElectrodeReactFragmentNavDelegate;
import com.ern.api.impl.navigation.MiniAppNavFragment;

public class ParentFragment extends MiniAppNavFragment {

    @NonNull
    @Override
    protected ElectrodeReactFragmentNavDelegate createFragmentDelegate() {
        return new CustomFragmentDelegate(this);
    }

    @Override
    public int fragmentLayoutId() {
        return R.layout.fragment_parent;
    }
}
