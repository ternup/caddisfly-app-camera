package org.akvo.mobile.caddisfly.activity;

import com.ternup.caddisfly.R;
import com.ternup.caddisfly.activity.ProgressActivity;
import com.ternup.caddisfly.app.Globals;
import com.ternup.caddisfly.fragment.AboutFragment;
import com.ternup.caddisfly.fragment.CalibrateFragment;
import com.ternup.caddisfly.fragment.HelpFragment;
import com.ternup.caddisfly.util.PreferencesHelper;

import org.akvo.mobile.caddisfly.fragment.SettingsFragment;
import org.akvo.mobile.caddisfly.fragment.StartFragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;


public class MainActivity extends Activity
        implements StartFragment.OnCalibrateListener, StartFragment.OnStartTestListener {

    private CalibrateFragment calibrateFragment = null;

    private HelpFragment helpFragment = null;

    private AboutFragment aboutFragment = null;

    private SettingsFragment settingsFragment = null;

    private final int REQUEST_TEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO: setup external app connection
        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        Boolean external = false;

        if (Globals.ACTION_WATER_TEST.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                external = true;
            }
        }

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, StartFragment.newInstance(external))
                    .commit();
        }
    }

    public void displayView(int position, boolean addToBackStack) {

        int index = getCurrentFragmentIndex();

        if (index == position) {
            // requested fragment is already showing
            return;
        }

        Fragment fragment;

        //isSettingsShowing = false;
        //isAboutShowing = false;
        switch (position) {
            case Globals.CALIBRATE_SCREEN_INDEX:
                if (calibrateFragment == null) {
                    calibrateFragment = new CalibrateFragment();
                }
                fragment = calibrateFragment;
                break;
            case Globals.SETTINGS_SCREEN_INDEX:
                //isSettingsShowing = true;
                if (settingsFragment == null) {
                    settingsFragment = new SettingsFragment();
                }
                fragment = settingsFragment;
                break;
            case Globals.HELP_SCREEN_INDEX:

                if (helpFragment == null) {
                    helpFragment = new HelpFragment();
                }
                fragment = helpFragment;
                break;
            case Globals.ABOUT_SCREEN_INDEX:
                //isAboutShowing = true;
                if (aboutFragment == null) {
                    aboutFragment = new AboutFragment();
                }
                fragment = aboutFragment;
                break;
            default:
                return;
        }
        if (fragment != null) {

            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.container, fragment, String.valueOf(position));
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            if (addToBackStack) {
                ft.addToBackStack(null);
            }
            ft.commit();
        }
    }

    /**
     * @return index of fragment currently showing
     */
    private int getCurrentFragmentIndex() {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.container);
        if (fragment != null) {
            String positionString = fragment.getTag();
            if (positionString != null) {
                try {
                    return Integer.parseInt(positionString);
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        }
        return -1;
    }

    @Override
    public void onCalibrate(int index) {
        displayView(index, true);
    }

    @Override
    public void onStartTest(int index) {

        final Intent intent = new Intent(getIntent());
        intent.setClass(this, ProgressActivity.class);
        //final Intent intent = new Intent(this, ProgressActivity.class);
        intent.putExtra("startTest", true);
        intent.putExtra(PreferencesHelper.CURRENT_TEST_TYPE_KEY, 0);
        intent.putExtra(PreferencesHelper.CURRENT_LOCATION_ID_KEY, -1);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_TEST:
                if (resultCode == Activity.RESULT_OK) {
                    Intent intent = new Intent(data);
                    this.setResult(Activity.RESULT_OK, intent);
                    finish();
                }
                break;
        }
    }

}
