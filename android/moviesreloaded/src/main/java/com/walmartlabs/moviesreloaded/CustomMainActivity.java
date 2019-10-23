package com.walmartlabs.moviesreloaded;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.ern.api.impl.navigation.ElectrodeBaseActivity;

public class CustomMainActivity extends ElectrodeBaseActivity {

    @Override
    protected int mainLayout() {
        return R.layout.activity_main;
    }

    @NonNull
    @Override
    public String getRootComponentName() {
        return "";
    }

    @Override
    public int getFragmentContainerId() {
        return R.id.fragment_container;
    }

    @NonNull
    @Override
    public Class<? extends Fragment> miniAppFragmentClass() {
        return ParentFragment.class;
    }
}
