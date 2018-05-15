package com.example.gameofthelife;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Activity_Rule extends ActionBarActivity {
	static private Button bt_rule;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rule);
		bt_rule = (Button) findViewById(R.id.bt_goto_MainActivity);
		bt_rule.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Activity_Rule.this,
						MainActivity.class);
				startActivityForResult(intent, 0);
				finish();
			}
		});
	}
}
