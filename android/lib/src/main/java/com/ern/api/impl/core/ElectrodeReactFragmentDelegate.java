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

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.IdRes;
import androidx.annotation.IntDef;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.ernnavigationApi.ern.model.ErnNavRoute;
import com.facebook.react.ReactRootView;
import com.walmartlabs.electrode.reactnative.bridge.helpers.Logger;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ElectrodeReactFragmentDelegate<T extends ElectrodeReactFragmentDelegate.MiniAppRequestListener> implements LifecycleObserver {
    private static final String TAG = ElectrodeReactFragmentDelegate.class.getSimpleName();

    protected final Fragment mFragment;
    protected T mMiniAppRequestListener;
    private DataProvider mDataProvider;

    private ReactRootView mMiniAppView;
    private View mRootView;

    private String miniAppComponentName;

    protected ElectrodeReactFragmentDelegate(@NonNull Fragment fragment) {
        mFragment = fragment;
        if (mFragment instanceof ElectrodeReactFragmentDelegate.DataProvider) {
            mDataProvider = (DataProvider) mFragment;
        } else {
            throw new IllegalStateException("Fragment should implement ElectrodeReactFragmentDelegate.DataProvider.");
        }
    }

    @SuppressWarnings("WeakerAccess")
    public void onAttach(Context context) {
        if (context instanceof MiniAppRequestListener) {
            //noinspection unchecked
            mMiniAppRequestListener = (T) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement a MiniAppRequestListener");
        }
    }

    @SuppressWarnings("WeakerAccess")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        //PlaceHolder
    }

    /**
     * Returns a ReactRootView of the passed MiniApp component (component name provided inside arguments under #KEY_MINI_APP_COMPONENT_NAME)
     * <p> Or
     * Returns a View hierarchy if a valid {@link DataProvider#fragmentLayoutId()} layout xml resource is passed.
     * Pass a valid {@link DataProvider#reactViewContainerId()} for the MiniApp component(provided inside arguments under #KEY_MINI_APP_COMPONENT_NAME) to be inflated properly inside the view hierarchy.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return View
     * <p>
     * Throws {@link IllegalStateException} when either a MiniApp component name is not passed as KEY_MINI_APP_COMPONENT_NAME in arguments or a valid lauout xml is not provided via {@link DataProvider#fragmentLayoutId()}
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mFragment.getArguments() != null) {
            miniAppComponentName = mFragment.getArguments().getString(ActivityDelegateConstants.KEY_MINI_APP_COMPONENT_NAME);
        }

        Logger.d(TAG, "delegate.onCreateView() called. MiniApp component name: " + miniAppComponentName);

        if (mMiniAppView == null) {
            if (!TextUtils.isEmpty(miniAppComponentName)) {
                mMiniAppView = (ReactRootView) mMiniAppRequestListener.createReactNativeView(miniAppComponentName, initialProps(savedInstanceState != null));
            } else {
                Logger.d(TAG, "MiniApp view not created");
            }
        }

        View rootView;
        if (mDataProvider.fragmentLayoutId() != DataProvider.NONE) {
            if (mRootView == null) {
                mRootView = inflater.inflate(mDataProvider.fragmentLayoutId(), container, false);

                setUpToolBarIfPresent();

                if (mDataProvider.reactViewContainerId() != DataProvider.NONE && mMiniAppView != null) {
                    View view = mRootView.findViewById(mDataProvider.reactViewContainerId());
                    if (view instanceof ViewGroup) {
                        ((ViewGroup) view).addView(mMiniAppView);
                    } else {
                        throw new IllegalStateException("reactViewContainerId() should represent a ViewGroup to be able to add a react root view inside it.");
                    }
                } else {
                    Logger.i(TAG, "Missing reactViewContainerId() or mMiniAppView is null. Will not add MiniApp view explicitly. Do you have a MiniAppView component defined in your layout xml resource file?.");
                }
            }
            Logger.d(TAG, "Returning view inflated using a custom layout.");
            rootView = mRootView;
        } else {
            if (mMiniAppView == null) {
                throw new IllegalStateException("MiniAppView is null. Should never reach here. onCreateView() should return a non-null view.");
            }
            Logger.d(TAG, "Returning a react root view.");
            rootView = mMiniAppView;
        }

        boolean showHomeAsUpEnabled = mFragment.getArguments().getBoolean(ActivityDelegateConstants.KEY_MINI_APP_FRAGMENT_SHOW_UP_ENABLED, false);
        handleUpNavigation(showHomeAsUpEnabled);

        return rootView;
    }

    private void setUpToolBarIfPresent() {
        if (mDataProvider.toolBarId() != DataProvider.NONE) {
            Toolbar toolBar = mRootView.findViewById(mDataProvider.toolBarId());
            if (mFragment.getActivity() instanceof AppCompatActivity) {
                AppCompatActivity appCompatActivity = (AppCompatActivity) mFragment.getActivity();
                if (appCompatActivity.getSupportActionBar() == null) {
                    appCompatActivity.setSupportActionBar(toolBar);
                } else {
                    Logger.w(TAG, "Hiding fragment layout toolBar. The Activity already has an action bar setup.");
                    toolBar.setVisibility(View.GONE);
                }
            } else {
                Logger.w(TAG, "Ignoring toolbar, looks like the activity is not an AppCompatActivity. Make sure you configure thr toolbar in your fragments onCreateView()");
            }
        }
    }

    @NonNull
    private Bundle initialProps(boolean isFragmentBeingReconstructed) {
        final Bundle initialProps = mFragment.getArguments() == null ? new Bundle() : mFragment.getArguments();

        //NOTE: If/When the system re-constructs a fragment from a previous state a stored Bundle is getting converted to a ParcelableData.
        //When this bundle is send across React native , RN frameworks WritableArray does not support parcelable conversion.
        //To avoid this issue we recreate the ErnNavRoute object from the bundle and regenerate a new bundle which again replaces the  ParcelableData with proper bundle object.
        //Checking for the existence of "path" key since that is the only required property to successfully build an ErnNavRoute object.
        if (isFragmentBeingReconstructed && initialProps.containsKey("path")) {
            initialProps.putAll(new ErnNavRoute(initialProps).toBundle());
        }

        Bundle props = mDataProvider.initialProps();
        if (props != null) {
            initialProps.putAll(props);
        }

        props = mMiniAppRequestListener.globalProps();
        if (props != null) {
            initialProps.putAll(props);
        }

        return initialProps;
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        //PlaceHolder
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    @SuppressWarnings("WeakerAccess")
    public void onStart() {
        Logger.d(TAG, "inside onStart");
        //PlaceHolder
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        Logger.d(TAG, "inside onResume");
        //PlaceHolder
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        Logger.d(TAG, "inside onPause");
        //PlaceHolder
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        Logger.d(TAG, "inside onStop");
        //PlaceHolder
    }

    @SuppressWarnings("WeakerAccess")
    public void onDestroyView() {
        Logger.d(TAG, "inside onDestroyView");
        //PlaceHolder
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    @CallSuper
    public void onDestroy() {
        Logger.d(TAG, "inside onDestroy");
        if (mMiniAppView != null) {
            assert mFragment.getArguments() != null;
            mMiniAppRequestListener.removeReactNativeView(miniAppComponentName, mMiniAppView);
            mMiniAppView = null;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public void onDetach() {
        mMiniAppRequestListener = null;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + miniAppComponentName;
    }

    protected String getReactComponentName() {
        if (mFragment.getArguments() != null && mFragment.getArguments().getString(ActivityDelegateConstants.KEY_MINI_APP_COMPONENT_NAME) != null) {
            return mFragment.getArguments().getString(ActivityDelegateConstants.KEY_MINI_APP_COMPONENT_NAME);
        }
        return "NAME_NOT_SET_YET";
    }

    private void handleUpNavigation(boolean showHomeAsUpEnabled) {
        if (mFragment.getActivity() instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) mFragment.getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(showHomeAsUpEnabled);
            }
        } else if (mFragment.getActivity() != null) {
            android.app.ActionBar actionBar = mFragment.getActivity().getActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(showHomeAsUpEnabled);
            }
        }
    }

    /**
     * This needs to be implemented by the fragment. The APIs in this interface should ask for data that needs to be provided by the fragment.
     */
    public interface DataProvider {
        int NONE = 0;

        /**
         * Initial properties needed for rendering the react component
         *
         * @return Bundle
         */
        @Nullable
        Bundle initialProps();


        /***
         * Return the layout xml that will be used by the fragment to create the view.
         * This is the layout where you can place your toolbar(optional) and an empty view group({@link #reactViewContainerId()}) to inflate the react native view a.k.a MiniApp View Component.
         * @return int, a valid @{@link LayoutRes } or {@link #NONE}
         */
        @LayoutRes
        int fragmentLayoutId();

        /**
         * Return the container ViewGroup id to which a react native view can be added.
         *
         * @return int a valid {@link IdRes} or {@link #NONE}
         */
        @IdRes
        int reactViewContainerId();

        /**
         * Provide the id of the toolbar if tool bar is part of the fragment layout, return NONE otherwise.
         *
         * @return a valid {@link IdRes} or {@link #NONE}
         */
        @IdRes
        int toolBarId();
    }

    /***
     * Interface that connects the fragment delegate to the hosting activity.
     */
    public interface MiniAppRequestListener {

        int ADD_TO_BACKSTACK = 0;
        int DO_NOT_ADD_TO_BACKSTACK = 1;

        @IntDef({ADD_TO_BACKSTACK, DO_NOT_ADD_TO_BACKSTACK})
        @Retention(RetentionPolicy.SOURCE)
        @interface AddToBackStackState {
        }

        /**
         * Returns a react root view for the given mini app.
         *
         * @param appName React native root component name
         * @param props   Optional properties for the component
         * @return
         */
        View createReactNativeView(@NonNull String appName, @Nullable Bundle props);

        @Deprecated
        void removeReactNativeView(@NonNull String appName);

        /**
         * Un-mounts a given react native view component. Typically done when your fragment is destroyed.
         *
         * @param componentName viewComponentName
         * @param reactRootView {@link ReactRootView} instance
         */
        void removeReactNativeView(@NonNull String componentName, @NonNull ReactRootView reactRootView);

        /**
         * starts a new fragment and inflate it with the given react component.
         *
         * @param componentName react view component name.
         * @param props         optional properties for the component rendering.
         */
        void startMiniAppFragment(@NonNull String componentName, @Nullable Bundle props);

        /**
         * starts a new fragment and inflate it with the given react component.
         *
         * @param componentName react view component name.
         * @param props         optional properties for the component rendering.
         */
        void startMiniAppFragment(@NonNull Class<? extends Fragment> fragmentClass, @NonNull String componentName, @Nullable Bundle props);

        /**
         * Utilize this api to pass in global props that is required by all components involved in a feature.
         *
         * @return Bundle common props required for all the RN components for a specific flow.
         */
        @Nullable
        Bundle globalProps();

        /**
         * Cal this to intercept react-native dev menu
         *
         * @param event
         * @return true if the menu was shown false otherwise
         */
        boolean showDevMenuIfDebug(KeyEvent event);
    }


    /**
     * This class is used as an indicator by the {@link com.ern.api.impl.core.ElectrodeReactFragmentActivityDelegate} to fall back to the DefaultFragment provided by the hosting activity's {@link com.ern.api.impl.core.ElectrodeReactFragmentActivityDelegate.DataProvider}
     */
    public static final class DefaultFragmentIndicator extends Fragment {
    }
}
