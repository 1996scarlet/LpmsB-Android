package ict.ac.humanmotion.uapplication;

import android.Manifest;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class LpmsBMainActivity extends FragmentActivity implements ActionBar.TabListener, MyFragment.MyFragmentListener, ConnectionFragment.OnConnectListener {
    Timer mTimer;

    TextView gyrXText, gyrYText, gyrZText;
    TextView accXText, accYText, accZText;
    TextView magXText, magYText, magZText;
    TextView quatXText, quatYText, quatZText, quatWText;
    TextView eulerXText, eulerYText, eulerZText;
    BluetoothAdapter btAdapter;
    boolean isLpmsBConnected = false;

    ImuStatus imuStatus = new ImuStatus();
    Handler handler = new Handler();
    Handler updateFragmentsHandler = new Handler();
    LpmsBData imuData = new LpmsBData();

    private int updateRate = 25;
    private boolean getImage = true;

    List<LpmsBThread> lpmsList = new ArrayList<LpmsBThread>();
    LpmsBThread lpmsB;

    private Map<Integer, String> mFragmentMap = new HashMap<Integer, String>();
    private AppSectionsPagerAdapter mAppSectionsPagerAdapter;
    ViewPager mViewPager;

    OutputStreamWriter logFileWriter;
    boolean isLoggingStarted = false;
    FileOutputStream logFileStream;

    boolean stopPollThread = false;

    private Runnable mUpdateFragmentsTask = new Runnable() {
        public void run() {
            synchronized (imuData) {
                updateFragment(imuData, imuStatus);
            }
            updateFragmentsHandler.postDelayed(mUpdateFragmentsTask, updateRate);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        setContentView(R.layout.main);
        initializeViews();

        Thread t = new Thread(new DataAnalysisThread());
        t.start();

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    10086);
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {
                Toast.makeText(this, "shouldShowRequestPermissionRationale", Toast.LENGTH_SHORT).show();
            }
        }

        Log.e("lpms", "Initializing..");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        stopPollThread = true;

        synchronized (lpmsList) {
            for (ListIterator<LpmsBThread> it = lpmsList.listIterator(); it.hasNext(); ) {
                LpmsBThread e = it.next();
                e.close();
            }
        }

        super.onDestroy();
    }

    @Override
    protected void onStart() {
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                timerMethod();
            }
        }, 25, 25);

        super.onStart();
    }

    @Override
    protected void onStop() {
        mTimer.cancel();

        super.onStop();
    }

    @Override
    protected void onResume() {
        startUpdateFragments();

        super.onResume();
    }

    @Override
    protected void onPause() {
        stopUpdateFragments();

        super.onPause();
    }

    void initializeViews() {
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());

        final ActionBar actionBar = getActionBar();

        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(16);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(actionBar.newTab().setText(mAppSectionsPagerAdapter.getPageTitle(i)).setTabListener(this));
        }

        mViewPager.setCurrentItem(0);
    }

    public void startUpdateFragments() {
        updateFragmentsHandler.removeCallbacks(mUpdateFragmentsTask);
        updateFragmentsHandler.postDelayed(mUpdateFragmentsTask, 100);
    }

    public void updateFragment(LpmsBData d, ImuStatus s) {
        int key = mViewPager.getCurrentItem();

        MyFragment statusFragment = (MyFragment) getSupportFragmentManager().findFragmentByTag(mFragmentMap.get(key));

        if (statusFragment != null) {
            statusFragment.updateView(d, s);
        } else {
        }
    }

    void stopUpdateFragments() {
        updateFragmentsHandler.removeCallbacks(mUpdateFragmentsTask);
    }

    private void timerMethod() {
        handler.post(new Runnable() {
            public void run() {
            }
        });
    }

    public class DataAnalysisThread implements Runnable {
        public void run() {
            while (stopPollThread == false) {
                synchronized (lpmsList) {
                    for (ListIterator<LpmsBThread> it = lpmsList.listIterator(); it.hasNext(); ) {
                        LpmsBThread e = it.next();
                        LpmsBData d = new LpmsBData();
                        while (e.hasNewData() == true) {
                            d = e.getLpmsBData();
                            if (lpmsB.getAddress().equals(e.getAddress()))
                                imuData = new LpmsBData(d);
                            logLpmsData(new LpmsBData(d));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        mFragmentMap.put(((MyFragment) fragment).getMyFragmentTag(), fragment.getTag());
    }

    @Override
    public void onUserInput(int input, String data) {
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onConnect(String address) {
        int id = 0;

        synchronized (lpmsList) {
            for (ListIterator<LpmsBThread> it = lpmsList.listIterator(); it.hasNext(); ) {
                if (address == it.next().getAddress()) {
                    Toast.makeText(getBaseContext(), address + " is already connected.", Toast.LENGTH_SHORT).show();
                    return;
                }
                id++;
            }

            lpmsB = new LpmsBThread(btAdapter);

            lpmsB.setAcquisitionParameters(true, true, true, true, true, true, true);
            if (lpmsB.connect(address, id) == true) {
                lpmsList.add(lpmsB);

                isLpmsBConnected = true;
                imuStatus.measurementStarted = true;

                Toast.makeText(getBaseContext(), "Connected to " + address, Toast.LENGTH_SHORT).show();

                ConnectionFragment connectionFragment = (ConnectionFragment) getSupportFragmentManager().findFragmentByTag(mFragmentMap.get(0));
                if (connectionFragment != null)
                    connectionFragment.confirmConnected(lpmsB.getDevice());
            } else {
                Toast.makeText(getBaseContext(), "Connection to " + address + " failed. Please reconnect.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public void startLogging() {
        if (isExternalStorageWritable() == true) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String currentDateandTime = sdf.format(new Date());
                File logFile = new File("/sdcard/LpResearch/DataLog" + currentDateandTime + ".csv");
                logFile.createNewFile();
                logFileStream = new FileOutputStream(logFile);
                logFileWriter = new OutputStreamWriter(logFileStream);
                Toast.makeText(getBaseContext(), "Logging to " + logFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                isLoggingStarted = true;
                logFileWriter.append("SensorId, TimeStamp (s), FrameNumber, AccX (g), AccY (g), AccZ (g), GyroX (deg/s), GyroY (deg/s), GyroZ (deg/s), MagX (uT), MagY (uT), MagZ (uT), EulerX (deg), EulerY (deg), EulerZ (deg), QuatW, QuatX, QuatY, QuatZ, LinAccX (m/s^2), LinAccY (m/s^2), LinAccZ (m/s^2), Pressure (mPa)\n");
                imuStatus.isLogging = true;
                imuStatus.logFileName = logFile.getAbsolutePath();
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getBaseContext(), "Couldn't write to external storage. Please detach device from PC.", Toast.LENGTH_SHORT).show();
        }
    }

    public void logLpmsData(LpmsBData d) {
        if (isLoggingStarted == true) {
            try {
                synchronized (logFileWriter) {
                    logFileWriter.append(d.imuId + ", " + d.timestamp + ", " + d.frameNumber + ", " + d.gyr[0] + ", " + d.gyr[1] + ", " + d.gyr[2] + ", " + d.acc[0] + ", " + d.acc[1] + ", " + d.acc[2] + ", " + d.mag[0] + ", " + d.mag[1] + ", " + d.mag[2] + ", " + d.quat[0] + ", " + d.quat[1] + ", " + d.quat[2] + ", " + d.quat[3] + ", " + d.euler[0] + ", " + d.euler[1] + ", " + d.euler[2] + ", " + d.linAcc[0] + ", " + d.linAcc[1] + ", " + d.linAcc[2] + ", " + d.pressure + "\n");
                }
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void stopLogging() {
        if (isLoggingStarted == false) return;
        try {
            synchronized (logFileWriter) {
                logFileWriter.close();
            }
            logFileStream.close();
            isLoggingStarted = false;
            imuStatus.isLogging = false;
            Toast.makeText(getBaseContext(), "Logging stopped", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void onSensorSelectionChanged(String address) {
        synchronized (lpmsList) {
            for (ListIterator<LpmsBThread> it = lpmsList.listIterator(); it.hasNext(); ) {
                LpmsBThread e = it.next();

                if (address.equals(e.getAddress())) {
                    lpmsB = e;
                    Log.e("lpms", "[LpmsBMainActivity] In main activity: " + lpmsB.getAddress());
                    DataFragment dataFragment = (DataFragment) getSupportFragmentManager().findFragmentByTag(mFragmentMap.get(2));
                    if (dataFragment != null) dataFragment.clearView();

                    return;
                }
            }
        }
    }

    @Override
    public void onDisconnect() {
        synchronized (lpmsList) {
            for (ListIterator<LpmsBThread> it = lpmsList.listIterator(); it.hasNext(); ) {
                LpmsBThread e = it.next();

                if (lpmsB.getAddress().equals(e.getAddress())) {
                    Toast.makeText(getBaseContext(), "Disconnected " + e.getAddress(), Toast.LENGTH_SHORT).show();

                    e.close();
                    lpmsList.remove(e);
                    if (lpmsList.size() == 0) {
                        imuStatus.measurementStarted = false;
                    }

                    return;
                }
            }
        }
    }
}