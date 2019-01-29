package cn.com.heaton.blelibrary.ble.request;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import cn.com.heaton.blelibrary.ble.Ble;
import cn.com.heaton.blelibrary.ble.BleHandler;
import cn.com.heaton.blelibrary.ble.BleDevice;
import cn.com.heaton.blelibrary.ble.BluetoothLeService;
import cn.com.heaton.blelibrary.ble.L;
import cn.com.heaton.blelibrary.ble.BleStates;
import cn.com.heaton.blelibrary.ble.TaskExecutor;
import cn.com.heaton.blelibrary.ble.annotation.Implement;
import cn.com.heaton.blelibrary.ble.callback.BleNotiftCallback;
import cn.com.heaton.blelibrary.ble.callback.wrapper.NotifyWrapperLisenter;

/**
 * Created by LiuLei on 2017/10/23.
 */
@Implement(NotifyRequest.class)
public class NotifyRequest<T extends BleDevice> implements NotifyWrapperLisenter<T> {

    private static final String TAG = "NotifyRequest";

    private BleNotiftCallback<T> mBleLisenter;
    private HashMap<T, BleNotiftCallback> mBleNotifyMap = new HashMap<>();
    private List<BleNotiftCallback> mNotifyCallbacks = new ArrayList<>();
    private HashMap<T, List<BleNotiftCallback>> mBleNotifyMaps = new HashMap<>();

    protected NotifyRequest() {}

    public void notify(T device, BleNotiftCallback<T> callback) {
//        if(callback != null && !mNotifyCallbacks.contains(callback)){
//            this.mNotifyCallbacks.add(callback);
//        }
//        if(!mBleNotifyMap.containsKey(device)){
//            this.mBleNotifyMap.put(device, callback);
//            this.mNotifyCallbacks.add(callback);
//        }
        if (!mNotifyCallbacks.contains(callback)) {
            List<BleNotiftCallback> bleCallbacks;
            if (mBleNotifyMaps.containsKey(device)) {
                bleCallbacks = mBleNotifyMaps.get(device);
                bleCallbacks.add(callback);
            } else {//不包含key
                bleCallbacks = new ArrayList<>();
                bleCallbacks.add(callback);
                mBleNotifyMaps.put(device, bleCallbacks);
            }
            mNotifyCallbacks.add(callback);
        }
    }

    public void unNotify(T device) {
//        if(callback != null && mNotifyCallbacks.contains(callback)){
//            this.mNotifyCallbacks.remove(callback);
//        }
//        if(mBleNotifyMap.containsKey(device)){
//            mNotifyCallbacks.remove(mBleNotifyMap.get(device));
//            mBleNotifyMap.remove(device);
//        }
        if (mBleNotifyMaps.containsKey(device)) {
            //移除该设备的所有通知
            mNotifyCallbacks.removeAll(mBleNotifyMaps.get(device));
            mBleNotifyMaps.remove(device);
        }
    }

    @Override
    public void onChanged(final T device, BluetoothGattCharacteristic characteristic) {
        TaskExecutor.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                for (BleNotiftCallback callback : mNotifyCallbacks) {
                    callback.onChanged(device, device.getNotifyCharacteristic());
                    L.e("handleMessage", "onChanged++");
                }
            }
        });
    }

    @Override
    public void onReady(T device) {

    }

    @Override
    public void onServicesDiscovered(final BluetoothGatt gatt) {
        TaskExecutor.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                for (BleNotiftCallback callback : mNotifyCallbacks) {
                    callback.onServicesDiscovered(gatt);
                }
            }
        });
    }

    @Override
    public void onNotifySuccess(final BluetoothGatt gatt) {
        TaskExecutor.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                for (BleNotiftCallback callback : mNotifyCallbacks) {
                    callback.onNotifySuccess(gatt);
                }
            }
        });
    }
}
