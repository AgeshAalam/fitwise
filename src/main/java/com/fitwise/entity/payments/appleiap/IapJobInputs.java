package com.fitwise.entity.payments.appleiap;

import com.fitwise.entity.Programs;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Date;

@Entity
@Getter
@Setter

public class IapJobInputs {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="job_id")
	private int id;
	
	@Column(name = "jobfile_name")
	private String jobfileName;
	
	private String status;
	//This column has to be removed once code stabilized.
	private String tempDir;
	
	@Column(name = "modified_date")
	@UpdateTimestamp
	private Date modifiedDate;

	@ManyToOne
	private Programs program;
}
