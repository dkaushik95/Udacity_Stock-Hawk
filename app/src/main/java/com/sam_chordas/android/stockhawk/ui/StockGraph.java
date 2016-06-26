package com.sam_chordas.android.stockhawk.ui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.rest.GraphDataHolder;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.rest.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class StockGraph extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener, OnChartValueSelectedListener {

    String URL;
    SeekBar xseek;
    TextView textx, name, volume, date, highlow, close;
    ProgressDialog prog;
    private CombinedChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_stock_graph);
        xseek = (SeekBar) findViewById(R.id.seekX);

        textx = (TextView) findViewById(R.id.rangeX);
        name = (TextView) findViewById(R.id.name);
        volume = (TextView) findViewById(R.id.volume);
        highlow = (TextView) findViewById(R.id.highlow);
        date = (TextView) findViewById(R.id.date);
        close = (TextView) findViewById(R.id.close);
        xseek.setOnSeekBarChangeListener(this);

        mChart = (CombinedChart) findViewById(R.id.combinedChart);

        if (!Utils.isConnected(this)) {
            AlertDialog.Builder alertbuilder = new AlertDialog.Builder(this);
            alertbuilder.setMessage(R.string.network_toast)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    }).show();


        }
        prog = new ProgressDialog(this);
        prog.setMessage(getString(R.string.msg_downloading_data));
        prog.setTitle(getString(R.string.msg_wait));
        prog.setIndeterminate(true);
        prog.setProgressStyle(R.style.AppCompatAlertDialogStyle);
        prog.show();


        mChart.setBackgroundColor(Color.DKGRAY);
        mChart.setDrawGridBackground(false);
        mChart.setDrawBarShadow(false);


        mChart.setDrawOrder(new CombinedChart.DrawOrder[]{
                CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.LINE
        });

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setTextColor(Color.WHITE);
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMinValue(0f);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMinValue(0f);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setTextColor(Color.WHITE);
        xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);


        Calendar cal = Calendar.getInstance();
        Date currentDate = cal.getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String current = formatter.format(currentDate);

        cal.add(Calendar.DAY_OF_YEAR, -365);
        Date newDate = cal.getTime();
        String previous_year = formatter.format(newDate);

        URL = "  https://query.yahooapis.com/v1/public/yql?q=select%20%2A%20from%20yahoo.finance.historicaldata%20where%20symbol%20%3D%20%22" + getIntent().getStringExtra(QuoteColumns.SYMBOL) + "%22%20and%20startDate%20%3D%20%22" + previous_year + "%22%20and%20endDate%20%3D%20%22" + current + "%22&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";

        JsonObjectRequest jsonObjReq1 = new JsonObjectRequest(Request.Method.GET,
                URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.i("respons", response.toString());
                    JSONArray records = response.getJSONObject("query").
                            getJSONObject("results").getJSONArray("quote");
                    for (int i = 0; i < records.length(); i++) {
                        JSONObject history = records.getJSONObject(i);
                        GraphDataHolder gr = new GraphDataHolder(history.getString("Symbol")
                                , history.getString("Date"), history.getString("Open"), history.getString("Close"),
                                history.getString("High"), history.getString("Low"), history.getString("Adj_Close"), history.getString("Volume"));
                        GraphDataHolder.addGraphObject(gr);


                    }

                    xseek.setMax(GraphDataHolder.count() - 1);
                    xseek.setProgress(100);
                    textx.setText("101");
                    mChart.setOnChartValueSelectedListener(StockGraph.this);

                    mChart.setDescription(getIntent().getStringExtra(QuoteColumns.NAME) + "(" + GraphDataHolder.getInstance(0).getmSymbol() + ")\n " + getString(R.string.stock_meta));
                    mChart.setDescriptionColor(Color.WHITE);
                    setData();

                    prog.dismiss();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                mChart.setData(null);
                prog.dismiss();
            }
        });
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjReq1);


    }

    public void setData() {
        CombinedData data = new CombinedData(GraphDataHolder.generateXvals(xseek.getProgress() + 1));
        data.setData(generateBarData());
        data.setData(generateLineData());
        mChart.animateX(1000, Easing.EasingOption.EaseInOutCubic);
        mChart.animateY(1500, Easing.EasingOption.EaseInOutCubic);
        mChart.setData(data);
        mChart.invalidate();
    }

    private LineData generateLineData() {

        LineData d = new LineData();
        LineDataSet t=GraphDataHolder.generateLineYvals(xseek.getProgress() + 1);
        t.setCircleColor(getResources().getColor(R.color.accent));
        t.setColor(getResources().getColor(R.color.accent));
        t.setValueTextColor(Color.WHITE);
        d.addDataSet(t);
        return d;
    }

    private BarData generateBarData() {

        BarData d = new BarData();
        BarDataSet t=GraphDataHolder.generateBarYvals(xseek.getProgress() + 1);
        t.setColor(getResources().getColor(R.color.accent));
        t.setValueTextColor(Color.WHITE);
        d.addDataSet(t);

        return d;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        textx.setText("" + (xseek.getProgress() + 1));


        setData();
        mChart.notifyDataSetChanged();
        mChart.invalidate();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GraphDataHolder.erase();
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        GraphDataHolder obj = GraphDataHolder.getInstance(e.getXIndex());
        name.setText(getIntent().getStringExtra(QuoteColumns.NAME));
        date.setText(obj.getmDate());
        volume.setText(obj.getmVolume() + "");
        close.setText("" + obj.getmClose());
        highlow.setText("" + obj.getmHigh() + "/" + obj.getmLow());
    }

    @Override
    public void onNothingSelected() {
        name.setText(getString(R.string.stock_name));
        date.setText(getString(R.string.date));
        volume.setText(getString(R.string.volume));
        close.setText(getString(R.string.close));
        highlow.setText(getString(R.string.highlow));
    }
}