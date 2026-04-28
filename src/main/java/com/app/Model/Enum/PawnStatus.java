package com.app.Model.Enum;

public enum PawnStatus {
    Activo,
    Vencido,
    Finalizado,
    Retirado,
    Perdido,
    Vendido;

    public boolean acceptsPayment() {
        return  this == Activo || this == Vencido;
    }

    public boolean isTerminal(){
        return this == Finalizado || this == Retirado|| this == Perdido || this == Vendido;
    }
}