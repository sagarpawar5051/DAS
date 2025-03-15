/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sai.das.dto;

import java.util.Date;

/**
 *
 * @author SAGAR PAWAR
 */
public class LoginDto {
    
    private Integer loginId;
        private String empName;
        private String userName;
        private String createdBy;
        private Date creationDate;

        private String role;
        private String dept;
        private String location;
        private String ouId;
        private String hodtktNo;
        private String hodName;
        private String hodemail;
        private String useremail;   
        private String authority;

    public LoginDto(Integer loginId, String empName, String userName, String createdBy, Date creationDate, String role, String dept, String location, String ouId, String hodtktNo, String hodName, String hodemail, String useremail,String authority) {
        this.loginId = loginId;
        this.empName = empName;
        this.userName = userName;
        this.createdBy = createdBy;
        this.creationDate = creationDate;
        this.role = role;
        this.dept = dept;
        this.location = location;
        this.ouId = ouId;
        this.hodtktNo = hodtktNo;
        this.hodName = hodName;
        this.hodemail = hodemail;
        this.useremail = useremail;
        this.authority = authority;
    }

    public Integer getLoginId() {
        return loginId;
    }

    public void setLoginId(Integer loginId) {
        this.loginId = loginId;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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

    public String getHodtktNo() {
        return hodtktNo;
    }

    public void setHodtktNo(String hodtktNo) {
        this.hodtktNo = hodtktNo;
    }

    public String getHodName() {
        return hodName;
    }

    public void setHodName(String hodName) {
        this.hodName = hodName;
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

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }
        
        
}
