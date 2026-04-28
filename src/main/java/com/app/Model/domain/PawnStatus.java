package com.app.Model.domain;

public enum PawnStatus {
    Activo,
    Vencido,
    Inactivo,
    Finalizado,
    Retirado,
    Perdido,
    Vendido;

    public boolean acceptsPawnStatus() {
        return  this == Activo || this == Vencido;
    }

    public boolean isTerminal(){
        return this == Finalizado || this == Retirado|| this == Perdido || this == Vendido;
    }
}