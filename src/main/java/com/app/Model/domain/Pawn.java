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
    private LocalDate pawnDate;
    private LocalDate returnDate;
    private boolean expired;
    private boolean returned;
    private LocalDateTime updatedAt;

    // Campos adicionales cargados mediante JOIN
    private String profileName;
    private String articleName;
    private String clienteName;

    public Pawn(boolean admin, int id, int clienteId, int amount, BigDecimal price, LocalDate pawnDate, LocalDate returnDate, boolean expired, boolean returned) {}

    /**
     * Constructor de creación (sin id ni timestamps).
     *
     * @param profileId  UUID del empleado que registra el empeño
     * @param articleId  ID del artículo empeñado
     * @param clienteId  ID del cliente
     * @param amount     Cantidad de unidades
     * @param price      Precio unitario del empeño
     * @param pawnDate   Fecha de empeño
     * @param returnDate Fecha límite de devolución
     * @param expired    Si el plazo ya expiró
     * @param returned   Si el artículo fue devuelto
     */
    public Pawn(String profileId, int articleId, int clienteId, int amount,
                BigDecimal price, LocalDate pawnDate, LocalDate returnDate,
                boolean expired, boolean returned) {
        this.profileId   = profileId;
        this.articleId   = articleId;
        this.clienteId   = clienteId;
        this.amount      = amount;
        this.price       = price;
        this.pawnDate    = pawnDate;
        this.returnDate  = returnDate;
        this.expired     = expired;
        this.returned    = returned;
    }

    /**
     * Constructor completo mapeado desde la base de datos.
     */
    public Pawn(int id, String profileId, int articleId, int clienteId,
                int amount, BigDecimal price, LocalDate pawnDate,
                LocalDate returnDate, boolean expired, boolean returned,
                LocalDateTime updatedAt) {
        this(profileId, articleId, clienteId, amount, price, pawnDate, returnDate, expired, returned);
        this.id        = id;
        this.updatedAt = updatedAt;
    }

    // ---- Getters / Setters ----------------------------------------

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getProfileId() { return profileId; }
    public void setProfileId(String profileId) { this.profileId = profileId; }

    /** @deprecated Use {@link #getProfileId()} */
    @Deprecated public String getProfile_id() { return profileId; }
    /** @deprecated Use {@link #setProfileId(String)} */
    @Deprecated public void setProfile_id(String profileId) { this.profileId = profileId; }

    public int getArticleId() { return articleId; }
    public void setArticleId(int articleId) { this.articleId = articleId; }

    /** @deprecated Use {@link #getArticleId()} */
    @Deprecated public int getArticle_id() { return articleId; }
    /** @deprecated Use {@link #setArticleId(int)} */
    @Deprecated public void setArticle_id(int articleId) { this.articleId = articleId; }

    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }

    /** @deprecated Use {@link #getClienteId()} */
    @Deprecated public int getCliente_id() { return clienteId; }
    /** @deprecated Use {@link #setClienteId(int)} */
    @Deprecated public void setCliente_id(int clienteId) { this.clienteId = clienteId; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public LocalDate getPawnDate() { return pawnDate; }
    public void setPawnDate(LocalDate pawnDate) { this.pawnDate = pawnDate; }

    /** @deprecated Use {@link #getPawnDate()} */
    @Deprecated public LocalDate getPawn_date() { return pawnDate; }
    /** @deprecated Use {@link #setPawnDate(LocalDate)} */
    @Deprecated public void setPawn_date(LocalDate pawnDate) { this.pawnDate = pawnDate; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    /** @deprecated Use {@link #getReturnDate()} */
    @Deprecated public LocalDate getReturn_date() { return returnDate; }
    /** @deprecated Use {@link #setReturnDate(LocalDate)} */
    @Deprecated public void setReturn_date(LocalDate returnDate) { this.returnDate = returnDate; }

    public boolean isExpired() { return expired; }
    public void setExpired(boolean expired) { this.expired = expired; }

    public boolean isReturned() { return returned; }
    public void setReturned(boolean returned) { this.returned = returned; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    /** @deprecated Use {@link #getUpdatedAt()} */
    @Deprecated public LocalDateTime getUpdated_at() { return updatedAt; }
    /** @deprecated Use {@link #setUpdatedAt(LocalDateTime)} */
    @Deprecated public void setUpdated_at(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getProfileName() { return profileName; }
    public void setProfileName(String profileName) { this.profileName = profileName; }

    /** @deprecated Use {@link #getProfileName()} */
    @Deprecated public String getProfile_name() { return profileName; }
    /** @deprecated Use {@link #setProfileName(String)} */
    @Deprecated public void setProfile_name(String profileName) { this.profileName = profileName; }

    public String getArticleName() { return articleName; }
    public void setArticleName(String articleName) { this.articleName = articleName; }

    /** @deprecated Use {@link #getArticleName()} */
    @Deprecated public String getArticle_name() { return articleName; }
    /** @deprecated Use {@link #setArticleName(String)} */
    @Deprecated public void setArticle_name(String articleName) { this.articleName = articleName; }

    public String getClienteName() { return clienteName; }
    public void setClienteName(String clienteName) { this.clienteName = clienteName; }

    /** @deprecated Use {@link #getClienteName()} */
    @Deprecated public String getCliente_name() { return clienteName; }
    /** @deprecated Use {@link #setClienteName(String)} */
    @Deprecated public void setCliente_name(String clienteName) { this.clienteName = clienteName; }

    // ---- Lógica de negocio ----------------------------------------

    /** Retorna {@code true} si el empeño está activo (no devuelto ni expirado). */
    public boolean isActive() {
        return !expired && !returned;
    }

    /** Calcula el valor total del empeño (precio × cantidad). */
    public BigDecimal getTotal() {
        if (price == null) return BigDecimal.ZERO;
        return price.multiply(BigDecimal.valueOf(amount));
    }

    /** Retorna un estado legible del empeño. */
    public String getStatus() {
        if (returned) return "Devuelto";
        if (expired)  return "Expirado";
        if (isOverdue()) return "Vencido";
        return "Activo";
    }

    private boolean isOverdue() {
        return returnDate != null && LocalDate.now().isAfter(returnDate) && !returned;
    }
}
