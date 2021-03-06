package com.qcast.tower.view.form;

import com.qcast.tower.Program;
import com.qcast.tower.R;
import com.slfuture.pluto.etc.Version;
import com.slfuture.pluto.view.annotation.ResourceView;
import com.slfuture.pluto.view.component.ActivityEx;
import com.slfuture.pretty.general.view.form.EnvironmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 关于界面
 */
@ResourceView(id = R.layout.activity_about)
public class AboutActivity extends ActivityEx {
	@ResourceView(id = R.id.about_image_close)
	public ImageView imgClose;
	@ResourceView(id = R.id.about_label_version)
	public TextView labVersion;
	
	
	/**
	 * 界面创建
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 界面处理
		prepare();
	}

	/**
	 * 界面预处理
	 */
	public void prepare() {
		imgClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AboutActivity.this.finish();
			}
		});
		labVersion.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				Intent intent = new Intent(AboutActivity.this, EnvironmentActivity.class);
				AboutActivity.this.startActivity(intent);
				return true;
			}
		});
		labVersion.setText("当前版本：" + Version.fetchVersion(Program.application).toString());
	}
}
