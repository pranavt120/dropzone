package com.flashsale.repo;

import com.flashsale.domain.Drops;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DropsRepository extends JpaRepository<Drops, UUID> {


}
