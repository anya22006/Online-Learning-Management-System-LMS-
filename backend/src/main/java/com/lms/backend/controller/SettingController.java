package com.lms.backend.controller;

import com.lms.backend.entity.Setting;
import com.lms.backend.service.SettingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/settings")
@CrossOrigin(origins = "http://localhost:5173")
public class SettingController {

    private final SettingService settingService;

    public SettingController(SettingService settingService) {
        this.settingService = settingService;
    }

    @GetMapping
    public List<Setting> getAllSettings() {
        return settingService.getAllSettings();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Setting> getSettingById(
            @PathVariable Integer id) {

        return settingService.getSettingById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/key/{key}")
    public ResponseEntity<Setting> getSettingByKey(
            @PathVariable String key) {

        return settingService.getSettingByKey(key)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Setting createSetting(
            @RequestBody Setting setting) {

        return settingService.saveSetting(setting);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Setting> updateSetting(
            @PathVariable Integer id,
            @RequestBody Setting setting) {

        return settingService.getSettingById(id)
                .map(existingSetting -> {

                    existingSetting.setSettingName(setting.getSettingName());

                    existingSetting.setSettingValue(
                            setting.getSettingValue());

                    return ResponseEntity.ok(
                            settingService.saveSetting(
                                    existingSetting)
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSetting(
            @PathVariable Integer id) {

        if (settingService.getSettingById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        settingService.deleteSetting(id);

        return ResponseEntity.noContent().build();
    }
}