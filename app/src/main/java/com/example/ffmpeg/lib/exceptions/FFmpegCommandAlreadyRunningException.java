package com.example.ffmpeg.lib.exceptions;

public class FFmpegCommandAlreadyRunningException extends Exception {

    public FFmpegCommandAlreadyRunningException(String message) {
        super(message);
    }

}
