package my.app.location;

import my.app.location.utils.Location;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

	private EditText etInput;
	private TextView tvOutput;
	private Button bSearch;

	private final int REQUEST_CODE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		initView();
	}

	private void initView() {
		etInput = (EditText) findViewById(R.id.etInput);
		tvOutput = (TextView) findViewById(R.id.tvOutput);
		bSearch = (Button) findViewById(R.id.bSearch);
		setListener();
	}

	private void setListener() {
		bSearch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String input = etInput.getText().toString();
				Intent intent = new Intent(MainActivity.this, MapActivity.class);
				intent.putExtra("input", input);
				startActivityForResult(intent, REQUEST_CODE);
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case (REQUEST_CODE): {
			if (resultCode == Activity.RESULT_OK) {
				String result = data.getStringExtra(MapActivity.RESULT);
				tvOutput.setText(result);
			}
			break;
		}
		}
	}

}
