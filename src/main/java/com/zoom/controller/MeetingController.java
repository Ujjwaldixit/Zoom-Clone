package com.zoom.controller;

import com.zoom.helper.PasswordGenerator;
import com.zoom.model.Meeting;
import com.zoom.service.MeetingService;
import com.zoom.service.impl.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


@Controller
public class MeetingController {

    @Autowired
    private PasswordGenerator passwordGenerator;

    @Autowired
    private MeetingService meetingService;

    @GetMapping("/scheduleMeeting")
    public String meeting(Model model) {
        Meeting meeting = new Meeting();

        meeting.setPassCode(passwordGenerator.generateStrongPassword());
        model.addAttribute("meeting", meeting);

        return "schedule";
    }

    @PostMapping("/scheduleMeeting")
    public String saveMeetingSchedule(@AuthenticationPrincipal UserDetailsImpl user,
                                      @ModelAttribute("meeting") Meeting meeting,
                                      @RequestParam("startDate") String startDate,
                                      @RequestParam("startTime") String startTime) throws ParseException {

        LocalDate localDate=LocalDate.parse(startDate);

        LocalTime localTime=LocalTime.parse(startTime);

        String formattedDate = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        localDate=LocalDate.parse(formattedDate);

        LocalDateTime localDateTime = localTime.atDate(localDate);
        meeting.setStartDateTime(Timestamp.valueOf(localDateTime));
        meeting.setMeetingHostId(user.getUser().getUserId());
        meetingService.saveMeetingSchedule(meeting);

        return "/dashboard";
    }
}