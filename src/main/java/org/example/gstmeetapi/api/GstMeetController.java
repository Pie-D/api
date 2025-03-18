package org.example.gstmeetapi.api;

import org.example.gstmeetapi.configuration.MeetingWebSocketHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.*;
import java.util.Base64;

@RestController
@RequestMapping("/gst")
public class GstMeetController {
    private MeetingWebSocketHandler meetingWebSocketHandler;
    private Process gstMeetProcess = null;
    private static final String SAVE_PATH = "/app/video/";

    @GetMapping("/start/{room_id}/{format}")
    public String startGstMeet(@PathVariable String room_id, @PathVariable String format) {
        if (gstMeetProcess != null) {
            return "gst-meet is already running!";
        }

        String command = String.format(
                "gst-meet --web-socket-url=wss://meet.cmcati.vn/xmpp-websocket " +
                        "--room-name=%s --xmpp-domain=meet.jitsi " +
                        "--recv-pipeline-participant-template=\"videoconvert name=video ! videorate ! video/x-raw,format=RGB,width=1280,height=720,framerate=1/30 ! queue ! %senc ! multifilesink location=%s{nick}_%%05d.%s\"",
                room_id, format, SAVE_PATH, format
        );

        try {
            ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
            pb.redirectErrorStream(true);
            gstMeetProcess = pb.start();

            // Theo dõi thư mục để gửi ảnh qua WS
            new Thread(() -> watchForNewImages(format)).start();

            return "gst-meet started!";
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to start gst-meet!";
        }
    }

    @GetMapping("/stop")
    public String stopGstMeet() {
        if (gstMeetProcess == null) {
            return "gst-meet is not running!";
        }
        gstMeetProcess.destroy();
        gstMeetProcess = null;
        return "gst-meet stopped!";
    }

    private void watchForNewImages(String format) {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            Path path = Paths.get(SAVE_PATH);
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

            while (gstMeetProcess != null) {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    Path filePath = path.resolve((Path) event.context());
                    if (filePath.toString().endsWith(format)) {
                        sendImageOverWS(filePath.toString());
                    }
                }
                key.reset();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendImageOverWS(String imagePath) {
        try {
            byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            String message = "{\"image\":\"" + base64Image + "\"}";
            meetingWebSocketHandler.sendImageToMeeting("1",imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}