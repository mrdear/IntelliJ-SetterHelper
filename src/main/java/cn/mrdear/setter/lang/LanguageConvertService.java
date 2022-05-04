package cn.mrdear.setter.lang;

import com.intellij.lang.Language;
import com.intellij.openapi.components.Service;
import com.intellij.psi.PsiFile;

import org.jetbrains.annotations.NotNull;

import cn.mrdear.setter.lang.java.JavaLanguageConvertEntrance;
import cn.mrdear.setter.lang.kotlin.KotlinLanguageConvertEntrance;
import cn.mrdear.setter.model.InputConvertContext;
import cn.mrdear.setter.model.OutputConvertResult;

import java.util.HashMap;
import java.util.Map;

/**
 * @author quding
 * @since 2022/5/3
 */
@Service
public final class LanguageConvertService {

    public static Map<String, LanguageConvertEntrance> CACHE_INSTANCE = new HashMap<>();

    static {
        // Java 转换实现
        JavaLanguageConvertEntrance javaInstance = new JavaLanguageConvertEntrance();
        CACHE_INSTANCE.put(javaInstance.support().getID(), javaInstance);

        // kotlin实现
        KotlinLanguageConvertEntrance kotlinInstance = new KotlinLanguageConvertEntrance();
        CACHE_INSTANCE.put(kotlinInstance.support().getID(), kotlinInstance);
    }

    /**
     * 查询是否支持当前语言
     * @param language 文件语言标识
     * @return true 支持
     */
    public boolean support(Language language) {
        return CACHE_INSTANCE.containsKey(language.getID());
    }

    /**
     * 执行转换的方法
     * @param context 参数上下文信息
     * @return 转换结果,有问题,抛出异常即可
     */
    @NotNull
    public OutputConvertResult handlerConvert(InputConvertContext context) {
        PsiFile file = context.getPsiFile();
        LanguageConvertEntrance convertEntrance = CACHE_INSTANCE.get(file.getLanguage().getID());
        return convertEntrance.handlerConvert(context);
    }

}
