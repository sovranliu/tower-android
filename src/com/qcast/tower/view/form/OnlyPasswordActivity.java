package com.qcast.tower.view.form;

import android.content.Intent;
import android.os.Bundle;

/**
 * 只有验证安全密码可以访问的界面
 */
public class OnlyPasswordActivity extends OnlyUserActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		switch(this.getIntent().getIntExtra("password", -1)) {
		case -1:
			finish();
			break;
		case 0:
			Intent intent = new Intent(OnlyPasswordActivity.this, PasswordActivity.class);
			intent.putExtra("mode", PasswordActivity.MODE_VERIFY);
			intent.putExtra("intent", this.getIntent());
			this.startActivity(intent);
			finish();
			break;
		case 1:
			break;
		}
	}
}
