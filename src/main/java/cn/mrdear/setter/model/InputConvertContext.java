package cn.mrdear.setter.model;

import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;

import lombok.Getter;
import lombok.Setter;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * 转换过程中所需要的参数集合
 * @author quding
 * @since 2022/2/7
 */
public class InputConvertContext implements DataProvider {
    /**
     * 扩展字段属性
     */
    private Map<String, Object> cacheData = new HashMap<>();
    /**
     * 当前项目
     */
    private Project project;
    /**
     * 当前光标所在对象
     */
    @Getter
    private PsiElement psiCurrent;
    /**
     * 上一级对象
     */
    @Getter
    private PsiElement psiParent;
    /**
     * 原始类型
     */
    @Getter
    private SourceClassModel sourceType;
    /**
     * 目标类型
     */
    @Getter
    private ReturnClassModel returnType;

    public InputConvertContext(Project project, PsiElement psiCurrent, PsiElement psiParent) {
        this.project = project;
        this.psiCurrent = psiCurrent;
        this.psiParent = psiParent;
    }

    public void setReturnType(PsiType returnType) {
        setReturnType(null, returnType);
    }

    public void setReturnType(String varName, PsiType returnType) {
        this.returnType = new ReturnClassModel(varName, returnType);
        this.returnType.initAccessFiled(project, psiCurrent);
    }

    public void setSourceType(String varName, PsiType sourceType) {
        this.sourceType = new SourceClassModel(varName, sourceType);
        this.sourceType.initAccessFiled(project, psiCurrent);
    }

    /**
     * 判断当前转换是否可进行
     * @return true 可进行
     */
    public boolean canConvert() {
        return true;
    }

    @Override
    public @Nullable Object getData(@NotNull @NonNls String s) {
        return cacheData.get(s);
    }

    /**
     * 存入上下文值
     */
    public void putData(DataKey<?> key, Object obj) {
        this.cacheData.put(key.getName(), obj);
    }
}
