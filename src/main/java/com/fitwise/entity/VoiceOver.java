package com.fitwise.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
public class VoiceOver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long voiceOverId;

    private String title;

    @OneToOne(cascade = {CascadeType.DETACH,CascadeType.MERGE})
    @JoinColumn(name = "audio_id")
    private Audios audios;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.DETACH,CascadeType.MERGE})
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany(fetch = FetchType.LAZY,cascade = {CascadeType.DETACH,CascadeType.MERGE} )
    @JoinTable(name = "voice_over_tag_mapping", joinColumns = @JoinColumn(name = "voice_over_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private List<VoiceOverTags> voiceOverTags;

    @Column(name = "created_date", updatable = false)
    private Date createdDate;
}
