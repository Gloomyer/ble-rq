package com.gloomyer.blerq.utils;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.gloomyer.blerq.R;
import com.gloomyer.blerq.exception.BleRqException;

/**
 * Time: 1/13/21
 * Author: Gloomy
 * Description:
 */
public class PermissionUtils {


    public interface Callback {
        /**
         * 获取权限的响应
         *
         * @param isGet 是否成功获取到了
         */
        void onResponse(boolean isGet);
    }

    public static class RequestFragment extends Fragment {

        private static final int REQ_CODE = 0xFF01;
        public Callback callback;
        public FragmentManager externalManager;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            TextView textView = new TextView(getContext());
            textView.setHeight(1);
            textView.setWidth(1);
            return textView;
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            if (getArguments() == null) {
                if (callback != null) callback.onResponse(false);
                close();
            } else {
                int type = getArguments().getInt("type");
                if (type == 1) {
                    String[] permissions = getArguments().getStringArray("permissions");
                    if (permissions == null || permissions.length == 0) {
                        if (callback != null) callback.onResponse(false);
                        close();
                    } else {
                        requestPermissions(permissions, REQ_CODE);
                    }
                } else if (type == 2) {
                    startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQ_CODE);
                } else {
                    if (callback != null) callback.onResponse(false);
                    close();
                }

            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grants) {
            super.onRequestPermissionsResult(requestCode, permissions, grants);
            for (int grant : grants) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    if (callback != null) callback.onResponse(false);
                    close();
                    return;
                }
            }

            if (callback != null) callback.onResponse(true);
            close();
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == REQ_CODE) {
                if (callback != null) callback.onResponse(resultCode == Activity.RESULT_OK);
                close();
            }
        }

        private void close() {
            externalManager.beginTransaction().hide(this).remove(this).commit();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            callback = null;
            externalManager = null;
        }
    }

    /**
     * 请求获得权限
     *
     * @param fm          fm
     * @param permissions permissions
     */
    public static void requestPermission(@NonNull FragmentManager fm, @NonNull Callback callback, String... permissions) {
        if (permissions.length == 0) {
            throw new BleRqException(R.string.blerq_permissions_must_greater_0);
        }
        FragmentTransaction transaction = fm.beginTransaction();
        RequestFragment fragment = new RequestFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("type", 1);
        bundle.putStringArray("permissions", permissions);
        fragment.setArguments(bundle);
        fragment.callback = callback;
        fragment.externalManager = fm;
        transaction.add(fragment, fragment.toString());
        transaction.show(fragment);
        transaction.commit();
    }


    /**
     * 请求打开蓝牙设备
     *
     * @param fm       fm
     * @param callback callback
     */
    public static void requestOpenBluetooth(@NonNull FragmentManager fm, @NonNull Callback callback) {
        FragmentTransaction transaction = fm.beginTransaction();
        RequestFragment fragment = new RequestFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("type", 2);
        fragment.setArguments(bundle);
        fragment.callback = callback;
        fragment.externalManager = fm;
        transaction.add(fragment, fragment.toString());
        transaction.show(fragment);
        transaction.commit();
    }
}
