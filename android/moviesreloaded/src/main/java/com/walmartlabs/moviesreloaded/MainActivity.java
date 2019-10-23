package com.walmartlabs.moviesreloaded;

import androidx.annotation.NonNull;

import com.ern.api.impl.navigation.ElectrodeBaseActivity;
import com.ern.api.impl.navigation.Route;

/**
 * This activity starts the MoviesList MiniApp feature.
 *
 * Demonstrates how a miniapp can be be launched using native android.
 */

public class MovieListActivity extends ElectrodeBaseActivity {

    @NonNull
    @Override
    public String getRootComponentName() {
        //First RN component to be started when the activity is launched.
        return "MoviesList";
    }

    @Override
    protected int mainLayout() {
        //Activity layout
        return R.layout.activity_main;
    }

    @Override
    public int getFragmentContainerId() {
        //ViewGroup where the fragments can be loaded.
        return R.id.fragment_container;
    }


    @Override
    public boolean navigate(Route route) {
        //Add any logic(if needed) to override the navigate call
        // and launch a native screen or any custom logic.
        return super.navigate(route);
    }
}