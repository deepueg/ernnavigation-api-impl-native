package com.ern.api.impl.core;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.IntDef;
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

public class ElectrodeBaseFragmentDelegate<T extends ElectrodeBaseFragmentDelegate.ElectrodeActivityListener, C extends ElectrodeFragmentConfig> implements LifecycleObserver {
    private static final String TAG = ElectrodeBaseFragmentDelegate.class.getSimpleName();

    protected final Fragment mFragment;

    protected T mElectrodeActivityListener;

    @Nullable
    protected C mFragmentConfig;

    @Nullable
    private ReactRootView mMiniAppView;

    @Nullable
    private View mRootView;

    private String miniAppComponentName = "NAME_NOT_SET_YET";

    @SuppressWarnings("unused")
    protected ElectrodeBaseFragmentDelegate(@NonNull Fragment fragment) {
        this(fragment, null);
    }

    /**
     * @param fragment       Hosting fragment
     * @param fragmentConfig Optional config that can be passed if your fragment needs to have a custom layout, etc.
     */
    protected ElectrodeBaseFragmentDelegate(@NonNull Fragment fragment, @Nullable C fragmentConfig) {
        mFragment = fragment;
        mFragmentConfig = fragmentConfig;
    }

    @SuppressWarnings("unused")
    public void onAttach(Context context) {
        if (context instanceof ElectrodeBaseFragmentDelegate.ElectrodeActivityListener) {
            //noinspection unchecked
            mElectrodeActivityListener = (T) context;
        } else {
            throw new RuntimeException(context.toString()
                    + "Activity must implement a ElectrodeActivityListener");
        }
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        //PlaceHolder
    }

    /**
     * Returns a ReactRootView of the passed MiniApp component (component name provided inside arguments under #KEY_MINI_APP_COMPONENT_NAME)
     * <p> Or
     * Returns a View hierarchy if a valid {@link ElectrodeFragmentConfig#fragmentLayoutId} layout xml resource is passed.
     * Pass a valid {@link ElectrodeFragmentConfig#reactViewContainerId} for the MiniApp component(provided inside arguments under #KEY_MINI_APP_COMPONENT_NAME) to be inflated properly inside the view hierarchy.
     *
     * @param inflater           The LayoutInflater object that can be used to inflates
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return View
     * <p>
     * Throws {@link IllegalStateException} when either a MiniApp component name is not passed as KEY_MINI_APP_COMPONENT_NAME in arguments or a valid lauout xml is not provided via {@link ElectrodeFragmentConfig#fragmentLayoutId}
     */
    @SuppressWarnings("unused")
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mFragment.getArguments() != null) {
            miniAppComponentName = mFragment.getArguments().getString(ActivityDelegateConstants.KEY_MINI_APP_COMPONENT_NAME);
        }

        Logger.d(TAG, "delegate.onCreateView() called. MiniApp component name: " + miniAppComponentName);

        if (mMiniAppView == null) {
            if (!TextUtils.isEmpty(miniAppComponentName)) {
                mMiniAppView = (ReactRootView) mElectrodeActivityListener.createReactNativeView(miniAppComponentName, initialProps(savedInstanceState != null));
            } else {
                Logger.i(TAG, "Missing miniAppComponentName inside arguments, will not create a MiniApp view.");
            }
        }

        View rootView;
        if (mFragmentConfig != null && mFragmentConfig.fragmentLayoutId != ElectrodeFragmentConfig.NONE) {
            if (mRootView == null) {
                mRootView = inflater.inflate(mFragmentConfig.fragmentLayoutId, container, false);

                setUpToolBarIfPresent();

                if (mFragmentConfig.reactViewContainerId != ElectrodeFragmentConfig.NONE && mMiniAppView != null) {
                    @SuppressWarnings("ConstantConditions")
                    View view = mRootView.findViewById(mFragmentConfig.reactViewContainerId);
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
        if (mFragmentConfig != null && mFragmentConfig.toolBarId != ElectrodeFragmentConfig.NONE) {
            if (mRootView == null) {
                throw new IllegalStateException("Should never reach here. mRootView should have been populated before calling this method");
            }
            Toolbar toolBar = mRootView.findViewById(mFragmentConfig.toolBarId);
            if (mFragment.getActivity() instanceof AppCompatActivity) {
                AppCompatActivity appCompatActivity = (AppCompatActivity) mFragment.getActivity();
                if (appCompatActivity.getSupportActionBar() == null) {
                    appCompatActivity.setSupportActionBar(toolBar);
                } else {
                    Logger.w(TAG, "Hiding fragment layout toolBar. The Activity already has an action bar setup, consider removing the toolBar from your fragment layout.");
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

        Bundle props = mElectrodeActivityListener.globalProps();
        if (props != null) {
            initialProps.putAll(props);
        }

        return initialProps;
    }

    @SuppressWarnings("unused")
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        //PlaceHolder
    }

    @SuppressWarnings("unused")
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart() {
        Logger.d(TAG, "inside onStart");
        //PlaceHolder
    }

    @SuppressWarnings("unused")
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        Logger.d(TAG, "inside onResume");
        //PlaceHolder
    }

    @SuppressWarnings("unused")
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        Logger.d(TAG, "inside onPause");
        //PlaceHolder
    }

    @SuppressWarnings("unused")
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        Logger.d(TAG, "inside onStop");
        //PlaceHolder
    }

    @SuppressWarnings("unused")
    public void onDestroyView() {
        Logger.d(TAG, "inside onDestroyView");
        //PlaceHolder
    }

    @SuppressWarnings("unused")
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    @CallSuper
    public void onDestroy() {
        Logger.d(TAG, "inside onDestroy");
        if (mMiniAppView != null) {
            assert mFragment.getArguments() != null;
            mElectrodeActivityListener.removeReactNativeView(miniAppComponentName, mMiniAppView);
            mMiniAppView = null;
            mFragmentConfig = null;
        }
    }

    @SuppressWarnings("unused")
    public void onDetach() {
        //PlaceHolder
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + miniAppComponentName;
    }

    @SuppressWarnings("unused")
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

    /***
     * Interface that connects the fragment delegate to the hosting activity.
     */
    public interface ElectrodeActivityListener {

        int ADD_TO_BACKSTACK = 0;
        int DO_NOT_ADD_TO_BACKSTACK = 1;

        @SuppressWarnings("unused")
        @IntDef({ADD_TO_BACKSTACK, DO_NOT_ADD_TO_BACKSTACK})
        @Retention(RetentionPolicy.SOURCE)
        @interface AddToBackStackState {
        }

        /**
         * Returns a react root view for the given mini app.
         *
         * @param appName React native root component name
         * @param props   Optional properties for the component
         * @return View returns a {@link ReactRootView} for the given component.
         */
        View createReactNativeView(@NonNull String appName, @Nullable Bundle props);


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
         * @param launchConfig  {@link LaunchConfig} to allow custom launch options for a fragment.
         */
        @SuppressWarnings("unused")
        void startMiniAppFragment(@NonNull String componentName, @NonNull LaunchConfig launchConfig);

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
         * @param event {@link KeyEvent}
         * @return true if the menu was shown false otherwise
         */
        @SuppressWarnings("unused")
        boolean showDevMenuIfDebug(KeyEvent event);
    }
}