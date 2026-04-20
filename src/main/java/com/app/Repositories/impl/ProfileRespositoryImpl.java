package com.app.Repositories.impl;

import com.app.Model.Dao.ProfileDao;
import com.app.Model.domain.Profile;
import com.app.Repositories.ProfileReposiitory;

import java.util.List;
import java.util.Optional;

public class ProfileRespositoryImpl implements ProfileReposiitory {
    private final ProfileDao profileDao = new ProfileDao();

    @Override
    public Optional<Profile> findByFullName(String fullName) throws Exception {
        return Optional.empty();
    }

    @Override
    public List<Profile> findAll() throws Exception {
        return List.of();
    }

    @Override
    public boolean updateActive(boolean active) throws Exception {
        return false;
    }
}
