package com.alumni.searcher.repository;

import com.alumni.searcher.entity.Alumni;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlumniRepository extends JpaRepository<Alumni, Long> {

    Optional<Alumni> findByLinkedinUrl(String linkedinUrl);
}
