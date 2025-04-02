package org.example.gstmeetapi.api;

import org.example.gstmeetapi.service.MinioService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/gst-meet")
public class Api {
    private final MinioService minioService;
    private final Map<String, Process> processMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    public Api(MinioService minioService) {
        this.minioService = minioService;
    }

    @PostMapping("/check-in")
    public String startCheckInGstMeet(@RequestParam(defaultValue = "30") int durationSeconds, @RequestParam String roomId, @RequestParam String domain, @RequestParam String framerate, @RequestParam(defaultValue = "720") String height, @RequestParam(defaultValue = "1280") String width) {
        String keyProcess = roomId + "_checkIn";
        if (processMap.containsKey(keyProcess) && processMap.get(keyProcess).isAlive()) {
            return "gst-meet is already running for room: " + roomId;
        }

        try {
            Path baseDir = Paths.get("/participants");
            if (!Files.exists(baseDir)) {
                Files.createDirectories(baseDir);
            }
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "gst-meet",
                    "--web-socket-url=wss://" + domain + "/xmpp-websocket",
                    "--room-name=" + roomId,
                    "--xmpp-domain=meet.jitsi",
                    "--recv-pipeline-participant-template=videoconvert name=video ! videorate ! video/x-raw,format=RGB,width=" + width + ",height="+ height + ",framerate=" + framerate +
                            "! queue max-size-buffers=0 max-size-time=0 max-size-bytes=0 leaky=2 ! pngenc ! identity sync=false " +
                            "! multifilesink location=/participants/{nick}/img_%05d.png sync=false async=false " +
                            "audioconvert name=audio ! queue max-size-buffers=0 max-size-time=0 max-size-bytes=0 leaky=2 ! fakesink sync=false async=false"
            );




            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

            Process gstProcess = processBuilder.start();
            processMap.put(keyProcess, gstProcess);  // Lưu tiến trình theo roomId

            // Lên lịch dừng tiến trình sau durationSeconds giây
            scheduler.schedule(() -> stopCheckInGstMeet(roomId), durationSeconds, TimeUnit.SECONDS);

            return "Checkin started successfully for room: " + roomId;
        } catch (IOException e) {
            e.printStackTrace();
            return "Error starting checkin: " + e.getMessage();
        }
    }
    @PostMapping("/record-audio")
    public String startRecordVoiceGstMeet(@RequestParam(defaultValue = "-1") int durationSeconds, @RequestParam String roomId, @RequestParam String domain) {
        String keyProcess = roomId + "_voice";
        if (processMap.containsKey(keyProcess) && processMap.get(keyProcess).isAlive()) {
            return "gst-meet is already running for room: " + roomId;
        }

        try {
            Path baseDir = Paths.get("/audios");
            if (!Files.exists(baseDir)) {
                Files.createDirectories(baseDir);
            }
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "gst-meet",
                    "--web-socket-url=wss://" + domain + "/xmpp-websocket",
                    "--room-name=" + roomId,
                    "--xmpp-domain=meet.jitsi",
                    "--recv-pipeline=audiomixer name=audio ! audioconvert ! audioresample ! wavenc ! filesink location=/audios/"+ roomId + ".wav"
            );




            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

            Process gstProcess = processBuilder.start();
            processMap.put(keyProcess, gstProcess);  // Lưu tiến trình theo roomId

            // Lên lịch dừng tiến trình sau durationSeconds giây
            if(durationSeconds != -1 && durationSeconds >= 0){
                scheduler.schedule(() -> stopRecordVoiceGstMeet(roomId), durationSeconds, TimeUnit.SECONDS);
            }

            return "starting record voice in: " + roomId;
        } catch (IOException e) {
            e.printStackTrace();
            return "Error starting record voice: " + e.getMessage();
        }
    }
    @PostMapping("/speech-to-text")
    public String startSpeedToText(@RequestParam(defaultValue = "30") int durationSeconds, @RequestParam String roomId, @RequestParam String domain, @RequestParam String whipEndpoint) {
        String keyProcess = roomId + "_stt";
        if (processMap.containsKey(keyProcess) && processMap.get(keyProcess).isAlive()) {
            return "gst-meet is already running for room: " + roomId;
        }

        try {
            Path baseDir = Paths.get("/participants");
            if (!Files.exists(baseDir)) {
                Files.createDirectories(baseDir);
            }
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "gst-meet",
                    "--web-socket-url=wss://" + domain + "/xmpp-websocket",
                    "--room-name=" + roomId,
                    "--xmpp-domain=meet.jitsi",
                    "--recv-pipeline=audiomixer name=audio ! audioconvert ! audioresample ! opusenc ! rtpopuspay ! rtpopusdepay ! opusparse ! whipclientsink name=ws signaller::whip-endpoint=" + whipEndpoint


            );




            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

            Process gstProcess = processBuilder.start();
            processMap.put(keyProcess, gstProcess);  // Lưu tiến trình theo roomId

            // Lên lịch dừng tiến trình sau durationSeconds giây
            scheduler.schedule(() -> stopCheckInGstMeet(roomId), durationSeconds, TimeUnit.SECONDS);

            return "Checkin started successfully for room: " + roomId;
        } catch (IOException e) {
            e.printStackTrace();
            return "Error starting checkin: " + e.getMessage();
        }
    }
    @PostMapping("check-in/stop")
    public String stopCheckInGstMeet(@RequestParam String roomId) {
        String keyProcess = roomId + "_checkIn";
        Process process = processMap.get(keyProcess);
        if (process != null && process.isAlive()) {
            process.destroy();
            processMap.remove(roomId); // Xóa tiến trình sau khi dừng
            try {
                File roomDir = new File("/participants/");
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
                return "gst-meet stopped, but failed to upload screenshots!";
            }
            return "gst-meet stopped for room: " + roomId + " and uploaded screenshots to MinIO!";
        }
        return "gst-meet is not running for room: " + roomId;
    }
    @PostMapping("check-in/stop")
    public String stopRecordVoiceGstMeet(@RequestParam String roomId) {
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
                        return "No files to upload in: " + audiosDir;
                    }
                    String minioPath = "audios-recording/";
                    for(File file : audiosDir.listFiles()) {
                        minioService.uploadFile(minioPath + file.getName(),file);
                        deleteFilesOnly(audiosDir);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "recording audio stopped, but failed to upload audio!";
            }
            return "recording audio stopped for room: " + roomId + " and uploaded audio to MinIO!";
        }
        return "recording audio is not running for room: " + roomId;
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