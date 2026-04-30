package com.app.Model.domain;

import java.math.BigDecimal;

public class DashBoardDto {
    private int activePawns;
    private int overduePawns;
    private int totalArticle;
    private  int totalClientes;
    private BigDecimal totalActiveValue;

    public  DashBoardDto(){}

    public DashBoardDto(int activePawns, int overduePawns, int totalArticle,
                        int totalClientes, BigDecimal totalActiveValue) {
        this.activePawns = activePawns;
        this.overduePawns = overduePawns;
        this.totalArticle = totalArticle;
        this.totalClientes = totalClientes;
        this.totalActiveValue = totalActiveValue;
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

    public boolean hasOverdueAlert(){
        return overduePawns > 0;
    }
}
