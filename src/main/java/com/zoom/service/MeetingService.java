package com.zoom.service;

import com.zoom.model.Meeting;

public interface MeetingService {

    Meeting getMeetingByMeetingId(Long meeting);

    void saveMeetingSchedule(Meeting meeting);
}
