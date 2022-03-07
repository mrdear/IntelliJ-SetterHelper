package cn.mrdear.setter.model;

import com.intellij.openapi.project.Project;
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

import org.apache.commons.lang3.ArrayUtils;

import cn.mrdear.setter.utils.PsiMyUtils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 针对输入类型的class包装
 *
 * @author quding
 * @since 2022/2/7
 */
public class ReturnClassModel {
    /**
     * 变量名
     */
    @Getter
    private String varName;
    /**
     * 包装的类型
     */
    @Getter
    private PsiType type;
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
    private Map<String, String> canAccessSetFiled = new LinkedHashMap<>();

    public ReturnClassModel(String varName, PsiType type) {
        this.type = type;
        this.psiClass = PsiTypesUtil.getPsiClass(type);
        this.varName = varName;
        // 没指定,则给定默认值
        if (null == this.varName && null != this.psiClass) {
            this.varName = PsiMyUtils.generateVarName(this.psiClass);
        }
    }

    /**
     * 判断当前是否可以采用builder模式
     */
    public boolean canBuilder() {
        if (this.varName.equalsIgnoreCase("this")) {
            return false;
        }
        return this.psiClass.hasAnnotation("lombok.Builder");
    }

    /**
     * 判断当前是否是this
     */
    public boolean isThis() {
        return this.varName.equals("this");
    }

    public void initAccessFiled(Project project, PsiElement psiCurrent) {
        if (null == this.psiClass) {
            return;
        }

        PsiResolveHelper resolveHelper = JavaPsiFacade.getInstance(project).getResolveHelper();
        PsiMethod[] methods = this.psiClass.getAllMethods();

        // 判断是否为lombok builder模式,追加builder对应class的set方法
        if (canBuilder()) {
            PsiClass innerClassByName = this.psiClass.findInnerClassByName(this.psiClass.getName() + "Builder", false);
            if (null != innerClassByName) {
                methods = ArrayUtils.addAll(methods, Arrays.stream(innerClassByName.getMethods()).filter(PsiMethod::hasParameters).toArray(PsiMethod[]::new));
            }
        }

        // 使用方法
        for (PsiMethod method : methods) {
            if (!PsiMyUtils.isValidMethod(method)
                || !resolveHelper.isAccessible(method, psiCurrent, this.psiClass)) {
                continue;
            }

            String methodName = method.getName();
            PsiParameterList methodParameter = method.getParameterList();

            if (methodParameter.getParameters().length == 1) {
                // 找Set
                int setIndex = methodName.indexOf("set");
                if (setIndex == 0) {
                    // var.setxxx(%s)
                    canAccessSetFiled.putIfAbsent(methodName.substring(3).toUpperCase(),
                        this.varName + "." + methodName + "(%s)");
                } else if (canBuilder()) {
                    // var.xxx(%s)
                    canAccessSetFiled.putIfAbsent(methodName.toUpperCase(),
                        "." + methodName + "(%s)");
                }
            }
        }

        // 使用this字段
        PsiField[] fields = this.psiClass.getAllFields();
        for (PsiField field : fields) {
            String name = field.getName();
            if (PsiMyUtils.isValidField(field)
                && resolveHelper.isAccessible(field, psiCurrent, this.psiClass) ) {
                canAccessSetFiled.putIfAbsent(name.toUpperCase(),
                    this.varName + "." + name +" = %s");
            }
        }

    }

}
