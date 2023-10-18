package top.dream;

import kd.bos.base.AbstractBasePlugIn;
import kd.bos.bill.BillShowParameter;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.CloseCallBack;
import kd.bos.form.ShowType;
import kd.bos.form.StyleCss;
import kd.bos.form.control.Button;
import kd.bos.form.control.Control;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.form.field.ComboEdit;
import kd.bos.form.field.ComboItem;
import kd.bos.metadata.dao.MetaCategory;
import kd.bos.metadata.dao.MetadataDao;
import kd.bos.metadata.form.ControlAp;
import kd.bos.metadata.form.FormMetadata;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import top.dream.tool.GetMessageVariable;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

/**
 * 基础资料界面
 */
public class InfoVariable extends AbstractBasePlugIn {

    @Override
    public void registerListener(EventObject e) {
        Button filterButton = this.getView().getControl("ozwe_buttonap");
        filterButton.addClickListener(this);
        this.addItemClickListeners("tbmain");
    }

    @Override
    public void click(EventObject e) {
        Control source = (Control)e.getSource();
        if (source.getKey().equalsIgnoreCase("ozwe_buttonap")) {
            if (this.getModel().getValue("number").toString().length()<=0) {
                this.getView().showMessage("请输入变量编码");
                return;
            } else {
                BillShowParameter billShowParameter = new BillShowParameter();
                StyleCss css = new StyleCss();
                css.setHeight("500");
                css.setWidth("1000");
                billShowParameter.getOpenStyle().setInlineStyleCss(css);
                billShowParameter.getOpenStyle().setShowType(ShowType.Modal);
                billShowParameter.setFormId("ozwe_filter");
                billShowParameter.setCloseCallBack(new CloseCallBack(this, "ozwe_filter"));
                //检测是否存在过滤条件数据
                DynamicObject filterInfo = BusinessDataServiceHelper.loadSingle("ozwe_filter",
                        "number,"
                                + "name," +
                                "id",
                        new QFilter[]{
                                new QFilter("number", QCP.equals, this.getModel().getValue("number").toString()),
                        });
                DynamicObject dynamicObject = (DynamicObject) this.getModel().getValue("ozwe_object");
                try {
                    billShowParameter.setCustomParam("number", this.getModel().getValue("number").toString());
                    billShowParameter.setCustomParam("formId", dynamicObject.getString("number"));
                } catch (NullPointerException exception) {
                    this.getView().showMessage("请选择业务对象");
                    return;
                }
                if (filterInfo != null) {
                    billShowParameter.setPkId(filterInfo.getLong("id"));
                }
                this.getView().showForm(billShowParameter);

            }
        }
    }

    /*@Override
    public void beforeBindData(EventObject e) {
        //修改通用过滤字段名称
        FilterGrid filterGrid = getView().getControl("ozwe_filtergridap");
        filterGrid.setEntityNumber("ozwe_customtemplate");
        List<String> keys = new ArrayList<String>();
        keys.add("number");
        keys.add("name");
        filterGrid.setFilterFieldKeys(keys);
    }*/

    @Override
    public void propertyChanged(PropertyChangedArgs event) {
        try {
            String changedField = event.getProperty().getName();
            if (changedField.equalsIgnoreCase("ozwe_object")) {
                ComboEdit comboEdit = this.getView().getControl("ozwe_readfield");
                DynamicObject dynamicObject = (DynamicObject) this.getModel().getValue("ozwe_object");
                String id = MetadataDao.getIdByNumber(dynamicObject.getString("number"), MetaCategory.Form);
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
