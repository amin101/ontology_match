package match;

class MyPresenter implements Presenter {

    Model model;
    MainFrame mainFrame;
    private Runnable onShowFrame;


    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public void setModel(Model model) {
        this.model = model;
    }

    @Override
    public MainFrame getView() {
        return mainFrame;
    }

    @Override
    public void setView(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    @Override
    public void run() {
//        loginModel.setUser("previousUser");
//        loginView.setPresenter(this);
//        loginView.updateViewFromModel();
//        loginView.open();
        mainFrame.open();
    }
}
