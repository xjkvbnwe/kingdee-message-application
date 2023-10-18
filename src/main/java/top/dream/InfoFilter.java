package top.dream;

import kd.bos.base.AbstractBasePlugIn;
import kd.bos.dataentity.serialization.SerializationUtils;
import kd.bos.entity.filter.FilterCondition;
import kd.bos.form.control.FilterGrid;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.metadata.dao.MetaCategory;
import kd.bos.metadata.dao.MetadataDao;
import kd.bos.metadata.form.ControlAp;
import kd.bos.metadata.form.FormMetadata;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

/**
 * 基础资料界面
 */
public class InfoFilter extends AbstractBasePlugIn {
    @Override
    public void beforeBindData(EventObject e) {
        String formId = getView().getFormShowParameter().getCustomParam("formId");
        if (formId != null) {
            //修改通用过滤字段名称
            FilterGrid filterGrid = getView().getControl("ozwe_filtergridap");
            filterGrid.setEntityNumber(formId);
            List<String> keys = new ArrayList<String>();
            String id = MetadataDao.getIdByNumber(formId, MetaCategory.Form);
            //获取表单元数据
            FormMetadata formMeta = (FormMetadata) MetadataDao.readRuntimeMeta(id, MetaCategory.Form);
            //获取所有控件集合
            List<ControlAp<?>> items = formMeta.getItems();
            for (ControlAp<?> item : items) {
                //控件编码
                String key = item.getKey();
                keys.add(key);
            }
            filterGrid.setFilterFieldKeys(keys);

        }
    }

    @Override
    public void afterBindData(EventObject e) {
        //读取已经配置好的对象
        FilterGrid filterGrid = getView().getControl("ozwe_filtergridap");
        String filterString = getModel().getValue("ozwe_filtercontext").toString();
        FilterCondition filterCondition = null;
        if (filterString.length()>0) {
            filterCondition = SerializationUtils.fromJsonString(filterString, FilterCondition.class);
        }
        if (filterCondition != null){
            filterGrid.SetValue(filterCondition);
        }
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        String number = getView().getFormShowParameter().getCustomParam("number");
        if (number != null) {
            this.getModel().setValue("number", number);
        }
    }

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addItemClickListeners("tbmain");
    }

    @Override
    //按钮功能执行
    public void beforeItemClick(BeforeItemClickEvent e) {
        String itemKey = e.getItemKey();
        if (itemKey.equalsIgnoreCase("ozwe_filtersave")) {
            // 把条件内容序列化为字符串，以便存储
            String filter = null;
            // 序列化
            FilterGrid filterGrid = getView().getControl("ozwe_filtergridap");
            FilterGrid.FilterGridState filterGridState = filterGrid.getFilterGridState();
            FilterCondition filterCondition = filterGridState.getFilterCondition();
            if (filterCondition != null) {
                filter = SerializationUtils.toJsonString(filterCondition);
            }
            getModel().setValue("ozwe_filtercontext", filter);
        }
    }

    @Override
    //按钮功能执行完成之后关闭界面
    public void itemClick(ItemClickEvent e) {
        String itemKey = e.getItemKey();
        if (itemKey.equalsIgnoreCase("ozwe_filtersave")) {
            this.getView().close();
        }
    }
}
