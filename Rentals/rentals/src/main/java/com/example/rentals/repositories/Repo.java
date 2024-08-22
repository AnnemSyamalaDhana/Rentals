package com.example.rentals.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.rentals.models.Owners_registration;

public interface Repo extends JpaRepository<Owners_registration,Integer> {

    Optional <Owners_registration> findByUsername(String username);
} 
