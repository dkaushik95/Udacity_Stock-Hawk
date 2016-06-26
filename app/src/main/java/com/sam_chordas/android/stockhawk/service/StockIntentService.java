package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.recievers.StockWidgetProvider;

/**
 * Created by sam_chordas on 10/1/15.
 */
public class StockIntentService extends IntentService {

  public StockIntentService() {
    super(StockIntentService.class.getName());
  }

  public StockIntentService(String name) {
    super(name);
  }

  @Override
  protected void onHandleIntent(Intent intent) {

    StockTaskService stockTaskService = new StockTaskService(this);
    Bundle args = new Bundle();
    if (intent.getStringExtra("tag").equals("add")) {
      args.putString("symbol", intent.getStringExtra("symbol"));
    }

    // We can call OnRunTask from the intent service to force it to run immediately instead of
    // scheduling a task.

    int result = stockTaskService.onRunTask(new TaskParams(intent.getStringExtra("tag"), args));
    if (result == GcmNetworkManager.RESULT_SUCCESS) {
      notifyWidget();
    }

  }

  private void notifyWidget() {
    // THIS BROADCAST WILL UPDATE THE WIDGET WHENEVER THE GCMTASKSERVICE IS CALLED
    Intent dataUpdatedIntent = new Intent(StockWidgetProvider.ACTION_DATA_UPDATED)
            .setPackage(getPackageName());
    sendBroadcast(dataUpdatedIntent);

  }
}