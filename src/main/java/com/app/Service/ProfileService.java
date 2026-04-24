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
                throw new ServiceException("No se encontró el perfil con ID: " + id);
            }
            return profile;
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar perfil: " + e.getMessage());
        }
    }

    public List<Profile> findAll() throws ServiceException {
        try {
            return profileDAO.findAll();
        } catch (SQLException e) {
            throw new ServiceException("Error al listar los perfiles");
        }
    }

    public void setActive(String id, boolean active) throws ServiceException {
        try {
            profileDAO.updateActive(id, active);

        } catch (SQLException e) {
            throw new ServiceException("Error al actualizar el estado: " + e.getMessage());

        }
    }

}
