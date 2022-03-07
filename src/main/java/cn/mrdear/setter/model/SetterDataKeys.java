package cn.mrdear.setter.model;

import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.psi.PsiMethod;

/**
 * @author quding
 * @since 2022/3/7
 */
public final class SetterDataKeys {

    /**
     * 当前方法
     */
    public static final DataKey<PsiMethod> CURRENT_METHOD = DataKey.create("CURRENT_METHOD");


}
