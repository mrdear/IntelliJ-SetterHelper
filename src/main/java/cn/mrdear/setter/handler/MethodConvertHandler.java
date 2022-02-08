package cn.mrdear.setter.handler;

import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.codeStyle.CodeStyleManager;

import cn.mrdear.setter.model.InputConvertContext;
import cn.mrdear.setter.model.Mode;
import cn.mrdear.setter.model.OutputConvertResult;
import cn.mrdear.setter.model.SourceClassModel;

import java.util.Map;

/**
 * 处理方法内生成
 * @author quding
 * @since 2022/2/7
 */
public class MethodConvertHandler implements ConvertHandler {

    @Override
    public Mode support() {
        return Mode.METHOD;
    }

    @Override
    public OutputConvertResult handler(InputConvertContext context) {
        OutputConvertResult result = new OutputConvertResult();

        SourceClassModel returnType = context.getReturnType();
        SourceClassModel sourceType = context.getSourceType();
        // 添加首行new对象
        result.appendInsert(returnType.getPsiClass().getName()).append(" ").append(returnType.getVarName())
            .append(" = new ").append(returnType.getPsiClass().getName()).append("();");

        // 添加set转换
        Map<String, String> getFiledMap = sourceType.getCanAccessGetFiled();
        returnType.getCanAccessSetFiled().forEach((k,v) -> {
            String getMethod = getFiledMap.getOrDefault(k, "");
            result.appendInsert(String.format(v, getMethod))
                .append(";");
        });

        // 添加return
        result.appendInsert("return ").append(returnType.getVarName()).append(";");

        return result;
    }


}
