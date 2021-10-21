package com.zoom.controller;

import java.sql.Timestamp;
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

    // OpenVidu object as entrypoint of the SDK
    private OpenVidu openVidu;

    // Collection to pair session names and OpenVidu Session objects
    private Map<String, Session> mapSessions = new ConcurrentHashMap<>();
    // Collection to pair session names and tokens (the inner Map pairs tokens and
    // role associated)
    private Map<String, Map<String, OpenViduRole>> mapSessionNamesTokens = new ConcurrentHashMap<>();

    // URL where our OpenVidu server is listening
    private String OPENVIDU_URL;
    // Secret shared with our OpenVidu server
    private String SECRET;

    public SessionController(@Value("${openvidu.secret}") String secret, @Value("${openvidu.url}") String openviduUrl) {
        this.SECRET = secret;
        this.OPENVIDU_URL = openviduUrl;
        this.openVidu = new OpenVidu(OPENVIDU_URL, SECRET);
    }

    @RequestMapping(value = "/session", method = RequestMethod.POST)
    public String joinSession(@AuthenticationPrincipal UserDetailsImpl user,
                              @RequestParam(name = "data") String clientData,
                              @RequestParam(name = "session-name") String sessionName,
                              @RequestParam(name="password") String password,
                              Model model,
                              HttpSession httpSession,
                              RedirectAttributes redirectAttributes) {

        if(user==null) {
            return "index";
        }

        System.out.println("Getting sessionId and token | {sessionName}={" + sessionName + "}");

        // Role associated to this user
        System.out.println("before session");
        System.out.println("session value "+httpSession.getAttribute("loggedUser") );
        //OpenViduRole role = UserController.users.get(httpSession.getAttribute("loggedUser")).role;
         OpenViduRole role=OpenViduRole.PUBLISHER;
        // Optional data to be passed to other users when this user connects to the
        // video-call. In this case, a JSON with the value we stored in the HttpSession
        // object on login

        String serverData = "{\"serverData\": \"" + httpSession.getAttribute("loggedUser") + "\"}";

        // Build connectionProperties object with the serverData and the role
        ConnectionProperties connectionProperties = new ConnectionProperties.Builder().type(ConnectionType.WEBRTC)
                .role(role).data(serverData).build();

        try {
            Meeting meeting = meetingService.getMeetingByMeetingId(Long.parseLong(sessionName));

            if (meeting != null) {
                if(!meeting.getPassCode().equals(password)){
                    redirectAttributes.addFlashAttribute("error", "!!!Wrong Password!!!");
                    model.addAttribute("username", httpSession.getAttribute("loggedUser"));
                    return "redirect:/dashboard";
                }
                sessionName = meeting.getMeetingId().toString();
                System.out.println(" check time " + meeting.getStartDateTime().compareTo(new Timestamp(System.currentTimeMillis())));

//                if (meeting.getStartDateTime().compareTo(new Timestamp(System.currentTimeMillis())) > 0) {
//                    redirectAttributes.addFlashAttribute("error", "!!!Meeting Has Not Started Yet!!!");
//                    model.addAttribute("username", httpSession.getAttribute("loggedUser"));
//                    return "redirect:/dashboard";
//                }

                if (this.mapSessions.get(sessionName) != null) {
                    // Session already exists
                    System.out.println("Existing session " + sessionName);
                    try {

                        // Generate a new token with the recently created connectionProperties
                        String token = this.mapSessions.get(sessionName).createConnection(connectionProperties).getToken();

                        // Update our collection storing the new token
                        this.mapSessionNamesTokens.get(sessionName).put(token, role);

                        // Add all the needed attributes to the template
                        model.addAttribute("sessionName", sessionName);
                        model.addAttribute("token", token);
                        model.addAttribute("nickName", clientData);
                        model.addAttribute("userName", httpSession.getAttribute("loggedUser"));

                        // Return session.html template
                        return "session";

                    } catch (Exception e) {
                        // If error just return dashboard.html template
                        model.addAttribute("username", httpSession.getAttribute("loggedUser"));
                        return "dashboard";
                    }
                }
                else {
                    // New session
                    System.out.println("New session " + sessionName);
                    try {

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

                        // Store the session and the token in our collections
                        this.mapSessions.put(sessionName, session);
                        this.mapSessionNamesTokens.put(sessionName, new ConcurrentHashMap<>());
                        this.mapSessionNamesTokens.get(sessionName).put(token, role);

                        // Add all the needed attributes to the template
                        model.addAttribute("sessionId", session.getSessionId());
                        model.addAttribute("sessionName", sessionName);
                        model.addAttribute("token", token);
                        model.addAttribute("nickName", clientData);
                        model.addAttribute("userName", httpSession.getAttribute("loggedUser"));

                        // Return session.html template
                        return "session";

                    } catch (Exception e) {
                        // If error just return dashboard.html template
                        model.addAttribute("username", httpSession.getAttribute("loggedUser"));
                        return "dashboard";
                    }
                }
            }
            else {
                // New session
                System.out.println("New session " + sessionName);
                try {
                    RecordingProperties recordingProperties = new RecordingProperties.Builder()
                            .outputMode(Recording.OutputMode.COMPOSED)
                            .resolution("640x480")
                            .frameRate(12)
                            .build();
                    SessionProperties sessionProperties = new SessionProperties.Builder()
                            .recordingMode(RecordingMode.MANUAL) // RecordingMode.ALWAYS for automatic recording
                            .defaultRecordingProperties(recordingProperties)
                            .build();
                    // Create a new OpenVidu Session
                    Session session = this.openVidu.createSession(sessionProperties);
                    // Generate a new token with the recently created connectionProperties
                    String token = session.createConnection(connectionProperties).getToken();

                    // Store the session and the token in our collections
                    this.mapSessions.put(sessionName, session);
                    this.mapSessionNamesTokens.put(sessionName, new ConcurrentHashMap<>());
                    this.mapSessionNamesTokens.get(sessionName).put(token, role);

                    // Add all the needed attributes to the template
                    model.addAttribute("sessionId", session.getSessionId());
                    model.addAttribute("sessionName", sessionName);
                    model.addAttribute("token", token);
                    model.addAttribute("nickName", clientData);
                    model.addAttribute("userName", httpSession.getAttribute("loggedUser"));

                    // Return session.html template
                    return "session";

                } catch (Exception e) {
                    // If error just return dashboard.html template
                    model.addAttribute("username", httpSession.getAttribute("loggedUser"));
                    return "dashboard";
                }
            }
        }catch (Exception e)
        {
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
        System.out.println("Removing user | sessioName=" + sessionName + ", token=" + token);

        // If the session exists ("TUTORIAL" in this case)
        if (this.mapSessions.get(sessionName) != null && this.mapSessionNamesTokens.get(sessionName) != null) {

            // If the token exists
            if (this.mapSessionNamesTokens.get(sessionName).remove(token) != null) {
                // User left the session
                if (this.mapSessionNamesTokens.get(sessionName).isEmpty()) {
                    // Last user left: session must be removed
                    this.mapSessions.remove(sessionName);
                }
                return "redirect:/dashboard";

            } else {
                // The TOKEN wasn't valid
                System.out.println("Problems in the app server: the TOKEN wasn't valid");
                return "redirect:/dashboard";
            }

        } else {
            // The SESSION does not exist
            System.out.println("Problems in the app server: the SESSION does not exist");
            return "redirect:/dashboard";
        }
    }



    private void checkUserLogged(HttpSession httpSession) throws Exception {
        if (httpSession == null || httpSession.getAttribute("loggedUser") == null) {
            throw new Exception("User not logged");
        }
    }


//    @RequestMapping(value = "/startRecording", method = RequestMethod.POST)
//    public String startRecording(@RequestParam(name = "session-name") String sessionName,
//                             @RequestParam(name = "token") String token, Model model, HttpSession httpSession) throws Exception {
//
//        try {
//            checkUserLogged(httpSession);
//        } catch (Exception e) {
//            return "index";
//        }
//        // If the session exists ("TUTORIAL" in this case)
//        if (this.mapSessions.get(sessionName) != null && this.mapSessionNamesTokens.get(sessionName) != null) {
//            Session session = this.mapSessions.get(sessionName);
//            RecordingProperties properties = new RecordingProperties.Builder()
//                    .name("MY_FIRST_RECORDING")
//                    .build();
//            Recording recording = openVidu.startRecording(session.getSessionId(), properties);
//        }
//        return "redirect:/session";
//    }
}
