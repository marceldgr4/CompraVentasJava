package com.app.ViewModel;

import javax.swing.*;

public abstract class BaseViewController <V extends BaseViewModel> {

    protected V viewModel;
    protected UINotifier uiNotifier;
    protected JComponent parentComponent;

    public BaseViewController(V viewModel, UINotifier uiNotifier, JComponent parentComponent) {
        this.viewModel = viewModel;
        this.uiNotifier = uiNotifier;
        this.parentComponent = parentComponent;

    }

    public interface UINotifier {
        void notifyLoading(String message);

        void showError(String message);

        void showSuccess(String message);

        void showWarning(String message);
    }
}