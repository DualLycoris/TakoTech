package moe.takochan.takotech.common;

import moe.takochan.takotech.Tags;

/**
 * 该类包含整个模组中使用的常量引用。
 */
public class Reference {

    /**
     * 模组版本。
     */
    public static final String VERSION = Tags.VERSION;

    /**
     * 模组的唯一标识符（Mod ID）。
     */
    public static final String MODID = "TakoTech";

    /**
     * 模组名称。
     */
    public static final String MODNAME = "TakoTech";

    /**
     * 模组的组名。
     */
    public static final String GROUPNAME = "moe.takochan.takotech";

    /**
     * GUI 工厂类的全限定名。 用于初始化模组的图形用户界面配置。
     */
    public static final String GUI_FACTORY = "moe.takochan.takotech.client.gui.config.TakoTechGuiConfigFactory";

    /**
     * 资源根标识符。
     */
    public static final String RESOURCE_ROOT_ID = MODID.toLowerCase();

    public static final String NEI_MOD_ID = "NotEnoughItems";

    /**
     * GTNH 2.9.0 的 Forge 依赖和加载顺序。IC2 下限采用其 {@code @Mod} 报告的版本。
     * gregtech_nh 标识 GTNH 分支，gregtech 声明主 Mod 的加载顺序。
     */
    public static final String DEPENDENCIES = "required-after:gtnhlib@[0.11.54,);"
        + "required-after:unimixins@[0.3.1,);"
        + "required-after:appliedenergistics2@[rv3-beta-1095-GTNH,);"
        + "required-after:IC2@[2.2.827-experimental,);"
        + "required-after:gregtech@[5.09.54.265,);"
        + "required-after:gregtech_nh;"
        + "after:Waila;"
        + "after:"
        + NEI_MOD_ID;
}
