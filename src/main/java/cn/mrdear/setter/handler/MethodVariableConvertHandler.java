package cn.mrdear.setter.handler;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiNewExpression;
import com.intellij.psi.util.PsiTreeUtil;

import cn.mrdear.setter.model.InputConvertContext;
import cn.mrdear.setter.model.Mode;
import cn.mrdear.setter.model.OutputConvertResult;
import cn.mrdear.setter.model.ReturnClassModel;
import cn.mrdear.setter.model.SetterDataKeys;
import cn.mrdear.setter.model.SourceClassModel;

import java.util.Objects;

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

        boolean isBuilder = returnType.canBuilder();

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

        // builder判断
        if (isBuilder) {
            result.appendInsert(".build();");
        }
        // return 判断,只有返回类型与当前判断一致,才添加return
        PsiMethod psiMethod = SetterDataKeys.CURRENT_METHOD.getData(context);
        if (Objects.equals(psiMethod.getReturnType(), returnType.getType())) {
            result.appendInsert("return ").append(returnType.getVarName()).append(";");
        }

        return result;
    }

}
