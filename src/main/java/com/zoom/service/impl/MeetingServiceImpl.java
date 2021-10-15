package com.zoom.service.impl;

import com.zoom.model.Meeting;
import com.zoom.repository.MeetingRepository;
import com.zoom.service.MeetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MeetingServiceImpl implements MeetingService {

    @Autowired
    private MeetingRepository meetingRepository;

    @Override
    public Meeting getMeetingByMeetingId(Long meetingId) {
        return meetingRepository.findMeetingByMeetingId(meetingId);
    }
}
