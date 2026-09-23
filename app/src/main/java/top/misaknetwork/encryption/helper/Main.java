package top.misaknetwork.encryption.helper;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import top.misaknetwork.encryption.helper.utils.*;
public class Main extends AppCompatActivity {

    private EditText etInput;
    private EditText etPassword;
    private RadioGroup rgAlgorithm;
    private RadioGroup rgMode;
    private TextView tvOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etInput = findViewById(R.id.etInput);
        etPassword = findViewById(R.id.etPassword);
        rgAlgorithm = findViewById(R.id.rgAlgorithm);
        rgMode = findViewById(R.id.rgMode);
        tvOutput = findViewById(R.id.tvOutput);

        findViewById(R.id.btnEncrypt).setOnClickListener(v -> handleCrypto(true));
        findViewById(R.id.btnDecrypt).setOnClickListener(v -> handleCrypto(false));
        findViewById(R.id.btnBase64Encode).setOnClickListener(v -> handleBase64(true));
        findViewById(R.id.btnBase64Decode).setOnClickListener(v -> handleBase64(false));
        findViewById(R.id.btnCopy).setOnClickListener(v -> copyResult());
    }

    /* ==================== 事件处理 ==================== */

    private void handleCrypto(boolean encrypt) {
        String input = etInput.getText().toString();
        if (TextUtils.isEmpty(input)) {
            toast("请先输入内容");
            return;
        }
        String password = etPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            toast("请输入密钥 / 密码");
            return;
        }

        String algorithm = selectedAlgorithm();
        String mode = selectedMode();

        try {
            String result = encrypt
                    ? CryptoUtils.encrypt(input, password, algorithm, mode)
                    : CryptoUtils.decrypt(input, password, algorithm, mode);
            showResult(result);
        } catch (Exception e) {
            showResult("处理失败：" + e.getClass().getSimpleName()
                    + "\n" + e.getMessage()
                    + "\n（请检查密钥是否正确、密文是否完整）");
        }
    }

    private void handleBase64(boolean encode) {
        String input = etInput.getText().toString();
        if (TextUtils.isEmpty(input)) {
            toast("请先输入内容");
            return;
        }
        try {
            showResult(encode ? Base64Utils.encode(input) : Base64Utils.decode(input));
        } catch (Exception e) {
            showResult("解码失败：不是合法的 Base64 字符串");
        }
    }

    private void copyResult() {
        String text = tvOutput.getText().toString();
        if (TextUtils.isEmpty(text)) {
            toast("暂无结果可复制");
            return;
        }
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            cm.setPrimaryClip(ClipData.newPlainText("crypto_result", text));
            toast("已复制到剪贴板");
        }
    }

    /* ==================== 辅助方法 ==================== */

    private String selectedAlgorithm() {
        return rgAlgorithm.getCheckedRadioButtonId() == R.id.rbDes
                ? CryptoUtils.ALGORITHM_DES
                : CryptoUtils.ALGORITHM_AES;
    }

    private String selectedMode() {
        return rgMode.getCheckedRadioButtonId() == R.id.rbEcb
                ? CryptoUtils.MODE_ECB
                : CryptoUtils.MODE_CBC;
    }

    private void showResult(String text) {
        tvOutput.setText(text);
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}