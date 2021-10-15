package com.zoom.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;

import java.sql.Timestamp;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "meeting")
public class Meeting {
    private Long meetingId;

    private String topic;

    private Date startDate;

    private Timestamp startTime;

    private String passCode;

    private boolean isActive;
}
