package cn.mrdear.setter.utils;

import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * 修饰PSI相关类的工具包
 * @author quding
 * @since 2022/2/7
 */
public class PsiMyUtils {

    private static Set<String> IGNORE_METHOD = Set.of("clone","wait","object","equals",
        "toString","notify","notifyAll","registerNatives","class","finalize","hashCode","canEqual","getClass");

    /**
     * 判断是否为有效方法
     * @param psiMethod 有效方法
     * @return 判断结果
     */
    public static boolean isValidMethod(PsiMethod psiMethod) {
        if (null == psiMethod) {
            return false;
        }
        String name = psiMethod.getName();
        if (IGNORE_METHOD.contains(name)) {
            return false;
        }
        return !(psiMethod.hasModifierProperty(PsiModifier.STATIC)
            || psiMethod.hasModifierProperty(PsiModifier.ABSTRACT) || psiMethod.isConstructor());
    }

    /**
     * 同上
     */
    public static boolean isValidField(PsiField psiField) {
        if (null == psiField) {
            return false;
        }
        String name = psiField.getName();
        if (IGNORE_METHOD.contains(name)) {
            return false;
        }

        return !(psiField.hasModifierProperty(PsiModifier.STATIC));
    }



    /**
     * 根据编辑器以及文件,获取对应光标指向位置
     * @param editor 编辑器
     * @param file 文件
     * @return 光标指向元素
     */
    public static PsiElement findCaretElement(@NotNull Editor editor, @NotNull PsiFile file) {
        CaretModel caretModel = editor.getCaretModel();
        int position = caretModel.getOffset();
        return file.findElementAt(position);
    }

    /**
     * 根据class获取局部变量命名
     * @param psiClass 类型
     * @return 命名字段
     */
    public static String generateVarName(PsiClass psiClass) {
        if (null == psiClass) {
            return "temp";
        }
        String className = psiClass.getName();
        if (null == className) {
            return "temp";
        }
        return String.valueOf(className.charAt(0)).toLowerCase().concat(className.substring(1));
    }

    /**
     * 判断给定类是否为Java自带的对象
     * @param psiClass 类型
     * @return true 是JDK内部对象
     */
    public static boolean isJavaClass(PsiClass psiClass) {
        if (null == psiClass) {
            return false;
        }
        String name = psiClass.getQualifiedName();
        if (null == name) {
            return false;
        }
        return name.startsWith("java");
    }

}
