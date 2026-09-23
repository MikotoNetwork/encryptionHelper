package top.misaknetwork.encryption.helper.utils;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES / DES 加解密工具类
 *
 * 默认使用 PKCS5Padding 填充，密文以 Base64 字符串返回。
 * 密钥由用户输入的密码通过 SHA-256 派生得到：
 *   AES -> 取 SHA-256 前 16 字节（AES-128）
 *   DES -> 取 SHA-256 前 8 字节
 * CBC 模式的 IV 由 密码 + 固定盐 的 MD5 派生（演示用，生产环境应随机生成并随密文一起保存）。
 */
public final class CryptoUtils {

    public static final String ALGORITHM_AES = "AES";
    public static final String ALGORITHM_DES = "DES";

    public static final String MODE_CBC = "CBC";
    public static final String MODE_ECB = "ECB";

    private static final int AES_KEY_LENGTH = 16; // 128 bit
    private static final int DES_KEY_LENGTH = 8;  // 64 bit
    private static final String IV_SALT = "@crypto_tool_iv";

    private CryptoUtils() {
        // 工具类，禁止实例化
    }

    /* ==================== 对外接口 ==================== */

    /**
     * 加密
     *
     * @param plainText 明文
     * @param password  用户输入的密码
     * @param algorithm {@link #ALGORITHM_AES} 或 {@link #ALGORITHM_DES}
     * @param mode      {@link #MODE_CBC} 或 {@link #MODE_ECB}
     * @return Base64 编码的密文
     */
    public static String encrypt(String plainText, String password,
                                 String algorithm, String mode) throws Exception {
        Cipher cipher = Cipher.getInstance(buildTransformation(algorithm, mode));
        SecretKeySpec keySpec = new SecretKeySpec(
                deriveKey(password, getKeyLength(algorithm)), algorithm);

        if (MODE_CBC.equals(mode)) {
            cipher.init(Cipher.ENCRYPT_MODE, keySpec,
                    new IvParameterSpec(deriveIv(password, cipher.getBlockSize())));
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        }

        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeToString(encrypted, Base64.NO_WRAP);
    }

    /**
     * 解密
     *
     * @param cipherText Base64 编码的密文
     * @return 明文
     */
    public static String decrypt(String cipherText, String password,
                                 String algorithm, String mode) throws Exception {
        byte[] data = Base64.decode(cipherText.trim(), Base64.DEFAULT);

        Cipher cipher = Cipher.getInstance(buildTransformation(algorithm, mode));
        SecretKeySpec keySpec = new SecretKeySpec(
                deriveKey(password, getKeyLength(algorithm)), algorithm);

        if (MODE_CBC.equals(mode)) {
            cipher.init(Cipher.DECRYPT_MODE, keySpec,
                    new IvParameterSpec(deriveIv(password, cipher.getBlockSize())));
        } else {
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
        }

        byte[] decrypted = cipher.doFinal(data);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /* ==================== 内部实现 ==================== */

    private static String buildTransformation(String algorithm, String mode) {
        return algorithm + "/" + mode + "/PKCS5Padding";
    }

    private static int getKeyLength(String algorithm) {
        return ALGORITHM_DES.equals(algorithm) ? DES_KEY_LENGTH : AES_KEY_LENGTH;
    }

    /** 由密码派生固定长度密钥 */
    private static byte[] deriveKey(String password, int length) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        byte[] key = new byte[length];
        System.arraycopy(hash, 0, key, 0, length);
        return key;
    }

    /** 由密码派生 IV（AES 需要 16 字节，DES 需要 8 字节） */
    private static byte[] deriveIv(String password, int length) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("MD5"); // 固定输出 16 字节
        byte[] hash = digest.digest((password + IV_SALT).getBytes(StandardCharsets.UTF_8));
        byte[] iv = new byte[length];
        System.arraycopy(hash, 0, iv, 0, length);
        return iv;
    }
}