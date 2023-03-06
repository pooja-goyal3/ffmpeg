 package com.example.ffmpeg.permission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.Map;

public class PermissionsHandler{
    public static final int PERMISSIONS_REQUEST_WRITE_STORAGE_STATE = 998;

    Context mContext;
    Activity current_activity;
    PermissionListener permissionResultCallback;
    IPermissionInterface mPermissionInterface;
    Map<String, Object> mMap;
    private String[] permissions;

    public PermissionsHandler(Context context, int permissionsType, IPermissionInterface permissionInterface, Map<String, Object> map) {
        this.mContext = context;
        this.current_activity = (Activity) context;
        permissionResultCallback = (PermissionListener) context;
        mPermissionInterface = permissionInterface;
        mMap = map;
        checkPermissions(permissionsType);
    }

    public void checkPermissions(int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            switch (requestCode) {

                case PERMISSIONS_REQUEST_WRITE_STORAGE_STATE:
                    permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    break;

                default:
                    break;
            }

            boolean flag = false;
            for (String s : permissions) {
                if (this.current_activity.getApplicationContext().checkSelfPermission(s) != PackageManager.PERMISSION_GRANTED)
                    flag = true;
            }
            if (flag)
                this.current_activity.requestPermissions(permissions, requestCode);
            else
                permissionResultCallback.OnPermissionResult(requestCode, true, mPermissionInterface, mMap);
        }
        else {
            permissionResultCallback.OnPermissionResult(requestCode, true, mPermissionInterface, mMap);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_WRITE_STORAGE_STATE){
            boolean flag = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                for (int i = 0; i < permissions.length; i++)
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                        flag = false;
            if (permissionResultCallback != null)
                permissionResultCallback.OnPermissionResult(requestCode, flag, mPermissionInterface, mMap);
        }
    }
}