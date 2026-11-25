package com.reliefbridge.auth.repository;

import java.util.Optional; 

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.reliefbridge.auth.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{

	@Query("SELECT u FROM User u WHERE u.email = :Email")
	Optional<User> findByEmail(String Email);
	
	boolean existsByEmail(String email);
	
}
