package com.fintellix.framework.validation.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Deepak Moudgil
 */
public class ValidationCleanupRecord implements Serializable {
    private Integer id;
    private String type;
    private String path;
    private Date createdDate;
    private Boolean isDeleted;

    public ValidationCleanupRecord() {
    }

    public ValidationCleanupRecord(String type, String path, Date createdDate, Boolean isDeleted) {
        this.type = type;
        this.path = path;
        this.createdDate = createdDate;
        this.isDeleted = isDeleted;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}