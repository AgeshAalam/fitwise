package com.fitwise.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;


@Entity
@Getter
@Setter
public class ProgramTypes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "program_type_id")
    private Long programTypeId;

    private String programTypeName;

    @Column(name = "icon_url")
    private String iconUrl;
    
	@OneToMany(mappedBy = "programType", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<ProgramSubTypes> programSubType = new ArrayList<>();

}
