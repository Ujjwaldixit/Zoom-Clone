package com.zoom.controller;

import com.zoom.helper.PasswordGenerator;
import com.zoom.model.Meeting;
import com.zoom.service.MeetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Timestamp;
import java.util.Date;

@Controller
public class MeetingController {

    @Autowired
    private PasswordGenerator passwordGenerator;

    @Autowired
    private MeetingService meetingService;

    @GetMapping("/scheduleMeeting")
    public String meeting(@RequestParam("meeting") Meeting meeting,
                          Model model) {
       meeting.setPassCode(passwordGenerator.generateStrongPassword());
        return "schedule";
    }

    @PostMapping("scheduleMeeting")
    public String saveMeetingSchedule(@ModelAttribute("meeting")Meeting meeting)
    {
        meetingService.saveMeetingSchedule(meeting);
        return "/dashboard";
    }
}
