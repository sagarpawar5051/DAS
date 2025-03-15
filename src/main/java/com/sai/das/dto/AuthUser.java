/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sai.das.dto;

import com.sai.das.entity.UserLogin;

/**
 *
 * @author Swaroopcomp
 */
public class AuthUser {
    
      private String token;
      private UserLogin user ;
      private String username;

    public AuthUser(){

    }

    public AuthUser(String token, UserLogin user) {
        this.token = token;
        this.user = user;
        this.username = user.getUserName();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserLogin getUser() {
        return user;
    }

    public void setUser(UserLogin user) {
        this.user = user;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
      
      
              
      
}
