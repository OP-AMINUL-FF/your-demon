package com.opx.yourdemon.modules;



import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.opx.yourdemon.R;
import com.opx.yourdemon.coremanger.utils.InstallPackage;
import com.opx.yourdemon.custom.Module;
import com.opx.yourdemon.utils.Core;
import com.opx.yourdemon.utils.CustomChrootCommand;
import com.opx.yourdemon.utils.CustomCommand;
import com.opx.yourdemon.utils.TaskRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class ModulesAdapter extends RecyclerView.Adapter<ModulesAdapter.ViewHolder> {
    public ArrayList<Module> modules;
    public Context context;
    public Activity activity;
    public Core core;
    public int id = 0;

    public ModulesAdapter(Context context2, Activity mActivity, ArrayList<Module> m) {
        context = context2;
        modules = m;
        activity = mActivity;
        core = new Core(context2);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.module_item, parent, false);
        return new ViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder adapter, @SuppressLint("RecyclerView") final int position) {
       Module m = modules.get(position);
       adapter.name.setText(m.getName());
       adapter.authorver.setText(m.getAuthor()+" ("+m.getVersion()+")");
       adapter.desc.setText(m.getDesc());
       String formated_name = m.getName().replace(" ","_");
       if (!core.checkmod(m.getName())){
           if (!m.isOnly64bit() || core.is64Bit()){
       adapter.install.setOnClickListener(view -> {
           adapter.install.setVisibility(View.GONE);
           adapter.prog.setVisibility(View.VISIBLE);
           // This code is downloading the module and installing it.
           new Thread(() -> {
               boolean d = download(m.getSrcinstall(),formated_name+".zip",adapter.prog);
               if (d){
                    TaskRunner.execute(() -> CustomCommand.execute("mkdir storage/emulated/0/YourDemon/modules", core));
                    TaskRunner.execute(() -> CustomCommand.execute("mv /storage/emulated/0/Download/"+formated_name+".zip /storage/emulated/0/YourDemon/modules/"+formated_name+".zip", core));
                    TaskRunner.execute(() -> CustomCommand.execute("mkdir /data/local/YourDemon/release/modules", core));
                    activity.runOnUiThread(() -> core.toaster("Installing "+m.getPksg()));
                    boolean apk = InstallPackage.execute(m.getPksg(),core);
                     Boolean o1 = CustomCommand.execute("rm -rf /data/local/YourDemon/release/modules/"+formated_name, core);
                     Boolean o = CustomCommand.execute("mkdir /storage/emulated/0/YourDemon/modules/"+formated_name, core);
                     core.unzip(new File("/storage/emulated/0/YourDemon/modules/"+formated_name+".zip"), new File("/storage/emulated/0/YourDemon/modules/" + formated_name));
                     Boolean move = CustomCommand.execute("mv /storage/emulated/0/YourDemon/modules/"+formated_name+" /data/local/YourDemon/release/modules/"+formated_name, core);

                    Intent mouduleinst = new Intent(activity, InstalModuleActivity.class);
                    mouduleinst.putExtra("path",formated_name);
                    mouduleinst.putExtra("install",true);
                    mouduleinst.putExtra("name",m.getName());
                    activity.startActivity(mouduleinst);

               }
           }).start();

       });}else{
               adapter.install.setOnClickListener(view -> core.toaster(core.str("only64")));

           }
           }else{
           adapter.install.setImageDrawable(context.getDrawable(R.drawable.delete));
           adapter.install.setOnClickListener(view -> {

                TaskRunner.execute(() -> CustomCommand.execute("rm -rf /storage/emulated/0/YourDemon/modules/"+formated_name, core));
                TaskRunner.execute(() -> CustomCommand.execute("rm /storage/emulated/0/YourDemon/modules/"+formated_name+".zip", core));
               //moduler(m.getName(),"/storage/emulated/0/YourDemon/modules/"+formated_name+"/delete.sh",false);
               Intent moduledel = new Intent(activity, InstalModuleActivity.class);
               moduledel.putExtra("path",formated_name);
               moduledel.putExtra("install",false);
               moduledel.putExtra("name",m.getName());
               activity.startActivity(moduledel);
           });
       }
    }

    @Override
    public int getItemCount() {

        return modules.size();
    }

    public void toaster(String msg) {
        activity.runOnUiThread(() -> {
            Toast toast = Toast.makeText(context,
                    msg, Toast.LENGTH_SHORT);
            toast.show();
        });

    }

    public void appendtext(String text, TextView output) {
        activity.runOnUiThread(() -> output.append(text));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView authorver;
        public TextView desc;
        public ImageView install;
       public CircularProgressIndicator prog;
        public MaterialCardView card;

        public ViewHolder(View v) {
            super(v);
            authorver = v.findViewById(R.id.module_author_and_ver);
            name = v.findViewById(R.id.module_name);
            desc = v.findViewById(R.id.module_desc);
            install = v.findViewById(R.id.module_install);
            prog = v.findViewById(R.id.module_indicator);
            card = v.findViewById(R.id.item);
        }

    }
    @SuppressLint("Range")
    public Boolean download(String url, String name, CircularProgressIndicator progress) {
        boolean ok = false;
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription(core.str("install2"));
        request.setTitle(core.str("wait"));
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);
        final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        final long downloadId = manager.enqueue(request);
        boolean downloading = true;
        while (downloading) {
            DownloadManager.Query q = new DownloadManager.Query();
            q.setFilterById(downloadId);
            Cursor cursor = manager.query(q);
            cursor.moveToFirst();
            @SuppressLint("Range") int bytes_downloaded = cursor.getInt(cursor
                    .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            @SuppressLint("Range") int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
            if (bytes_total == 0) {
                break;
            }
            final int dl_progress = (int) ((bytes_downloaded * 100L) / bytes_total);
            setProg(progress, dl_progress);
            int statusCol = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            if (statusCol == DownloadManager.STATUS_SUCCESSFUL) {
                downloading = false;
                ok = true;

            } else if (statusCol == DownloadManager.STATUS_FAILED) {
                downloading = false;
            }
            cursor.close();
        }
        return ok;

    }
    public void setText(TextView textView, String text, boolean animate) {
        activity.runOnUiThread(() -> {
            if (animate) {
                Animation fade = AnimationUtils.loadAnimation(context, R.anim.fade_in);
                textView.startAnimation(fade);
            }
            textView.setText(text);
        });
    }
    public void moduler(String name, String path,boolean install){
        activity.runOnUiThread(() -> {
            final Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.module_progress);
            dialog.setCancelable(false);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            CircularProgressIndicator prog = dialog.findViewById(R.id.module_prog);
            TextView title = dialog.findViewById(R.id.module_title);
            TextView progress = dialog.findViewById(R.id.module_progress);
            TextView cancel = dialog.findViewById(R.id.module_cancel);
            cancel.setVisibility(View.INVISIBLE);
            cancel.setOnClickListener(view -> dialog.dismiss());
            ArrayList<String> commands = new ArrayList<>();
            if (install){
            title.setText(R.string.installing+name);}else{
                title.setText(core.str("deleting")+name);
            }
            try (BufferedReader br = new BufferedReader(new FileReader(path))) {
                String line;
                while ((line = br.readLine()) != null) {
                    commands.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            int pr = 100 / commands.size();
            final int[] total = {0};
            new Thread(() -> {
                for (String cmd: commands){
                    if (cmd.startsWith("#")){
                        setText(progress,cmd.replace("#",""),false);
                        setProg(prog, total[0]);
                    }else {
                        try {
                            Boolean bool = CustomChrootCommand.execute(cmd, core);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        total[0] = total[0] + pr;
                    }
                }
                activity.runOnUiThread(() -> cancel.setVisibility(View.VISIBLE));

                setProg(prog,100);
                setText(cancel,"OK",false);
                setText(progress,core.str("finished"),false);
            }).start();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        });


    }


    public void setProg(CircularProgressIndicator progressIndicator, int prog) {
        activity.runOnUiThread(() -> {
            progressIndicator.setVisibility(View.INVISIBLE);
            progressIndicator.setIndeterminate(false);

            progressIndicator.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                progressIndicator.setProgress(prog, true);
            }
        });

    }
}
