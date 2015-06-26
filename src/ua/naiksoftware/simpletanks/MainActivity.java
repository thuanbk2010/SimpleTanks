package ua.naiksoftware.simpletanks;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.app.*;
import android.content.*;
import android.view.*;

public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.getName();

	private GameMode gameMode;
	private GameServer gameConn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		((Button) findViewById(R.id.btnPlay)).setOnClickListener(btnListener);
	}

	View.OnClickListener btnListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnPlay:
				new AlertDialog.Builder(MainActivity.this)
						.setItems(R.array.online_modes, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface di, int pos) {
								if (pos == 0) { // Start server
									gameMode = GameMode.SERVER;
									gameConn = new GameServer(MainActivity.this);
									gameConn.start();
								} else if (pos == 1) { // Connect to server
									gameMode = GameMode.CLIENT;
								}
							}
						}).show();
				break;
			}
		}
	};
	
	@Override
	public void onDestroy() {
		if (gameConn != null) {
			gameConn.stop();
		}
		super.onDestroy();
	}
}
