package com.lms.backend.service;

import com.lms.backend.entity.Setting;
import com.lms.backend.repository.SettingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SettingService {

    private final SettingRepository settingRepository;

    public SettingService(SettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    public List<Setting> getAllSettings() {
        return settingRepository.findAll();
    }

    public Optional<Setting> getSettingById(Integer id) {
        return settingRepository.findById(id);
    }

    public Optional<Setting> getSettingByKey(String key) {
        return settingRepository.findBySettingKey(key);
    }

    public Setting saveSetting(Setting setting) {
        return settingRepository.save(setting);
    }

    public void deleteSetting(Integer id) {
        settingRepository.deleteById(id);
    }
}