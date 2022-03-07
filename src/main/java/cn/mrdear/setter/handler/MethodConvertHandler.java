package cn.mrdear.setter.handler;

import cn.mrdear.setter.model.InputConvertContext;
import cn.mrdear.setter.model.Mode;
import cn.mrdear.setter.model.OutputConvertResult;
import cn.mrdear.setter.model.ReturnClassModel;
import cn.mrdear.setter.model.SourceClassModel;

/**
 * 处理方法内生成
 *
 * @author quding
 * @since 2022/2/7
 */
public class MethodConvertHandler extends AbstractConvertHandler {

    @Override
    public Mode support() {
        return Mode.METHOD;
    }

    @Override
    public OutputConvertResult handler(InputConvertContext context) {
        OutputConvertResult result = new OutputConvertResult();

        ReturnClassModel returnType = context.getReturnType();
        SourceClassModel sourceType = context.getSourceType();

        boolean isThis = returnType.isThis();
        boolean isBuilder = returnType.canBuilder();

        // 添加首行new对象
        if (isBuilder) {
            result.appendInsert(returnType.getPsiClass().getName()).append(" ").append(returnType.getVarName())
                .append(" = ").append(returnType.getPsiClass().getName()).append(".builder()\n");
        } else if (!isThis) {
            result.appendInsert(returnType.getPsiClass().getName()).append(" ").append(returnType.getVarName())
                .append(" = new ").append(returnType.getPsiClass().getName()).append("();");
        }

        // 填充中间转换
        fillMainSetConvert(result, returnType, sourceType);

        // 添加return
        if (isBuilder) {
            result.appendInsert(".build();");
            result.appendInsert("return ").append(returnType.getVarName()).append(";");
        } else if (!isThis) {
            result.appendInsert("return ").append(returnType.getVarName()).append(";");
        }

        return result;
    }


}
