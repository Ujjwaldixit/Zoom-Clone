package com.zoom.controller;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;

import com.zoom.model.Meeting;
import com.zoom.service.MeetingService;
import com.zoom.service.impl.UserDetailsImpl;
import io.openvidu.java.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SessionController {

    @Autowired
    private MeetingService meetingService;

    private OpenVidu openVidu;

    private Map<String, Session> mapSessions = new ConcurrentHashMap<>();

    private Map<String, Map<String, OpenViduRole>> mapSessionNamesTokens = new ConcurrentHashMap<>();

    private String OPENVIDU_URL;

    private String SECRET;

    private Session session;

    public SessionController(@Value("${openvidu.secret}") String secret, @Value("${openvidu.url}") String openviduUrl) {
        this.SECRET = secret;
        this.OPENVIDU_URL = openviduUrl;
        this.openVidu = new OpenVidu(OPENVIDU_URL, SECRET);
    }

    @RequestMapping(value = "/session", method = RequestMethod.POST)
    public String joinSession(@AuthenticationPrincipal UserDetailsImpl user,
                              @RequestParam(name = "data") String clientData,
                              @RequestParam(name = "session-name") String sessionName,
                              @RequestParam(name = "password") String password,
                              Model model,
                              HttpSession httpSession,
                              RedirectAttributes redirectAttributes) {

        if (user == null) {
            return "index";
        }

        OpenViduRole role = OpenViduRole.PUBLISHER;

        String serverData = "{\"serverData\": \"" + httpSession.getAttribute("loggedUser") + "\"}";

        ConnectionProperties connectionProperties = new ConnectionProperties
                .Builder()
                .type(ConnectionType.WEBRTC)
                .role(role).data(serverData).build();

        try {
            Meeting meeting = meetingService.getMeetingByMeetingId(Long.parseLong(sessionName));

            if (meeting != null) {
                if (!meeting.getPassCode().equals(password)) {
                    redirectAttributes.addFlashAttribute("error", "!!!Wrong Password!!!");
                    model.addAttribute("username", httpSession.getAttribute("loggedUser"));
                    return "redirect:/dashboard";
                }

                sessionName = meeting.getMeetingId().toString();

                if (meeting.getStartDateTime().compareTo(new Timestamp(System.currentTimeMillis())) > 0) {
                    redirectAttributes.addFlashAttribute("error", "!!!Meeting Has Not Started Yet!!!");
                    model.addAttribute("username", httpSession.getAttribute("loggedUser"));
                    return "redirect:/dashboard";
                }

                if (this.mapSessions.get(sessionName) != null) {

                    try {
                        String token = this.mapSessions.get(sessionName).createConnection(connectionProperties).getToken();

                        this.mapSessionNamesTokens.get(sessionName).put(token, role);

                        // Add all the needed attributes to the template
                        model.addAttribute("sessionId", session.getSessionId());
                        model.addAttribute("sessionName", sessionName);
                        model.addAttribute("token", token);
                        model.addAttribute("nickName", clientData);
                        model.addAttribute("userName", httpSession.getAttribute("loggedUser"));

                        return "session";

                    } catch (Exception e) {

                        model.addAttribute("username", httpSession.getAttribute("loggedUser"));
                        return "dashboard";
                    }
                } else {
                    try {
                        session = this.openVidu.createSession();
                        // Create a new OpenVidu Session
                        RecordingProperties recordingProperties = new RecordingProperties.Builder()
                                .outputMode(Recording.OutputMode.COMPOSED)
                                .resolution("640x480")
                                .frameRate(24)
                                .build();
                        SessionProperties sessionProperties = new SessionProperties.Builder()
                                .recordingMode(RecordingMode.MANUAL) // RecordingMode.ALWAYS for automatic recording
                                .defaultRecordingProperties(recordingProperties)
                                .build();

                        Session session = this.openVidu.createSession(sessionProperties);
                        // Generate a new token with the recently created connectionProperties
                        String token = session.createConnection(connectionProperties).getToken();

                        this.mapSessions.put(sessionName, session);
                        this.mapSessionNamesTokens.put(sessionName, new ConcurrentHashMap<>());
                        this.mapSessionNamesTokens.get(sessionName).put(token, role);

                        // Add all the needed attributes to the template
                        model.addAttribute("sessionId", session.getSessionId());
                        model.addAttribute("sessionName", sessionName);
                        model.addAttribute("token", token);
                        model.addAttribute("nickName", clientData);
                        model.addAttribute("userName", httpSession.getAttribute("loggedUser"));

                        return "session";

                    } catch (Exception e) {
                        model.addAttribute("username", httpSession.getAttribute("loggedUser"));
                        return "dashboard";
                    }
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "!!!Invalid Session Id!!!");
                model.addAttribute("username", httpSession.getAttribute("loggedUser"));
                return "redirect:/dashboard";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "!!!Invalid Session Id!!!");
            model.addAttribute("username", httpSession.getAttribute("loggedUser"));
            return "redirect:/dashboard";
        }
    }

    @RequestMapping(value = "/leave-session", method = RequestMethod.POST)
    public String removeUser(@RequestParam(name = "session-name") String sessionName,
                             @RequestParam(name = "token") String token, Model model, HttpSession httpSession) throws Exception {

        try {
            checkUserLogged(httpSession);
        } catch (Exception e) {
            return "index";
        }

        if (this.mapSessions.get(sessionName) != null && this.mapSessionNamesTokens.get(sessionName) != null) {

            if (this.mapSessionNamesTokens.get(sessionName).remove(token) != null) {
                if (this.mapSessionNamesTokens.get(sessionName).isEmpty()) {
                    this.mapSessions.remove(sessionName);
                }
                return "redirect:/dashboard";

            } else {
                return "redirect:/dashboard";
            }

        } else {
            return "redirect:/dashboard";
        }
    }

    void recording(String OPENVIDU_URL, String OPENVIDU_SECRET) throws OpenViduJavaClientException, OpenViduHttpException {
        OpenVidu openvidu = new OpenVidu(OPENVIDU_URL, OPENVIDU_SECRET);
        RecordingProperties recordingProperties = new RecordingProperties.Builder()
                .outputMode(Recording.OutputMode.COMPOSED)
                .resolution("640x480")
                .frameRate(24)
                .build();
        SessionProperties sessionProperties = new SessionProperties.Builder()
                .recordingMode(RecordingMode.ALWAYS) // RecordingMode.ALWAYS for automatic recording
                .defaultRecordingProperties(recordingProperties)
                .build();
        Session session = openVidu.createSession(sessionProperties);
    }

    private void checkUserLogged(HttpSession httpSession) throws Exception {
        if (httpSession == null || httpSession.getAttribute("loggedUser") == null) {
            throw new Exception("User not logged");
        }
    }
}