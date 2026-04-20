package com.app.Service;

import com.app.Model.Dao.ProfileDAO;
import com.app.Model.domain.Profile;

import java.sql.SQLException;
import java.util.List;

public class ProfileService {
    private final ProfileDAO profileDAO = new ProfileDAO();

    public Profile findById(String id) throws ServiceException {
        try {
            Profile profile = profileDAO.findById(id);
            if (profile == null) {
                throw new ServiceException("profile not found for id:" + id);
            }
            return profile;
        } catch (SQLException e) {
            throw new ServiceException("Error search ´profile" + e.getMessage());
        }
    }

    public List<Profile> findAll() throws ServiceException {
        try {
            return profileDAO.findAll();
        } catch (SQLException e) {
            throw new ServiceException("Error list the profile");
        }
    }

    public void setActive(String id, boolean active) throws ServiceException {
        try {
            profileDAO.updateActive(id, active);

        } catch (SQLException e) {
            throw new ServiceException("Error update status:" + e.getMessage());

        }
    }

    public static class ServiceException extends Exception {
        public ServiceException(String message) {
            super(message);
        }
    }
}
