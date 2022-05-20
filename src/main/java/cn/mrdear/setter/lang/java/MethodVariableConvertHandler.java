package cn.mrdear.setter.lang.java;

import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiNewExpression;
import com.intellij.psi.util.PsiTreeUtil;

import cn.mrdear.setter.lang.ConvertHandler;
import cn.mrdear.setter.model.InputConvertContext;
import cn.mrdear.setter.model.OutputConvertResult;
import cn.mrdear.setter.model.SetterDataKeys;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 处理方法内生成
 *
 * @author quding
 * @since 2022/2/7
 */
public class MethodVariableConvertHandler implements ConvertHandler {

    @Override
    public OutputConvertResult handler(InputConvertContext context) {
        OutputConvertResult result = new OutputConvertResult();

        JavaReturnClassModel returnType = (JavaReturnClassModel)SetterDataKeys.RETURN_MODEL.getData(context);
        JavaSourceClassModel sourceType = (JavaSourceClassModel)SetterDataKeys.SOURCE_MODEL.getData(context);

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

        // 填充中间转换
        Map<String, List<Pair<PsiClass, String>>> sourceMap = Optional.ofNullable(sourceType)
            .map(JavaSourceClassModel::getCanAccessGetFiled).orElse(Collections.emptyMap());

        returnType.getCanAccessSetFiled().forEach((k, v) -> {
            List<Pair<PsiClass, String>> pairs = sourceMap.get(k);

            if (null != pairs) {
                boolean isMatch = false;
                for (Pair<PsiClass, String> pair : pairs) {
                    if (pair.getFirst().isEquivalentTo(v.getFirst())) {
                        result.appendInsert(String.format(v.second, pair.second));
                        if (!isBuilder) {
                            result.appendInsert(";");
                        }
                        isMatch = true;
                        break;
                    }
                }

                if (!isMatch) {
                    result.appendInsert(String.format(v.second, "")).append(";");
                }
            } else {
                result.appendInsert(String.format(v.second, "")).append(";");
            }

            result.appendInsert("\n");
        });

        // builder判断
        if (isBuilder) {
            result.appendInsert(".build();");
        }
        // return 判断,只有返回类型与当前判断一致,才添加return
        PsiMethod psiMethod = SetterDataKeys.CURRENT_METHOD.getData(context);
        if (Objects.equals(psiMethod.getReturnType(), returnType.getPsiType())) {
            result.appendInsert("return ").append(returnType.getVarName()).append(";");
        }

        return result;
    }

}
