package com.walmartlabs.moviesreloaded.demo.bottomsheet.persistent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.ern.api.impl.navigation.ElectrodeNavigationActivityListener;
import com.ern.api.impl.navigation.ElectrodeNavigationFragmentConfig;
import com.ern.api.impl.navigation.ElectrodeNavigationFragmentDelegate;
import com.ern.api.impl.navigation.MiniAppNavigationFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.walmartlabs.moviesreloaded.R;

public class PersistentBottomSheetFragment extends MiniAppNavigationFragment {
    private BottomSheetBehavior bottomSheetBehavior;

    @NonNull
    @Override
    protected ElectrodeNavigationFragmentDelegate createFragmentDelegate() {
        ElectrodeNavigationFragmentConfig config = new ElectrodeNavigationFragmentConfig();
        config.setFragmentLayoutId(R.layout.fragment_bottom_sheet_persistent);
        config.setReactViewContainerId(R.id.react_view_container);
        return new ElectrodeNavigationFragmentDelegate(this, config);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        if (view != null && bottomSheetBehavior == null) {
            View bottomSheetView = view.findViewById(R.id.react_view_container);
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView);

            bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View view, int state) {
                    if (state == BottomSheetBehavior.STATE_HIDDEN) {
                        if (getActivity() instanceof ElectrodeNavigationActivityListener) {
                            ((ElectrodeNavigationActivityListener) getActivity()).finishFlow(null);
                        }
                    }
                }

                @Override
                public void onSlide(@NonNull View view, float v) {

                }
            });
        } else {
            throw new RuntimeException("Should never reach here");
        }
        return view;
    }
}
