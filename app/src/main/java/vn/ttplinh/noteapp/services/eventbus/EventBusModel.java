package vn.ttplinh.noteapp.services.eventbus;

import android.os.Bundle;

/**
 * Created by linhtang on 7/28/17.
 */

public class EventBusModel {
    private EventBusMessage message;
    private Bundle dataBundle;

    public EventBusModel (EventBusMessage message, Bundle bundle) {
        this.message = message;
        this.dataBundle = bundle;
    }

    public EventBusMessage getMessage() {
        return message;
    }

    public void setMessage(EventBusMessage message) {
        this.message = message;
    }

    public Bundle getDataBundle() {
        return dataBundle;
    }

    public void setDataBundle(Bundle dataBundle) {
        this.dataBundle = dataBundle;
    }
}
