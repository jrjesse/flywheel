package com.antigravity.sales.queue.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "interaction_status_logs")
public class InteractionStatusLog {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interaction_id", nullable = false)
    private InteractionQueue interaction;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 50)
    private InteractionStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 50)
    private InteractionStatus newStatus;

    @Column(name = "transitioned_at", nullable = false, updatable = false)
    private Instant transitionedAt;

    @Column(name = "reason")
    private String reason;

    public InteractionStatusLog() {}

    public InteractionStatusLog(InteractionQueue interaction, InteractionStatus previousStatus, InteractionStatus newStatus, String reason) {
        this.interaction = interaction;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.reason = reason;
        this.transitionedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public InteractionQueue getInteraction() {
        return interaction;
    }

    public void setInteraction(InteractionQueue interaction) {
        this.interaction = interaction;
    }

    public InteractionStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(InteractionStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public InteractionStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(InteractionStatus newStatus) {
        this.newStatus = newStatus;
    }

    public Instant getTransitionedAt() {
        return transitionedAt;
    }

    public void setTransitionedAt(Instant transitionedAt) {
        this.transitionedAt = transitionedAt;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
