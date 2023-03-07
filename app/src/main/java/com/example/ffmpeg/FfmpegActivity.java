package com.example.ffmpeg;

import static com.example.ffmpeg.permission.PermissionsHandler.PERMISSIONS_REQUEST_WRITE_STORAGE_STATE;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.ffmpeg.lib.ExecuteBinaryResponseHandler;
import com.example.ffmpeg.lib.FFmpeg;
import com.example.ffmpeg.lib.LoadBinaryResponseHandler;
import com.example.ffmpeg.lib.exceptions.FFmpegCommandAlreadyRunningException;
import com.example.ffmpeg.lib.exceptions.FFmpegNotSupportedException;
import com.example.ffmpeg.permission.IPermissionInterface;
import com.example.ffmpeg.permission.PermissionListener;
import com.example.ffmpeg.permission.PermissionsHandler;

import java.io.File;
import java.util.Map;

public class FfmpegActivity extends Activity implements View.OnClickListener, PermissionListener {

    private static final String TAG = "FFMPEG LOGS";
    FFmpeg ffmpeg;
    EditText inputArgEditText;
    EditText inputFileEditText;
    EditText outputArgEditText;
    Button runButton;
    LinearLayout output;
    private ProgressDialog progressDialog;
    private PermissionsHandler mPermissionsHandler;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (mPermissionsHandler != null)
            mPermissionsHandler.onRequestPermissionsResult(requestCode, permissions, grantResults);
        loadFFMpegBinary();
    }

    @Override
    public void OnPermissionResult(int requestCode, boolean hasPermission, IPermissionInterface iPermissionInterface, Map<String, Object> map) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && requestCode == PERMISSIONS_REQUEST_WRITE_STORAGE_STATE) {
            iPermissionInterface.onPermissionServed(requestCode, hasPermission, map, this);
        }
        loadFFMpegBinary();
    }

    @Override
    public void requestPermissions(int requestCode, IPermissionInterface permissionInterface, Map<String, Object> map) {
        mPermissionsHandler = new PermissionsHandler(this, requestCode, permissionInterface, map);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.run_command:
                String inputArgs = inputArgEditText.getText().toString().trim();
                String inputFile = inputFileEditText.getText().toString().trim();

                String outfile = getFilesDir().getAbsolutePath() + File.separator + "test.mp4";
                String outArgs = outputArgEditText.getText().toString().trim();

                String cmd= "-version";
                if (!TextUtils.isEmpty(inputArgs) && !TextUtils.isEmpty(inputFile) && !TextUtils.isEmpty(outArgs)) {
                    cmd = inputArgs + " " + inputFile + " " + outArgs + " " + outfile;
                }
                else if (!TextUtils.isEmpty(inputArgs) && !TextUtils.isEmpty(inputFile)) {
                    cmd = inputArgs + " " + inputFile + " " + outfile;
                }
                else{
                    Toast.makeText(this, getString(R.string.empty_command_toast1), Toast.LENGTH_LONG).show();
                    break;
                }

                cmd = "-i https://video.twimg.com/ext_tw_video/1632625449011654657/pu/pl/KiQ6AIh_7qIqfEAF.m3u8?variant_version=1&tag=12&container=fmp4 -c copy -f mpegts ";
                Log.e("cmd",cmd);
                String[] command = cmd.split(" ");
                if (command.length != 0) {
                    execFFmpegBinary(command);
                } else {
                    Toast.makeText(this, getString(R.string.empty_command_toast), Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ffmpeg);
        ffmpeg = FFmpeg.getInstance(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                getApplicationContext().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        else {
            loadFFMpegBinary();
        }
        initUI();
    }

    private void initUI() {
        inputArgEditText = findViewById(R.id.input_arg);
        inputFileEditText = findViewById(R.id.input_file);
        outputArgEditText = findViewById(R.id.output_args);
        runButton = findViewById(R.id.run_command);
        output = findViewById(R.id.command_output);

        runButton.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(null);
    }

    private void loadFFMpegBinary() {
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    showUnsupportedExceptionDialog();
                }
            });
        } catch (FFmpegNotSupportedException e) {
            showUnsupportedExceptionDialog();
        }
    }

    private void execFFmpegBinary(final String[] command) {
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    addTextViewToLayout("FAILED with output : " + s);
                }

                @Override
                public void onSuccess(String s) {
                    addTextViewToLayout("SUCCESS with output : " + s);
                }

                @Override
                public void onProgress(String s) {
                    Log.d(TAG, "Started command : ffmpeg " + command);
                    addTextViewToLayout("progress : " + s);
                    progressDialog.setMessage("Processing\n" + s);
                }

                @Override
                public void onStart() {
                    output.removeAllViews();
                    Log.d(TAG, "Started command : ffmpeg " + command);
                    progressDialog.setMessage("Processing...");
                    progressDialog.show();
                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "Finished command : ffmpeg " + command);
                    progressDialog.dismiss();
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // do nothing for now
        }
    }

    private void addTextViewToLayout(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        output.addView(textView);
    }

    private void showUnsupportedExceptionDialog() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.device_not_supported))
                .setMessage(getString(R.string.device_not_supported_message))
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .create()
                .show();

    }

}
