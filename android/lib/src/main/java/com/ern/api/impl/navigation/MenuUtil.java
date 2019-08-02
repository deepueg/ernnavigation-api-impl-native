package com.ern.api.impl.navigation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.StrictMode;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.URLUtil;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ernnavigationApi.ern.model.NavigationBar;
import com.ernnavigationApi.ern.model.NavigationBarButton;
import com.walmartlabs.electrode.reactnative.bridge.helpers.Logger;
import com.walmartlabs.ern.navigation.BuildConfig;

import java.io.IOException;
import java.net.URL;

final class MenuUtil {

    private MenuUtil() {
    }

    private static final String TAG = MenuUtil.class.getSimpleName();

    static void updateMenuItems(@NonNull Menu menu, @NonNull NavigationBar navigationBar, @NonNull OnNavBarItemClickListener navBarButtonClickListener, @Nullable MenuItemDataProvider menuItemDataProvider, @NonNull Context context) {
        menu.clear();

        if (navigationBar.getButtons() == null || navigationBar.getButtons().size() == 0) {
            Logger.d(TAG, "No buttons found in the NavBar");
            return;
        }

        for (final NavigationBarButton button : navigationBar.getButtons()) {
            if ("right".equalsIgnoreCase(button.getLocation())) {
                addButtonAsMenuItem(button, menu, navBarButtonClickListener, menuItemDataProvider, context);
            } else {
                Logger.w(TAG, "NavBarButton location type not supported yet: " + button.getLocation());
            }
        }
    }

    private static boolean canLoadIconFromURI(String icon) {
        return BuildConfig.DEBUG && URLUtil.isValidUrl(icon) && Patterns.WEB_URL.matcher(icon).matches();
    }

    private static MenuItem addButtonAsMenuItem(@NonNull NavigationBarButton button, @NonNull Menu menu, @NonNull final OnNavBarItemClickListener navBarButtonClickListener, @Nullable MenuItemDataProvider menuItemDataProvider, @NonNull Context context) {
        MenuItemProperties menuItemProperties = null;
        @DrawableRes int icon = Menu.NONE;
        @IdRes int itemId = Menu.NONE;

        if (menuItemDataProvider != null) {
            menuItemProperties = menuItemDataProvider.menuItemPropertiesFor(button);
            if (menuItemProperties != null) {
                icon = menuItemProperties.icon();
                itemId = menuItemProperties.itemId();
            }
        }

        MenuItem menuItem = menu.add(Menu.NONE, itemId, Menu.NONE, button.getTitle() != null ? button.getTitle() : button.getId());

        if (icon == Menu.NONE && button.getIcon() != null) {
            String iconLocation = button.getIcon();

            if (canLoadIconFromURI(iconLocation)) {
                try {
                    Logger.d(TAG, "Attempting to load icon from URL: " + iconLocation);
                    StrictMode.ThreadPolicy oldPolicy = StrictMode.getThreadPolicy();
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
                    StrictMode.setThreadPolicy(policy);
                    URL iconUrl = new URL(iconLocation);
                    Bitmap iconBitmap = BitmapFactory.decodeStream(iconUrl.openConnection().getInputStream());
                    menuItem.setIcon(new BitmapDrawable(context.getResources(), iconBitmap));
                    menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                    StrictMode.setThreadPolicy(oldPolicy);
                } catch (IOException e) {
                    Logger.w(TAG, "Load failed for icon from URL: " + iconLocation);
                }
            } else {
                icon = context.getResources().getIdentifier(iconLocation, "drawable", context.getPackageName());
                if (icon != Menu.NONE) {
                    menuItem.setIcon(icon);
                    menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                } else {
                    Logger.i(TAG, "Icon not found for button:%s", button.getId());
                }
            }
        }

        if (button.getDisabled() != null) {
            menuItem.setEnabled(!button.getDisabled());
        }

        if (menuItemProperties == null || !menuItemProperties.isHandleClickInActivity()) {
            registerItemClickListener(menuItem, button, navBarButtonClickListener);
        }

        return menuItem;
    }

    private static void registerItemClickListener(@NonNull final MenuItem menuItem, @NonNull final NavigationBarButton button, @NonNull final OnNavBarItemClickListener navBarButtonClickListener) {
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                navBarButtonClickListener.onNavBarButtonClicked(button, item);
                return true;
            }
        });
    }
}
