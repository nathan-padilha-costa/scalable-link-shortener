package com.linkshortener.demo.model;
import org.springframework.stereotype.Repository;



import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface LinkRepository extends JpaRepository<Link, String> {
    
}
