package com.app.Repositories;


import com.app.Model.domain.Profile;

import java.awt.*;
import java.util.List;
import java.util.Optional;

public interface ProfileReposiitory {
    Optional<Profile> findByFullName(String fullName) throws Exception;
    List<Profile> findAll() throws Exception;
    boolean updateActive( boolean active) throws Exception;
}
