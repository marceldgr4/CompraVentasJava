package com.app.ViewModel;

import com.app.Model.domain.Profile;
import com.app.Service.ProfileService;

import java.util.ArrayList;
import java.util.List;

public class ProfileViewModel extends BaseViewModel{
    private final ProfileService profileService = new ProfileService();
    private List<Profile> profiles = new ArrayList<>();

    public void loadAllProfiles() throws ProfileService.ServiceException{
        profiles = profileService.findAll();
        notifyObservers("Profile_loaded", profiles);
    }

    public void setProfilesActive( String profileId, boolean active) throws ProfileService.ServiceException{
        profileService.setActive(profileId, active);
        profiles.replaceAll(profile -> profile.getId().equals(profileId) ? new Profile(profileId, profile.getFullName(),profile.getRol(),active):profile);
        notifyObservers("Profile_Updated", profileId);
    }
    public List<Profile> getProfiles() {
        return new ArrayList<>(profiles);
    }
}
