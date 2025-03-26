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

    @PostMapping("/start")
    public String startGstMeet(@RequestParam(defaultValue = "60") int durationSeconds, @RequestParam String roomId) {
        if (processMap.containsKey(roomId) && processMap.get(roomId).isAlive()) {
            return "gst-meet is already running for room: " + roomId;
        }

        try {
            Path baseDir = Paths.get("/participants");
            if (!Files.exists(baseDir)) {
                Files.createDirectories(baseDir);
            }
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "gst-meet",
                    "--web-socket-url=wss://meet.cmcati.vn/xmpp-websocket",
                    "--room-name=" + roomId,
                    "--xmpp-domain=meet.jitsi",
                    "--recv-pipeline-participant-template=videoconvert name=video ! videorate ! video/x-raw,format=RGB,width=1280,height=720,framerate=1/30 " +
                            "! queue max-size-buffers=0 max-size-time=0 max-size-bytes=0 leaky=2 ! pngenc ! identity sync=false " +
                            "! multifilesink location=/participants/{nick}/img_%05d.png sync=false async=false " +
                            "audioconvert name=audio ! queue max-size-buffers=0 max-size-time=0 max-size-bytes=0 leaky=2 ! fakesink sync=false async=false"
            );




            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

            Process gstProcess = processBuilder.start();
            processMap.put(roomId, gstProcess);  // Lưu tiến trình theo roomId

            // Lên lịch dừng tiến trình sau durationSeconds giây
            scheduler.schedule(() -> stopGstMeet(roomId), durationSeconds, TimeUnit.SECONDS);

            return "gst-meet started successfully for room: " + roomId;
        } catch (IOException e) {
            e.printStackTrace();
            return "Error starting gst-meet: " + e.getMessage();
        }
    }

    @PostMapping("/stop")
    public String stopGstMeet(@RequestParam String roomId) {
        Process process = processMap.get(roomId);
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

    private void deleteFolder(File folder) throws IOException {
        Files.walk(folder.toPath())
                .sorted((p1, p2) -> p2.compareTo(p1)) // Xóa file trước, thư mục sau
                .map(Path::toFile)
                .forEach(File::delete);
    }

}