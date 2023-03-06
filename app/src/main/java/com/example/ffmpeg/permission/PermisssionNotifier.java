/*****************************************************************
 *
 * BLUESKY CONFIDENTIAL
 * __________________
 *
 *  BS Inventions Pvt Ltd
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of BS Inventions Pvt Ltd and its partners,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to BS Inventions Pvt Ltd and partners.
 * Dissemination of this information or reproduction of this
 * material is strictly forbidden unless prior written
 * permission is obtained from BS Inventions Pvt Ltd.
 */
package com.example.ffmpeg.permission;

public class PermisssionNotifier {
    static private PermisssionNotifier sNotifier;
    // FCM Listener...
    private PermissionListenerInterface mListener;

    public static PermisssionNotifier getInstance() {
        if (sNotifier == null) {
            sNotifier = new PermisssionNotifier();
        }
        return sNotifier;
    }

    public void setOnEventListener(PermissionListenerInterface listener) {
        mListener = listener;
    }

    public void sendUpdateFCMMessage() {
        if (mListener != null)
            mListener.onUpdateFCMToken();
    }

    public void sendFCMTokenReceivedMessage() {
        if (mListener != null)
            mListener.onFCMTokenIsReady();
    }

    public void sendIMEIPermissionServedMessage() {
        if (mListener != null)
            mListener.OnIMEIPermissionServed();
    }
}