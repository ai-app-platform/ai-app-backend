package com.aiplatform.shared.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VersionInfo {

    @Column(name = "semantic_version", nullable = false)
    private String semanticVersion;

    @Column(name = "is_latest", nullable = false)
    private boolean latest;

    @Column(name = "version_sequence")
    private Integer versionSequence;

    public VersionInfo(String semanticVersion, boolean latest, Integer versionSequence) {
        this.semanticVersion = semanticVersion;
        this.latest = latest;
        this.versionSequence = versionSequence;
    }

    public VersionInfo increment() {
        String[] parts = semanticVersion.split("\\.");
        int major = Integer.parseInt(parts[0]);
        int minor = Integer.parseInt(parts[1]);
        int patch = Integer.parseInt(parts[2]);
        String newVersion = String.format("%d.%d.%d", major, minor, patch + 1);
        return new VersionInfo(newVersion, true, (versionSequence != null ? versionSequence : 0) + 1);
    }

    public static VersionInfo initial() {
        return new VersionInfo("1.0.0", true, 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VersionInfo that = (VersionInfo) o;
        return Objects.equals(semanticVersion, that.semanticVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(semanticVersion);
    }
}
