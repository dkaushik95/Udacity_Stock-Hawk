package com.sam_chordas.android.stockhawk.service;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

/**
 * Created by dishantkaushik on 21/06/16.
 */
public class StackWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private static int mCount;
    private Context mContext;
    private int mAppWidgetId;
    private Cursor cursor;

    public StackRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    public void onCreate() {
    }

    public void onDestroy() {

    }

    public int getCount() {
        return mCount;
    }

    public RemoteViews getViewAt(int position) {


        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.stackitem);


        if (cursor == null || cursor.getCount() == 0) {
            rv.setEmptyView(R.id.stack_view, R.id.empty_view);
            return null;
        }

        cursor.moveToPosition(position);

        rv.setTextViewText(R.id.widgetquote, cursor.getString(cursor.getColumnIndex("symbol")));
        rv.setTextViewText(R.id.widgetbidprice, cursor.getString(cursor.getColumnIndex("bid_price")));

        rv.setTextViewText(R.id.widgetchange, cursor.getString(cursor.getColumnIndex("percent_change")));
        if (cursor.getInt(cursor.getColumnIndex("is_up")) == 1) {
            rv.setInt(R.id.widgetchange, "setBackgroundResource", R.drawable.percent_change_pill_green);
        } else {
            rv.setInt(R.id.widgetchange, "setBackgroundResource", R.drawable.percent_change_pill_red);

        }

        Intent graphactivityintent = new Intent();
        graphactivityintent.putExtra("Quote", cursor.getString(cursor.getColumnIndex(QuoteColumns.SYMBOL)));
        graphactivityintent.putExtra("Stockname", cursor.getString(cursor.getColumnIndex(QuoteColumns.NAME)));
        rv.setOnClickFillInIntent(R.id.stackWidgetItem, graphactivityintent);

        return rv;
    }

    public RemoteViews getLoadingView() {

        return null;
    }

    public int getViewTypeCount() {
        return 1;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }

    public void onDataSetChanged() {
        final long identityToken = Binder.clearCallingIdentity();


        cursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP, QuoteColumns.NAME},
                QuoteColumns.ISCURRENT + " = 1 ",
                null,
                null);
        Binder.restoreCallingIdentity(identityToken);


        if (cursor != null && cursor.getCount() != 0) {
            mCount = cursor.getCount();
        }
    }
}
