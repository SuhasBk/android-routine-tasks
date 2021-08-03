package com.android.pantomime;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

@RequiresApi(api = Build.VERSION_CODES.Q)
public class HtEPaper extends Fragment {

    private WebView wv;

    public HtEPaper() {
        // Required empty public constructor
    }

    public static HtEPaper newInstance() {
        return new HtEPaper();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_ht_epaper, container, false);
        wv = root.findViewById(R.id.webview);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.loadUrl("https://ht-epaper.herokuapp.com/");
        return root;
    }
}