package com.ern.api.impl.navigation;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.ern.api.impl.core.ElectrodeReactFragmentActivityDelegate;
import com.ernnavigationApi.ern.model.NavigationBar;
import com.facebook.react.ReactRootView;

import org.json.JSONObject;

public abstract class ElectrodeBaseActivity extends AppCompatActivity implements ElectrodeReactFragmentActivityDelegate.DataProvider, MiniAppNavConfigRequestListener {
    public static final int DEFAULT_TITLE = -1;

    protected ElectrodeReactFragmentActivityDelegate mElectrodeReactNavDelegate;

    /**
     * Override and provide the main layout resource.
     *
     * @return int {@link LayoutRes}
     */
    @LayoutRes
    protected abstract int mainLayout();

    /**
     * Override to provide title for the landing page.
     *
     * @return int @{@link StringRes}
     */
    @StringRes
    protected int title() {
        return DEFAULT_TITLE;
    }

    /**
     * Override this method to hide/show the nav bar.
     *
     * @return false | true Default: false
     */
    protected boolean hideNavBar() {
        return false;
    }

    /**
     * Override if you need to provide a custom delegate.
     *
     * @return ElectrodeReactFragmentActivityDelegate
     */
    @NonNull
    protected ElectrodeReactFragmentActivityDelegate createElectrodeDelegate() {
        return new ElectrodeReactFragmentActivityDelegate(this);
    }

    @Override
    @CallSuper
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mElectrodeReactNavDelegate = createElectrodeDelegate();
        getLifecycle().addObserver(mElectrodeReactNavDelegate);
        setContentView(mainLayout());
        mElectrodeReactNavDelegate.onCreate(savedInstanceState);

        setupNavBar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mElectrodeReactNavDelegate = null;
    }

    @Override
    public void onBackPressed() {
        if (!mElectrodeReactNavDelegate.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mElectrodeReactNavDelegate.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (mElectrodeReactNavDelegate.onKeyUp(keyCode, event)) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void setupNavBar() {
        if (getSupportActionBar() != null) {
            if (hideNavBar()) {
                getSupportActionBar().hide();
            } else if (title() != DEFAULT_TITLE) {
                getSupportActionBar().setTitle(getString(title()));
            }
        }
    }

    @Nullable
    @Override
    public Bundle getProps() {
        return null;
    }

    @NonNull
    @Override
    public Class<? extends Fragment> miniAppFragmentClass() {
        return MiniAppNavFragment.class;
    }

    @Override
    public boolean navigate(Route route) {
        return false;
    }

    @Override
    public void finishFlow(@Nullable JSONObject finalPayload) {
        finish();
    }

    @Override
    @Deprecated
    public void updateNavBar(@NonNull NavigationBar navigationBar, @NonNull OnNavBarItemClickListener navBarButtonClickListener) {
        //Do Nothing. This method will be removed in a future release as it's deprecated.
    }

    @Override
    public boolean backToMiniApp(@Nullable String miniAppComponentName) {
        return mElectrodeReactNavDelegate.switchBackToFragment(miniAppComponentName);
    }

    @Override
    public View createReactNativeView(@NonNull String componentName, @Nullable Bundle props) {
        return mElectrodeReactNavDelegate.createReactRootView(componentName, props);
    }

    @Override
    public void removeReactNativeView(@NonNull String appName) {
        mElectrodeReactNavDelegate.removeMiniAppView(appName);
    }

    @Override
    public void removeReactNativeView(@NonNull String appName, @NonNull ReactRootView reactRootView) {
        mElectrodeReactNavDelegate.removeMiniAppView(appName, reactRootView);
    }

    @Override
    public void startMiniAppFragment(@NonNull String componentName, @Nullable Bundle props) {
        ElectrodeReactFragmentActivityDelegate.StartMiniAppConfig config = new ElectrodeReactFragmentActivityDelegate.StartMiniAppConfig.Builder(miniAppFragmentClass()).props(props).build();
        mElectrodeReactNavDelegate.startMiniAppFragment(componentName, config);
    }

    @Override
    public void startMiniAppFragment(@NonNull Class<? extends Fragment> fragmentClass, @NonNull String componentName, @Nullable Bundle props) {
        ElectrodeReactFragmentActivityDelegate.StartMiniAppConfig config = new ElectrodeReactFragmentActivityDelegate.StartMiniAppConfig.Builder(fragmentClass).props(props).build();
        mElectrodeReactNavDelegate.startMiniAppFragment(componentName, config);
    }

    @Override
    public void startMiniAppFragment(@NonNull String componentName, @NonNull ElectrodeReactFragmentActivityDelegate.StartMiniAppConfig startMiniAppConfig) {
        mElectrodeReactNavDelegate.startMiniAppFragment(componentName, startMiniAppConfig);
    }

    @Nullable
    @Override
    public Bundle globalProps() {
        return null;
    }

    @Override
    public boolean showDevMenuIfDebug(KeyEvent event) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mElectrodeReactNavDelegate.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
