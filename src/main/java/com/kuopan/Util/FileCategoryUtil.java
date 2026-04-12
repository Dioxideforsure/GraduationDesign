package com.kuopan.Util;

import org.apache.commons.lang3.StringUtils;

/**
 * 与库表 file_category / file_type 注释对齐的简易分类（上传 MVP 用）。
 * 分片合并、视频转码后可在此扩展或改为独立策略类。
 */
public final class FileCategoryUtil {

    private FileCategoryUtil() {
    }

    /**
     * @return int[]{fileCategory, fileType}
     */
    public static int[] resolveFromExtension(String extension) {
        if (StringUtils.isBlank(extension)) {
            return new int[]{5, 10};
        }
        String ext = extension.toLowerCase();
        switch (ext) {
            case "mp4":
            case "avi":
            case "mkv":
            case "mov":
            case "wmv":
            case "flv":
            case "webm":
                return new int[]{1, 1};
            case "mp3":
            case "wav":
            case "flac":
            case "aac":
            case "ogg":
            case "m4a":
                return new int[]{2, 2};
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
            case "webp":
            case "bmp":
                return new int[]{3, 3};
            case "pdf":
                return new int[]{4, 4};
            case "doc":
            case "docx":
                return new int[]{4, 5};
            case "xls":
            case "xlsx":
                return new int[]{4, 6};
            case "txt":
            case "md":
            case "log":
                return new int[]{4, 7};
            case "java":
            case "js":
            case "ts":
            case "vue":
            case "py":
            case "go":
            case "c":
            case "cpp":
            case "h":
            case "sql":
            case "json":
            case "xml":
            case "html":
            case "css":
            case "scss":
                return new int[]{5, 8};
            case "zip":
            case "rar":
            case "7z":
            case "tar":
            case "gz":
                return new int[]{5, 9};
            default:
                return new int[]{5, 10};
        }
    }
}
