package com.example.rentals.repositories;


import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.rentals.models.Owners_registration;

import jakarta.transaction.Transactional;

@Repository
public interface UserRepository extends JpaRepository<Owners_registration, Integer> {

  Optional<Owners_registration> findByEmail(String email);


  @Modifying
    @Transactional
    @Query("UPDATE Owners_registration u SET u.password = ?2 WHERE u.email = ?1")
    void updateUserPasswordByEmail(String email, String password);
}
