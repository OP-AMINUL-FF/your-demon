package com.opx.yourdemon.utils;


import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class CheckUpdates {

    public static String check() {
        try {
            Document doc = Jsoup.connect("https://raw.githubusercontent.com/OP-AMINUL-FF/your-demon/main/updater/update.txt").get();
            return doc.text();
        } catch (IOException e) {
            Log.d("Debug: ", "An IOException was caught: " + e.getMessage());
            return "Error";
        }
    }
}
