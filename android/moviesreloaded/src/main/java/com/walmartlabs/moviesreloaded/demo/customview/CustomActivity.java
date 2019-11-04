package com.walmartlabs.moviesreloaded.demo.customview;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.ern.api.impl.navigation.ElectrodeBaseActivity;
import com.walmartlabs.moviesreloaded.R;

public class CustomActivity extends ElectrodeBaseActivity {

    @Override
    protected int mainLayout() {
        return R.layout.activity_main;
    }

    @NonNull
    @Override
    protected String getRootComponentName() {
        //Pass empty component as the first view is loaded inside the RootFragment layout xml.
        return "";
    }

    @Override
    protected int getFragmentContainerId() {
        return R.id.fragment_container;
    }

    @NonNull
    @Override
    protected Class<? extends Fragment> miniAppFragmentClass() {
        return RootFragment.class;
    }
}
