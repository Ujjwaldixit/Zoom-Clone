package com.zoom.controller;

import com.zoom.helper.PasswordGenerator;
import com.zoom.helper.SessionTokenGenerator;
import com.zoom.model.Meeting;
import com.zoom.service.MeetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    public String saveMeetingSchedule(@ModelAttribute("meeting") Meeting meeting,
                                      @RequestParam("startDate") String startDate,
                                      @RequestParam("startTime") String startTime) throws ParseException {

        LocalDate ld=LocalDate.parse(startDate);

        LocalTime lt=LocalTime.parse(startTime);

        String formattedDate = ld.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        ld=LocalDate.parse(formattedDate);

        LocalDateTime localDateTime = lt.atDate(ld);
        meeting.setStartDateTime(Timestamp.valueOf(localDateTime));

        meetingService.saveMeetingSchedule(meeting);

        return "/dashboard";
    }
}