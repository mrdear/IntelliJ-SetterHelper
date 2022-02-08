package cn.mrdear.setter.model;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiResolveHelper;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiTypesUtil;

import lombok.Getter;

import cn.mrdear.setter.utils.PsiMyUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 针对输入类型的class包装
 *
 * @author quding
 * @since 2022/2/7
 */
public class SourceClassModel {
    /**
     * 变量名
     */
    @Getter
    private String varName;
    /**
     * 包装的类型
     */
    private PsiType type;
    /**
     * 对应的class信息
     */
    @Getter
    private PsiClass psiClass;
    /**
     * 获取可以访问的字段对象
     * key字段名,用于匹配
     * value对应拼接值,可能是this.xxx,也可能是var.getxxx(), var.xxx()
     */
    @Getter
    private Map<String, String> canAccessGetFiled = new HashMap<>();
    /**
     * 获取可以设置值的字段
     * key字段名,用于匹配
     * value可能是 this.xxx = %s, 可能是var.setxxx(%s), var.xxx(%s)
     */
    @Getter
    private Map<String, String> canAccessSetFiled = new LinkedHashMap<>();

    public SourceClassModel(String varName, PsiType type) {
        this.type = type;
        this.psiClass = PsiTypesUtil.getPsiClass(type);
        this.varName = varName;
        // 没指定,则给定默认值
        if (null == this.varName && null != this.psiClass) {
            this.varName = PsiMyUtils.generateVarName(this.psiClass);
        }
    }

    public void initAccessFiled(Project project) {
        if (null == this.psiClass) {
            return;
        }
        //PsiResolveHelper resolveHelper = JavaPsiFacade.getInstance(project).getResolveHelper();
        PsiMethod[] methods = this.psiClass.getAllMethods();

        // 使用方法
        for (PsiMethod method : methods) {
            if (!PsiMyUtils.isValidMethod(method)) {
                continue;
            }

            String methodName = method.getName();
            PsiParameterList methodParameter = method.getParameterList();
            // 优先找get
            if (methodParameter.isEmpty()) {
                int getIndex = methodName.indexOf("get");
                if (getIndex == 0) {
                    // var.getxxx() 形式
                    canAccessGetFiled.putIfAbsent(methodName.substring(3).toUpperCase(),
                        this.varName + "." + methodName + "()");
                } else {
                    // var.xxx() 形式
                    canAccessGetFiled.putIfAbsent(methodName.toUpperCase(),
                        this.varName + "." + methodName + "()");
                }
                continue;
            }

            if (methodParameter.getParameters().length == 1) {
                // 找Set
                int setIndex = methodName.indexOf("set");
                if (setIndex == 0) {
                    // var.setxxx(%s)
                    canAccessSetFiled.putIfAbsent(methodName.substring(3).toUpperCase(),
                        this.varName + "." + methodName + "(%s)");
                } else {
                    // var.xxx(%s)
                    canAccessSetFiled.putIfAbsent(methodName.toUpperCase(),
                        this.varName + "." + methodName + "(%s)");
                }
            }
        }

        // 使用this字段
        PsiField[] fields = this.psiClass.getAllFields();
        for (PsiField field : fields) {
            String name = field.getName();
            if (PsiMyUtils.isValidField(field)) {
                canAccessSetFiled.putIfAbsent(name.toUpperCase(),
                    this.varName + "." + name +" = %s");

                canAccessGetFiled.putIfAbsent(name.toUpperCase(),
                    this.varName + "." + name);
            }
        }

    }

}
