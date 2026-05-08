package com.app.Model.domain;

import com.app.Model.Enum.PawnStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Representa un artículo empeñado en el sistema.
 */
public class Pawn {
    private int id;
    private String employeeId;
    private int articleId;
    private int clientId;
    private int amount;
    private BigDecimal price;
    // ---nuevos elementos ---
    private BigDecimal weightGrams;     // Obligatorio cuando category = Joyeria
    private int installmentCount;       // Número de cuotas pactadas (>= 1)
    private int installmentsPaid;       // Cuotas pagadas hasta ahora
    private int installmentsMissed;     // Cuotas consecutivas sin pagar
    // --------------------------

    private LocalDate pawnDate;
    private LocalDate returnDate;
    private PawnStatus status;

    private String notes;

    private LocalDateTime updatedAt;

    // Campos adicionales cargados mediante JOIN
    private String employeeName;
    private String articleName;
    private String clientName;


    public Pawn(String employeeId, int articleId, int clientId, int amount,
                BigDecimal price, BigDecimal weightGrams, int installmentCount,
                LocalDate pawnDate, LocalDate returnDate, String notes) {

        this.employeeId = employeeId;
        this.articleId = articleId;
        this.clientId = clientId;
        this.amount = amount;
        this.price = price;

        this.weightGrams = weightGrams;
        this.installmentCount = installmentCount;
        this.installmentsPaid = 0;
        this.installmentsMissed = 0;

        this.pawnDate = pawnDate != null ? pawnDate : LocalDate.now();
        this.returnDate = returnDate;
        this.status = PawnStatus.Activo;
        this.notes = notes;
    }

    /**
     * Constructor simplificado sin peso ni notas.
     */
    public Pawn(String employeeId,
                int articleId,
                int clientId,
                int amount,
                BigDecimal price,
                int installmentCount,
                LocalDate pawnDate,
                LocalDate returnDate) {
        this(employeeId, articleId, clientId, amount, price,
                null, installmentCount,
                pawnDate, returnDate, null);
    }


    /**
     * Constructor completo mapeado desde la base de datos.
     */
    public Pawn(int id, String employeeId, int articleId, int clientId,
                int amount, BigDecimal price, BigDecimal weightGrams,
                int installmentCount, int installmentsPaid, int installmentsMissed,
                LocalDate pawnDate, LocalDate returnDate,
                PawnStatus status, String notes, LocalDateTime updatedAt) {
        this(employeeId, articleId, clientId, amount, price, weightGrams,
                installmentCount, pawnDate, returnDate, notes);
        this.id = id;
        this.installmentsPaid = installmentsPaid;
        this.installmentsMissed = installmentsMissed;
        this.status = status != null ? status : PawnStatus.Activo;
        this.updatedAt = updatedAt;
    }

    // ---- Getters / Setters ----------------------------------------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }


    public int getArticleId() {
        return articleId;
    }

    public void setArticleId(int articleId) {
        this.articleId = articleId;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getWeightGrams() {
        return weightGrams;
    }

    public void setWeightGrams(BigDecimal weightGrams) {
        this.weightGrams = weightGrams;
    }

    public int getInstallmentCount() {
        return installmentCount;
    }

    public void setInstallmentCount(int installmentCount) {
        this.installmentCount = installmentCount;
    }

    public int getInstallmentsPaid() {
        return installmentsPaid;
    }

    public void setInstallmentsPaid(int installmentsPaid) {
        this.installmentsPaid = installmentsPaid;
    }

    public int getInstallmentsMissed() {
        return installmentsMissed;
    }

    public void setInstallmentsMissed(int installmentsMissed) {
        this.installmentsMissed = installmentsMissed;
    }

    public LocalDate getPawnDate() {
        return pawnDate;
    }

    public void setPawnDate(LocalDate pawnDate) {
        this.pawnDate = pawnDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public PawnStatus getStatus() {
        return status;
    }

    public void setStatus(PawnStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }


    public String getArticleName() {
        return articleName;
    }

    public void setArticleName(String articleName) {
        this.articleName = articleName;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }


    // ---- Lógica de negocio ----------------------------------------

    public int getRemainingInstallments() {
        return Math.max(0, installmentCount - installmentsPaid);
    }

    public BigDecimal getTotal() {
        if (price == null) return BigDecimal.ZERO;
        return price.multiply(BigDecimal.valueOf(amount));
    }

    public boolean isActive() {
        return status == PawnStatus.Activo;
    }

    public boolean acceptsPayment() {
        return status != null && status.acceptsPayment();
    }

    public String getStatusLabel() {
        return status != null ? status.name() : "desconocido";
    }
}
