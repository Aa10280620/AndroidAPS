package info.nightscout.androidaps.plugins.PumpDanaR.comm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import info.nightscout.androidaps.Config;
import info.nightscout.androidaps.MainApp;
import info.nightscout.androidaps.R;
import info.nightscout.androidaps.events.EventRefreshOverview;
import info.nightscout.androidaps.interfaces.PluginBase;
import info.nightscout.androidaps.plugins.PumpDanaR.DanaRPlugin;
import info.nightscout.androidaps.plugins.PumpDanaR.DanaRPump;
import info.nightscout.androidaps.plugins.PumpDanaRKorean.DanaRKoreanPlugin;
import info.nightscout.utils.ToastUtils;

public class MsgInitConnStatusTime extends MessageBase {
    private static Logger log = LoggerFactory.getLogger(MsgInitConnStatusTime.class);

    public MsgInitConnStatusTime() {
        SetCommand(0x0301);
    }

    @Override
    public void handleMessage(byte[] bytes) {
        if (bytes.length - 10 > 7) {
            ToastUtils.showToastInUiThread(MainApp.instance().getApplicationContext(),MainApp.sResources.getString(R.string.wrongpumpdriverselected), R.raw.error);
            ((DanaRPlugin) MainApp.getSpecificPlugin(DanaRPlugin.class)).disconnect("Wrong Model");
            log.debug("Wrong model selected. Switching to Korean DanaR");
            MainApp.getSpecificPlugin(DanaRKoreanPlugin.class).setFragmentEnabled(PluginBase.PUMP, true);
            MainApp.getSpecificPlugin(DanaRKoreanPlugin.class).setFragmentVisible(PluginBase.PUMP, true);
            MainApp.getSpecificPlugin(DanaRPlugin.class).setFragmentEnabled(PluginBase.PUMP, false);
            MainApp.getSpecificPlugin(DanaRPlugin.class).setFragmentVisible(PluginBase.PUMP, false);
            DanaRPump.getInstance().lastConnection = new Date(0); // mark not initialized

            //If profile coming from pump, switch it as well
            if(MainApp.getSpecificPlugin(DanaRPlugin.class).isEnabled(PluginBase.PROFILE)){
                (MainApp.getSpecificPlugin(DanaRPlugin.class)).setFragmentEnabled(PluginBase.PROFILE, false);
                (MainApp.getSpecificPlugin(DanaRKoreanPlugin.class)).setFragmentEnabled(PluginBase.PROFILE, true);
            }

            MainApp.getConfigBuilder().storeSettings();
            MainApp.bus().post(new EventRefreshOverview("MsgInitConnStatusTime"));
            return;
        }

        Date time = dateTimeSecFromBuff(bytes, 0);
        int versionCode = intFromBuff(bytes, 6, 1);

        if (Config.logDanaMessageDetail) {
            log.debug("Pump time: " + time);
            log.debug("Version code: " + versionCode);
        }
    }
}
