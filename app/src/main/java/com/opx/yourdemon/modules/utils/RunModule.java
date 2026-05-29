package com.opx.yourdemon.modules.utils;


import android.app.Activity;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.widget.TextView;

import com.opx.yourdemon.utils.Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

public class RunModule {

    public static boolean execute(String formatted_name, Core core, TextView log, Activity activity, boolean install, String name) {
        boolean result = false;

        try {
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            stdin.write((Core.EXECUTE+" ash"+"\n").getBytes());
            stdin.flush();
            stdin.write(("cd /modules/"+ formatted_name +"\n").getBytes());
            stdin.flush();
            stdin.write(("chmod 777 *\n").getBytes());
            if (install){
            stdin.write(("/modules/"+formatted_name+"/install.sh"+"\n").getBytes());
            }
            else{
                stdin.write(("/modules/"+formatted_name+"/delete.sh"+"\n").getBytes());
            }
            stdin.write(("exit\n").getBytes());
            stdin.flush();
            stdin.close();
            ArrayList<String> out = new ArrayList<>();
            ArrayList<String> outerror = new ArrayList<>();
            new Thread(() -> {
                String line;
                try{
                BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
                while ((line = br.readLine()) != null) {
                    out.add(line);
                    String finalLine = line;
                    activity.runOnUiThread(() -> appendText(log, finalLine,false));
                }
                br.close();}
             catch (IOException e) {
                Log.d("Debug: ", "An IOException was caught: " + e.getMessage());
            }
            }).start();
            new Thread(() -> {
                BufferedReader br1 = new BufferedReader(new InputStreamReader(stderr));
                String line1 = null;
                while (true) {
                    try {
                        if ((line1 = br1.readLine()) == null) break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (line1 != null && !line1.contains("%") && line1.contains("exists")){
                        outerror.add(line1);}
                    String finalLine = line1;
                    if (line1 != null) {
                        String fl = line1;
                        activity.runOnUiThread(() -> appendText(log, fl, true));
                    }
                }
                try {
                    br1.close() ;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            process.waitFor();
            process.destroy();
            if (process.exitValue() == 0) {
                result = true;
            }
        } catch (IOException e) {
            Log.d("Debug: ", "An IOException was caught: " + e.getMessage());
        } catch (InterruptedException ex) {
            Log.d("Debug: ", "An InterruptedException was caught: " + ex.getMessage());
        }
        if (install){core.installmod(name);}else{core.deletemod(name);}
        return result;
    }

    private static void appendText(TextView log, String text, boolean iserror){
        if (log != null) {
            if (!iserror){
                log.append(white(text));
            }else{
                log.append(red(text));
            }
            log.append("\n");
        }
    }
    private static Spanned white(String out) {
        return Html.fromHtml("<font color='#FFFFFF'>" + out + "</font>");
    }

    private static Spanned red(String out) {
        return Html.fromHtml("<font color='#F60B0B'>" + out + "</font>");
    }
}
