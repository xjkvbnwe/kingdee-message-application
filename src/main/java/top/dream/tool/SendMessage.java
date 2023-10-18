package top.dream.tool;

import java.util.ArrayList;
import java.util.List;

import kd.bos.dataentity.entity.ILocaleString;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.message.api.CountryCode;
import kd.bos.message.api.MessageChannels;
import kd.bos.message.api.ShortMessageInfo;
import kd.bos.message.service.handler.MessageHandler;
import kd.bos.servicehelper.workflow.MessageCenterServiceHelper;
import kd.bos.workflow.engine.msg.info.MessageInfo;

public class SendMessage {
    /**
     * 向用户发送信息
     * @param Content
     * @param userPhone
     * @return
     */
    public static String sendMessage(String Content,List<String> userPhone) {
        ShortMessageInfo shortMessageInfo = new ShortMessageInfo();
        shortMessageInfo.setPhone(userPhone);
        shortMessageInfo.setCountryCode(CountryCode.CN);
        shortMessageInfo.setMessage(Content);
        MessageHandler.sendShortMessage(shortMessageInfo);

        return "success";

    }

}

