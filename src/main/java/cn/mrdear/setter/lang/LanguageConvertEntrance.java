package cn.mrdear.setter.lang;

import com.intellij.lang.Language;

import cn.mrdear.setter.model.InputConvertContext;
import cn.mrdear.setter.model.OutputConvertResult;

/**
 * 转换入口
 * @author quding
 * @since 2022/5/3
 */
public interface LanguageConvertEntrance {

    /**
     * 支持的预览
     * @return 语言类
     */
    Language support();

    /**
     * 转换执行方法
     * @param context 参数上下文
     * @return 转换结果
     */
    OutputConvertResult handlerConvert(InputConvertContext context);
}
