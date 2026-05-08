package com.app.Model.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PawnPayment {
    private int id;
    private int pawnId;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String notes;
    private String createByEmployeeId;
    private boolean isMissed;
    private LocalDateTime createdAt;

    public PawnPayment() {}

    public PawnPayment(int pawnId, BigDecimal amount,
                       LocalDate paymentDate, String notes,
                       String createByEmployeeId, boolean isMissed) {
        this.pawnId = pawnId;
        this.amount = amount;
        this.paymentDate = paymentDate != null ? paymentDate : LocalDate.now();
        this.notes = notes;
        this.createByEmployeeId = createByEmployeeId;
        this.isMissed = isMissed;
    }
    public static PawnPayment missedInstallment(int pawnId,String adminEmployeeId, String notes){
        PawnPayment p = new PawnPayment();
        p.pawnId = pawnId;
        p.amount = BigDecimal.ZERO;
        p.paymentDate = LocalDate.now();
        p.notes = notes;
        p.createByEmployeeId = adminEmployeeId;
        p.isMissed = true;
        return p;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPawnId() {
        return pawnId;
    }

    public void setPawnId(int pawnId) {
        this.pawnId = pawnId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCreateByEmployeeId() {
        return createByEmployeeId;
    }

    public void setCreateByEmployeeId(String createByEmployeeId) {
        this.createByEmployeeId = createByEmployeeId;
    }


    public boolean isMissed() {
        return isMissed;
    }

    public void setMissed(boolean missed) {
        isMissed = missed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
