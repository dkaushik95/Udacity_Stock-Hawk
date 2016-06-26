package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.QuoteCursorAdapter;
import com.sam_chordas.android.stockhawk.rest.RecyclerViewItemClickListener;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockIntentService;
import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.melnykov.fab.FloatingActionButton;
import com.sam_chordas.android.stockhawk.touch_helper.SimpleItemTouchHelperCallback;

public class MyStocksActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {

  boolean isConnected;
  RecyclerView recyclerView;
  TextView mNoStocks;
  Toolbar toolbar;
  RelativeLayout cont;
  BroadcastReceiver receiver;
  LinearLayout mainview;
  Button prestocks;
  /**
   * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
   */


  private CharSequence mTitle;
  private Intent mServiceIntent;
  private ItemTouchHelper mItemTouchHelper;
  private QuoteCursorAdapter mCursorAdapter;
  private Context mContext;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mContext = this;

    isConnected = Utils.isConnected(getApplicationContext());
    setContentView(R.layout.activity_my_stocks);
    mainview = (LinearLayout) findViewById(R.id.mainview);
    toolbar = (Toolbar) findViewById(R.id.tooly);
    cont = (RelativeLayout) findViewById(R.id.hiddenpage);
    prestocks = (Button) findViewById(R.id.prestocks);

    setSupportActionBar(toolbar);


    mNoStocks = (TextView) findViewById(R.id.nostocks);
    mServiceIntent = new Intent(this, StockIntentService.class);

    recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    getLoaderManager().initLoader(Utils.CURSOR_LOADER_ID, null, this);

    mCursorAdapter = new QuoteCursorAdapter(this, null);
    recyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(this,
            new RecyclerViewItemClickListener.OnItemClickListener() {
              @Override
              public void onItemClick(View v, int position) {
                String symbol = ((TextView) (v.findViewById(R.id.stock_symbol))).getText().toString();
                if (Utils.deleteStock) {
                  mContext.getContentResolver().delete(QuoteProvider.Quotes.CONTENT_URI, QuoteColumns.SYMBOL + " = '" + symbol
                          + "'", null);
                  Utils.deleteStock = false;
                  recyclerView.setFocusable(false);
                  Toast.makeText(getApplicationContext(), getString(R.string.toast_stock_removed) + symbol, Toast.LENGTH_SHORT).show();
                } else {
                  Intent in = new Intent(getApplicationContext(), StockGraph.class);
                  in.putExtra(QuoteColumns.NAME , ((QuoteCursorAdapter.ViewHolder) recyclerView.getChildViewHolder(v)).name);
                  in.putExtra(QuoteColumns.SYMBOL, ((QuoteCursorAdapter.ViewHolder) recyclerView.getChildViewHolder(v)).quote);

                  startActivity(in);
                }
              }
            }));
    recyclerView.setAdapter(mCursorAdapter);

    prestocks.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Cursor c = callContent();
        if (c != null && c.getCount() != 0) {
          cont.setVisibility(View.GONE);
          mCursorAdapter.swapCursor(callContent());

        } else {
          Toast.makeText(getApplicationContext(), R.string.no_offline_Stocks, Toast.LENGTH_SHORT).show();
        }
      }
    });


    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.attachToRecyclerView(recyclerView);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (Utils.isConnected(getApplicationContext())) {
          new MaterialDialog.Builder(mContext).title(R.string.symbol_search)
                  .content(R.string.content_test)
                  .inputType(InputType.TYPE_CLASS_TEXT)
                  .input(R.string.input_hint, R.string.input_prefill, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                      if (input.toString().trim().equals("")) {
                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor se = sp.edit();
                        se.putInt(mContext.getString(R.string.server_status), StockTaskService.Stock_INVALID_INPUT);
                        se.apply();
                        return;
                      }

                      Cursor c = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                              new String[]{QuoteColumns.SYMBOL}, QuoteColumns.SYMBOL + "= ?",
                              new String[]{input.toString()}, null);
                      if (c.getCount() != 0) {
                        Toast toast =
                                Toast.makeText(MyStocksActivity.this, getString(R.string.existing_stock),
                                        Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
                        toast.show();

                      } else {
                        mServiceIntent.putExtra("tag", "add");
                        mServiceIntent.putExtra("symbol", input.toString());
                        startService(mServiceIntent);

                      }
                    }

                  })
                  .show();
        } else {
          networkToast();
        }

      }
    });

    ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mCursorAdapter);
    mItemTouchHelper = new ItemTouchHelper(callback);
    mItemTouchHelper.attachToRecyclerView(recyclerView);

    mTitle = getTitle();
    if (isConnected) {
      long period = 36000L;
      long flex = 10L;
      String periodicTag = "periodic";

      PeriodicTask periodicTask = new PeriodicTask.Builder()
              .setService(StockTaskService.class)
              .setPeriod(period)
              .setFlex(flex)
              .setTag(periodicTag)
              .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
              .setRequiresCharging(false)
              .build();
      GcmNetworkManager.getInstance(this).schedule(periodicTask);
    }
  }


  @Override
  public void onResume() {
    super.onResume();

  }

  @Override
  protected void onPause() {
    super.onPause();

  }

  public void networkToast() {
    Toast.makeText(mContext, getString(R.string.network_toast), Toast.LENGTH_SHORT).show();
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.my_stocks, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    int id = item.getItemId();

    if (id == R.id.action_settings) {
      return true;
    }


    if (id == R.id.action_change_units) {
      if (Utils.showPercent) {
        item.setTitle(getString(R.string.to_percentage));
        item.setIcon(R.drawable.percent);

      } else {
        item.setTitle(getString(R.string.to_dollar));
        item.setIcon(R.drawable.ic_attach_money_white_24dp);
      }
      Utils.showPercent = !Utils.showPercent;
      this.getContentResolver().notifyChange(QuoteProvider.Quotes.CONTENT_URI, null);
    }

    return super.onOptionsItemSelected(item);
  }



  public void updateErrorView() {
    switch (Utils.getServerStatus(getApplicationContext())) {
      case StockTaskService.Stock_STATUS_SERVER_DOWN:
        mNoStocks.setText(getString(R.string.server_down));
        cont.setVisibility(View.VISIBLE);
        break;
      case StockTaskService.Stock_STATUS_SERVER_INVALID:
        mNoStocks.setText(getString(R.string.server_invalid));
        cont.setVisibility(View.VISIBLE);
        break;
      case StockTaskService.Stock_STATUS_OK:
        cont.setVisibility(View.GONE);
        break;
      case StockTaskService.Stock_STATUS_UNKNOWN:
        cont.setVisibility(View.VISIBLE);
        mNoStocks.setText(getString(R.string.unknown_server));
        break;
      case StockTaskService.Stock_INVALID_INPUT:
        cont.setVisibility(View.VISIBLE);
        mNoStocks.setText(getString(R.string.invalid_input));
        break;


      default:
        cont.setVisibility(View.VISIBLE);
        mNoStocks.setText(getString(R.string.empty_db_msg));

    }

    resetSharedPref();
  }


  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {

    return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
            new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                    QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP, QuoteColumns.NAME},
            QuoteColumns.ISCURRENT + " = 1 ",
            null,
            null);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    if (data != null && data.getCount() != 0) {
      mCursorAdapter.swapCursor(data);

      cont.setVisibility(View.GONE);
    } else {

      if (!Utils.isConnected(getApplicationContext()))
        mNoStocks.setText(getString(R.string.no_internet));
      else {
        mNoStocks.setText(getString(R.string.empty_db_msg));
        if (Utils.getServerStatus(getApplicationContext()) != StockTaskService.Stock_STATUS_OK)
          updateErrorView();
      }
      cont.setVisibility(View.VISIBLE);

    }
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    mCursorAdapter.swapCursor(null);
  }

  private void resetSharedPref() {
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    sp.unregisterOnSharedPreferenceChangeListener(this);
    SharedPreferences.Editor se = sp.edit();
    se.putInt(mContext.getString(R.string.server_status), StockTaskService.Stock_STATUS_OK);
    se.apply();
    sp.registerOnSharedPreferenceChangeListener(this);

  }

  public Cursor callContent() {
    return getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
            new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                    QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP, QuoteColumns.NAME},
            QuoteColumns.ISCURRENT + " = 1 ",
            null,
            null);
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    if (key.equals(getString(R.string.server_status)))
      updateErrorView();

    if (key.equals(getString(R.string.loaders_switch))) {
      if (sharedPreferences.getString(getString(R.string.loaders_switch), "null").equals("on"))
        getLoaderManager().restartLoader(Utils.CURSOR_LOADER_ID, null, this);


      else
        getLoaderManager().destroyLoader(Utils.CURSOR_LOADER_ID);

    }

  }
}