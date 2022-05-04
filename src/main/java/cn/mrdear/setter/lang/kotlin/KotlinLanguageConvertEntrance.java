package cn.mrdear.setter.lang.kotlin;

import com.intellij.lang.Language;

import cn.mrdear.setter.lang.LanguageConvertEntrance;
import cn.mrdear.setter.model.InputConvertContext;
import cn.mrdear.setter.model.OutputConvertResult;
import cn.mrdear.setter.model.SetterHelperException;

/**
 * @author quding
 * @since 2022/5/3
 */
public class KotlinLanguageConvertEntrance implements LanguageConvertEntrance {

    @Override
    public Language support() {
        return Language.findLanguageByID("kotlin");
    }

    @Override
    public OutputConvertResult handlerConvert(InputConvertContext context) {
        throw new SetterHelperException("not support");
    }

}
