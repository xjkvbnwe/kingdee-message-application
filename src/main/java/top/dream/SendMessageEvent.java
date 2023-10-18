package top.dream;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import kd.bos.bec.api.IEventHandler;
import kd.bos.bec.model.KDBizEvent;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import top.dream.tool.GetMessageVariable;
import top.dream.tool.SendMessage;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import java.util.*;

import static top.dream.tool.EmailMessage.createMimeMessage;

public class SendMessageEvent implements IEventHandler {
    @Override
    public Object execute(KDBizEvent kdBizEvent, String s) {
        String eventNumber = kdBizEvent.getEventNumber();
        String eventFormBillNo = "";
        JSONArray jsonArray = JSONArray.parseArray(kdBizEvent.getSource());
        for (Object o : jsonArray) {
            JSONObject jsonObject = (JSONObject) o;
            if (jsonObject.getString("billno") != null) {
                eventFormBillNo = jsonObject.getString("billno");
            } else {
                eventFormBillNo = jsonObject.getString("number");
            }
        }
        DynamicObject dy = BusinessDataServiceHelper.loadSingle("ozwe_message_object",new QFilter[]{new QFilter("ozwe_eventnumber", QCP.equals,eventNumber+".SendAllMessage")});
        if (dy != null) {
            if ("0".equalsIgnoreCase(dy.getString("ozwe_combofield"))) {
                String phoneAll = getReceivers(dy, "ozwe_userid");
                String context = getFormField(kdBizEvent.getSource(), getContext(dy));
                List<String> receivers = new ArrayList<>();
                //获取发送用户
                if ("1".equalsIgnoreCase(dy.getString("ozwe_origin"))) {
                    String[] phoneArray = phoneAll.split(",");
                    for (String phone : phoneArray) {
                        if (phone.length() == 11 && !receivers.contains(phone)) {
                            receivers.add(phone);
                        }
                    }
                } else {
                    DynamicObject eventEntity = BusinessDataServiceHelper.loadSingle("evt_event", new QFilter[] {new QFilter("numberview", QCP.equals, eventNumber)} );
                    String targetObjectFormName = eventEntity.getDynamicObject("entity").getString("number");
                    DynamicObject targetObject;
                    try {
                        targetObject = BusinessDataServiceHelper.loadSingle(targetObjectFormName, new QFilter[]{new QFilter("billno", QCP.equals, eventFormBillNo)});
                    } catch (Exception ee) {
                        targetObject = BusinessDataServiceHelper.loadSingle(targetObjectFormName, new QFilter[]{new QFilter("number", QCP.equals, eventFormBillNo)});
                    }
                    if (targetObject == null) {
                        targetObject = BusinessDataServiceHelper.loadSingle(targetObjectFormName, new QFilter[]{new QFilter("number", QCP.equals, eventFormBillNo)});
                    }

                    String targetReceiver = targetObject.getString(dy.getString("ozwe_targetfield"));
                    try {
                        DynamicObject userObjectPre = targetObject.getDynamicObject(dy.getString("ozwe_targetfield"));
                        targetReceiver = userObjectPre.getString("id");
                        System.out.println(targetReceiver);
                        long id = Long.parseLong(targetReceiver);
                        DynamicObject userObject = BusinessDataServiceHelper.loadSingle("bos_user", new QFilter[]{new QFilter("id", QCP.equals, id)});
                        receivers.add(userObject.getString("phone"));
                    } catch (Exception ee) {
                        String[] phoneArray = targetReceiver.split(",");
                        for (String phone : phoneArray) {
                            if (phone.length() == 11 && !receivers.contains(phone)) {
                                receivers.add(phone);
                            }
                        }
                    }
                }
                SendMessage.sendMessage(context, receivers);
            } else if ("2".equalsIgnoreCase(dy.getString("ozwe_combofield"))) {
                DynamicObject emailObject = dy.getDynamicObject("ozwe_toemail");
                String myEmailAccount = emailObject.getString("ozwe_sendnumber");
                String myEmailPassword = emailObject.getString("ozwe_sendsecret");
                String myEmailSMTPHost = emailObject.getString("ozwe_smtp");
                String[] receiveMailAccount;
                if ("1".equalsIgnoreCase(dy.getString("ozwe_origin"))) {
                    receiveMailAccount = getReceivers(dy, "ozwe_destination_email").split(",");
                } else {
                    DynamicObject eventEntity = BusinessDataServiceHelper.loadSingle("evt_event", new QFilter[] {new QFilter("numberview", QCP.equals, eventNumber)} );
                    String targetObjectFormName = eventEntity.getDynamicObject("entity").getString("number");
                    DynamicObject targetObject;
                    try {
                        targetObject = BusinessDataServiceHelper.loadSingle(targetObjectFormName, new QFilter[]{new QFilter("billno", QCP.equals, eventFormBillNo)});
                    } catch (Exception ee) {
                        targetObject = BusinessDataServiceHelper.loadSingle(targetObjectFormName, new QFilter[]{new QFilter("number", QCP.equals, eventFormBillNo)});
                    }
                    if (targetObject == null) {
                        targetObject = BusinessDataServiceHelper.loadSingle(targetObjectFormName, new QFilter[]{new QFilter("number", QCP.equals, eventFormBillNo)});
                    }

                    String targetReceiver = targetObject.getString(dy.getString("ozwe_targetfield"));
                    try {
                        DynamicObject userObjectPre = targetObject.getDynamicObject(dy.getString("ozwe_targetfield"));
                        targetReceiver = userObjectPre.getString("id");
                        long id = Long.parseLong(targetReceiver);
                        DynamicObject userObject = BusinessDataServiceHelper.loadSingle("bos_user", new QFilter[]{new QFilter("id", QCP.equals, id)});
                        receiveMailAccount = new String[1];
                        receiveMailAccount[0] = userObject.getString("email");
                    } catch (Exception ee) {
                        receiveMailAccount = targetReceiver.split(",");
                    }
                }
                //获取邮件配置
                Properties props = new Properties();
                props.setProperty("mail.transport.protocol", "smtp");
                props.setProperty("mail.smtp.host", myEmailSMTPHost);
                props.setProperty("mail.smtp.auth", "true");
                Session session = Session.getInstance(props);

                //寻找消息变量
                String context = getFormField(kdBizEvent.getSource(), getContext(dy));
                Set<String> mailSet = new TreeSet<>(Arrays.asList(receiveMailAccount));

                for(String receive : mailSet) {
                    if (receive.length() <= 1) {
                        continue;
                    }
                    MimeMessage message = null;
                    try {
                        message = createMimeMessage(session, myEmailAccount, receive, dy.getString("ozwe_title"), context);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // 4. 根据 Session 获取邮件传输对象
                    Transport transport;
                    try {
                        transport = session.getTransport();
                        transport.connect(myEmailAccount, myEmailPassword);
                        transport.sendMessage(message, message.getAllRecipients());
                        transport.close();
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                }

                // 3. 创建一封邮件即邮件对象
            }
        }
        return null;
    }

    private String getReceivers(DynamicObject dy, String fieldName) {
        String phoneAll = dy.getString(fieldName);
        DynamicObject[] variableObjectArray = BusinessDataServiceHelper.load("ozwe_variable",
                "number," +
                        "ozwe_object," +
                        "ozwe_readfield," +
                        "ozwe_devided," +
                        "ozwe_readtype",
                new QFilter[] {new QFilter("number", QCP.not_equals, null)} );
        for (DynamicObject variableObject: variableObjectArray) {
            String number = variableObject.getString("number");
            DynamicObject filterObject = BusinessDataServiceHelper.loadSingle("ozwe_filter", new QFilter[] {new QFilter("number", QCP.equals, number)} );
            phoneAll = phoneAll.replace("%"+number+"%", GetMessageVariable.getMessageVariable(number, variableObject, filterObject));
        }

        return phoneAll;
    }

    private String getFormField(String source , String fieldName) {
        JSONArray jsonArray = JSONArray.parseArray(source);
        for (Object o : jsonArray) {
            JSONObject jsonObject = (JSONObject) o;
            for (String key : jsonObject.keySet()) {
                fieldName = fieldName.replace("{"+key+"}", jsonObject.getString(key));
            }
        }
        return fieldName;
    }

    private String getContext(DynamicObject dy) {
        String context = dy.getString("ozwe_context");
        DynamicObject[] variableObjectArray = BusinessDataServiceHelper.load("ozwe_variable",
                "number," +
                        "ozwe_object," +
                        "ozwe_readfield," +
                        "ozwe_devided," +
                        "ozwe_readtype",
                new QFilter[] {new QFilter("number", QCP.not_equals, null)} );
        for (DynamicObject variableObject: variableObjectArray) {
            String number = variableObject.getString("number");
            DynamicObject filterObject = BusinessDataServiceHelper.loadSingle("ozwe_filter", new QFilter[] {new QFilter("number", QCP.equals, number)} );
            context = context.replace("%"+number+"%", GetMessageVariable.getMessageVariable(number, variableObject, filterObject));
        }
        return context;
    }
}

