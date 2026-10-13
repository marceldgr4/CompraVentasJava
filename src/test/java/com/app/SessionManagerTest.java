package com.app;

import com.app.Infrastructure.security.SessionManager;
import com.app.Model.Enum.RolUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SessionManagerTest {

    @BeforeEach
    @AfterEach
    public void cleanup() {
        SessionManager.endSession();
    }

    @Test
    public void testNoActiveSession() {
        assertFalse(SessionManager.isActive());
        assertFalse(SessionManager.isAdmin());
        assertFalse(SessionManager.isEmployee());
        assertThrows(IllegalStateException.class, SessionManager::getFullName);
        assertThrows(IllegalStateException.class, SessionManager::getEmployeeId);
    }

    @Test
    public void testAdminSession() {
        SessionManager.startSession("emp-123", "Administrador Principal", RolUser.Admin, "token1", "refresh1");

        assertTrue(SessionManager.isActive());
        assertTrue(SessionManager.isAdmin());
        assertFalse(SessionManager.isEmployee());
        assertEquals("emp-123", SessionManager.getEmployeeId());
        assertEquals("Administrador Principal", SessionManager.getFullName());
        assertEquals("token1", SessionManager.getAccessToken());

        SessionManager.updateFullName("Administrador Renombrado");
        assertEquals("Administrador Renombrado", SessionManager.getFullName());

        SessionManager.endSession();
        assertFalse(SessionManager.isActive());
    }

    @Test
    public void testEmployeeSession() {
        SessionManager.startSession("emp-456", "Empleado Caja", RolUser.Empleado, "token2", "refresh2");

        assertTrue(SessionManager.isActive());
        assertFalse(SessionManager.isAdmin());
        assertTrue(SessionManager.isEmployee());
        assertEquals("emp-456", SessionManager.getEmployeeId());
        assertEquals("Empleado Caja", SessionManager.getFullName());

        SessionManager.endSession();
        assertFalse(SessionManager.isActive());
    }
}
