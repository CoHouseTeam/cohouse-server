package com.zero.cohousesever.settlement.service;

import com.zero.cohousesever.settlement.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantRepository extends JpaRepository<Participant,Long> {
}
