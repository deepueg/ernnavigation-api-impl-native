package com.ern.api.impl.core;

import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.ern.api.impl.core.ElectrodeReactFragmentDelegate.MiniAppRequestListener.AddToBackStackState;
import com.walmartlabs.electrode.reactnative.bridge.helpers.Logger;
import com.walmartlabs.ern.container.ElectrodeReactActivityDelegate;

import static com.ern.api.impl.core.ElectrodeReactFragmentDelegate.MiniAppRequestListener.ADD_TO_BACKSTACK;


public class ElectrodeReactFragmentActivityDelegate extends ElectrodeReactActivityDelegate implements LifecycleObserver {

    private static final String TAG = ElectrodeReactFragmentActivityDelegate.class.getSimpleName();

    private DataProvider dataProvider;
    protected FragmentActivity mFragmentActivity;

    public ElectrodeReactFragmentActivityDelegate(@NonNull FragmentActivity activity) {
        super(activity, null);
        if (activity instanceof DataProvider) {
            dataProvider = (DataProvider) activity;

        } else {
            throw new IllegalStateException("The activity should implement the ElectrodeReactFragmentActivityDelegate.DataProvider.");
        }
        this.mFragmentActivity = activity;
        if (mFragmentActivity instanceof BackKeyHandler) {
            setBackKeyHandler((BackKeyHandler) mFragmentActivity);
        }
    }

    //Not putting this under the OnLifecycleEvent sine we need the savedInstanceState
    @Override
    public void onCreate(Bundle savedInstanceState) {
        if(savedInstanceState == null) {
            startReactNative();
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
        dataProvider = null;
        super.onDestroy();
    }

    private void startReactNative() {
        String appName = dataProvider.getRootComponentName();
        Logger.d(TAG, "Loading the react view inside MiniApp fragment.");
        startMiniAppFragment(appName, dataProvider.getProps());
    }

    public void startMiniAppFragment(@NonNull String componentName, @Nullable Bundle props) {
        startMiniAppFragment(dataProvider.miniAppFragmentClass(), componentName, props);
    }

    public void startMiniAppFragment(@NonNull Class<? extends Fragment> fragmentClass, @NonNull String componentName, @Nullable Bundle props) {
        if (props == null) {
            props = new Bundle();
        }
        props.putString(ActivityDelegateConstants.KEY_MINI_APP_COMPONENT_NAME, componentName);
        switchToFragment(fragmentClass, props);
    }

    private void switchToFragment(@NonNull Class<?> fragmentClass, @NonNull Bundle bundle) {
        switchToFragment(fragmentClass, bundle, ADD_TO_BACKSTACK);
    }

    private void switchToFragment(@NonNull Class<?> fragmentClass, @NonNull Bundle bundle,
                                  @AddToBackStackState int addToBackStackState) {
        try {
            Fragment fragment = (Fragment) fragmentClass.newInstance();
            fragment.setArguments(bundle);

            String tag = (bundle.containsKey(ActivityDelegateConstants.KEY_MINI_APP_FRAGMENT_TAG)) ? bundle.getString(ActivityDelegateConstants.KEY_MINI_APP_FRAGMENT_TAG) : bundle.getString(ActivityDelegateConstants.KEY_MINI_APP_COMPONENT_NAME);

            switchToFragment(fragment, addToBackStackState, tag);
        } catch (Exception e) {
            Logger.e(TAG, "Failed to create " + fragmentClass.getName() + " fragment", e);
        }
    }

    /**
     * Switch to {@param fragment}, adding to fragment back stack if specified.
     *
     * @param fragment            {@link Fragment} to be switched.
     * @param addToBackStackState {@link AddToBackStackState}
     */
    private void switchToFragment(@NonNull Fragment fragment,
                                  @AddToBackStackState int addToBackStackState, @Nullable String tag) {
        final FragmentTransaction transaction = mFragmentActivity.getSupportFragmentManager().beginTransaction();

        transaction.replace(dataProvider.getFragmentContainerId(), fragment, tag);
        if (ADD_TO_BACKSTACK == addToBackStackState) {
            transaction.addToBackStack(tag);
        }
        transaction.commit();
    }

    public boolean switchBackToFragment(@Nullable String tag) {
        final FragmentManager manager = mFragmentActivity.getSupportFragmentManager();

        int backStackCount = manager.getBackStackEntryCount();
        if (backStackCount == 1) {
            if (tag == null || tag.equals(manager.getBackStackEntryAt(0).getName())) {
                mFragmentActivity.finish();
                return true;
            }
        }

        return manager.popBackStackImmediate(tag, 0);
    }

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
         * Props that needs to be passed to the root component.
         *
         * @return Bundle
         */
        @Nullable
        Bundle getProps();


        /***
         * Return the fragment class that needs to be instantiated to render react native component.
         *
         * Note: Default available fragments: {@link DefaultMiniAppFragment}
         * @return Class
         */
        @NonNull
        Class<? extends Fragment> miniAppFragmentClass();
    }
}
