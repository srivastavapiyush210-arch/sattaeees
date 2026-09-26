package com.sattaees.sattaees.job.dto;

import com.sattaees.sattaees.job.entity.JobStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateJobStatusDto {

    @NotNull(message = "Job status must be provided")
    private JobStatus status;
}
