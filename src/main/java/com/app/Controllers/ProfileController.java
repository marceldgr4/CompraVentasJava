package com.app.Controllers;

import Infrastructure.security.SessionManager;
import com.app.Model.domain.Profile;
import com.app.Service.ProfileService;
import com.app.Service.exceptions.ServiceException;

import javax.swing.*;
import java.awt.Component;
import java.util.List;

/**
 * Controller para la gestión de Perfiles de usuario (Empleados/Admin).
 *
 * <p>Coordina las operaciones entre el {@link ProfileService} y los componentes
 * de vista ({@code ProfilePanel}), garantizando que toda I/O de base de datos
 * se ejecute fuera del EDT y los resultados se despachen en el EDT.
 *
 * <p>Todas las operaciones de este controller requieren rol Admin,
 * ya que la gestión de perfiles es una función exclusiva del administrador.
 *
 * @see ProfileService
 */
public class ProfileController extends BaseController {

    private final ProfileService profileService;

    /**
     * Constructor con inyección de servicio (facilita testing con mock).
     *
     * @param profileService servicio de perfiles a usar
     */
    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * Constructor por defecto que crea su propia instancia del servicio.
     */
    public ProfileController() {
        this.profileService = new ProfileService();
    }

    // -------------------------------------------------------
    // READ
    // -------------------------------------------------------

    /**
     * Carga todos los perfiles de usuario registrados en el sistema.
     * Solo disponible para administradores.
     *
     * @param parent    componente padre para centrar mensajes de error
     * @param onSuccess callback con la lista de perfiles (en EDT)
     * @param onError   callback con el mensaje de error (en EDT)
     */
    public void loadAll(Component parent, OnSuccess<List<Profile>> onSuccess, OnError onError) {
        if (!SessionManager.isAdmin()) {
            showError(parent, "Solo el administrador puede ver los perfiles.");
            return;
        }
        log.info("Cargando todos los perfiles de usuario");
        runAsync(
                () -> {
                    try {
                        return profileService.findAll();
                    } catch (ServiceException e) {
                        throw new RuntimeException(e);
                    }
                },
                result -> {
                    log.debug("Perfiles cargados: {}", result.size());
                    onSuccess.onResult(result);
                },
                (msg, ex) -> {
                    log.error("Error al cargar perfiles: {}", msg);
                    onError.onError("Error al cargar perfiles: " + msg, ex);
                }
        );
    }

    /**
     * Carga el perfil de un usuario específico por su ID.
     *
     * @param profileId UUID del perfil a buscar
     * @param parent    componente padre para mensajes de error
     * @param onSuccess callback con el perfil encontrado (en EDT)
     * @param onError   callback con el mensaje de error (en EDT)
     */
    public void loadById(String profileId, Component parent,
                         OnSuccess<Profile> onSuccess, OnError onError) {
        if (profileId == null || profileId.isBlank()) {
            onError.onError("El ID del perfil no puede ser nulo.", null);
            return;
        }
        log.debug("Buscando perfil con ID: {}", profileId);
        runAsync(
                () -> {
                    try {
                        return profileService.findById(profileId);
                    } catch (ServiceException e) {
                        throw new RuntimeException(e);
                    }
                },
                onSuccess,
                (msg, ex) -> onError.onError("Error al buscar perfil: " + msg, ex)
        );
    }

    // -------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------

    /**
     * Activa o desactiva una cuenta de usuario.
     * Solo disponible para administradores.
     *
     * <p>Un usuario desactivado no podrá iniciar sesión aunque sus credenciales
     * sean correctas (verificado en {@code AuthService.Login}).
     *
     * @param profileId ID del perfil a modificar
     * @param active    {@code true} para activar, {@code false} para desactivar
     * @param userName  nombre del usuario (para mostrar en el mensaje de confirmación)
     * @param parent    componente padre para mensajes y confirmación
     * @param onSuccess callback invocado si se actualizó correctamente (en EDT)
     * @param onError   callback con el mensaje de error (en EDT)
     */
    public void setActive(String profileId, boolean active, String userName,
                          Component parent, Runnable onSuccess, OnError onError) {

        if (!SessionManager.isAdmin()) {
            showError(parent, "Solo el administrador puede cambiar el estado de las cuentas.");
            return;
        }

        // Evitar que el admin se desactive a sí mismo
        try {
            if (profileId.equals(SessionManager.getProfileId()) && !active) {
                showError(parent, "No puedes desactivar tu propia cuenta de administrador.");
                return;
            }
        } catch (IllegalStateException ignored) {
            // Sin sesión activa: dejar pasar
        }

        String action = active ? "activar" : "desactivar";
        boolean confirmed = showConfirmation(
                parent,
                "¿" + (active ? "Activar" : "Desactivar") + " la cuenta de \"" + userName + "\"?",
                "Confirmar cambio de estado");

        if (!confirmed) return;

        log.info("{} cuenta de usuario: {} (ID={})", action, userName, profileId);
        runAsyncVoid(
                () -> profileService.setActive(profileId, active),
                () -> {
                    log.info("Cuenta {} correctamente: {} (ID={})", action + "da", userName, profileId);
                    showSuccess(parent, "Cuenta de \"" + userName + "\" " + action + "da correctamente.");
                    onSuccess.run();
                },
                (msg, ex) -> {
                    log.error("Error al {} cuenta {}: {}", action, profileId, msg);
                    onError.onError("No se pudo " + action + " la cuenta: " + msg, ex);
                }
        );
    }

    // -------------------------------------------------------
    // CREATE
    // -------------------------------------------------------

    /**
     * Crea un nuevo perfil de usuario en la tabla {@code profile}.
     *
     * <p>Normalmente el perfil se crea automáticamente vía el trigger
     * {@code on_auth_user_created} al registrar el usuario en Supabase Auth.
     * Este método se usa como respaldo cuando el trigger falla (auto-repair
     * implementado en {@code AuthService.Login}).
     *
     * @param profile   perfil a crear
     * @param parent    componente padre para mensajes
     * @param onSuccess callback invocado si se creó correctamente (en EDT)
     * @param onError   callback con el mensaje de error (en EDT)
     */
    public void create(Profile profile, Component parent, Runnable onSuccess, OnError onError) {
        if (!SessionManager.isAdmin()) {
            showError(parent, "Solo el administrador puede crear perfiles.");
            return;
        }
        log.info("Creando perfil para: {} ({})", profile.getFullName(), profile.getEmail());
        runAsyncVoid(
                () -> profileService.create(profile),
                () -> {
                    log.info("Perfil creado para: {}", profile.getEmail());
                    showSuccess(parent, "Perfil de \"" + profile.getFullName() + "\" creado correctamente.");
                    onSuccess.run();
                },
                (msg, ex) -> {
                    log.error("Error al crear perfil {}: {}", profile.getEmail(), msg);
                    onError.onError("No se pudo crear el perfil: " + msg, ex);
                }
        );
    }

    // -------------------------------------------------------
    // Sesión actual (helpers para las vistas)
    // -------------------------------------------------------

    /** @return {@code true} si el usuario activo es administrador */
    public boolean isAdmin() {
        return SessionManager.isAdmin();
    }

    /**
     * @return nombre completo del usuario activo, o cadena vacía si no hay sesión
     */
    public String getActiveUserName() {
        try {
            return SessionManager.getFullName();
        } catch (IllegalStateException e) {
            return "";
        }
    }

    /**
     * @return ID del perfil del usuario activo, o cadena vacía si no hay sesión
     */
    public String getActiveProfileId() {
        try {
            return SessionManager.getProfileId();
        } catch (IllegalStateException e) {
            return "";
        }
    }
}