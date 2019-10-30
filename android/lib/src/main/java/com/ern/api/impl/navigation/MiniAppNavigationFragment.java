package com.ern.api.impl.navigation;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.ern.api.impl.core.ElectrodeBaseFragment;
import com.ern.api.impl.core.LaunchConfig;

/**
 * Fragment with inbuilt navigation for react native components.
 * <p>
 * Use this fragment to host a react native component that uses ern-navigation library to navigate between pages.
 */
public class MiniAppNavigationFragment extends ElectrodeBaseFragment<ElectrodeNavigationFragmentDelegate> implements ElectrodeNavigationFragmentDelegate.FragmentNavigator, ElectrodeNavigationFragmentDelegate.OnUpdateNextPageLaunchConfigListener {
    @NonNull
    @Override
    protected ElectrodeNavigationFragmentDelegate createFragmentDelegate() {
        return new ElectrodeNavigationFragmentDelegate(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        electrodeReactFragmentDelegate.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return electrodeReactFragmentDelegate.onOptionsItemSelected(item);
    }

    @Override
    public boolean navigate(Route route) {
        return false;
    }

    @Override
    public void updateNextPageLaunchConfig(@NonNull String nextPageName, @NonNull LaunchConfig defaultLaunchConfig) {
        //Override if needed.
    }
}
