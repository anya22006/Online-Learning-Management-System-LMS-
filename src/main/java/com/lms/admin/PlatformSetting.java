package com.lms.admin;

import jakarta.persistence.*;

@Entity
@Table(name = "platform_settings")
public class PlatformSetting {

    @Id
    private String settingKey;

    @Column(columnDefinition = "TEXT")
    private String settingValue;

    public PlatformSetting() {}

    public PlatformSetting(String settingKey, String settingValue) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
    }

    public String getSettingKey() { return settingKey; }
    public void setSettingKey(String settingKey) { this.settingKey = settingKey; }

    public String getSettingValue() { return settingValue; }
    public void setSettingValue(String settingValue) { this.settingValue = settingValue; }
}
