package mc.obliviate.rocketparkour.statistics;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "statistics")
public final class ParkourStatistic {

    @Column(nullable = false)
    private UUID playerUniqueId;

    @Column(nullable = false)
    private String parkourName;

    @Column(name = "duration", nullable = false)
    private Duration duration;

    private UUID id = UUID.randomUUID();

    public ParkourStatistic(UUID playerUniqueId, String parkourName, Duration duration) {
        this.playerUniqueId = playerUniqueId;
        this.parkourName = parkourName;
        this.duration = duration;
    }

    public ParkourStatistic() {

    }

    public String getParkourName() {
        return parkourName;
    }

    public void setParkourName(String parkourName) {
        this.parkourName = parkourName;
    }

    public UUID getPlayerUniqueId() {
        return playerUniqueId;
    }

    public void setPlayerUniqueId(UUID playerUniqueId) {
        this.playerUniqueId = playerUniqueId;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Id
    public UUID getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ParkourStatistic) obj;
        return Objects.equals(this.playerUniqueId, that.playerUniqueId) &&
                Objects.equals(this.parkourName, that.parkourName) &&
                Objects.equals(this.duration, that.duration);
    }
}
