package cn.mrdear.setter.model;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;

import lombok.Getter;

/**
 * 转换过程中所需要的参数集合
 * @author quding
 * @since 2022/2/7
 */
public class InputConvertContext {
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
    private SourceClassModel returnType;

    public InputConvertContext(Project project, PsiElement psiCurrent, PsiElement psiParent) {
        this.project = project;
        this.psiCurrent = psiCurrent;
        this.psiParent = psiParent;
    }

    public void setReturnType(PsiType returnType) {
        setReturnType(null, returnType);
    }

    public void setReturnType(String varName, PsiType returnType) {
        this.returnType = new SourceClassModel(varName, returnType);
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


}
