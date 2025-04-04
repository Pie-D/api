package org.example.gstmeetapi.api;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.gstmeetapi.service.GstMeetService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequestMapping("/gst-meet")
public class Api {
    Map<String, Process> processMap = new ConcurrentHashMap<>();
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    GstMeetService gstMeetService;

    @PostMapping("/check-in")
    public String startCheckInGstMeet(@RequestParam(defaultValue = "30") int durationSeconds, @RequestParam String roomId, @RequestParam String domain, @RequestParam String framerate, @RequestParam(defaultValue = "720") String height, @RequestParam(defaultValue = "1280") String width, @RequestParam String xmppDomain) {
        String keyProcess = roomId + "_checkIn";
        if (processMap.containsKey(keyProcess) && processMap.get(keyProcess).isAlive()) {
            return "gst-meet is already running for room: " + roomId;
        }

        try {
            Path baseDir = Paths.get("/participants/" + roomId);
            if (!Files.exists(baseDir)) {
                Files.createDirectories(baseDir);
            }
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "gst-meet",
                    "--web-socket-url=wss://" + domain + "/xmpp-websocket",
                    "--room-name=" + roomId,
                    "--xmpp-domain=" + xmppDomain,
                    "--recv-pipeline-participant-template=videoconvert name=video ! videorate ! video/x-raw,format=RGB,width=" + width + ",height="+ height + ",framerate=" + framerate +
                            "! queue max-size-buffers=0 max-size-time=0 max-size-bytes=0 leaky=2 ! pngenc ! identity sync=false " +
                            "! multifilesink location=/participants/" + roomId + "/{nick}/img_%05d.png sync=false async=false " +
                            "audioconvert name=audio ! queue max-size-buffers=0 max-size-time=0 max-size-bytes=0 leaky=2 ! fakesink sync=false async=false"
            );




            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

            Process gstProcess = processBuilder.start();
            processMap.put(keyProcess, gstProcess);  // Lưu tiến trình theo roomId

            scheduler.schedule(() -> gstMeetService.stopProcess(processMap,roomId,"checkIn"), durationSeconds, TimeUnit.SECONDS);

            return "Checkin started successfully for room: " + roomId;
        } catch (IOException e) {
            e.printStackTrace();
            return "Error starting checkin: " + e.getMessage();
        }
    }
    @PostMapping("/record-audio")
    public String startRecordVoiceGstMeet(@RequestParam String roomId, @RequestParam String domain, @RequestParam String xmppDomain) {
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
                    "--xmpp-domain=" + xmppDomain,
                    "--recv-pipeline=audiomixer name=audio ! audioconvert ! audioresample ! wavenc ! filesink location=/audios/"+ roomId + ".wav"
            );




            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

            Process gstProcess = processBuilder.start();
            processMap.put(keyProcess, gstProcess);  // Lưu tiến trình theo roomId
            return "starting record voice in: " + roomId;
        } catch (IOException e) {
            e.printStackTrace();
            return "Error starting record voice: " + e.getMessage();
        }
    }
    @PostMapping("/whip-connect")
    public String startSpeedToText(@RequestParam String roomId, @RequestParam String domain, @RequestParam String whipEndpoint, @RequestParam String xmppDomain) {
        String keyProcess = roomId + "_whip";
        if (processMap.containsKey(keyProcess) && processMap.get(keyProcess).isAlive()) {
            return "gst-meet is already running for room: " + roomId;
        }

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "gst-meet",
                    "--web-socket-url=wss://" + domain + "/xmpp-websocket",
                    "--room-name=" + roomId,
                    "--xmpp-domain=" + xmppDomain,
                    "--recv-pipeline=audiomixer name=audio ! audioconvert ! audioresample ! opusenc ! rtpopuspay ! rtpopusdepay ! opusparse ! whipclientsink name=ws signaller::whip-endpoint=" + whipEndpoint
            );

            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

            Process gstProcess = processBuilder.start();
            processMap.put(keyProcess, gstProcess);  // Lưu tiến trình theo roomId

            return "Checkin started successfully for room: " + roomId;
        } catch (IOException e) {
            e.printStackTrace();
            return "Error starting checkin: " + e.getMessage();
        }
    }
    @PostMapping("check-in/stop")
    public String stopCheckInGstMeet(@RequestParam String roomId) {
        return gstMeetService.stopProcess(processMap,roomId,"checkIn");
    }
    @PostMapping("record-audio/stop")
    public String stopRecordVoiceGstMeet(@RequestParam String roomId) {
        return gstMeetService.stopProcess(processMap,roomId,"audio");
    }
    @PostMapping("whip-connect/stop")
    public String stopWhipConnectGstMeet(@RequestParam String roomId) {
        return gstMeetService.stopProcess(processMap,roomId,"whip");
    }

}