package cn.mrdear.setter.handler;

import cn.mrdear.setter.model.InputConvertContext;
import cn.mrdear.setter.model.Mode;
import cn.mrdear.setter.model.OutputConvertResult;
import cn.mrdear.setter.model.SourceClassModel;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * 处理方法内生成
 * @author quding
 * @since 2022/2/7
 */
public class MethodConvertHandler implements ConvertHandler {

    @Override
    public Mode support() {
        return Mode.METHOD;
    }

    @Override
    public OutputConvertResult handler(InputConvertContext context) {
        OutputConvertResult result = new OutputConvertResult();

        SourceClassModel returnType = context.getReturnType();
        SourceClassModel sourceType = context.getSourceType();

        boolean isThis = returnType.isThis();
        boolean isBuilder = returnType.isBuilder();

        // 添加首行new对象
        if (!isThis) {
            if (isBuilder) {
                result.appendInsert(returnType.getPsiClass().getName()).append(" ").append(returnType.getVarName())
                    .append(" = ").append(returnType.getPsiClass().getName()).append(".builder()\n");
            } else {
                result.appendInsert(returnType.getPsiClass().getName()).append(" ").append(returnType.getVarName())
                    .append(" = new ").append(returnType.getPsiClass().getName()).append("();");
            }
        }

        // 添加set转换
        Map<String, String> getFiledMap = Optional.ofNullable(sourceType).map(SourceClassModel::getCanAccessGetFiled).orElse(Collections.emptyMap());
        returnType.getCanAccessSetFiled().forEach((k,v) -> {
            String getMethod = getFiledMap.getOrDefault(k, "");
            result.appendInsert(String.format(v, getMethod));
            if (!isBuilder) {
                result.appendInsert(";");
            }
            result.appendInsert("\n");
        });

        // 添加return
        if (!isThis) {
            if (isBuilder) {
                result.appendInsert(".build();");
                result.appendInsert("return ").append(returnType.getVarName()).append(";");
            } else {
                result.appendInsert("return ").append(returnType.getVarName()).append(";");
            }
        }

        return result;
    }


}
