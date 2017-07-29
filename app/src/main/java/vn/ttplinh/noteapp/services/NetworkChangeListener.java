package vn.ttplinh.noteapp.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import vn.ttplinh.noteapp.services.eventbus.EventBusManager;
import vn.ttplinh.noteapp.services.eventbus.EventBusMessage;
import vn.ttplinh.noteapp.utils.Constants;

/**
 * Created by linhtang on 7/28/17.
 */

public class NetworkChangeListener extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle dataExtras = intent.getExtras();
        if (dataExtras != null) {
            @SuppressWarnings("deprecation")
            NetworkInfo ni = (NetworkInfo) dataExtras.get(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
                sendMessage();
            }
        }

    }

    private void sendMessage() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(Constants.INTENT_BOOLEAN, true);
        EventBusManager.post(EventBusMessage.NETWORK_CONNECTED, bundle);
    }
}
