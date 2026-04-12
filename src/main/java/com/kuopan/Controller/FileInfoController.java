package com.kuopan.Controller;

import com.kuopan.Annotation.GlobalInterceptor;
import com.kuopan.Entity.dto.SessionWebUserDto;
import com.kuopan.Service.impl.FileInfoServiceImpl;
import com.kuopan.vo.ResponseVO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController("fileInfoController")
@RequestMapping("/file")
public class FileInfoController extends BaseController {

    @Resource
    private FileInfoServiceImpl fileInfoService;

    @RequestMapping("/upload")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO upload(HttpSession session, String filePid, MultipartFile file) {
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
        Map<String, Object> uploadResult = fileInfoService.uploadFile(sessionWebUserDto.getUserId(), filePid, file);
        return ResponseVO.success(uploadResult);
    }

    @RequestMapping("/list")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO list(HttpSession session, String filePid, String fileNameFuzzy, String category) {
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
        List<Map<String, Object>> list = fileInfoService.listUserFiles(sessionWebUserDto.getUserId(), filePid, fileNameFuzzy, category);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        return ResponseVO.success(data);
    }

    @RequestMapping("/uploadChunk")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO uploadChunk(HttpSession session, String fileMd5, Integer chunkIndex, Integer chunks, MultipartFile chunk) {
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
        Map<String, Object> r = fileInfoService.uploadChunk(sessionWebUserDto.getUserId(), fileMd5, chunkIndex, chunks, chunk);
        return ResponseVO.success(r);
    }

    @RequestMapping("/mergeChunk")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO mergeChunk(HttpSession session, String filePid, String fileMd5, String fileName, Long fileSize, Integer chunks) {
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
        Map<String, Object> r = fileInfoService.mergeChunks(sessionWebUserDto.getUserId(), filePid, fileMd5, fileName, fileSize, chunks);
        return ResponseVO.success(r);
    }

    @RequestMapping("/checkMd5")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO checkMd5(HttpSession session, String fileMd5, Long fileSize) {
        boolean exists = fileInfoService.checkFileExists(fileMd5, fileSize);
        Map<String, Object> data = new HashMap<>();
        data.put("exists", exists);
        return ResponseVO.success(data);
    }

    @RequestMapping("/newFolder")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO newFolder(HttpSession session, String filePid, String fileName) {
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
        Map<String, Object> result = fileInfoService.newFolder(sessionWebUserDto.getUserId(), filePid, fileName);
        return ResponseVO.success(result);
    }

    @RequestMapping("/delFile")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO delFile(HttpSession session, String fileIds) {
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
        fileInfoService.deleteFile(sessionWebUserDto.getUserId(), fileIds);
        return ResponseVO.success(null);
    }
}