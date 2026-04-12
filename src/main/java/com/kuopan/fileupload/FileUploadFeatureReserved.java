package com.kuopan.fileupload;

/**
 * <ul>
 *     <li>秒传：配合 file_md5 + 服务端已存在文件校验</li>
 *     <li>分片上传与合并：独立 chunk 接口与临时目录，合并后写入 file_info</li>
 *     <li>视频切片：上传/转码队列，更新 status 与 file_path / 封面字段</li>
 *     <li>缩略图：写入 file_cover</li>
 *     <li>在线预览：独立 preview 接口，可结合 file_type</li>
 * </ul>
 * 当前 MVP 仅使用 {@code /file/upload} 整文件上传。
 */
public final class FileUploadFeatureReserved {

    private FileUploadFeatureReserved() {
    }
}
