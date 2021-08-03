package com.android.pantomime;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

import org.jsoup.Jsoup;


public class CovidFragment extends Fragment {

    private static final Gson GSON = new Gson();
    public TextView tv;

    private Handler tvHandler = new Handler(message -> {
        tv.setText(message.getData().getString("covidData"));
        return true;
    });

    private void getCovidData(String state, String city) {
        Message m = tvHandler.obtainMessage();
        Bundle bundle = new Bundle();
        new Thread(() -> {
            String result;
            String url = "https://masterbyte.herokuapp.com/api/covid?state=%s&city=%s";
            url = String.format(url, state, city);
            try {
                String data = Jsoup.connect(url)
                        .timeout(100000)
                        .ignoreContentType(true)
                        .ignoreHttpErrors(true)
                        .execute()
                        .body();
                result = GSON.fromJson(data, String.class);
            } catch(Exception e) {
                result = e.toString();
            }
            bundle.putString("covidData", result);
            m.setData(bundle);
            tvHandler.sendMessage(m);
        }).start();
    }

    private void updateCovidData() {
        View view = this.getView();
        EditText stateInput = view.findViewById(R.id.state);
        EditText cityInput = view.findViewById(R.id.city);
        tv = view.findViewById(R.id.covidData);
        tv.setText("Fetching data.. please wait...");

        String state = stateInput.getText().toString();
        String city = cityInput.getText().toString();

        getCovidData(state, city);
    }

    public CovidFragment() {
        // Required empty public constructor
    }

    public static CovidFragment newInstance() {
        return new CovidFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_covid, container, false);
        root.findViewById(R.id.button).setOnClickListener(view -> updateCovidData());
        return root;
    }
}