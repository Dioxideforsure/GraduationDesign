package com.kuopan.Util;

import com.kuopan.Exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

@Slf4j
public class FfmpegUtils {

    // 基础的命令行执行方法
    public static void executeCommand(String command) {
        try {
            log.info("开始执行FFmpeg命令: {}", command);
            Process process = Runtime.getRuntime().exec(command);
            // 必须清空标准输出和错误输出流，否则会导致进程阻塞卡死
            clearStream(process.getInputStream());
            clearStream(process.getErrorStream());
            process.waitFor();
            log.info("FFmpeg命令执行完成");
        } catch (Exception e) {
            log.error("FFmpeg命令执行失败: {}", command, e);
            throw new BusinessException("视频处理失败");
        }
    }

    private static void clearStream(InputStream stream) {
        new Thread(() -> {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, "GBK"))) { // Windows环境下通常是GBK
                String line;
                while ((line = br.readLine()) != null) {
                    // 可以取消注释下面这行来查看 FFmpeg 的实时转码日志
                    // log.debug(line);
                }
            } catch (Exception e) {
                log.error("读取进程流异常", e);
            }
        }).start();
    }

    /**
     * 生成视频缩略图 (截取第1秒)
     */
    public static void createCover(String videoPath, String coverPath) {
        // 注意路径两边加双引号，防止路径中存在空格导致命令解析失败
        String cmd = String.format("ffmpeg -y -i \"%s\" -ss 00:00:01 -vframes 1 -vf scale=150:-1 \"%s\"", videoPath, coverPath);
        executeCommand(cmd);
    }

    /**
     * 将视频切片为 m3u8 (HLS格式)
     */
    public static void transferToM3u8(String videoPath, String tsFolder) {
        String m3u8Path = tsFolder + "index.m3u8";
        String cmd = String.format("ffmpeg -y -i \"%s\" -c:v libx264 -c:a aac -strict -2 -f hls -hls_time 10 -hls_list_size 0 -hls_segment_filename \"%s%%04d.ts\" \"%s\"",
                videoPath, tsFolder, m3u8Path);
        executeCommand(cmd);
    }
}