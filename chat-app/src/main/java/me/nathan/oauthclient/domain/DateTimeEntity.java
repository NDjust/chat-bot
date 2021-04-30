package me.nathan.oauthclient.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.MappedSuperclass;
import java.util.Date;

@MappedSuperclass
@Getter @Setter
public abstract class DateTimeEntity {

    private Long createdAt = new Date().getTime();

    private Long lastAt = new Date().getTime();
}
