package match;

class Application {
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                Model model = null;
                model = new Model();
                MyPresenter myPresenter = new MyPresenter();
                MainFrame mainFrame = new MainFrame();
                mainFrame.setPresenter(myPresenter);
                myPresenter.setView(mainFrame);
                myPresenter.setModel(model);

                myPresenter.run();
            }
        });
    }
}
