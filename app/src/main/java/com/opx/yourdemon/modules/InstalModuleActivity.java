package com.opx.yourdemon.modules;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.opx.yourdemon.R;
import com.opx.yourdemon.modules.utils.RunModule;
import com.opx.yourdemon.utils.Core;
import com.opx.yourdemon.utils.CustomCommand;
import com.opx.yourdemon.utils.TaskRunner;

/**
 * Installs a module from a given path
 */
public class InstalModuleActivity extends AppCompatActivity {

    public String path;
    public TextView log;
    public Activity activity;
    public Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        path = getIntent().getExtras().getString("path");
        String name = getIntent().getExtras().getString("name");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instal_module);
        ExtendedFloatingActionButton relaunch = findViewById(R.id.relauch_button);
        log = findViewById(R.id.logview);
        activity = this;
        context = this;
        relaunch.shrink();
        relaunch.hide();
        new Thread(() -> {
            try {
                boolean module = RunModule.execute(path,new Core(context),log,activity,getIntent().getExtras().getBoolean("install"),name);
                runOnUiThread(relaunch::show);
                runOnUiThread(relaunch::extend);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        relaunch.setOnClickListener(view -> {
            TaskRunner.execute(() -> CustomCommand.execute("am start -n com.opx.yourdemon/.MainActivity", new Core(context)));
            finishAffinity();
        });



    }
    @Override
    public void onBackPressed() {
        new Core(context).toaster("Relaunch app!");
    }
}
