package top.dream.tool;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.serialization.SerializationUtils;
import kd.bos.entity.EntityMetadataCache;
import kd.bos.entity.MainEntityType;
import kd.bos.entity.filter.FilterBuilder;
import kd.bos.entity.filter.FilterCondition;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;

import java.util.List;

public class GetMessageVariable {
    public static String getMessageVariable(String variable, DynamicObject variableObject, DynamicObject filterObject) {
        String filterString = filterObject.getString("ozwe_filtercontext");
        FilterCondition filterCondition = SerializationUtils.fromJsonString(filterString, FilterCondition.class);
        if (filterCondition != null) {
            //获取选中的变量表单拿QFilter
            DynamicObject selectObject = variableObject.getDynamicObject("ozwe_object");
            MainEntityType mainEntityType = EntityMetadataCache.getDataEntityType(selectObject.getString("number"));
            FilterBuilder filterBuilder = new FilterBuilder(mainEntityType, filterCondition, true);
            filterBuilder.buildFilter();
            List<QFilter> qFilterList = filterBuilder.getQFilters();
            QFilter[] qFilters = qFilterList.toArray(new QFilter[0]);
            //用QFilter拿信息
            DynamicObject[] targetObject = BusinessDataServiceHelper.load(selectObject.getString("number"), variableObject.getString("ozwe_readfield"), qFilters);
            if ("0".equals(variableObject.getString("ozwe_readtype"))) {
                StringBuilder sb = new StringBuilder();
                if (targetObject.length == 0) {
                    return "";
                }
                for (int i = 0 ; i< targetObject.length; i++) {
                    sb.append(targetObject[i].getString(variableObject.getString("ozwe_readfield")));
                    if (targetObject.length!= 1 && i != targetObject.length-1) {
                        sb.append(variableObject.getString("ozwe_devided"));
                    }
                }
                return sb.toString();
            } else if ("1".equals(variableObject.getString("ozwe_readtype"))) {
                try {
                    double sum = 0;
                    for (DynamicObject targetObjectSingle : targetObject) {
                        double value = Double.parseDouble(targetObjectSingle.getString(variableObject.getString("ozwe_readfield")));
                        sum += value;
                    }
                    return sum+"";
                } catch (Exception ee) {
                    return "读取内容无法转换为数值，请检查配置";
                }
            } else if ("2".equals(variableObject.getString("ozwe_readtype"))) {
                try {
                    double sum = 0;
                    for (DynamicObject targetObjectSingle : targetObject) {
                        double value = Double.parseDouble(targetObjectSingle.getString(variableObject.getString("ozwe_readfield")));
                        sum += value;
                    }
                    return sum/ targetObject.length + "";
                } catch (Exception ee) {
                    return "读取内容无法转换为数值，请检查配置";
                }
            } else if ("3".equals(variableObject.getString("ozwe_readtype"))) {
                try {
                    double k = Double.parseDouble(targetObject[0].getString(variableObject.getString("ozwe_readfield")));
                    for (int i = 1; i < targetObject.length; i++) {
                        double indexValue = Double.parseDouble(targetObject[i].getString(variableObject.getString("ozwe_readfield")));
                        if (indexValue > k) {
                            k = indexValue;
                        }
                    }
                    return k+"";
                } catch (Exception ee) {
                    return "读取内容无法转换为数值，请检查配置";
                }
            }
            else {
                try {
                    double k = Double.parseDouble(targetObject[0].getString(variableObject.getString("ozwe_readfield")));
                    for (int i = 1; i < targetObject.length; i++) {
                        double indexValue = Double.parseDouble(targetObject[i].getString(variableObject.getString("ozwe_readfield")));
                        if (indexValue < k) {
                            k = indexValue;
                        }
                    }
                    return k+"";
                } catch (Exception ee) {
                    return "读取内容无法转换为数值，请检查配置";
                }
            }
        } else {
            return variable;
        }
    }
}
