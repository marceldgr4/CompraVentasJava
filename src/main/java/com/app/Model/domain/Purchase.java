package com.app.Model.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Purchase {
    private int id;
    private String profileId;
    private int clienteId;
    private  int articleId;
    private BigDecimal purchasePrice;
    private LocalDate purchaseDate;
    private String notes;

    private  String clienteName;
    private String  articleName;
    private String profileName;

    public Purchase(){}

    public Purchase(String profileId, int clientId, int articleId,
                    BigDecimal purchasePrice, String notes) {
        this.profileId = profileId;
        this.clienteId = clientId;
        this.articleId = articleId;
        this.purchasePrice = purchasePrice;
        this.notes = notes;

    }

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

    public int getClientId() {
        return clienteId;
    }

    public void setClientId(int clientId) {
        this.clienteId = clientId;
    }

    public int getArticleId() {
        return articleId;
    }

    public void setArticleId(int articleId) {
        this.articleId = articleId;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getClienteName() {
        return clienteName;
    }

    public void setClienteName(String clienteName) {
        this.clienteName = clienteName;
    }

    public String getArticleName() {
        return articleName;
    }

    public void setArticleName(String articleName) {
        this.articleName = articleName;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }
}
