package com.app.Service;

import com.app.Service.exceptions.ServiceException;
import com.app.Model.Dao.ProfileDao;
import com.app.Model.domain.Profile;

import java.sql.SQLException;
import java.util.List;

public class ProfileService {
    private final ProfileDao profileDAO = new ProfileDao();

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

}
