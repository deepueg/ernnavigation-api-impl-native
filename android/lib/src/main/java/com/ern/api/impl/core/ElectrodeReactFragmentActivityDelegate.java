/*
 * Copyright 2019 Walmart Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ern.api.impl.core;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.ern.api.impl.core.ElectrodeReactFragmentDelegate.MiniAppRequestListener.AddToBackStackState;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.walmartlabs.electrode.reactnative.bridge.helpers.Logger;
import com.walmartlabs.ern.container.ElectrodeReactActivityDelegate;

import static com.ern.api.impl.core.ElectrodeReactFragmentDelegate.MiniAppRequestListener.ADD_TO_BACKSTACK;


public class ElectrodeReactFragmentActivityDelegate extends ElectrodeReactActivityDelegate implements LifecycleObserver {

    private static final String TAG = ElectrodeReactFragmentActivityDelegate.class.getSimpleName();

    protected FragmentActivity mFragmentActivity;

    private StartMiniAppConfig mDefaultMiniAppConfig;
    private String mRootComponentName;

    private boolean mUpEnabledForRoot;

    /**
     * Set this to true if you want to enable up navigation for the root component.
     * <p>
     * PS: This method needs to be called before {@link #onCreate(Bundle)} is called.
     */
    public void setUpEnabledForRoot(boolean upEnabledForRoot) {
        mUpEnabledForRoot = upEnabledForRoot;
    }

    /**
     * @deprecated call {@link #ElectrodeReactFragmentActivityDelegate(FragmentActivity, String, StartMiniAppConfig)} instead.
     * This has been deprecated as part of removing {@link DataProvider} interface and replacing it with {@link StartMiniAppConfig} as a constructor param.
     * Once updated to use the new Constructor, it's no longer required to implement {@link DataProvider} by the hosting activity.
     */
    @Deprecated
    public ElectrodeReactFragmentActivityDelegate(@NonNull FragmentActivity activity) {
        this(activity, null, null);
    }

    public ElectrodeReactFragmentActivityDelegate(@NonNull final FragmentActivity activity, @Nullable final String rootComponentName, @NonNull final StartMiniAppConfig defaultStartMiniAppConfig) {
        super(activity, null);
        mFragmentActivity = activity;
        //noinspection ConstantConditions
        if (activity instanceof DataProvider && defaultStartMiniAppConfig == null/*Keeping this for backward compatibility*/) {
            DataProvider dataProvider = (DataProvider) activity;
            mRootComponentName = dataProvider.getRootComponentName();
            mDefaultMiniAppConfig = new StartMiniAppConfig.Builder(dataProvider.miniAppFragmentClass())
                    .props(dataProvider.getProps())
                    .fragmentContainerId(dataProvider.getFragmentContainerId())
                    .build();
        } else {
            mRootComponentName = rootComponentName;
            mDefaultMiniAppConfig = defaultStartMiniAppConfig;
        }

        if (mFragmentActivity instanceof BackKeyHandler) {
            setBackKeyHandler((BackKeyHandler) mFragmentActivity);
        }
    }

    //Not putting this under the OnLifecycleEvent sine we need the savedInstanceState
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            if (mDefaultMiniAppConfig.fragmentContainerId != 0) {
                startReactNative();
            } else {
                Logger.i(TAG, "ElectrodeReactFragmentActivityDelegate.onCreate() Will not start a fragment as the dataProvider is missing a getFragmentContainerId():%s",
                        mDefaultMiniAppConfig.fragmentContainerId);
            }
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
        mDefaultMiniAppConfig = null;
        super.onDestroy();
    }

    /***
     *
     * @param menu
     * @return
     * @deprecated This delegate is no longer needed. remove from your activity.
     */
    @Deprecated
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

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

    private void startReactNative() {
        Logger.d(TAG, "Starting react native root component. Loading the react view inside a fragment.");
        startMiniAppFragment(mRootComponentName, mDefaultMiniAppConfig);
    }

    /**
     * @deprecated use {@link #startMiniAppFragment(String, StartMiniAppConfig)} instead
     */
    @Deprecated
    public void startMiniAppFragment(@NonNull String componentName, @Nullable Bundle props) {
        StartMiniAppConfig config = new StartMiniAppConfig.Builder(mDefaultMiniAppConfig.fragmentClass).props(props).build();
        startMiniAppFragment(componentName, config);
    }

    /**
     * @deprecated Start using {@link #startMiniAppFragment(String, StartMiniAppConfig)}
     */
    @Deprecated
    public void startMiniAppFragment(@NonNull Class<? extends Fragment> fragmentClass, @NonNull String componentName, @Nullable Bundle props) {
        StartMiniAppConfig config = new StartMiniAppConfig.Builder(fragmentClass).props(props).build();
        startMiniAppFragment(componentName, config);
    }

    /**
     * @deprecated Start using {@link #startMiniAppFragment(String, StartMiniAppConfig)} instead by passing the props inside the {@link StartMiniAppConfig} param
     */
    @Deprecated
    public void startMiniAppFragment(@NonNull String componentName, @Nullable Bundle props, @NonNull StartMiniAppConfig startMiniAppConfig) {
        if (props == null) {
            props = new Bundle();
        }
        props.putString(ActivityDelegateConstants.KEY_MINI_APP_COMPONENT_NAME, componentName);

        Logger.d(TAG, "startMiniAppFragment: fragmentClass->%s, componentName->%s, props->%s", startMiniAppConfig.fragmentClass.getSimpleName(), componentName, props);

        switchToFragment(props, ADD_TO_BACKSTACK, startMiniAppConfig);
    }

    public void startMiniAppFragment(@NonNull String componentName, @NonNull StartMiniAppConfig startMiniAppConfig) {
        Bundle props = startMiniAppConfig.props != null ? startMiniAppConfig.props : new Bundle();
        props.putString(ActivityDelegateConstants.KEY_MINI_APP_COMPONENT_NAME, componentName);

        Logger.d(TAG, "startMiniAppFragment: fragmentClass->%s, componentName->%s, props->%s", startMiniAppConfig.fragmentClass.getSimpleName(), componentName, props);

        switchToFragment(props, ADD_TO_BACKSTACK, startMiniAppConfig);
    }

    private void switchToFragment(@NonNull Bundle bundle,
                                  @AddToBackStackState int addToBackStackState, @NonNull StartMiniAppConfig startMiniAppConfig) {
        try {

            Class<? extends Fragment> fragmentClass = startMiniAppConfig.fragmentClass.equals(ElectrodeReactFragmentDelegate.DefaultFragmentIndicator.class) ? mDefaultMiniAppConfig.fragmentClass : startMiniAppConfig.fragmentClass;
            Fragment fragment = fragmentClass.newInstance();

            String tag = (bundle.containsKey(ActivityDelegateConstants.KEY_MINI_APP_FRAGMENT_TAG)) ? bundle.getString(ActivityDelegateConstants.KEY_MINI_APP_FRAGMENT_TAG) : bundle.getString(ActivityDelegateConstants.KEY_MINI_APP_COMPONENT_NAME);
            Logger.d(TAG, "Switching to a new fragment, tag: %s, fragment: %s ", tag, fragment);

            final FragmentManager fragmentManager = startMiniAppConfig.fragmentManager != null ? startMiniAppConfig.fragmentManager : mFragmentActivity.getSupportFragmentManager();
            final FragmentTransaction transaction = fragmentManager.beginTransaction();

            if (ADD_TO_BACKSTACK == addToBackStackState) {
                transaction.addToBackStack(tag);
            }
            bundle.putBoolean(ActivityDelegateConstants.KEY_MINI_APP_FRAGMENT_SHOW_UP_ENABLED, shouldShowUpEnabled());
            fragment.setArguments(bundle);
            if (fragment instanceof BottomSheetDialogFragment) {
                ((BottomSheetDialogFragment) fragment).show(fragmentManager, tag);
            } else {
                int fragmentContainerId = (startMiniAppConfig.fragmentContainerId != 0) ? startMiniAppConfig.fragmentContainerId : mDefaultMiniAppConfig.fragmentContainerId;
                transaction.replace(fragmentContainerId, fragment, tag);
                transaction.commit();
            }
        } catch (Exception e) {
            Logger.e(TAG, "Failed to create " + startMiniAppConfig.fragmentClass.getName() + " fragment", e);
        }
    }

    private boolean shouldShowUpEnabled() {
        int backStackCount = mFragmentActivity.getSupportFragmentManager().getBackStackEntryCount();
        return backStackCount > 0 || (backStackCount == 0 && mUpEnabledForRoot);
    }

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

    /**
     * Use the new {@link ElectrodeReactFragmentActivityDelegate} constructor and pass the {@link StartMiniAppConfig} as the default config.
     */
    @Deprecated
    public interface DataProvider {

        /**
         * React native component name that will be rendered when the activity is first launched.
         *
         * @return String
         */
        @NonNull
        String getRootComponentName();


        /**
         * Id for the fragment container.
         *
         * @return IdRes of the fragment holder in your layout xml.
         */
        @IdRes
        int getFragmentContainerId();

        /**
         * Props that needs to be passed to the root component defined in your activity.
         *
         * @return Bundle
         */
        @Nullable
        Bundle getProps();


        /***
         * Return the default fragment class that needs to be instantiated to render react native component.
         * The returned fragment class will serve as the DefaultFragment if a FragmentClass is not passed inside {@link StartMiniAppConfig}
         *
         * Reference: {@link DefaultMiniAppFragment} {@link com.ern.api.impl.navigation.MiniAppNavFragment}
         * @return Class
         */
        @NonNull
        Class<? extends Fragment> miniAppFragmentClass();
    }

    /**
     * Class that defines the custom configurations that can be passed while starting a new MiniApp fragment.
     */
    public static class StartMiniAppConfig {
        @Nullable
        final FragmentManager fragmentManager;

        @NonNull
        final Class<? extends Fragment> fragmentClass;


        @IdRes
        final int fragmentContainerId;

        /**
         * Any props that you want to pass to the component.
         */
        final Bundle props;

        final String fragmentTag;

        boolean showAsBottomSheet;

        private StartMiniAppConfig(Builder builder) {
            fragmentManager = builder.fragmentManager;
            fragmentClass = builder.fragmentClass;
            fragmentContainerId = builder.fragmentContainerId;
            props = builder.props;
            fragmentTag = builder.fragmentTag;
            showAsBottomSheet = builder.showAsBottomSheet;
        }

        public static class Builder {
            FragmentManager fragmentManager;
            Class<? extends Fragment> fragmentClass;
            @IdRes
            int fragmentContainerId = 0;
            Bundle props;
            String fragmentTag;
            boolean showAsBottomSheet;


            /**
             * Fragment class responsible for hosting the react native view.
             *
             * @param fragmentClass A fragment class that properly creates the {@link ElectrodeReactFragmentDelegate} instance.
             *                      Refer: {@link ElectrodeReactCoreFragment}
             */
            public Builder(@NonNull Class<? extends Fragment> fragmentClass) {
                this.fragmentClass = fragmentClass;
            }

            /**
             * Pass a fragmentManager that you want the delegate to use to switch fragments. If not passed the {@link AppCompatActivity#getSupportFragmentManager()} would be used to switch between fragments.
             *
             * @param fragmentManager {@link FragmentManager}
             * @return Builder
             */
            public Builder fragmentManager(@Nullable FragmentManager fragmentManager) {
                this.fragmentManager = fragmentManager;
                return this;
            }

            /**
             * ViewGroup id to which the fragment needs to be loaded in your layout xml.
             *
             * @param fragmentContainerId {@link IdRes}
             * @return Builder
             */
            public Builder fragmentContainerId(@IdRes int fragmentContainerId) {
                this.fragmentContainerId = fragmentContainerId;
                return this;
            }

            /**
             * Optional props that you need to pass to a react native component as initialProps.
             *
             * @param props {@link Bundle}
             * @return Builder
             */
            public Builder props(@Nullable Bundle props) {
                this.props = props;
                return this;
            }

            public Builder showAsBottomSheet(boolean value) {
                this.showAsBottomSheet = value;
                return this;
            }

            public StartMiniAppConfig build() {
                return new StartMiniAppConfig(this);
            }
        }
    }
}
