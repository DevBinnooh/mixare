package org.mixare;

import org.mixare.data.DataSourceStorage;
import org.mixare.plugin.PluginLoader;
import org.mixare.plugin.PluginType;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

/**
 * This is the plugin loading activity for mixare. This activity will load a splashscreen and then initializes the PluginLoader
 * It will then launch the visible bootstrap plugins and waits for their results. After all bootstrap plugins are loaded
 * then mixare will be launched.
 * @author A.Egal
 */
public class PluginLoaderActivity extends Activity {

	private static final int SPLASHTIME = 2000; // 2 seconds
	public static final int SCANNER_REQUEST_CODE = 0;
	private static final String CLOSE_ACTIVITY_CALL = "closed";
	protected Handler exitHandler = null;
	protected Runnable exitRunnable = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		DataSourceStorage.init(this);
		PluginLoader.getInstance().setActivity(this);
		PluginLoader.getInstance().unBindServices();
		PluginLoader.newInstance();
		PluginLoader.getInstance().setActivity(this);
		PluginLoader.getInstance().loadPlugin(PluginType.BOOTSTRAP_PHASE_1);

		if (arePendingActivitiesFinished()) {
			startDefaultSplashScreen();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	
	private void startDefaultSplashScreen() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.splashscreen);
		// Runnable exiting the splash screen and launching the menu
		exitRunnable = new Runnable() {
			public void run() {
				startMixare();
			}
		};
		// Run the exitRunnable in in _splashTime ms
		exitHandler = new Handler();
		exitHandler.postDelayed(exitRunnable, SPLASHTIME);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if(exitHandler != null){
				//only call this when the default splashscreen is used
				exitHandler.removeCallbacks(exitRunnable);
			}
			startMixare();
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(data != null && data.getExtras() != null && data.getExtras().getString(CLOSE_ACTIVITY_CALL) != null){
			//back button was pressed, close mixare now.
			finish();
			return;
		}	

		processDataSourceFromPlugin(data);
		procesCustomSplashScreen(data);

		PluginLoader.getInstance().decreasePendingActivitiesOnResult();
		startMixare();
	}

	private void startMixare() {
		if(!PluginLoader.getInstance().isPluginLoaded(PluginType.MARKER)){
			loadPlugins();
		}
		if (arePendingActivitiesFinished()) {
			startActivityForResult(new Intent(this, MixView.class),0);
		}
	}

	private boolean arePendingActivitiesFinished() {
		return (PluginLoader.getInstance().getPendingActivitiesOnResult() <= 0);
	}

	private void processDataSourceFromPlugin(Intent data) {
		if (data != null
				&& data.getExtras().getString("resultType")
						.equals("Datasource")) {
			String[] url = data.getExtras().getStringArray("url");
			// clear all datasources for a reinit
			for (int i = 0; i < url.length; i++) {
				DataSourceStorage.getInstance().clear();
				DataSourceStorage.getInstance().add("DataSource0",
						"Barcode source|" + url[i] + "|5|2|true");
				DataSourceStorage.getInstance().setCustomDataSourceSelected(
						true);
			}
		}
	}

	@Override
	protected void onDestroy() {
		PluginLoader.getInstance().unBindServices();
		super.onDestroy();
	}

	private void procesCustomSplashScreen(Intent data) {
		if (data != null
				&& data.getExtras().getString("resultType")
						.equals("Splashscreen")) {
			loadPlugins();
		}
	}

	private void loadPlugins() {
		PluginLoader.getInstance().setActivity(this);
		PluginLoader.getInstance().loadPlugin(PluginType.MARKER);
		PluginLoader.getInstance().loadPlugin(PluginType.BOOTSTRAP_PHASE_2);
		PluginLoader.getInstance().loadPlugin(PluginType.DATAHANDLER);
	}
}
