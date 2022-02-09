package cn.mrdear.setter.handler;

import com.intellij.psi.PsiNewExpression;
import com.intellij.psi.util.PsiTreeUtil;

import cn.mrdear.setter.model.InputConvertContext;
import cn.mrdear.setter.model.Mode;
import cn.mrdear.setter.model.OutputConvertResult;
import cn.mrdear.setter.model.ReturnClassModel;
import cn.mrdear.setter.model.SourceClassModel;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * 处理方法内生成
 *
 * @author quding
 * @since 2022/2/7
 */
public class MethodVariableConvertHandler extends AbstractConvertHandler {

    @Override
    public Mode support() {
        return Mode.METHOD_VARIABLE;
    }

    @Override
    public OutputConvertResult handler(InputConvertContext context) {
        OutputConvertResult result = new OutputConvertResult();

        ReturnClassModel returnType = context.getReturnType();
        SourceClassModel sourceType = context.getSourceType();

        boolean isBuilder = returnType.isBuilder();

        // 添加首行new对象
        if (isBuilder) {
            result.appendInsert(" = ").append(returnType.getPsiClass().getName()).append(".builder()\n");
        } else {
            // 当自身没有初始化函数时,才给定初始化
            if (null == PsiTreeUtil.getNextSiblingOfType(context.getPsiCurrent(), PsiNewExpression.class)) {
                result.appendInsert(" = new ").append(returnType.getPsiClass().getName()).append("();");
            }
        }

        // 添加set转换
        fillMainSetConvert(result, returnType, sourceType);

        // 添加return
        if (isBuilder) {
            result.appendInsert(".build();");
            result.appendInsert("return ").append(returnType.getVarName()).append(";");
        } else {
            result.appendInsert("return ").append(returnType.getVarName()).append(";");
        }

        return result;
    }

}
