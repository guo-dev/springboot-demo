package com.springboot.guo.bean;

import lombok.Data;

import java.util.Date;

@Data
public class ZgTicket {

    private String ticketId;

    private Integer ticketCount;

    private String ticketStatus;

    private Date ticketTime;

    private Integer version;
}
