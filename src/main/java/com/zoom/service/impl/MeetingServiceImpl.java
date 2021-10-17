package com.zoom.service.impl;

import com.zoom.model.Meeting;
import com.zoom.repository.MeetingRepository;
import com.zoom.service.MeetingService;
import io.openvidu.java.client.OpenVidu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MeetingServiceImpl implements MeetingService {

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private SessionTokenGenerator sessionTokenGenerator;

    private OpenVidu openVidu;

    @Override
    public Meeting getMeetingByMeetingId(Long meetingId) {
        return meetingRepository.findMeetingByMeetingId(meetingId);
    }

    @Override
    public void saveMeetingSchedule(Meeting meeting){
        try {
            meeting.setActive(true);
            meetingRepository.save(meeting);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}