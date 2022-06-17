package cn.mrdear.setter.action;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.CodeInsightAction;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.JavaCodeFragmentFactory;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpressionCodeFragment;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLocalVariable;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.EditorTextField;

import org.jetbrains.annotations.NotNull;

import cn.mrdear.setter.lang.LanguageConvertService;
import cn.mrdear.setter.model.InputConvertContext;
import cn.mrdear.setter.model.OutputConvertResult;
import cn.mrdear.setter.model.SetterHelperException;
import cn.mrdear.setter.utils.LogUtils;
import cn.mrdear.setter.utils.PsiMyUtils;

/**
 * 目前是所有转换的入口处,程序自动判断当前所处于的环境,选择最优方式生成
 * @author quding
 * @since 2022/2/7
 */
public class SetterConvertGenerateActionGroup extends CodeInsightAction {

    @Override
    protected boolean isValidForFile(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        LanguageConvertService service = project.getService(LanguageConvertService.class);
        return service.support(file.getLanguage());
    }

    /**
     * 补全执行入口
     * 入口的功能是尽可能的收集当前环境信息,不解析具体设计到语言级别的数据
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
                JBPopupFactory.getInstance().createMessage("No element found for setter helper")
                    .showInBestPositionFor(editor);
                return;
            }

            try {
                // 定义转换依赖的上下文信息
                InputConvertContext context = new InputConvertContext(project, psiFile, element, psiParent);

                // 执行转换
                LanguageConvertService languageConvertService = project.getService(LanguageConvertService.class);
                OutputConvertResult result = languageConvertService.handlerConvert(context);

                // 结果写回
                int curLine = editor.getCaretModel().getLogicalPosition().line;
                PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
                Document document = psiDocumentManager.getDocument(psiFile);
                if (null == document) {
                    throw new RuntimeException("can't find document");
                }
                int lineEndOffset = document.getLineEndOffset(curLine);
                document.insertString(lineEndOffset, result.getInsertText());

                psiDocumentManager.doPostponedOperationsAndUnblockDocument(document);
                psiDocumentManager.commitDocument(document);

                // 格式化代码块
                CodeStyleManager.getInstance(project)
                    .reformatRange(psiFile, lineEndOffset, lineEndOffset + result.getInsertText().length());
            } catch (Exception e) {
                String message = "No element found for setter helper";
                if (e instanceof SetterHelperException) {
                    message = e.getMessage();
                }
                JBPopupFactory.getInstance().createMessage(message)
                    .showInBestPositionFor(editor);
            }
        };
    }

}
