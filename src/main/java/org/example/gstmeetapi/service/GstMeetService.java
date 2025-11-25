package org.example.gstmeetapi.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.gstmeetapi.helper.response.ResponseObject;
import org.example.gstmeetapi.helper.response.entity.HttpStatusHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GstMeetService {
    MinioService minioService;

    public ResponseObject<String> stopProcess(Map<String, Process> processMap, String roomId, String type){
        switch (type) {
            case "checkIn":{
                String keyProcess = roomId + "_checkIn";
                Process process = processMap.get(keyProcess);
                if (process != null && process.isAlive()) {
                    process.destroy();
                    processMap.remove(keyProcess); // Xóa tiến trình sau khi dừng
                    System.out.println("CheckIn stopped for room:" + roomId + "and uploaded screenshots to MinIO!");
                    try {
                        File roomDir = new File("/gstmeet/" + roomId);
                        if (roomDir.exists() && roomDir.isDirectory()) {
                            File[] nickDirs = roomDir.listFiles(File::isDirectory);
                            if (nickDirs != null) {
                                for (File nickDir : nickDirs) {
                                    String nick = nickDir.getName();
                                    minioService.uploadDirectory(roomId, nick, nickDir.getAbsolutePath());
                                    deleteFolder(nickDir);
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        return new ResponseObject<>(HttpStatusHelper.INTERNAL_SERVER_ERROR,"CheckIn stopped, but failed to upload screenshots!");
                    }
                    return new ResponseObject<>(HttpStatusHelper.SUCCESS,"CheckIn stopped for room: " + roomId + " and uploaded screenshots to MinIO!");
                }
                System.out.println("checkIn is not running for room: " + roomId);
                return  new ResponseObject<>(HttpStatusHelper.NOTFOUND,"checkIn is not running for room: " + roomId);
            }
            case "whip":{
                String keyProcess = roomId + "_whip";
                Process process = processMap.get(keyProcess);
                if (process != null && process.isAlive()) {
                    process.destroy();
                    processMap.remove(keyProcess); // Xóa tiến trình sau khi dừng
                    System.out.println("connecting whip stopped for room: " + roomId);
                    return new ResponseObject<>(HttpStatusHelper.SUCCESS,"connecting whip stopped for room: " + roomId) ;
                }
                System.out.println("whip-connect is not running for room: " + roomId);
                return new ResponseObject<>(HttpStatusHelper.NOTFOUND,"whip-connect is not running for room: " + roomId);
            }
            case "audio":{
                String keyProcess = roomId + "_voice";
                Process process = processMap.get(keyProcess);
                if (process != null && process.isAlive()) {
                    process.destroy();
                    processMap.remove(keyProcess); // Xóa tiến trình sau khi dừng
                    try {
                        File audiosDir = new File("/audios/");
                        if (audiosDir.exists() && audiosDir.isDirectory()) {
                            File[] files = audiosDir.listFiles();
                            if (files == null || files.length == 0) {
                                return new ResponseObject<>(HttpStatusHelper.NOTFOUND,"No files to upload in: " + audiosDir);
                            }
                            String minioPath = "audios-recording/";
                            for(File file : audiosDir.listFiles()) {
                                minioService.uploadFile(minioPath + file.getName(),file);
                                deleteFilesOnly(audiosDir);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        return new ResponseObject<>(HttpStatusHelper.INTERNAL_SERVER_ERROR,"recording audio stopped, but failed to upload audio!");
                    }
                    System.out.println("recording audio stopped for room: " + roomId + " and uploaded audio to MinIO!");
                    return new ResponseObject<>(HttpStatusHelper.SUCCESS,"recording audio stopped for room: " + roomId + " and uploaded audio to MinIO!");
                }
                System.out.println("audio-connect is not running for room: " + roomId);
                return new ResponseObject<>(HttpStatusHelper.NOTFOUND,"recording audio is not running for room: " + roomId);
            }
        }
        return new ResponseObject<>(HttpStatusHelper.NOTFOUND,"Type is valid: " + type);
    }

    private void deleteFolder(File folder) throws IOException {
        Files.walk(folder.toPath())
                .sorted((p1, p2) -> p2.compareTo(p1)) // Xóa file trước, thư mục sau
                .map(Path::toFile)
                .forEach(File::delete);
    }
    private void deleteFilesOnly(File folder) throws IOException {
        Files.walk(folder.toPath())
                .filter(Files::isRegularFile) // Chỉ lấy file, bỏ qua thư mục
                .map(Path::toFile)
                .forEach(File::delete);
    }
}
