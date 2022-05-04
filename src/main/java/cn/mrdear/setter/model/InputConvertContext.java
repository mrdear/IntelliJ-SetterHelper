package cn.mrdear.setter.model;

import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import lombok.Getter;

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
    @Getter
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

    @Getter
    private PsiFile psiFile;

    public InputConvertContext(Project project, PsiFile psiFile, PsiElement psiCurrent, PsiElement psiParent) {
        this.project = project;
        this.psiFile = psiFile;
        this.psiCurrent = psiCurrent;
        this.psiParent = psiParent;
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
