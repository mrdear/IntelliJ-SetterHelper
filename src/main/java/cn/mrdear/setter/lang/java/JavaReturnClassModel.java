package cn.mrdear.setter.lang.java;

import com.intellij.openapi.util.Pair;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiResolveHelper;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiTypesUtil;

import lombok.Getter;

import cn.mrdear.setter.model.InputConvertContext;
import cn.mrdear.setter.utils.PsiMyUtils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 针对输入类型的class包装
 *
 * @author quding
 * @since 2022/2/7
 */
class JavaReturnClassModel {

    static final String THIS = "this";

    private InputConvertContext context;
    /**
     * 变量名
     */
    @Getter
    private String varName;
    /**
     * 包装的类型
     */
    @Getter
    private PsiType psiType;
    /**
     * 对应的class信息
     */
    @Getter
    private PsiClass psiClass;
    /**
     * 获取可以设置值的字段
     * key字段名,用于匹配
     * value可能是 this.xxx = %s, 可能是var.setxxx(%s), var.xxx(%s)
     */
    @Getter
    private Map<String, Pair<PsiClass,String>> canAccessSetFiled = new LinkedHashMap<>();

    public JavaReturnClassModel(InputConvertContext context, String varName, PsiType psiType) {
        this.psiType = psiType;
        this.context = context;
        this.psiClass = PsiTypesUtil.getPsiClass(psiType);
        this.varName = varName;
    }

    /**
     * 判断当前是否可以采用builder模式
     */
    public boolean canBuilder() {
        if (this.varName.equalsIgnoreCase(THIS)) {
            return false;
        }
        return this.psiClass.hasAnnotation("lombok.Builder");
    }

    /**
     * 判断当前是否是this
     */
    public boolean isThis() {
        return this.varName.equals(THIS);
    }

    public JavaReturnClassModel buildSetMethod() {
        PsiResolveHelper resolveHelper = JavaPsiFacade.getInstance(context.getProject()).getResolveHelper();
        PsiElement psiCurrent = context.getPsiCurrent();

        // build模式下,不走set方法,这里实际上有取舍,build模式下构造方式与当前不同
        if (canBuilder()) {
            return this;
        }

        PsiMethod[] methods = this.psiClass.getAllMethods();
        for (PsiMethod method : methods) {
            if (!PsiMyUtils.isValidMethod(method) || !resolveHelper.isAccessible(method, psiCurrent, this.psiClass)) {
                continue;
            }
            String methodName = method.getName();
            PsiParameterList methodParameter = method.getParameterList();
            if (methodParameter.getParameters().length == 1) {
                PsiClass tmpClass = PsiTypesUtil.getPsiClass(methodParameter.getParameter(0).getType());
                // 找Set
                int setIndex = methodName.indexOf("set");
                if (setIndex == 0) {
                    // var.setxxx(%s)
                    canAccessSetFiled.putIfAbsent(methodName.substring(3).toUpperCase(),
                        Pair.create(tmpClass, this.varName + "." + methodName + "(%s)"));
                } else {
                    // var.xxx(%s)
                    canAccessSetFiled.putIfAbsent(methodName.toUpperCase(), Pair.create(tmpClass, this.varName + "." + methodName + "(%s)"));
                }
            }
        }
        return this;
    }

    /**
     * filed模式支持
     */
    public JavaReturnClassModel buildFiled() {
        // build模式下,不走filed方法,这里实际上有取舍,build模式下构造方式与当前不同
        if (canBuilder()) {
            return this;
        }

        PsiResolveHelper resolveHelper = JavaPsiFacade.getInstance(context.getProject()).getResolveHelper();
        PsiElement psiCurrent = context.getPsiCurrent();

        // 使用this字段
        PsiField[] fields = this.psiClass.getAllFields();
        for (PsiField field : fields) {
            String name = field.getName();
            if (PsiMyUtils.isValidField(field) && resolveHelper.isAccessible(field, psiCurrent, this.psiClass) ) {
                canAccessSetFiled.putIfAbsent(name.toUpperCase(), Pair.create(PsiTypesUtil.getPsiClass(field.getType()), this.varName + "." + name +" = %s"));
            }
        }
        return this;
    }

    /**
     * builder模式支持
     */
    public JavaReturnClassModel buildModeBuilder() {
        if (!canBuilder()) {
            return this;
        }
        List<PsiMethod> methods = new LinkedList<>();
        // 判断是否为lombok builder模式,追加builder对应class的set方法
        PsiClass innerClassByName = this.psiClass.findInnerClassByName(this.psiClass.getName() + "Builder", false);
        if (null != innerClassByName) {
            Arrays.stream(innerClassByName.getAllMethods()).filter(PsiMethod::hasParameters).forEach(methods::add);
        }

        PsiResolveHelper resolveHelper = JavaPsiFacade.getInstance(context.getProject()).getResolveHelper();
        PsiElement psiCurrent = context.getPsiCurrent();

        for (PsiMethod method : methods) {
            if (!PsiMyUtils.isValidMethod(method) || !resolveHelper.isAccessible(method, psiCurrent, innerClassByName)) {
                continue;
            }
            String methodName = method.getName();
            PsiClass tmpClass = PsiTypesUtil.getPsiClass(method.getReturnType());
            if (!innerClassByName.equals(tmpClass)) {
                continue;
            }
            PsiParameterList methodParameter = method.getParameterList();
            if (methodParameter.getParameters().length == 1) {
                tmpClass = PsiTypesUtil.getPsiClass(methodParameter.getParameter(0).getType());
                // var.xxx(%s)
                canAccessSetFiled.putIfAbsent(methodName.toUpperCase(), Pair.create(tmpClass,  "." + methodName + "(%s)"));
            }
        }
        return this;
    }

}
