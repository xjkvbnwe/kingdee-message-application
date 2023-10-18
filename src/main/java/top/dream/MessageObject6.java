package top.dream;

import kd.bos.base.AbstractBasePlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.field.ComboEdit;
import kd.bos.form.field.ComboItem;
import kd.bos.metadata.dao.MetaCategory;
import kd.bos.metadata.dao.MetadataDao;
import kd.bos.metadata.form.ControlAp;
import kd.bos.metadata.form.FormMetadata;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.sdk.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * 基础资料界面
 */
public class MessageObject6 extends AbstractBasePlugIn implements Plugin {
    @Override
    public void propertyChanged(PropertyChangedArgs event) {
        try {
            String changedField = event.getProperty().getName();
            if (changedField.equalsIgnoreCase("ozwe_msgevent")) {
                ComboEdit comboEdit = this.getView().getControl("ozwe_targetfield");
                DynamicObject dynamicObject = (DynamicObject) this.getModel().getValue("ozwe_msgevent");
                String eventNumber = dynamicObject.getString("eventnumber");
                DynamicObject eventEntity = BusinessDataServiceHelper.loadSingle("evt_event", new QFilter[] {new QFilter("numberview", QCP.equals, eventNumber)} );
                String id = MetadataDao.getIdByNumber(eventEntity.getDynamicObject("entity").getString("number"), MetaCategory.Form);
                //获取表单元数据
                FormMetadata formMeta = (FormMetadata) MetadataDao.readRuntimeMeta(id, MetaCategory.Form);
                //获取所有控件集合
                List<ControlAp<?>> items = formMeta.getItems();
                List<ComboItem> comboItemList = new ArrayList<>();
                for (ControlAp<?> item : items) {
                    //控件名称
                    String name = item.getName().getLocaleValue();
                    //控件编码
                    String key = item.getKey();
                    ComboItem comboItem = new ComboItem();
                    LocaleString caption = new LocaleString(name+" ("+key+")");
                    comboItem.setCaption(caption);
                    comboItem.setValue(key);
                    comboItemList.add(comboItem);
                }
                comboEdit.setComboItems(comboItemList);

                super.propertyChanged(event);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}