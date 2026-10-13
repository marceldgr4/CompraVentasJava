package com.app;

import com.app.Model.Enum.ClienteStatus;
import com.app.Model.Enum.RegistrationType;
import com.app.Model.domain.Cliente;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClienteTest {

    @Test
    public void testCreateRapido() {
        Cliente rapido = Cliente.createRapido("1012345678", "Carlos", "Gomez", "3001234567");

        assertEquals("1012345678", rapido.getCedula());
        assertEquals("Carlos", rapido.getFirstName());
        assertEquals("Gomez", rapido.getLastName());
        assertEquals("3001234567", rapido.getPhone());
        assertNull(rapido.getEmail());
        assertEquals(ClienteStatus.Activo, rapido.getStatus());
        assertEquals(RegistrationType.RAPIDO, rapido.getRegistrationType());
        assertEquals("Gomez, Carlos", rapido.getFullName());
    }

    @Test
    public void testClienteCompleto() {
        Cliente completo = new Cliente("98765432", "Maria", "Lopez", "maria@test.com", "3119876543", "Calle 10 # 5-20", "Bogotá");

        assertEquals("98765432", completo.getCedula());
        assertEquals("maria@test.com", completo.getEmail());
        assertEquals(RegistrationType.COMPLETO, completo.getRegistrationType());
        assertEquals("Lopez, Maria", completo.getFullName());
    }
}
