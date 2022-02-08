package cn.mrdear.setter.action;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.CodeInsightAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLocalVariable;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiType;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiTypesUtil;

import org.jetbrains.annotations.NotNull;

import cn.mrdear.setter.handler.MethodConvertHandler;
import cn.mrdear.setter.model.InputConvertContext;
import cn.mrdear.setter.model.Mode;
import cn.mrdear.setter.model.OutputConvertResult;
import cn.mrdear.setter.utils.LogUtils;
import cn.mrdear.setter.utils.PsiMyUtils;

import java.util.Objects;

/**
 * 目前是所有转换的入口处,程序自动判断当前所处于的环境,选择最优方式生成
 * @author quding
 * @since 2022/2/7
 */
public class SetterConvertGenerateActionGroup extends CodeInsightAction {

    private static Logger LOGGER = Logger.getInstance(SetterConvertGenerateActionGroup.class);

    @Override
    protected boolean isValidForFile(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        // 目前只针对Java补全
        return file.getLanguage().getID().equalsIgnoreCase("JAVA");
    }

    /**
     * 补全执行函数
     */
    @Override
    protected @NotNull CodeInsightActionHandler getHandler() {
        return (project, editor, psiFile) -> {
            // 获取光标所在位置元素
            PsiElement element = PsiMyUtils.findCaretElement(editor, psiFile);
            PsiElement psiParent = PsiTreeUtil.getParentOfType(element,
                PsiLocalVariable.class, PsiMethod.class, PsiClass.class);
            LogUtils.debug("psiElement=%s, psiParent=%s", element, psiParent);
            if (psiParent == null) {
                return;
            }
            // 定义转换依赖的上下问信息
            InputConvertContext context = new InputConvertContext(project, element, psiParent);

            Mode mode = Mode.NULL;

            // 整个的生成基于当前光标所在元素以及父元素定位,分为三种情况
            // 父元素为PsiClass,无视当前元素,表名在方法体外部生成
            if (psiParent instanceof PsiClass) {

            }

            // 父元素为PsiLocalVariable,当前元素为任意输入,表名在方法体内部,且指定了变量
            if (psiParent instanceof PsiLocalVariable) {

            }

            // 父元素为PsiMethod,当前元素不为PsiLocalVariable,表名在方法体内部,且未指定了变量
            if (psiParent instanceof PsiMethod) {
                PsiMethod psiMethod = (PsiMethod)psiParent;

                mode = Mode.METHOD; // 更改为方法处理模式
                PsiType returnType = psiMethod.getReturnType();
                context.setReturnType(returnType); // 获取返回类型

                // 如果上述没获取成功,说明是构造函数,无返回值
                if (null == returnType && psiMethod.isConstructor()) {
                    returnType = PsiTypesUtil.getClassType(Objects.requireNonNull(psiMethod.getContainingClass()));
                    context.setReturnType("this", returnType);
                }

                PsiParameterList list = psiMethod.getParameterList();
                for (PsiParameter parameter : list.getParameters()) {
                    PsiType type = parameter.getType();
                    PsiClass parameterClass = PsiTypesUtil.getPsiClass(type);
                    // 过滤Java自带的属性类
                    if (PsiMyUtils.isJavaClass(parameterClass)) {
                        continue;
                    }
                    context.setSourceType(parameter.getName(), type); // 设置为第一个不属于Java自带的类
                    break;
                }
            }

            // 开始转换
            if (!context.canConvert()) {
                LogUtils.debug("can't convert");
                return;
            }

            MethodConvertHandler handler = null;
            switch (mode) {
                case METHOD:
                    handler = new MethodConvertHandler();
                    break;
                default:
                    return;
            }

            OutputConvertResult result = handler.handler(context);

            // 结果写回
            int curLine = editor.getCaretModel().getLogicalPosition().line;
            writeDocument(project, psiFile, curLine, result.getInsertText());
        };
    }


    /**
     * 写入文档
     * @param writeCnt 写入内容
     */
    protected static void writeDocument(Project project, PsiFile psiFile,Integer curLine, String writeCnt) {
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        Document document = psiDocumentManager.getDocument(psiFile);
        if (null == document) {
            throw new RuntimeException("can't find document");
        }
        int lineEndOffset = document.getLineEndOffset(curLine);
        document.insertString(lineEndOffset, writeCnt);

        psiDocumentManager.doPostponedOperationsAndUnblockDocument(document);
        psiDocumentManager.commitDocument(document);

        CodeStyleManager.getInstance(project)
            .reformatRange(psiFile, lineEndOffset, lineEndOffset + writeCnt.length());
    }

}
