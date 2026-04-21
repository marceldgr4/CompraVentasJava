package com.app.Controllers;

import com.app.Model.domain.Profile;
import com.app.ViewModel.BaseViewController;
import com.app.ViewModel.ProfileViewModel;

import javax.swing.*;
import java.util.List;

public class ProfileController extends BaseViewController <ProfileViewModel>{
    public ProfileController(ProfileViewModel viewModel, UINotifier notifier, JComponent parentComponent) {
     super(viewModel, notifier, parentComponent);
    }
    public void loadAllProfiles(){
        new Thread(()->{
            try {
                viewModel.loadAllProfiles();
            }catch (Exception ex){
                SwingUtilities.invokeLater(()->uiNotifier.showError("Error loading all profiles!"+ex.getMessage()));

            }
        }).start();
    }
    public List<Profile> getCurrentProfiles(){
        return viewModel.getProfiles();
    }
    private boolean isAdmin(){
        return Infrastructure.security.SessionManager.isAdmin();
    }
}
