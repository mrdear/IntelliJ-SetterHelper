package cn.mrdear.setter.handler;

import cn.mrdear.setter.model.OutputConvertResult;
import cn.mrdear.setter.model.ReturnClassModel;
import cn.mrdear.setter.model.SourceClassModel;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * @author quding
 * @since 2022/2/9
 */
public abstract class AbstractConvertHandler implements ConvertHandler {

    protected void fillMainSetConvert(OutputConvertResult result, ReturnClassModel returnType, SourceClassModel sourceType) {
        // 添加set转换
        boolean isBuilder = returnType.isBuilder();
        Map<String, String> getFiledMap = Optional.ofNullable(sourceType).map(SourceClassModel::getCanAccessGetFiled).orElse(Collections.emptyMap());

        returnType.getCanAccessSetFiled().forEach((k, v) -> {
            String getMethod = getFiledMap.getOrDefault(k, "");

            result.appendInsert(String.format(v, getMethod));

            if (!isBuilder) {
                result.appendInsert(";");
            }
            result.appendInsert("\n");
        });
    }

}
