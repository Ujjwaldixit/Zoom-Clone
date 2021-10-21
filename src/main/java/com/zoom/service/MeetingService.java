package com.zoom.service;

import com.zoom.model.Meeting;
import org.springframework.stereotype.Service;

@Service
public interface MeetingService {

    Meeting getMeetingByMeetingId(Long meeting);

    void saveMeetingSchedule(Meeting meeting);
}
