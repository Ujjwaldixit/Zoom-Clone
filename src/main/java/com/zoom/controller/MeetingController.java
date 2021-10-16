package com.zoom.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Timestamp;
import java.util.Date;

@Controller
public class MeetingController {
    @GetMapping("/scheduleMeeting")
    public String meeting(@RequestParam("topic") String topic,
                        @RequestParam("startDate") Date startDate,
                        @RequestParam("startTime")Timestamp startTime,
                          Model model)
    {

        return "scheduleMeeting";
    }

}
