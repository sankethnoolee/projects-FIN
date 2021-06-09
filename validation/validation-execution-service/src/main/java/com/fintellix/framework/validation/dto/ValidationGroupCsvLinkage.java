package com.fintellix.framework.validation.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @author Deepak Moudgil
 */

@Entity
@Table(name = "VALIDATION_GROUP_CSV_LINKAGE")
@SequenceGenerator(name = "VALIDATION_CSV_LINKAGE_GEN", sequenceName = "VALIDATION_CSV_LINKAGE_SEQ", allocationSize = 1)
public class ValidationGroupCsvLinkage implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "VALIDATION_CSV_LINKAGE_GEN")
    @Column(name = "ID")
    private Long id;

    @Column(name = "RUN_ID")
    private Long runId;

    @Column(name = "VALIDATION_ID")
    private Long validationId;

    @Column(name = "GROUP_FOLDER_NAME")
    private String groupFolderName;

    @Column(name = "GROUP_CSV_NAME")
    private String groupCsvName;

    @Column(name = "CSV_GEN_STATUS")
    private Boolean csvGenerationStatus;

    public ValidationGroupCsvLinkage() {
    }

    public ValidationGroupCsvLinkage(Long runId, Long validationId, String groupFolderName, String groupCsvName,
                                     Boolean csvGenerationStatus) {
        this.runId = runId;
        this.validationId = validationId;
        this.groupFolderName = groupFolderName;
        this.groupCsvName = groupCsvName;
        this.csvGenerationStatus = csvGenerationStatus;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRunId() {
        return runId;
    }

    public void setRunId(Long runId) {
        this.runId = runId;
    }

    public Long getValidationId() {
        return validationId;
    }

    public void setValidationId(Long validationId) {
        this.validationId = validationId;
    }

    public String getGroupFolderName() {
        return groupFolderName;
    }

    public void setGroupFolderName(String groupFolderName) {
        this.groupFolderName = groupFolderName;
    }

    public String getGroupCsvName() {
        return groupCsvName;
    }

    public void setGroupCsvName(String groupCsvName) {
        this.groupCsvName = groupCsvName;
    }

    public Boolean getCsvGenerationStatus() {
        return csvGenerationStatus;
    }

    public void setCsvGenerationStatus(Boolean csvGenerationStatus) {
        this.csvGenerationStatus = csvGenerationStatus;
    }
}
