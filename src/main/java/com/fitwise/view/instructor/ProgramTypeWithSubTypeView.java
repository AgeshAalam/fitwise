package com.fitwise.view.instructor;

import com.fitwise.entity.ProgramSubTypes;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProgramTypeWithSubTypeView {
	private Long programTypeId;
	private String programTypeName;
	private String iconUrl;
	private ProgramSubTypes programSubType;
}
