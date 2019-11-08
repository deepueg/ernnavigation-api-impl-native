package com.ern.api.impl.navigation;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.ern.api.impl.core.ElectrodeBaseFragmentDelegate;
import com.ern.api.impl.core.ElectrodeFragmentConfig;
import com.ern.api.impl.core.LaunchConfig;
import com.ernnavigationApi.ern.api.EnNavigationApi;
import com.ernnavigationApi.ern.model.NavigationBar;
import com.ernnavigationApi.ern.model.NavigationBarButton;
import com.walmartlabs.electrode.reactnative.bridge.helpers.Logger;

public class ElectrodeNavigationFragmentDelegate<T extends ElectrodeBaseFragmentDelegate.ElectrodeActivityListener, C extends ElectrodeFragmentConfig> extends ElectrodeBaseFragmentDelegate<ElectrodeNavigationActivityListener, ElectrodeNavigationFragmentConfig> {

    private static final String TAG = ElectrodeNavigationFragmentDelegate.class.getSimpleName();

    private ReactNavigationViewModel mNavViewModel;
    @Nullable
    private FragmentNavigator mFragmentNavigator;
    @Nullable
    private OnUpdateNextPageLaunchConfigListener mOnUpdateNextPageLaunchConfigListener;

    @NonNull
    private OnNavBarItemClickListener navBarButtonClickListener;

    @Nullable
    private MenuItemDataProvider mMenuItemDataProvider;

    @Nullable
    private Menu mMenu;

    private final Observer<Route> routeObserver = new Observer<Route>() {
        @Override
        public void onChanged(@Nullable Route route) {
            if (route != null && !route.isCompleted()) {
                Logger.d(TAG, "Delegate:%s received a new navigation route: %s", ElectrodeNavigationFragmentDelegate.this, route.getArguments());

                if (!route.getArguments().containsKey(ReactNavigationViewModel.KEY_NAV_TYPE)) {
                    throw new IllegalStateException("Missing NAV_TYPE in route arguments");
                }

                //NOTE: We can't put KEY_NAV_TYPE as a parcelable since ReactNative side does not support Parcelable deserialization yet.
                ReactNavigationViewModel.Type navType = ReactNavigationViewModel.Type.valueOf(route.getArguments().getString(ReactNavigationViewModel.KEY_NAV_TYPE));
                switch (navType) {
                    case NAVIGATE:
                        navigate(route);
                        break;
                    case UPDATE:
                        update(route);
                        break;
                    case BACK:
                        back(route);
                        break;
                    case FINISH:
                        finish(route);
                        break;
                }
                if (!route.isCompleted()) {
                    throw new IllegalStateException("Should never reach here. A result should be set for the route at this point. Make sure a setResult is called on the route object after the appropriate action is taken on a nav type.");
                }
                Logger.d(TAG, "Nav request handling completed by delegate: %s", ElectrodeNavigationFragmentDelegate.this);
            } else {
                Logger.d(TAG, "Delegate:%s has ignored an already handled route: %s, ", ElectrodeNavigationFragmentDelegate.this, route != null ? route.getArguments() : null);
            }
        }
    };

    /**
     * @param fragment {@link Fragment} current Fragment
     */
    public ElectrodeNavigationFragmentDelegate(@NonNull Fragment fragment) {
        this(fragment, null);
    }

    /**
     * @param fragment       {@link Fragment} current Fragment
     * @param fragmentConfig {@link ElectrodeFragmentConfig} Configuration used by the fragment delegate while creating the view.
     */
    public ElectrodeNavigationFragmentDelegate(@NonNull Fragment fragment, @Nullable ElectrodeNavigationFragmentConfig fragmentConfig) {
        super(fragment, fragmentConfig);
        if (mFragment instanceof ElectrodeNavigationFragmentDelegate.FragmentNavigator) {
            mFragmentNavigator = (ElectrodeNavigationFragmentDelegate.FragmentNavigator) mFragment;
        }

        if (mFragment instanceof OnUpdateNextPageLaunchConfigListener) {
            mOnUpdateNextPageLaunchConfigListener = (OnUpdateNextPageLaunchConfigListener) mFragment;
        }

        navBarButtonClickListener = new ElectrodeNavigationFragmentDelegate.DefaultNavBarButtonClickListener();
    }

    @Override
    @CallSuper
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragment.setHasOptionsMenu(true);
    }

    @CallSuper
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mNavViewModel = ViewModelProviders.of(mFragment).get(ReactNavigationViewModel.class);
        mNavViewModel.getRouteLiveData().observe(mFragment.getViewLifecycleOwner(), routeObserver);
    }

    @CallSuper
    public void onResume() {
        super.onResume();
        mNavViewModel.registerNavRequestHandler();
    }

    @SuppressWarnings("unused")
    @CallSuper
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mMenu = menu;
        updateNavBar(mFragment.getArguments());
    }

    @SuppressWarnings("unused")
    @CallSuper
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mFragment.getActivity() != null) {
                mFragment.getActivity().onBackPressed();
                return true;
            }
        }
        return false;
    }

    @CallSuper
    public void onPause() {
        super.onPause();
        mNavViewModel.unRegisterNavRequestHandler();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMenu != null) {
            mMenu = null;
        }
        mFragmentNavigator = null;
    }

    public void setMenuItemDataProvider(@NonNull MenuItemDataProvider menuItemDataProvider) {
        mMenuItemDataProvider = menuItemDataProvider;
    }

    private void back(@NonNull Route route) {
        //Manage fragment back-stack popping. If the given route.path is not in the stack pop a new fragment.
        boolean result = mElectrodeActivityListener.backToMiniApp(route.getArguments().getString("path"));
        route.setResult(result, !result ? "back navigation failed. component not found in the back stack" : null);
    }

    private void update(@NonNull Route route) {
        if (mFragment.getArguments() != null) {
            mFragment.getArguments().putAll(route.getArguments());
        }
        boolean result = updateNavBar(route.getArguments());
        route.setResult(true, !result ? "failed to update nav bar." : null);
    }

    private void finish(@NonNull Route route) {
        Logger.d(TAG, "finish triggered by RN. Hosting activity will be notified.");
        mElectrodeActivityListener.finishFlow(NavUtils.getPayload(route.getArguments()));
        route.setResult(true, null);
    }

    private void navigate(@NonNull Route route) {
        final String path = NavUtils.getPath(route.getArguments());
        Logger.d(TAG, "navigating to: " + path);

        if (!TextUtils.isEmpty(path)) {
            //If the hosting activity or fragment has not handled the navigation fall back to the default.
            if (!mElectrodeActivityListener.navigate(route) && (mFragmentNavigator == null || !mFragmentNavigator.navigate(route))) {
                LaunchConfig launchConfig = createDefaultLaunchConfig(route);
                if (mOnUpdateNextPageLaunchConfigListener != null) {
                    //noinspection ConstantConditions
                    mOnUpdateNextPageLaunchConfigListener.updateNextPageLaunchConfig(path, launchConfig);
                }
                //noinspection ConstantConditions
                mElectrodeActivityListener.startMiniAppFragment(path, launchConfig);
            }
            route.setResult(true, "Navigation completed.");
        } else {
            route.setResult(false, "Navigation failed. Received empty/null path");
        }
    }


    private LaunchConfig createDefaultLaunchConfig(@NonNull Route route) {
        LaunchConfig launchConfig = new LaunchConfig();
        launchConfig.setInitialProps(route.getArguments());
        launchConfig.setFragmentManager(mFragmentConfig != null && mFragmentConfig.useChildFragmentManager ? mFragment.getChildFragmentManager() : null);
        return launchConfig;
    }

    /**
     * Override this method if you want to update launch config before it's built.
     *
     * @param nextPageName {@link String} Next page name
     * @param launchConfig {@link LaunchConfig} with default config values pre-populated for the next page fragment launch.
     *                     Update this config with new props, different fragment class names, layouts etc.
     */
    protected void updateNextPageLaunchConfig(@NonNull final String nextPageName, @NonNull final LaunchConfig launchConfig) {
        //Do nothing. Placeholder for child delegates to add data.
    }

    private boolean updateNavBar(@Nullable Bundle arguments) {
        if (arguments != null) {
            NavigationBar navigationBar = NavUtils.getNavBar(arguments);
            if (navigationBar != null) {
                updateNavBar(navigationBar);
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    private class DefaultNavBarButtonClickListener implements OnNavBarItemClickListener {

        private OnNavBarItemClickListener suppliedButtonClickListener;

        DefaultNavBarButtonClickListener() {
            this.suppliedButtonClickListener = (mFragment instanceof OnNavBarItemClickListener) ? (OnNavBarItemClickListener) mFragment : null;
        }

        @Override
        public boolean onNavBarButtonClicked(@NonNull NavigationBarButton button, @NonNull MenuItem item) {
            if (suppliedButtonClickListener == null || !suppliedButtonClickListener.onNavBarButtonClicked(button, item)) {
                EnNavigationApi.events().emitOnNavButtonClick(button.getId());
            }
            return true;
        }
    }


    /**
     * Fragments can implement this interface when it needs to override a navigate call.
     */
    public interface FragmentNavigator {
        /**
         * Use to delegate a navigate call to the fragment before it is being handled by the delegate.
         *
         * @param route Route object
         * @return true | false
         */
        boolean navigate(Route route);
    }

    /**
     * Fragments can implement this interface when they need to customize the defaultLaunchConfog {@link LaunchConfig} while navigating to a new page.
     */
    public interface OnUpdateNextPageLaunchConfigListener {
        /**
         * Simply update the config here.
         *
         * @param nextPageName        {@link String} Next page name
         * @param defaultLaunchConfig {@link LaunchConfig} with default values for the next page launch config.
         */
        void updateNextPageLaunchConfig(@NonNull final String nextPageName, @NonNull final LaunchConfig defaultLaunchConfig);
    }

    private void updateNavBar(@NonNull NavigationBar navigationBar) {
        Logger.d(TAG, "Updating nav bar: %s", navigationBar);
        updateTitle(navigationBar);

        if (mMenu != null && mFragment.getActivity() != null) {
            MenuUtil.updateMenuItems(mMenu, navigationBar, navBarButtonClickListener, mMenuItemDataProvider, mFragment.getActivity());
        }
    }

    private void updateTitle(@NonNull NavigationBar navigationBar) {
        Activity activity = mFragment.getActivity();
        if (activity != null)
            if (activity instanceof AppCompatActivity) {
                ActionBar actionBar;
                actionBar = ((AppCompatActivity) activity).getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setTitle("");
                    actionBar.setTitle(navigationBar.getTitle());
                }
            } else {
                android.app.ActionBar actionBar = activity.getActionBar();
                if (actionBar != null) {
                    actionBar.setTitle("");
                    actionBar.setTitle(navigationBar.getTitle());
                }
            }
    }
}
