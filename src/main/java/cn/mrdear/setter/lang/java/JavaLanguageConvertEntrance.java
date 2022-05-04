package cn.mrdear.setter.lang.java;

import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLocalVariable;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiTypesUtil;

import cn.mrdear.setter.lang.ConvertHandler;
import cn.mrdear.setter.lang.LanguageConvertEntrance;
import cn.mrdear.setter.model.InputConvertContext;
import cn.mrdear.setter.model.OutputConvertResult;
import cn.mrdear.setter.model.SetterDataKeys;
import cn.mrdear.setter.model.SetterHelperException;
import cn.mrdear.setter.utils.PsiMyUtils;

import java.util.Objects;

/**
 * @author quding
 * @since 2022/5/3
 */
public class JavaLanguageConvertEntrance implements LanguageConvertEntrance {

    @Override
    public Language support() {
        return Language.findLanguageByID("JAVA");
    }

    /**
     * Java的转换,首先识别模式,然后调用不同的Handler执行转换逻辑
     */
    @Override
    public OutputConvertResult handlerConvert(InputConvertContext context) {
        PsiElement currentElement = context.getPsiCurrent();
        PsiElement parentElement = context.getPsiParent();

        JavaSourceClassModel sourceModel = null;
        JavaReturnClassModel returnModel = null;
        ConvertHandler handler = null;
        // 识别模式,根据当前父级节点类型判断,优先级是Local变量,方法,Class对象
        if (parentElement instanceof PsiLocalVariable) {
            // 当前光标指向了局部变量,首先获取当前的方法
            PsiMethod method = PsiTreeUtil.getParentOfType(parentElement, PsiMethod.class);
            if (null == method) {
                throw new UnsupportedOperationException("not support setter convert, caret must be in method");
            }
            context.putData(SetterDataKeys.CURRENT_METHOD, method);

            sourceModel = new JavaSourceClassModel(context, method);

            PsiType returnType = ((PsiLocalVariable)parentElement).getType();
            returnModel = new JavaReturnClassModel(context, ((PsiLocalVariable)parentElement).getName(), returnType);
            handler = new MethodVariableConvertHandler();
        }

        if (parentElement instanceof PsiMethod) {
            // 当前光标指向了方法体内的空对象
            PsiMethod psiMethod = (PsiMethod)parentElement;
            context.putData(SetterDataKeys.CURRENT_METHOD, parentElement);

            sourceModel = new JavaSourceClassModel(context, psiMethod);

            PsiType returnType = psiMethod.getReturnType();
            // 如果上述没获取成功,说明是构造函数,或者void函数,无返回值,类就设置为当前对象
            if (null == returnType || psiMethod.isConstructor()) {
                returnType = PsiTypesUtil.getClassType(Objects.requireNonNull(psiMethod.getContainingClass()));
                returnModel = new JavaReturnClassModel(context, JavaReturnClassModel.THIS, returnType);
            } else {
                returnModel = new JavaReturnClassModel(context, PsiMyUtils.generateVarName(returnType), returnType);
            }
            handler = new MethodConvertHandler();
        }

        if (null == sourceModel) {
            throw new SetterHelperException("can't found input parameter or return type");
        }

        // 开始转换
        sourceModel.buildGetMethod().buildFiled();
        returnModel.buildSetMethod().buildFiled().buildModeBuilder();
        context.putData(SetterDataKeys.SOURCE_MODEL, sourceModel);
        context.putData(SetterDataKeys.RETURN_MODEL, returnModel);

        return handler.handler(context);
    }

}
