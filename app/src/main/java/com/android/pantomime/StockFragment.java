package com.android.pantomime;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StockFragment extends Fragment {

    private static final Gson gson = new Gson();
    private static ArrayAdapter<String> adapter;

    public StockFragment() {
        // Required empty public constructor
    }

    private Handler stockSearchHandler = new Handler(message -> {
        ArrayList<String> stocks = message.getData().getStringArrayList("searchResults");
        String[] stocksArr = stocks.toArray(new String[stocks.size()]);
        adapter.clear();
        adapter.addAll(stocksArr);
        adapter.notifyDataSetChanged();
        return true;
    });

    private void searchStocks(String input) {
        ArrayList<String> stocks = new ArrayList<>();
        Message message = stockSearchHandler.obtainMessage();
        Bundle bundle = new Bundle();
        new Thread(() -> {
            try {
                Matcher scripMatcher;
                Pattern scripPattern = Pattern.compile("\\d+");

                String url = "https://api.bseindia.com/Msource/90D/getQouteSearch.aspx?Type=EQ&text=%s&flag=gq";
                url = String.format(url, input);

                Document doc = Jsoup.connect(url).get();

                Elements list = doc.getElementsByTag("li");
                for (Element item : list) {
                    StringBuilder stock = new StringBuilder();
                    Element firstName = item.getElementsByTag("strong").first();
                    Node secondName = firstName.nextSibling();
                    String fullName = firstName.text() + secondName.toString();
                    stock.append(fullName);
                    stock.append("_");
                    String href = item.getElementsByTag("a").first().attr("href");
                    scripMatcher = scripPattern.matcher(href);
                    if(scripMatcher.find())
                        stock.append(scripMatcher.group());
                    stocks.add(stock.toString());
                }
                bundle.putStringArrayList("searchResults", stocks);
                message.setData(bundle);
                stockSearchHandler.sendMessage(message);
            } catch (Exception e) {}
        }).start();
    }

    private void displayStock(int scripCode, String stockName, View view) {
        String url = String.format("https://api.bseindia.com/BseIndiaAPI/api/getScripHeaderData/w?Debtflag=&scripcode=%d&seriesid=", scripCode);
        new Thread(() -> {
            TextView textView = view.findViewById(R.id.textView);
            textView.setText(stockName.toUpperCase()+" : ");
            try {
                String jsonString = Jsoup.connect(url)
                        .ignoreContentType(true)
                        .execute()
                        .body();
                Map<String, Object> json = gson.fromJson(jsonString, Map.class);
                Map<String, String> currRate = (Map<String, String>) json.get("CurrRate");
                String ltp = currRate.get("LTP");
                Float change = Float.parseFloat(currRate.get("Chg"));
                String ticker;
                if(change<0) {
                    textView.setTextColor(Color.RED);
                    ticker = " 📉 🙁";
                } else {
                    textView.setTextColor(Color.GREEN);
                    ticker = " 📈 😃";
                }
                textView.setText(textView.getText() + ltp + ticker);
            } catch (IOException e) {
                textView.setText("Invalid URL");
            }
        }).start();
    }

    public static StockFragment newInstance() {
        StockFragment fragment = new StockFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_stock, container, false);
        AutoCompleteTextView atv = root.findViewById(R.id.autoCompleteTextView);
        String[] stocksArr = new String[]{};
        Button resetButton = root.findViewById(R.id.reset);

        resetButton.setOnClickListener(view -> atv.setText(""));
        adapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_dropdown_item_1line, stocksArr);
        atv.setAdapter(adapter);

        atv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                String input = editable.toString();
                if(input.length() >= 3 && input.length() < 10) {
                    searchStocks(input);
                }
            }
        });

        atv.setOnItemClickListener((adapterView, view, i, l) -> {
            String input = atv.getText().toString();
            String stockName = input.split("_")[0];
            int id = Integer.parseInt(input.split("_")[1]);
            displayStock(id, stockName, root);
        });

        return root;
    }
}