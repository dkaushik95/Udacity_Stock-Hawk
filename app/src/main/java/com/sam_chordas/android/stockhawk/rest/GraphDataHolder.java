package com.sam_chordas.android.stockhawk.rest;

import android.graphics.Color;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

/**
 * Created by dishantkaushik on 21/06/16.
 */
public class GraphDataHolder {
    private static ArrayList<GraphDataHolder> mGraphObjectList = new ArrayList();
    private String mSymbol, mDate;
    private float mOpen, mClose, mHigh, mLow, mAdj_close;
    private int mVolume;

    public GraphDataHolder(String mSymbol, String mDate, String mOpen, String mClose, String mHigh, String mLow, String mAdj_close, String mVolume) {
        this.mSymbol = mSymbol;
        this.mAdj_close = Float.parseFloat(mAdj_close);
        this.mClose = Float.parseFloat(mClose);
        this.mDate = mDate;
        this.mOpen = Float.parseFloat(mOpen);
        this.mHigh = Float.parseFloat(mHigh);
        this.mLow = Float.parseFloat(mLow);
        this.mVolume = Integer.parseInt(mVolume);


    }

    public static void addGraphObject(GraphDataHolder gr) {
        if (mGraphObjectList == null)
            mGraphObjectList = new ArrayList<GraphDataHolder>();

        mGraphObjectList.add(gr);
    }

    public static int count() {
        return mGraphObjectList.size();
    }

    public static ArrayList<String> generateXvals(int limit) {
        ArrayList<String> xVals = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            GraphDataHolder gr = mGraphObjectList.get(i);
            xVals.add(gr.getmDate());
        }
        return xVals;
    }

    public static LineDataSet generateLineYvals(int limit) {
        ArrayList<Entry> LineYvals = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            GraphDataHolder gr = mGraphObjectList.get(i);
            LineYvals.add(new Entry(gr.getmClose(), i));
        }
        LineDataSet set = new LineDataSet(LineYvals, "Close");
        set.setColor(Color.rgb(240, 238, 70));
        set.setLineWidth(2.5f);
        set.setCircleColor(Color.rgb(240, 238, 70));
        set.setCircleRadius(5f);
        set.setFillColor(Color.rgb(240, 238, 70));
        set.setDrawCubic(true);
        set.setDrawValues(true);
        set.setValueTextSize(10f);
        set.setValueTextColor(Color.rgb(240, 238, 70));

        set.setAxisDependency(YAxis.AxisDependency.RIGHT);
        return set;


    }

    public static GraphDataHolder getInstance(int index) {
        return mGraphObjectList.get(index);
    }

    public static BarDataSet generateBarYvals(int limit) {
        ArrayList<BarEntry> entries = new ArrayList<BarEntry>();

        for (int index = 0; index < limit; index++) {
            GraphDataHolder gr = mGraphObjectList.get(index);
            entries.add(new BarEntry(gr.getmVolume(), index));
        }

        BarDataSet set = new BarDataSet(entries, "Volume");
        set.setColor(Color.rgb(60, 220, 78));
        set.setValueTextColor(Color.rgb(60, 220, 78));
        set.setValueTextSize(10f);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);

        return set;
    }

    public static void erase() {
        mGraphObjectList = null;
    }

    public String getmSymbol() {
        return mSymbol;
    }

    public void setmSymbol(String mSymbol) {
        this.mSymbol = mSymbol;
    }

    public String getmDate() {
        return mDate;
    }

    public void setmDate(String mDate) {
        this.mDate = mDate;
    }

    public float getmOpen() {
        return mOpen;
    }

    public void setmOpen(float mOpen) {
        this.mOpen = mOpen;
    }

    public float getmClose() {
        return mClose;
    }

    public void setmClose(float mClose) {
        this.mClose = mClose;
    }

    public float getmHigh() {
        return mHigh;
    }

    public void setmHigh(float mHigh) {
        this.mHigh = mHigh;
    }

    public float getmLow() {
        return mLow;
    }

    public void setmLow(float mLow) {
        this.mLow = mLow;
    }

    public float getmAdj_close() {
        return mAdj_close;
    }

    public void setmAdj_close(float mAdj_close) {
        this.mAdj_close = mAdj_close;
    }

    public int getmVolume() {
        return mVolume;
    }

    public void setmVolume(int mVolume) {
        this.mVolume = mVolume;
    }
}