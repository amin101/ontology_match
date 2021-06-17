package match;

import openllet.owlapi.OWL;
import org.apache.jena.ontology.OntModel;
import org.semanticweb.owlapi.formats.ManchesterSyntaxDocumentFormat;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.*;
import java.io.File;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Optional;

import static match.OntologyConst.*;

public class MainFrame extends JFrame implements View {

    private Presenter presenter;
    private JPanel mainPanel;
    private JButton browseBtn;
    private JTextArea ontologyTextarea;
    private JLabel swrlLabel;
    private JLabel ontologyLabel;
    private JRadioButton OWLXMLRadioButton;
    private JRadioButton manchesterRadioButton;
    private JRadioButton RDFRadioButton;
    private JButton saveOntologyButton;
    private JLabel browseLabel;
    private JComboBox individualsComboBox;
    private JLabel comboBoxLabel;
    private JTextPane propertyTextPane;
    private JTextField sparqlQueryTextField;
    private JLabel sparqlQueryLabel;
    private JTextPane sparqlQueryResultTextPane;
    private JButton runQueryButton;
    private JTextArea textArea1;
    private JCheckBox activeReasonerCheckBox;
    private ButtonGroup formatButtonGroup;
    public static final int WIDTH = 900;
    public static final int HEIGHT = 500;
    private PrefixDocumentFormat ontologyCurrentFormat;
    private boolean activeReasoner = false;


    public MainFrame() {
        initComponent();
    }

    private void initComponent() {
//        listModel = new DefaultListModel<>();
//        myRecommendedLaptops.setModel(listModel);


        setTitle("Ontology Loader");
        //  underlyingDiseasePanel.add(new JCheckBox("dd"));

        setSize(WIDTH, HEIGHT);
        mainPanel.setBorder(new EmptyBorder(20, 10, 20, 10));
        setContentPane(mainPanel);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);


        browseBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                JFileChooser chooser = new JFileChooser(new File(System.getProperty("user.home") + "\\Desktop")); //Downloads Directory as default
                chooser.setDialogTitle("Select Location");
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setAcceptAllFileFilterUsed(false);

                if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    String path = chooser.getSelectedFile().getPath();
//                    System.out.println(path);
//                    txtField.setText(fileID);
                    browseLabel.setText(path);

                    try {
                        Ontology ontology = getPresenter().getModel().initOntology(path);
//                    getPresenter().getModel().loadOntology(path);

                        setOntologyTextarea(new RDFXMLDocumentFormat());

                        ontology.getAllIndividuals(ontology.ont).forEach(x -> individualsComboBox.addItem(x));


//                        System.out.println(String.join( ",",aa));
                        //     presenter.getModel().getOntology().getAllIndividuals(ontology.ont);


                    } catch (OWLOntologyCreationException | URISyntaxException error) {
                        error.printStackTrace();
                    }
                }
            }
        });


        runQueryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //todo only for test
                OntModel jenaont = getPresenter().getModel().getOntology().jenaLoadOntology(getPath());

                String result = getPresenter().
                        getModel().
                        getOntology().
                        jenaRunSparql(sparqlQueryTextField.getText(), jenaont);

                sparqlQueryResultTextPane.setText(result);

            }
        });


        saveOntologyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedFormat = getSelectedButtonText(formatButtonGroup);
                PrefixDocumentFormat formatObject = getFormatObject(selectedFormat);


            }
        });


        OWLXMLRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setOntologyTextarea(new OWLXMLDocumentFormat());
            }
        });
        manchesterRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setOntologyTextarea(new ManchesterSyntaxDocumentFormat());
            }
        });
        RDFRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setOntologyTextarea(new RDFXMLDocumentFormat());
            }
        });


        saveOntologyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    System.out.println(activeReasoner);
                    getPresenter().getModel().getOntology().saveOntology(
                            getPresenter().getModel().getOntology().getOwlOntology(),
                            ontologyCurrentFormat
                    );
                } catch (OWLOntologyStorageException owlOntologyStorageException) {
                    owlOntologyStorageException.printStackTrace();
                }
            }
        });


        individualsComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Ontology ontology = getPresenter().getModel().getOntology();
//                System.out.println(individualsComboBox.getSelectedItem().toString());
                ArrayList<String> aa = ontology.listAllDataPropertyValues(
                        OWL.Individual(IRI.create(ontology.prefix + individualsComboBox.getSelectedItem().toString())),
                        ontology.ont, ontology.reasoner);

                ArrayList<String> bb = ontology.listAllObjectPropertyValues(
                        OWL.Individual(IRI.create(ontology.prefix + individualsComboBox.getSelectedItem().toString())),
                        ontology.ont, ontology.reasoner);

                aa.addAll(bb);
                propertyTextPane.setContentType("text/html");
                propertyTextPane.setText(String.join("<br/>", aa));
            }
        });

    }


    private void setOntologyTextarea(PrefixDocumentFormat format) {
        this.ontologyCurrentFormat = format;
        Ontology ontology = getPresenter().getModel().getOntology();

        Optional<OutputStream> myOnt = null;
        try {
            myOnt = ontology.outputOntology(
                    ontology.getOwlOntology(),
                    format
            );
        } catch (OWLOntologyStorageException e) {
            e.printStackTrace();
        }
        ontologyTextarea.setText(myOnt.toString());
    }


    public PrefixDocumentFormat getFormatObject(String format) {
        switch (format) {
            case RDF:
                return new RDFXMLDocumentFormat();

            case MANCHESTER:
                return new ManchesterSyntaxDocumentFormat();

            default:
                OWL_XML:
                return new OWLXMLDocumentFormat();
        }
    }


    public String getSelectedButtonText(ButtonGroup buttonGroup) {
        for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements(); ) {
            AbstractButton button = buttons.nextElement();

            if (button.isSelected()) {
                return button.getText();
            }
        }

        return null;
    }

    @Override
    public Presenter getPresenter() {
        return presenter;
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void updateModelFromView() {
        // getPresenter().getModel().setUser(jTextField1.getText());
    }

    @Override
    public void updateViewFromModel() {
        //  jTextField1.setText(getPresenter().getModel().getUser());
    }

    @Override
    public void open() {
        setVisible(true);
//        jTextField1.selectAll();
//        jTextField1.requestFocus();
    }

    @Override
    public void close() {
        dispose();
    }

    public String getPath() {
        return browseLabel.getText();
    }
}
