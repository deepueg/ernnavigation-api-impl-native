package com.walmartlabs.moviesreloaded;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ern.api.impl.navigation.ElectrodeReactFragmentNavDelegate;
import com.ern.api.impl.navigation.Route;

public class CustomFragmentDelegate extends ElectrodeReactFragmentNavDelegate {
    public CustomFragmentDelegate(@NonNull Fragment fragment) {
        super(fragment);
    }

    @Nullable
    @Override
    protected Class<? extends Fragment> fragmentClassForRoute(@NonNull Route route) {
        //For all routes.
        return ChildFragment.class;
    }
}
