package com.lms.backend.repository;

import com.lms.backend.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettingRepository
        extends JpaRepository<Setting, Integer> {

    Optional<Setting> findBySettingKey(String settingKey);
}