/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sai.das.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author SAGAR PAWAR
 */
@Entity
@Table(name = "documenthistory")
public class DocumentHistory implements Serializable{
    
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    private Integer historyId;
    private Integer docId;
    private String fileName;
    private String filePath;
    private String version;
    private String filestatus;
    private Date creationDate;
    private String createdBy;
    private Date updationDate;
    private String updatedBy;
    private String comment;
    private String hodtktNo;
    private String hodemail;
    private String useremail;
    private String dept;
    private String location;
    private String ouId;
    private String hodName;
    private String userName;
    private String remark;
     private String authorityemail;
    private String authorityName;
    private String authoritytktNo;
    private String reviewerName;
    private String revieweremail;
    private String reviewertktNo;

    public Integer getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Integer historyId) {
        this.historyId = historyId;
    }

    public Integer getDocId() {
        return docId;
    }

    public void setDocId(Integer docId) {
        this.docId = docId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getFilestatus() {
        return filestatus;
    }

    public void setFilestatus(String filestatus) {
        this.filestatus = filestatus;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getUpdationDate() {
        return updationDate;
    }

    public void setUpdationDate(Date updationDate) {
        this.updationDate = updationDate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getHodtktNo() {
        return hodtktNo;
    }

    public void setHodtktNo(String hodtktNo) {
        this.hodtktNo = hodtktNo;
    }

    public String getHodemail() {
        return hodemail;
    }

    public void setHodemail(String hodemail) {
        this.hodemail = hodemail;
    }

    public String getUseremail() {
        return useremail;
    }

    public void setUseremail(String useremail) {
        this.useremail = useremail;
    }

    public String getDept() {
        return dept;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getOuId() {
        return ouId;
    }

    public void setOuId(String ouId) {
        this.ouId = ouId;
    }

    public String getHodName() {
        return hodName;
    }

    public void setHodName(String hodName) {
        this.hodName = hodName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getAuthorityemail() {
        return authorityemail;
    }

    public void setAuthorityemail(String authorityemail) {
        this.authorityemail = authorityemail;
    }

    public String getAuthorityName() {
        return authorityName;
    }

    public void setAuthorityName(String authorityName) {
        this.authorityName = authorityName;
    }

    public String getAuthoritytktNo() {
        return authoritytktNo;
    }

    public void setAuthoritytktNo(String authoritytktNo) {
        this.authoritytktNo = authoritytktNo;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public String getRevieweremail() {
        return revieweremail;
    }

    public void setRevieweremail(String revieweremail) {
        this.revieweremail = revieweremail;
    }

    public String getReviewertktNo() {
        return reviewertktNo;
    }

    public void setReviewertktNo(String reviewertktNo) {
        this.reviewertktNo = reviewertktNo;
    }
    
    
    
}
