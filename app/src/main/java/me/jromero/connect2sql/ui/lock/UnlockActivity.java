package me.jromero.connect2sql.ui.lock;

import android.os.Bundle;
import android.text.TextUtils;

import java.util.List;

import javax.inject.Inject;

import me.jromero.connect2sql.ApplicationUtils;
import me.jromero.connect2sql.data.LockManager;
import me.zhanghai.android.patternlock.ConfirmPatternActivity;
import me.zhanghai.android.patternlock.PatternUtils;
import me.zhanghai.android.patternlock.PatternView;

/**
 *
 */
public class UnlockActivity extends ConfirmPatternActivity {

    @Inject
    LockManager mLockManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ApplicationUtils.getApplication(this).getApplicationComponent().inject(this);
    }

    @Override
    public boolean isPatternCorrect(List<PatternView.Cell> pattern) {
        String passphrase = mLockManager.getPassphrase();
        if (passphrase != null) {
            return TextUtils.equals(PatternUtils.patternToSha1String(pattern), passphrase);
        } else {
            return false;
        }
    }

    @Override
    public void onCancel() {
        ApplicationUtils.backgroundApp(this);
    }

    @Override
    public void onForgotPassword() {
        mLockManager.startForgotLockActivity(this);
    }
}
