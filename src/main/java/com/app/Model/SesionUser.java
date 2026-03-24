package com.app.Model;

public class SesionUser {
    private static SesionUser instance;

    private String profileId;
    private String fullName;
    private RolUser rol;
    private String accessToken;
    private String refreshToken;

    private SesionUser() {}
    public static SesionUser getInstance() {
        if (instance == null) instance = new SesionUser();
            return  instance;
        }
        public void start(String profileId, String fullName, RolUser rol, String accessToken, String refreshToken) {
        this.profileId = profileId;
        this.fullName = fullName;
        this.rol = rol;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        }
        public void close() {
        this.profileId = null;
            this.fullName = null;
            this.rol = null;
            this.accessToken = null;
            this.refreshToken = null;
        }
        public boolean isActive() {
        return profileId !=null;
        }
        public boolean isAdmin() {
        return RolUser.Admin.equals(rol);
        }
        public boolean isEmployee() {
        return RolUser.Empleado.equals(rol);
        }

        public String getProfileId() {
        return profileId;
        }
        public String getFullName() {
        return fullName;
        }
        public RolUser getRol() {
        return rol;
        }
        public String getAccessToken() {
        return accessToken;
        }
        public String getRefreshToken() {
        return refreshToken;
        }
}
