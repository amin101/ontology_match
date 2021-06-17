package match;

interface Presenter {
    Model getModel();
    void setModel(Model model);
    MainFrame getView();
    void setView(MainFrame mainFrame);
    void run();

}
