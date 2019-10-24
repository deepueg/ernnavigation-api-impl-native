package com.ern.api.impl.navigation;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ern.api.impl.core.BaseElectrodeBottomSheetFragment;

public class MiniAppBottomSheetFragment extends BaseElectrodeBottomSheetFragment<ElectrodeReactFragmentNavDelegate> {
    @NonNull
    @Override
    protected ElectrodeReactFragmentNavDelegate createFragmentDelegate() {
        return new ElectrodeReactFragmentNavDelegate(this);
    }

    @Nullable
    @Override
    public Bundle initialProps() {
        return null;
    }
}
