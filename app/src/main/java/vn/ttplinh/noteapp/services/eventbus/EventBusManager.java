package vn.ttplinh.noteapp.services.eventbus;

import android.os.Bundle;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by linhtang on 7/28/17.
 */

public class EventBusManager {
    public static void post(EventBusMessage message, Bundle bundle) {
        EventBusModel model = new EventBusModel(message, bundle);
        EventBus.getDefault().post(model);
    }
}
