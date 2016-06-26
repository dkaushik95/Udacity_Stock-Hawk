package com.sam_chordas.android.stockhawk.rest;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.touch_helper.ItemTouchHelperAdapter;
import com.sam_chordas.android.stockhawk.touch_helper.ItemTouchHelperViewHolder;

/**
 * Created by sam_chordas on 10/6/15.
 *  Credit to skyfishjy gist:
 *    https://gist.github.com/skyfishjy/443b7448f59be978bc59
 * for the code structure
 */
public class QuoteCursorAdapter extends CursorRecyclerViewAdapter<QuoteCursorAdapter.ViewHolder>
        implements ItemTouchHelperAdapter {

  private static Context mContext;
  private static Typeface robotoLight;
  private boolean isPercent;

  public QuoteCursorAdapter(Context context, Cursor cursor) {
    super(context, cursor);
    mContext = context;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    robotoLight = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Light.ttf");
    View itemView = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.list_item_quote, parent, false);
    ViewHolder vh = new ViewHolder(itemView);
    return vh;
  }

  @Override
  public void onBindViewHolder(final ViewHolder viewHolder, final Cursor cursor) {
    String mChange;

    viewHolder.name = cursor.getString(cursor.getColumnIndex("name"));
    viewHolder.quote = cursor.getString(cursor.getColumnIndex("symbol"));
    viewHolder.symbol.setText(cursor.getString(cursor.getColumnIndex("symbol")));
    viewHolder.symbol.setContentDescription(mContext.getString(R.string.stock_is) + cursor.getString(cursor.getColumnIndex("name")));
    viewHolder.bidPrice.setText(cursor.getString(cursor.getColumnIndex("bid_price")));
    viewHolder.bidPrice.setContentDescription(mContext.getString(R.string.bid_price_is, cursor.getString(cursor.getColumnIndex("bid_price"))));

    if (cursor.getInt(cursor.getColumnIndex("is_up")) == 1) {
      mChange = mContext.getString(R.string.is_up_by);

      viewHolder.change.setBackgroundResource(R.drawable.percent_change_pill_green);

    } else {
      mChange = mContext.getString(R.string.is_down_by);
      viewHolder.change.setBackgroundResource(R.drawable.percent_change_pill_red);
    }
    if (Utils.showPercent) {
      viewHolder.change.setText(cursor.getString(cursor.getColumnIndex("percent_change")));
      viewHolder.change.setContentDescription(mContext.getString(R.string.total_content_description, mChange, cursor.getString(cursor.getColumnIndex("percent_change")), ""));
    } else {
      viewHolder.change.setText(cursor.getString(cursor.getColumnIndex("change")));
      viewHolder.change.setContentDescription(mContext.getString(R.string.total_content_description, mChange, cursor.getString(cursor.getColumnIndex("change")), " dollars"));
    }

  }

  @Override
  public void onItemDismiss(int position) {
    Cursor c = getCursor();
    c.moveToPosition(position);
    String symbol = c.getString(c.getColumnIndex(QuoteColumns.SYMBOL));
    mContext.getContentResolver().delete(QuoteProvider.Quotes.withSymbol(symbol), null, null);
    notifyItemRemoved(position);
  }

  @Override
  public int getItemCount() {
    return super.getItemCount();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder
          implements ItemTouchHelperViewHolder, View.OnClickListener {
    public final TextView symbol;
    public final TextView bidPrice;
    public final TextView change;
    public String name, quote; //NEED THIS FOR PARSING THE VALUES TO THE 'StockGraphActivity'

    public ViewHolder(View itemView) {

      super(itemView);

      symbol = (TextView) itemView.findViewById(R.id.stock_symbol);
      symbol.setTypeface(robotoLight);
      bidPrice = (TextView) itemView.findViewById(R.id.bid_price);
      change = (TextView) itemView.findViewById(R.id.change);
    }

    @Override
    public void onItemSelected() {
      itemView.setBackgroundColor(Color.LTGRAY);
    }

    @Override
    public void onItemClear() {
      itemView.setBackgroundColor(0);
    }

    @Override
    public void onClick(View v) {


    }
  }
}