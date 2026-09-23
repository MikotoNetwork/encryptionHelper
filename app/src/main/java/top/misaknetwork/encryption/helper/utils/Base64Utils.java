package top.misaknetwork.encryption.helper.utils;

import android.util.Base64;

import java.nio.charset.StandardCharsets;

/**
 * Base64 编解码工具类（基于 android.util.Base64）
 */
public final class Base64Utils {

    private Base64Utils() {
        // 工具类，禁止实例化
    }

    /** 编码：字符串 -> Base64 */
    public static String encode(String plainText) {
        return Base64.encodeToString(
                plainText.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
    }

    /** 解码：Base64 -> 字符串，兼容 URL_SAFE 与缺失填充的情况 */
    public static String decode(String base64Text) {
        // 去掉换行、空格等空白字符
        String text = base64Text.replaceAll("\\s", "");

        byte[] data;
        try {
            data = Base64.decode(text, Base64.DEFAULT);
        } catch (IllegalArgumentException e) {
            data = Base64.decode(text, Base64.URL_SAFE | Base64.NO_PADDING);
        }
        return new String(data, StandardCharsets.UTF_8);
    }
}