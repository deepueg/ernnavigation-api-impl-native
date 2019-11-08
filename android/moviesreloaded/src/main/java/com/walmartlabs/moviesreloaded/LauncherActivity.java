package com.walmartlabs.moviesreloaded;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.walmartlabs.moviesreloaded.demo.bottomsheet.modal.ModalBottomSheetActivity;
import com.walmartlabs.moviesreloaded.demo.bottomsheet.persistent.PersistentBottomSheetActivity;
import com.walmartlabs.moviesreloaded.demo.customview.CustomActivity;
import com.walmartlabs.moviesreloaded.demo.navmenuhandler.NavMenuActivity;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        setTitle(this.getString(R.string.launcher_title));
    }


    public void handleButtonClick(View view) {
        Class<? extends Activity> clazz;
        switch (view.getId()) {
            case R.id.id_button_custom:
                clazz = CustomActivity.class;
                break;
            case R.id.id_button_navbar:
                clazz = NavMenuActivity.class;
                break;
            case R.id.id_button_bottomsheet:
                clazz = ModalBottomSheetActivity.class;
                break;
            case R.id.id_button_persistent_bottomsheet:
                clazz = PersistentBottomSheetActivity.class;
                break;
            default:
                clazz = MainActivity.class;
                break;
        }

        this.startActivity(new Intent(this, clazz));
    }
}
