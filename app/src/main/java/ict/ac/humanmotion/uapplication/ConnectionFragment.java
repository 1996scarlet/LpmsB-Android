package ict.ac.humanmotion.uapplication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Objects;

public class ConnectionFragment extends MyFragment implements OnClickListener {
    final int FRAGMENT_TAG = 0;
    public static final String ARG_SECTION_NUMBER = "section_number";
    View rootView;

    private static final String TAG = "ConnectionFragment";

    BluetoothAdapter btAdapter;
    OnConnectListener connectListener;
    String currentLpms = "";
    String currentConnectedLpms;

    final ArrayList<String> dcLpms = new ArrayList<>();
    ArrayAdapter dcAdapter;
    ListView btList;
    boolean firstDc = true;

    final ArrayList<String> connectedDevicesLpms = new ArrayList<>();
    ArrayAdapter connectedDevicesAdapter;
    ListView connectedDevicesList;
    boolean firstConnectedDevice = true;

    TextView loggingStateText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.connect_screen, container, false);
        Bundle args = getArguments();
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        prepareButtons();
        prepareDiscoveredDevicesList();
        prepareConnectedDevicesList();

        return rootView;
    }

    void prepareButtons() {
        Button b = rootView.findViewById(R.id.button_discover);
        b.setOnClickListener(this);
        b = rootView.findViewById(R.id.button_connect);
        b.setOnClickListener(this);
        b = rootView.findViewById(R.id.button_disconnect);
        b.setOnClickListener(this);
        loggingStateText = rootView.findViewById(R.id.logging_status);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_discover:
                startBtDiscovery();
                break;

            case R.id.button_connect:
                startBtConnect();
                break;

            case R.id.button_disconnect:
                onDisconnect();
                break;
        }
    }

    @Override
    public int getMyFragmentTag() {
        return FRAGMENT_TAG;
    }

    @Override
    public void setMyFragmentTag(int i) {

    }

    public interface OnConnectListener {
        void onConnect(String address);

        void onDisconnect();
    }

    void startBtConnect() {
        btAdapter.cancelDiscovery();

        if (btAdapter != null && !currentLpms.isEmpty()) {
            Toast.makeText(getActivity(), "Connecting to " + currentLpms, Toast.LENGTH_SHORT).show();

            connectListener.onConnect(currentLpms);
        }
    }

    void onDisconnect() {
        connectListener.onDisconnect();

        synchronized (connectedDevicesLpms) {
            Log.e("lpms", "[ConnectionFragment] Remove from list: " + currentConnectedLpms);
            if (connectedDevicesLpms.remove(currentConnectedLpms)) {
                connectedDevicesAdapter.notifyDataSetChanged();

                if (connectedDevicesLpms.size() == 0) {
                    connectedDevicesLpms.add("Press connect button to connect to device..");
                    connectedDevicesAdapter.notifyDataSetChanged();

                    firstConnectedDevice = true;

                    connectedDevicesList.getChildAt(0).setBackgroundColor(Color.TRANSPARENT);
                } else {
                    for (int a = 0; a < connectedDevicesList.getChildCount(); a++) {
                        connectedDevicesList.getChildAt(a).setBackgroundColor(Color.TRANSPARENT);
                    }
                    connectedDevicesList.getChildAt(0).setBackgroundColor(Color.rgb(128, 64, 85));
                    currentConnectedLpms = (String) connectedDevicesList.getItemAtPosition(0);
                    ((LpmsBMainActivity) Objects.requireNonNull(getActivity())).onSensorSelectionChanged(currentConnectedLpms);

                    Log.e("lpms", "[ConnectionFragment] After disconnect selected: " + currentConnectedLpms);
                }
            }
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();


            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (device == null) return;

                synchronized (dcLpms) {
                    if ((device.getName() != null) && (device.getName().length() > 0)) {
                        if (device.getName().contains("LPMSB2")) {
                            for (String dcLpm : dcLpms) {
                                if (device.getAddress().equals(dcLpm)) return;
                            }
                            if (firstDc) {

                                Log.d(TAG, "onReceive: ACTION_FOUND");
                                dcLpms.clear();
                                btList.getChildAt(0).setBackgroundColor(Color.rgb(128, 64, 85));
                                currentLpms = device.getAddress();
                                firstDc = false;
                            }
                            dcLpms.add(device.getAddress());
                            dcAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            }
        }
    };

    public void prepareDiscoveredDevicesList() {
        btList = rootView.findViewById(R.id.list);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        Objects.requireNonNull(getActivity()).registerReceiver(mReceiver, filter);
        dcAdapter = new ArrayAdapter(getActivity(), R.layout.list_view_text_item, dcLpms);
        btList.setAdapter(dcAdapter);
        dcLpms.add("Press Discover button to start discovery..");
        dcAdapter.notifyDataSetChanged();
        firstDc = true;

        btList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (firstDc) return;

                for (int a = 0; a < parent.getChildCount(); a++) {
                    parent.getChildAt(a).setBackgroundColor(Color.TRANSPARENT);
                }
                view.setBackgroundColor(Color.rgb(128, 64, 85));

                currentLpms = (String) btList.getItemAtPosition(position);
            }
        });
    }

    public void prepareConnectedDevicesList() {
        connectedDevicesList = rootView.findViewById(R.id.connected_devices_list);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        Objects.requireNonNull(getActivity()).registerReceiver(mReceiver, filter);
        connectedDevicesAdapter = new ArrayAdapter(getActivity(), R.layout.list_view_text_item, connectedDevicesLpms);
        connectedDevicesList.setAdapter(connectedDevicesAdapter);
        connectedDevicesLpms.add("Press connect button to connect to device..");
        connectedDevicesAdapter.notifyDataSetChanged();
        firstConnectedDevice = true;

        connectedDevicesList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (firstConnectedDevice) return;

                for (int a = 0; a < parent.getChildCount(); a++) {
                    parent.getChildAt(a).setBackgroundColor(Color.TRANSPARENT);
                }
                view.setBackgroundColor(Color.rgb(128, 64, 85));

                String itemValue = (String) connectedDevicesList.getItemAtPosition(position);
                ((LpmsBMainActivity) getActivity()).onSensorSelectionChanged(itemValue);

                currentConnectedLpms = itemValue;
                Log.e("lpms", "[ConnectionFragment] Switched to pos " + position + " value " + itemValue);
            }
        });
    }

    public void confirmConnected(BluetoothDevice device) {
        synchronized (connectedDevicesLpms) {
            Log.e("lpms", "[ConnectionFragment] Connecion callback to device: " + device.getAddress() + (device.getName()));

            if (device.getName().contains("LPMSB2")) {
                for (String connectedDevicesLpm : connectedDevicesLpms) {
                    if (device.getAddress().equals(connectedDevicesLpm)) {
                        Log.e("lpms", "[ConnectionFragment] Detected double device: " + device.getAddress());
                        return;
                    }
                }
                if (firstConnectedDevice) {
                    connectedDevicesLpms.clear();
                    connectedDevicesList.getChildAt(0).setBackgroundColor(Color.rgb(128, 64, 85));
                    ((LpmsBMainActivity) Objects.requireNonNull(getActivity())).onSensorSelectionChanged(device.getName());
                    firstConnectedDevice = false;
                }

                connectedDevicesLpms.add(device.getAddress() /* + device.getImuId()*/);
                connectedDevicesAdapter.notifyDataSetChanged();

                for (int a = 0; a < connectedDevicesList.getChildCount(); a++) {
                    connectedDevicesList.getChildAt(a).setBackgroundColor(Color.TRANSPARENT);
                }
                connectedDevicesList.getChildAt(0).setBackgroundColor(Color.rgb(128, 64, 85));
                currentConnectedLpms = (String) btList.getItemAtPosition(0);
                ((LpmsBMainActivity) getActivity()).onSensorSelectionChanged(currentConnectedLpms);

                Log.e("lpms", "[ConnectionFragment] After connect selected: " + currentConnectedLpms);
            }
        }
    }

    public void startBtDiscovery() {
        Toast.makeText(getActivity(), "Starting discovery..", Toast.LENGTH_SHORT).show();

        btAdapter.cancelDiscovery();
        btAdapter.startDiscovery();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            connectListener = (OnConnectListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }
    }

    @Override
    public void updateView(LpmsBData d, ImuStatus s) {
    }
}