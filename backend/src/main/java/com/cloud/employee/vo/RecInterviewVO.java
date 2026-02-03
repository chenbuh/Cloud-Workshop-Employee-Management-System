package com.cloud.employee.vo;

import com.cloud.employee.entity.RecInterview;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RecInterviewVO extends RecInterview {
    private String candidateName;
    private String jobTitle;
    private Long jobId;
}
