package com.walmartlabs.moviesreloaded;

import androidx.annotation.NonNull;

import com.ern.api.impl.navigation.ElectrodeReactFragmentNavDelegate;
import com.ern.api.impl.navigation.MiniAppBottomSheetFragment;
import com.ern.api.impl.navigation.Route;

public class MyBottomFragment extends MiniAppBottomSheetFragment implements ElectrodeReactFragmentNavDelegate.FragmentNavigator {

    @NonNull
    @Override
    protected ElectrodeReactFragmentNavDelegate createFragmentDelegate() {
        return new CustomFragmentDelegate(this);
    }

    @Override
    public int fragmentLayoutId() {
        return R.layout.fragment_bottom_sheet;
    }

    @Override
    public int reactViewContainerId() {
        return R.id.miniapp_container;
    }

    @Override
    public boolean navigate(Route route) {
        return false;
    }
}
