package com.ern.api.impl.core;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.walmartlabs.electrode.reactnative.bridge.helpers.Logger;
import com.walmartlabs.ern.container.ElectrodeReactActivityDelegate;

import static com.ern.api.impl.core.ElectrodeReactFragmentDelegate.MiniAppRequestListener.ADD_TO_BACKSTACK;

public class ElectrodeBaseActivityDelegate extends ElectrodeReactActivityDelegate implements LifecycleObserver {
    private static final String TAG = ElectrodeBaseActivityDelegate.class.getSimpleName();

    @SuppressWarnings("WeakerAccess")
    protected FragmentActivity mFragmentActivity;
    private final LaunchConfig mDefaultLaunchConfig;
    private final String mRootComponentName;
    private boolean mUpEnabledForRoot;

    /**
     * Set this to true if you want to enable up navigation for the root component.
     * <p>
     * PS: This method needs to be called before {@link #onCreate(Bundle)} is called.
     */
    @SuppressWarnings("unused")
    public void setUpEnabledForRoot(boolean upEnabledForRoot) {
        mUpEnabledForRoot = upEnabledForRoot;
    }

    /**
     * @param activity            Hosting activity
     * @param rootComponentName   First react native component to be launched.
     * @param initialLaunchConfig : {@link LaunchConfig} that acts as the the initial configuration to load the rootComponent.
     *                            This configuration will also be used as a default configuration when the root component tries to navigate to a new pages if a proper launch config is passed inside {@link #startMiniAppFragment(String, LaunchConfig)}.
     */
    public ElectrodeBaseActivityDelegate(@NonNull FragmentActivity activity, @Nullable String rootComponentName, @NonNull LaunchConfig initialLaunchConfig) {
        super(activity, null);

        mFragmentActivity = activity;
        mRootComponentName = rootComponentName;
        mDefaultLaunchConfig = initialLaunchConfig;
        if (mFragmentActivity instanceof BackKeyHandler) {
            setBackKeyHandler((BackKeyHandler) mFragmentActivity);
        }
    }

    //Not putting this under the OnLifecycleEvent sine we need the savedInstanceState
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Logger.d(TAG, "Starting react native root component(%s). Loading the react view inside a fragment.", mRootComponentName);
            startMiniAppFragment(mRootComponentName, mDefaultLaunchConfig);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart() {
        //PlaceHolder
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    @Override
    public void onResume() {
        super.onResume();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    @Override
    public void onPause() {
        super.onPause();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        //PlaceHolder
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    @Override
    public void onDestroy() {
        mFragmentActivity = null;
        super.onDestroy();
    }

    @SuppressWarnings("unused")
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mFragmentActivity.onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    public boolean onBackPressed() {
        Logger.d(TAG, "Handling back press");
        int backStackEntryCount = mFragmentActivity.getSupportFragmentManager().getBackStackEntryCount();
        if (backStackEntryCount == 1) {
            Logger.d(TAG, "Last item in the back stack, will finish the activity.");
            mFragmentActivity.finish();
            return true;
        } else {
            return false;
        }
    }

    public void startMiniAppFragment(@NonNull String componentName, @NonNull LaunchConfig launchConfig) {
        Logger.d(TAG, "entering startMiniAppFragment for component: %s", componentName);
        Fragment fragment;
        Class<? extends Fragment> fClazz = launchConfig.fragmentClass != null ? launchConfig.fragmentClass : mDefaultLaunchConfig.fragmentClass;
        try {
            if (fClazz == null) {
                throw new RuntimeException("Missing fragment class in both launchConfig and defaultLaunchConfig. This needs to be set in one of these configurations.");
            }
            fragment = fClazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create " + fClazz + " fragment", e);
        }

        Bundle props = launchConfig.initialProps != null ? launchConfig.initialProps : new Bundle();
        props.putString(ActivityDelegateConstants.KEY_MINI_APP_COMPONENT_NAME, componentName);
        props.putBoolean(ActivityDelegateConstants.KEY_MINI_APP_FRAGMENT_SHOW_UP_ENABLED, shouldShowUpEnabled());
        fragment.setArguments(props);

        Logger.d(TAG, "starting fragment: fragmentClass->%s, props->%s", fragment.getClass().getSimpleName(), props);
        switchToFragment(fragment, launchConfig, componentName);
    }

    private void switchToFragment(@NonNull Fragment fragment, @NonNull LaunchConfig launchConfig, @Nullable String tag) {
        if (launchConfig.showAsBottomSheet) {
            Logger.w(TAG, "TODO: call fragment.show() for a bottom sheet experience");
            //TODO: call fragment.show() for a bottom sheet experience
        } else {
            final FragmentManager fragmentManager = getFragmentManager(launchConfig);
            int fragmentContainerId = (launchConfig.fragmentContainerId != LaunchConfig.NONE) ? launchConfig.fragmentContainerId : mDefaultLaunchConfig.fragmentContainerId;
            if (fragmentContainerId == LaunchConfig.NONE) {
                throw new RuntimeException("Missing fragmentContainerId to add the " + fragment.getClass().getSimpleName() + ". Should never reach here.");
            }

            final FragmentTransaction transaction = fragmentManager.beginTransaction();
            if (ADD_TO_BACKSTACK == launchConfig.addToBackStack) {
                Logger.d(TAG, "fragment(%s) added to back stack", tag);
                transaction.addToBackStack(tag);
            }
            transaction.replace(fragmentContainerId, fragment, tag);
            transaction.commit();
            Logger.d(TAG, "startMiniAppFragment completed successfully.");
        }
    }

    private FragmentManager getFragmentManager(@NonNull LaunchConfig launchConfig) {
        if (launchConfig.fragmentManager != null) {
            return launchConfig.fragmentManager;
        }

        if (mDefaultLaunchConfig.fragmentManager != null) {
            return mDefaultLaunchConfig.fragmentManager;
        }

        return mFragmentActivity.getSupportFragmentManager();
    }

    private boolean shouldShowUpEnabled() {
        int backStackCount = mFragmentActivity.getSupportFragmentManager().getBackStackEntryCount();
        return backStackCount > 0 || (backStackCount == 0 && mUpEnabledForRoot);
    }

    @SuppressWarnings("unused")
    public boolean switchBackToFragment(@Nullable String tag) {
        Logger.d(TAG, "switchBackToFragment, tag:  %s", tag);
        final FragmentManager manager = mFragmentActivity.getSupportFragmentManager();

        int backStackCount = manager.getBackStackEntryCount();
        if (backStackCount == 1) {
            if (tag == null || tag.equals(manager.getBackStackEntryAt(0).getName())) {
                Logger.d(TAG, "Last fragment in the stack, will finish the activity.");
                mFragmentActivity.finish();
                return true;
            }
        }

        return manager.popBackStackImmediate(tag, 0);
    }
}
