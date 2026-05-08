package Infrastructure.security;

import com.app.Model.Enum.RolUser;

import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SessionManager {

    private static Session currentSession = null;
    private static final ReadWriteLock SESSION_LOCK = new ReentrantReadWriteLock();

    private SessionManager() {}

    // -------------------------------------------------------
    // API de compatibilidad para llamadas de instancia
    // -------------------------------------------------------

    /**
     * Retorna un proxy de sesión para compatibilidad con código que usaba
     * {@code SessionManager.getInstance().isAdmin()} etc.
     * Preferir los métodos estáticos directamente.
     *
     * @return proxy de sesión (nunca null)
     */
    public static SessionProxy getInstance() {
        return new SessionProxy();
    }

    /**
     * Proxy liviano que delega a los métodos estáticos de {@link SessionManager}.
     * Existe solo para mantener compatibilidad con código que llamaba
     * {@code SessionManager.getInstance().isAdmin()}.
     */
    public static final class SessionProxy {
        private SessionProxy() {}
        public boolean isAdmin()     { return SessionManager.isAdmin(); }
        public boolean isEmployee()  { return SessionManager.isEmployee(); }
        public boolean isActive()    { return SessionManager.isActive(); }
        public String  getEmployeeId(){ return SessionManager.getEmployeeId(); }
        public String  getFullName() { return SessionManager.getFullName(); }
    }

    public static void startSession(
            String employeeId,
            String fullName,
            RolUser rol,
            String accessToken,
            String refreshToken) {
        SESSION_LOCK.writeLock().lock();
        try{
           currentSession = new Session(employeeId, fullName, rol, accessToken, refreshToken);
        } finally {
            SESSION_LOCK.writeLock().unlock();
        }
    }
    /*
    * cerrar sesion o fin de la sesion
    * */
    public static void endSession() {
        SESSION_LOCK.writeLock().lock();
        try{
            currentSession = null;
        }finally {
            SESSION_LOCK.writeLock().unlock();
        }
    }

    /**
     * Actualiza el nombre completo en la sesión actual sin cerrarla.
     * @param newFullName nuevo nombre completo
     */
    public static void updateFullName(String newFullName) {
        SESSION_LOCK.writeLock().lock();
        try {
            if (currentSession != null) {
                currentSession = new Session(
                        currentSession.employeeId,
                        newFullName,
                        currentSession.rol,
                        currentSession.accessToken,
                        currentSession.refreshToken
                );
            }
        } finally {
            SESSION_LOCK.writeLock().unlock();
        }
    }

    public static Optional<Session> getCurrentSession(){
        SESSION_LOCK.readLock().lock();
    try{
        return Optional.ofNullable(currentSession);
    }finally {
        SESSION_LOCK.readLock().unlock();
        }
    }
    /*
    * @retorna
    * @codigo verdadero
    * si la sesion es actuamente activa
    * */
    public static boolean isActive(){
        return getCurrentSession().isPresent();
    }
    /*
    * @retorna @codigo verdadero si el usuario tiene el rol de admin
    * */
    public static boolean isAdmin(){
        return getCurrentSession()
                .map(session ->session.rol == RolUser.Admin)
                .orElse(false);
    }
    /*
    *@retorna codigo verdadero si el usuario activo es empleado
    * */
    public static boolean isEmployee(){
        return getCurrentSession()
                .map(session -> session.rol == RolUser.Empleado)
                .orElse(false);
    }
    /*
    * @retorna si el "id" del empleado del usario está activo
    * @throws IllegalStateException si no hay ninguna sesión activa
    * */
    public static String getEmployeeId() {
        return getCurrentSession()
                .map(session -> session.employeeId)
                .orElseThrow(() -> new IllegalStateException("No active session"));
    }
    /*
    * @retorna el nombre completo del usurio
    * @throws IllegalStateException si no hay ninguna sesión activa
    * */
    public static String getFullName() {
        return getCurrentSession()
                .map(session -> session.fullName)
                .orElseThrow(() -> new IllegalStateException("No active session"));
    }

    public static RolUser getRol() {
        return getCurrentSession()
                .map(s -> s.rol)
                .orElseThrow(() -> new IllegalStateException("No active session"));
    }

    public static String getAccessToken() {
        return getCurrentSession()
                .map(s -> s.accessToken)
                .orElseThrow(() -> new IllegalStateException("No active session"));
    }

    public static String getRefreshToken() {
        return getCurrentSession()
                .map(s -> s.refreshToken)
                .orElseThrow(() -> new IllegalStateException("No active session"));
    }


    public static class Session
    {
        public final String employeeId;
        public final String fullName;
        public final RolUser rol;
        public final String accessToken;
        public final String refreshToken;

        public Session(String employeeId, String fullName, RolUser rol, String accessToken, String refreshToken) {
            this.employeeId = employeeId;
            this.fullName = fullName;
            this.rol = rol;
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }
}

