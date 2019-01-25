package cn.com.heaton.blelibrary.ble.request;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Message;
import android.support.annotation.IntRange;

import java.math.BigDecimal;

import cn.com.heaton.blelibrary.ble.BleHandler;
import cn.com.heaton.blelibrary.ble.BleDevice;
import cn.com.heaton.blelibrary.ble.Ble;
import cn.com.heaton.blelibrary.ble.BleStates;
import cn.com.heaton.blelibrary.ble.BluetoothLeService;
import cn.com.heaton.blelibrary.ble.TaskExecutor;
import cn.com.heaton.blelibrary.ble.annotation.Implement;
import cn.com.heaton.blelibrary.ble.callback.BleWriteCallback;
import cn.com.heaton.blelibrary.ble.callback.BleWriteEntityCallback;
import cn.com.heaton.blelibrary.ble.exception.BleException;
import cn.com.heaton.blelibrary.ble.exception.BleWriteException;

/**
 *
 * Created by LiuLei on 2017/10/23.
 */
@Implement(WriteRequest.class)
public class WriteRequest<T extends BleDevice> implements IMessage {

    private BleWriteCallback<T> mBleLisenter;
    private BleWriteEntityCallback<T> mBleEntityLisenter;
    private BleHandler mHandler;
    private final Object lock = new Object();
    private boolean isWritingEntity;

    protected WriteRequest() {
        mHandler = BleHandler.getHandler();
        mHandler.setHandlerCallback(this);
    }

    public boolean write(T device,byte[]data, BleWriteCallback<T> lisenter){
        this.mBleLisenter = lisenter;
        boolean result = false;
        BluetoothLeService service = Ble.getInstance().getBleService();
        if (service != null) {
            result = service.wirteCharacteristic(device.getBleAddress(),data);
        }
        return result;
    }

    public void cancelWriteEntity(){
        if (isWritingEntity){
            isWritingEntity = false;
        }
    }

    public void writeEntity(final T device, final byte[]data, final int packLength, final int delay, BleWriteEntityCallback<T> lisenter){
        this.mBleEntityLisenter = lisenter;
        final BluetoothLeService service = Ble.getInstance().getBleService();
        if(data == null || data.length == 0) {
            try {
                throw new BleWriteException("Send Entity cannot be empty");
            } catch (BleException e) {
                e.printStackTrace();
                return;
            }
        }
        TaskExecutor.executeTask(new Runnable() {
            @Override
            public void run() {
                isWritingEntity = true;
                int index = 0;
                int length = data.length;
                while (index < length){
                    if (!isWritingEntity){
                        if (mBleEntityLisenter != null){
                            mBleEntityLisenter.onWriteCancel();
                        }
                        return;
                    }
                    byte[] txBuffer = new byte[packLength];
                    for (int i=0; i<packLength; i++){
                        if(index < length){
                            txBuffer[i] = data[index++];
                        }
                    }
                    boolean result = service.wirteCharacteristic(device.getBleAddress(), txBuffer);
                    if(!result){
                        if(mBleEntityLisenter != null){
                            mBleEntityLisenter.onWriteFailed();
                            isWritingEntity = false;
                            return;
                        }
                    }else {
                        if (mBleEntityLisenter != null){
                            double progress = new BigDecimal((float)index / length).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                            mBleEntityLisenter.onWriteProgress(progress);
                        }
                    }
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if(mBleEntityLisenter != null){
                    mBleEntityLisenter.onWriteSuccess();
                    isWritingEntity = false;
                }
            }
        });
    }

//    public boolean writeAutoEntity(final T device, final byte[]data, final int packLength){
//        if(data.length == 0 || packLength == 0)return false;
//        final BluetoothLeService service = Ble.getInstance().getBleService();
//        Future<Boolean> futureTask = FutureThreadPool.getInstance().executeTask(new Callable<Boolean>() {
//            @Override
//            public Boolean call() throws Exception {
//                int index = 0;
//                int length = data.length;
//                boolean isLosePack = true;
//                do {
//                    byte[] txBuffer = new byte[packLength];
//                    for (int i=0; i<packLength; i++){
//                        if(index < length){
//                            txBuffer[i] = data[index++];
//                        }else {
//                            return true;
//                        }
//                    }
//                    L.e("writeAutoEntity",index+"+++");
//                    isLosePack = service.wirteCharacteristic(device.getBleAddress(), txBuffer);
//                    Thread.sleep(1L);
//                } while (isLosePack);
//                    return false;
//            }
//        });
//        try {
//            return futureTask.get();
//        } catch (InterruptedException | ExecutionException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what){
            case BleStates.BleStatus.Write:
                if(msg.obj instanceof BluetoothGattCharacteristic){
                    BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) msg.obj;
                    if(mBleLisenter != null){
                        mBleLisenter.onWriteSuccess(characteristic);
                    }
                }
                break;
            default:
                break;
        }
    }
}
