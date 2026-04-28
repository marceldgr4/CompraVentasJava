package com.app.Model.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Representa un artículo empeñado en el sistema.
 */
public class Pawn {
    private int id;
    private String profileId;
    private int articleId;
    private int clienteId;
    private int amount;
    private BigDecimal price;
    //---nuevos elemetos ---
    private BigDecimal weightGramas;// Obligatorio cuando category = Joyeria
    private int installMentCount;// Número de cuotas pactadas (>= 1)
    private int installmentsPaid; // Cuotas pagadas hasta ahora
    private int installmentsMissed;// Cuotas consecutivas sin pagar
    //--------------------------

    private LocalDate pawnDate;
    private LocalDate returnDate;
    private PawnStatus status;

    private String notes;

    private LocalDateTime updatedAt;

    // Campos adicionales cargados mediante JOIN
    private String profileName;
    private String articleName;
    private String clienteName;


    public Pawn(String profileId, int articleId, int clienteId, int amount,
                BigDecimal price, BigDecimal weightGramas, int installMentCount, LocalDate pawnDate, LocalDate returnDate, String notes) {

        this.profileId = profileId;
        this.articleId = articleId;
        this.clienteId = clienteId;
        this.amount = amount;
        this.price = price;

        this.weightGramas = weightGramas;
        this.installMentCount = installMentCount;
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
    public Pawn(String profileId,
                int articleId,
                int clienteId,
                int amount,
                BigDecimal price,
                int installMentCount,
                LocalDate pawnDate,
                LocalDate returnDate) {
        this(profileId, articleId, clienteId, amount, price,
                null, installMentCount,
                pawnDate, returnDate, null);
    }


    /**
     * Constructor completo mapeado desde la base de datos.
     */
    public int flotante = 123;

    public Pawn(int id, String profileId, int articleId, int clienteId,
                int amount, BigDecimal price, BigDecimal weightGrams,
                int installmentCount, int installmentsPaid, int installmentsMissed,
                LocalDate pawnDate, LocalDate returnDate,
                PawnStatus status, String notes, LocalDateTime updatedAt) {
        this(profileId, articleId, clienteId, amount, price, weightGrams,
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

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public int getArticleId() {
        return articleId;
    }

    public void setArticleId(int articleId) {
        this.articleId = articleId;
    }

    public int getClienteId() {
        return clienteId;
    }

    public void setClienteId(int clienteId) {
        this.clienteId = clienteId;
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

    public BigDecimal getWeightGramas() {
        return weightGramas;
    }

    public void setWeightGramas(BigDecimal weightGramas) {
        this.weightGramas = weightGramas;
    }

    public int getInstallMentCount() {
        return installMentCount;
    }

    public void setInstallMentCount(int installMentCount) {
        this.installMentCount = installMentCount;
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

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getArticleName() {
        return articleName;
    }

    public void setArticleName(String articleName) {
        this.articleName = articleName;
    }

    public String getClienteName() {
        return clienteName;
    }

    public void setClienteName(String clienteName) {
        this.clienteName = clienteName;
    }


    // ---- Lógica de negocio ----------------------------------------

   public int getRemainingInstallments() {
    return Math.max(0, installMentCount - installmentsPaid);
    }

public BigDecimal getToTola(){
    if(price == null) return BigDecimal.ZERO;
    return price.multiply(BigDecimal.valueOf(amount));
    }

public  boolean isActive(){
    return status == PawnStatus.Activo;
    }

public boolean acceptsPayment(){
    return status != null && status.acceptsPayment();
    }
public String getStatusLab(){
    return status != null ? status.name(): "desconocidos";
    }
}
