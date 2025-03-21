/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sai.das.dao;

import com.sai.das.entity.UserLogin;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 *
 * @author SAGAR PAWAR
 */
public interface UserLoginDao extends CrudRepository<UserLogin, Integer>{
    
    public Optional<UserLogin> findByUserNameAndPassword(String userName,String password);
    
    UserLogin findByUserName(String userName);
    
    public List<UserLogin> findByHodtktNoAndRole(String hodtktNo,String role);
    
    
    
    
}
