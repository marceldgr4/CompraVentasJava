package Infrastructure.security;

import com.app.Model.domain.RolUser;

import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SessionManager {

    private static final ThreadLocal<Session> CURRENT_SESSION =new ThreadLocal<>();
    private static final ReadWriteLock SESSION_LOCK = new ReentrantReadWriteLock();

    private SessionManager() {}

    public static void startSession(String profileId, String fullName, RolUser rol,String accessToken, String refreshToken) {
        SESSION_LOCK.writeLock().lock();
        try{
            Session session = new Session(profileId,fullName,rol,accessToken,refreshToken);
            CURRENT_SESSION.set(session);
        } finally {
            SESSION_LOCK.writeLock().unlock();
        }
    }
    public static void endSession() {
        SESSION_LOCK.writeLock().lock();
        try{
            CURRENT_SESSION.remove();
        }finally {
            SESSION_LOCK.writeLock().unlock();
        }
    }
    public static Optional<Session> getCurrentSession(){
        SESSION_LOCK.readLock().lock();
    try{
        return Optional.ofNullable(CURRENT_SESSION.get());
    }finally {
        SESSION_LOCK.readLock().unlock();
        }
    }
    public static boolean isActive(){
        return getCurrentSession().isPresent();
    }
    public static boolean isAdmin(){
        return getCurrentSession()
                .map(s ->s.rol == RolUser.Admin)
                .orElse(false);
    }
    public static boolean isEmployee(){
        return getCurrentSession()
                .map(s -> s.rol == RolUser.Empleado)
                .orElse(false);
    }
    public static String getProfileId() {
        return getCurrentSession()
                .map(s -> s.profileId)
                .orElseThrow(() -> new IllegalStateException("No active session"));
    }

    public static String getFullName() {
        return getCurrentSession()
                .map(s -> s.fullName)
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

    public static SessionManager getInstance() {

        return null;
    }


    public static class Session
    {
        public final String profileId;
        public final String fullName;
        public final RolUser rol;
        public final String accessToken;
        public final String refreshToken;

        public Session(String profileId, String fullName, RolUser rol, String accessToken, String refreshToken) {
            this.profileId = profileId;
            this.fullName = fullName;
            this.rol = rol;
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }
}
