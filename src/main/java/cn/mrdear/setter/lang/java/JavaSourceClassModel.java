package cn.mrdear.setter.lang.java;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiResolveHelper;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.JvmPsiConversionHelperImpl;
import com.intellij.psi.util.PsiClassUtil;
import com.intellij.psi.util.PsiTypesUtil;

import lombok.Getter;

import org.apache.commons.lang3.StringUtils;

import cn.mrdear.setter.model.InputConvertContext;
import cn.mrdear.setter.utils.PsiMyUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 针对输入类型的class包装
 *
 * @author quding
 * @since 2022/2/7
 */
class JavaSourceClassModel {

    private InputConvertContext context;

    /**
     * 存放可以作为参数的入参类型
     */
    private List<Pair<PsiType, String>> parameterList = new LinkedList<>();

    /**
     * 获取可以访问的字段对象
     * key字段名,用于匹配
     * value对应拼接值,可能是this.xxx,也可能是var.getxxx(), var.xxx()
     */
    @Getter
    private Map<String, List<Pair<PsiClass, String>>> canAccessGetFiled = new HashMap<>();

    public JavaSourceClassModel(InputConvertContext context, PsiMethod method) {
        this.context = context;

        PsiParameterList parameterList = method.getParameterList();
        if (!parameterList.isEmpty()) {
            for (PsiParameter parameter : parameterList.getParameters()) {
                this.parameterList.add(Pair.create(parameter.getType(), parameter.getName()));
            }
        }
    }

    /**
     * 寻找get方法
     */
    public JavaSourceClassModel buildGetMethod() {
        PsiResolveHelper resolveHelper = JavaPsiFacade.getInstance(context.getProject()).getResolveHelper();
        PsiElement psiCurrent = context.getPsiCurrent();

        for (Pair<PsiType, String> pair : this.parameterList) {
            PsiClass psiClass = PsiTypesUtil.getPsiClass(pair.first);
            if (null == psiClass) {
                continue;
            }

            // 代表是Jvm自带的类,这种直接放入即可
            if (PsiMyUtils.isJavaClass(psiClass)) {
                List<Pair<PsiClass, String>> list = this.canAccessGetFiled.computeIfAbsent(pair.second.toUpperCase(), k -> new LinkedList<>());
                list.add(Pair.create(psiClass, pair.second));
                continue;
            }

            // 代表是用户定义的类,或者外部参数
            PsiMethod[] methods = psiClass.getAllMethods();
            for (PsiMethod method : methods) {
                if (!PsiMyUtils.isValidMethod(method) || !resolveHelper.isAccessible(method, psiCurrent, psiClass)) {
                    continue;
                }
                String methodName = method.getName();
                PsiType returnType = method.getReturnType();
                PsiClass returnClass = PsiTypesUtil.getPsiClass(returnType);

                PsiParameterList methodParameter = method.getParameterList();
                // 优先找get
                if (methodParameter.isEmpty()) {
                    int getIndex = methodName.indexOf("get");
                    if (getIndex == 0) {
                        // var.getxxx() 形式
                        List<Pair<PsiClass, String>> list = canAccessGetFiled.computeIfAbsent(methodName.substring(3).toUpperCase(), k -> new LinkedList<>());
                        list.add(Pair.create(returnClass, pair.second + "." + methodName + "()"));
                    } else {
                        // var.xxx() 形式
                        List<Pair<PsiClass, String>> list = canAccessGetFiled.computeIfAbsent(methodName.toUpperCase(), k -> new LinkedList<>());
                        list.add(Pair.create(returnClass, pair.second + "." + methodName + "()"));
                    }
                }
            }
        }

        return this;
    }

    /**
     * 构建字段
     */
    public JavaSourceClassModel buildFiled() {
        PsiResolveHelper resolveHelper = JavaPsiFacade.getInstance(context.getProject()).getResolveHelper();
        PsiElement psiCurrent = context.getPsiCurrent();

        for (Pair<PsiType, String> pair : this.parameterList) {
            PsiClass psiClass = PsiTypesUtil.getPsiClass(pair.first);
            if (null == psiClass) {
                continue;
            }
            // 使用this字段
            PsiField[] fields = psiClass.getAllFields();
            for (PsiField field : fields) {
                String name = field.getName();
                if (PsiMyUtils.isValidField(field)
                    && resolveHelper.isAccessible(field, psiCurrent, psiClass) ) {

                    List<Pair<PsiClass, String>> list = canAccessGetFiled.computeIfAbsent(name.toUpperCase(), k -> new LinkedList<>());
                    list.add(Pair.create(psiClass, pair.second + "." + name));
                }
            }
        }
        return this;
    }

}
