package com.cloud.employee.vo;

import com.cloud.employee.entity.RecCandidate;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RecCandidateVO extends RecCandidate {
    private String jobTitle;
}
