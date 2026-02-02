package com.cloud.employee.vo;

import com.cloud.employee.entity.EmpProfile;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class EmpProfileVO extends EmpProfile {
    private String deptName;
    private String postName;
    private String levelName;
}
