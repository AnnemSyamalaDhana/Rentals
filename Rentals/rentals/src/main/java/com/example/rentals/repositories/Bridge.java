package com.example.rentals.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.rentals.models.Owners_rooms_details;

@Repository
public interface Bridge extends JpaRepository<Owners_rooms_details,Integer>{
    List <Owners_rooms_details> findByUsername(String username);
    List <Owners_rooms_details> findByCity(String city);
}
