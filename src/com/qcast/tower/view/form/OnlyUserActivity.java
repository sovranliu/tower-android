package com.qcast.tower.view.form;

import android.content.Intent;
import android.os.Bundle;

import com.qcast.tower.business.Me;
import com.slfuture.pluto.view.component.ActivityEx;

/**
 * 只有用户可以访问的界面
 */
public class OnlyUserActivity extends ActivityEx {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(null != Me.instance) {
			return;
		}
		Intent intent = new Intent(OnlyUserActivity.this, LoginActivity.class);
		intent.putExtra("intent", this.getIntent());
		this.startActivity(intent);
		finish();
	}
}
