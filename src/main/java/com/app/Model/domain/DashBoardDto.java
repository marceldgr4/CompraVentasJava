package com.app.Model.domain;

import java.math.BigDecimal;

public class DashBoardDto {
    private int activePawns;
    private int overduePawns;
    private int totalArticle;
    private  int totalClientes;
    private BigDecimal totalActiveValue;
    private int incompleteClients; // cliente rapida
    private  int purchaseToday;// compras del dia

    public  DashBoardDto(){}

    public DashBoardDto(int activePawns,
                        int overduePawns,
                        int totalArticlesStock,
                        int totalClientesActivos,
                        BigDecimal totalActiveValue,
                        int incompleteClients,
                        int purchaseToday) {

        this.activePawns = activePawns;
        this.overduePawns = overduePawns;
        this.totalArticle = totalArticlesStock;
        this.totalClientes = totalClientesActivos;
        this.totalActiveValue = totalActiveValue;
        this.incompleteClients = incompleteClients;
        this.purchaseToday = purchaseToday;
    }
    public DashBoardDto(int activePawns, int overduePawns, int totalArticlesStock,
                        int totalClientesActivos, BigDecimal totalActiveValue) {
        this(activePawns, overduePawns, totalArticlesStock, totalClientesActivos,
                totalActiveValue, 0, 0);
    }

    public int getActivePawns() {
        return activePawns;
    }

    public void setActivePawns(int activePawns) {
        this.activePawns = activePawns;
    }

    public int getOverduePawns() {
        return overduePawns;
    }

    public void setOverduePawns(int overduePawns) {
        this.overduePawns = overduePawns;
    }

    public int getTotalArticle() {
        return totalArticle;
    }

    public void setTotalArticle(int totalArticle) {
        this.totalArticle = totalArticle;
    }

    public int getTotalClientes() {
        return totalClientes;
    }

    public void setTotalClientes(int totalClientes) {
        this.totalClientes = totalClientes;
    }

    public BigDecimal getTotalActiveValue() {
        return totalActiveValue;
    }

    public void setTotalActiveValue(BigDecimal totalActiveValue) {
        this.totalActiveValue = totalActiveValue;
    }

    public int getIncompleteClients() {
        return incompleteClients;
    }

    public void setIncompleteClients(int incompleteClients) {
        this.incompleteClients = incompleteClients;
    }


    public int getPurchaseToday() {
        return purchaseToday;
    }

    public void setPurchaseToday(int purchaseToday) {
        this.purchaseToday = purchaseToday;
    }

    public boolean hasOverdueAlert(){
        return overduePawns > 0;
    }
}
