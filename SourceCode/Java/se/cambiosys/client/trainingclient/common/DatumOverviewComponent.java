package se.cambiosys.client.trainingclient.common;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.cambiosys.client.framework.DefaultExceptionHandler;
import se.cambiosys.client.framework.Framework;
import se.cambiosys.client.framework.components.CambioButton;
import se.cambiosys.client.framework.components.CambioComboBox;
import se.cambiosys.client.framework.components.CambioDateChooser;
import se.cambiosys.client.framework.components.CambioLabel;
import se.cambiosys.client.framework.components.CambioPanel;
import se.cambiosys.client.framework.components.CambioScrollPane;
import se.cambiosys.client.framework.components.CambioSelectionComboBox;
import se.cambiosys.client.framework.components.CambioTextArea;
import se.cambiosys.client.framework.components.CambioTimeChooser;
import se.cambiosys.client.framework.datum.AbstractDatumOverviewComponent;
import se.cambiosys.client.framework.datum.DatumObject;
import se.cambiosys.client.framework.datum.StatusEvent;
import se.cambiosys.client.framework.datum.StatusListener;
import se.cambiosys.client.framework.settings.SettingHandlerService;
import se.cambiosys.client.healthcaremodel.contact.gui.ContactSelectionComponent;
import se.cambiosys.client.resourceplanning.component.CareProviderData;
import se.cambiosys.client.resourceplanning.component.CareProviderSelectionComboBox;
import se.cambiosys.client.trainingclient.Module;
import se.cambiosys.client.trainingclient.datahandler.ClientDataManager;
import se.cambiosys.client.trainingclient.views.MedicalRecordView;
import se.cambiosys.spider.HealthCareModel.ContactData;
import se.cambiosys.spider.StructureService.Date;
import se.cambiosys.spider.StructureService.DateTime;
import se.cambiosys.spider.StructureService.Time;
import se.cambiosys.spider.SubjectOfCareService.SubjectOfCare;
import se.cambiosys.spider.medicalrecordsmodule.TrainingMedicalNoteData;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static se.cambiosys.client.trainingclient.views.MedicalRecordView.getCareProvider;

public class DatumOverviewComponent extends AbstractDatumOverviewComponent
{

  private static final String COMPONENT_ID = "se.cambiosys.client.trainingclient";

  private static final String ACTION_REVIEW_TYPE = "MedicalRecordReview";
  
  private StatusListener statusListener;

  private CambioPanel actionDataPanel = null;

  private CambioPanel patientIdPanel = null;

  private CambioLabel nameLabel = null;

  private long selectedActivityId = -1L;

  private ContactSelectionComponent contactSelectionComponent;

  private CambioButton  removeButton;
  private CambioButton  saveButton;
  private CambioButton  signButton;
  private CambioButton  closeButton;
  private CambioSelectionComboBox unitSelectionForNote;
  private CambioTextArea noteTextArea;
  private CareProviderSelectionComboBox careProviderSelectionComboBox;
  private CambioComboBox noteTypesComboBox;
  private CambioDateChooser datePicker;
  private CambioTimeChooser timePicker;
  private CambioLabel unsigned;

  private Map<Long, TrainingMedicalNoteData> trainingMedicalNoteDataMap = new HashMap<Long, TrainingMedicalNoteData>();

  private static TrainingMedicalNoteData selectedDatum ;

  private static final Logger logger = LoggerFactory.getLogger(DatumOverviewComponent.class);

  public DatumOverviewComponent()
  {
    logger.error("Initializing Datum Overview");
    initialize();
  }

  /**
   * This method initializes the panel show when a Datum Object is selected
   *
   */
  private void initialize()
  {
    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;

    this.setLayout(new GridBagLayout());
    this.add(createNotePanel(),gridBagConstraints);
    this.setVisible(true);

  }

  /**
   * Get UNSIGNED and UNATTESTED action data for given staff, unit and a specific SOC
   * then create Datum object from that action data
   *
   * @param staffID[*],unitId[*] and SOC
   * @return DatumObject[*]
   */
  @Override
  public DatumObject[] getDatum(String[] careProviderIds, String[] unitIDs, SubjectOfCare soc)
  {
    if (soc == null || unitIDs == null || careProviderIds == null)
    {
      throw new IllegalArgumentException("Given argument is invalid!");
    }

    if (soc.id != "")
    {
      try
      {
        List<TrainingMedicalNoteData> unsignedNotes =
          MedicalRecordView.getUnsignedMedicalNotes(careProviderIds, unitIDs, soc.id);
        return getDatumObjectByMedicalNotes(unsignedNotes);

      }
      catch (Exception e)
      {
        DefaultExceptionHandler.getInstance().handleThrowable(e);
      }
    }
    else if (soc.id == "")
    {
      try
      {
        List<TrainingMedicalNoteData> unsignedNotes =
          MedicalRecordView.getAllUnsignedMedicalNote(careProviderIds, unitIDs);
        return getDatumObjectByMedicalNotes(unsignedNotes);
      }
      catch (Exception e)
      {
        DefaultExceptionHandler.getInstance().handleThrowable(e);
      }
    }

    return new DatumObject[0];
  }


  private DatumObject[] getDatumObjectByMedicalNotes(List<TrainingMedicalNoteData> medicalNoteWrappers)
  {

    if (medicalNoteWrappers == null || medicalNoteWrappers.isEmpty() ||
        Framework.getInstance().getActiveSubjectOfCare().id == null)
    {
      return new DatumObject[0];
    }
    final DatumObject[] datums = new DatumObject[medicalNoteWrappers.size()];
    for (int i = 0; i < datums.length; i++)
    {
      TrainingMedicalNoteData note = medicalNoteWrappers.get(i);
      final DatumObject datumObj = new DatumObject(note.versionedData.id,
                                                   getComponentID(),
                                                   DatumObject.FORM,
                                                   DatumObject.STATUS_UNSIGNED,
                                                   note.dateTime,
                                                   Long.toString(note.careProvider),
                                                   Long.toString(note.unit),
                                                   note.socId);

      datums[i] = datumObj;
    }

    return datums;
  }



  /**
   * Gives the component Id
   *
   * @return COMPONENT_ID
   */
  @Override
  public String getComponentID()
  {
    return COMPONENT_ID;
  }

  /**
   * Register a listener to fire when a Datum Object status changed
   * then the datum object will be removed from Table
   */
  @Override
  public void registerStatusListener(StatusListener statusListener)
  {
    this.statusListener = statusListener;
  }

  @Override public String[] getDatumTypes()
  {
    return new String[] { DatumObject.ACTION_REVIEW_TYPE };
  }

  /**
   * Generate the Data view for unsiged/unattested avtivity Data
   */
  @Override
  public void setSelectedDatum(DatumObject datumObject)
  {
    if (datumObject != null)
    {
      //TrainingMedicalNoteData medicalNoteWrapper = null;
      clearContactData();
      try
      {
        selectedDatum = ClientDataManager.getDataById(Long.parseLong(datumObject.getID()));
      }
      catch (Exception e)
      {
        DefaultExceptionHandler.getInstance().handleThrowable(e);
      }

      if(selectedDatum!=null){
        loadData();
        contactSelectionComponent.setSelectedContact(Long.toString(selectedDatum.contact));
        noteTextArea.setText(selectedDatum.noteText);
        noteTypesComboBox.setSelectedItem(selectedDatum.noteType);
        unitSelectionForNote.setSelectedId(Long.toString(selectedDatum.unit));

        CareProviderData careProviderData = new CareProviderData(Long.toString(selectedDatum.careProvider), getCareProvider(selectedDatum.careProvider));
        careProviderSelectionComboBox.setSelectedItem(careProviderData);
        //careProviderSelectionComboBox.setSelectedIndex((int)medicalNoteWrapper.careProvider);
        datePicker.setDate(selectedDatum.dateTime.date);
        timePicker.setTime(selectedDatum.dateTime.time);

      }

    }
  }


  private void statusChanged()
  {
    statusListener.statusChanged(new StatusEvent(StatusEvent.SIGNED));
  }

  public CambioPanel createNotePanel()
  {
    CambioPanel notePanel = new CambioPanel(new GridBagLayout());
    notePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.BOTH;
    c.anchor = GridBagConstraints.LINE_START;
    c.weightx = 1;
    c.weighty = 0;
    c.insets = new Insets(1, 1, 1, 1);

    int i = 0;

    c.gridx = 0;
    c.gridy = i;
    c.gridwidth = 2;

    contactSelectionComponent = new ContactSelectionComponent();
    contactSelectionComponent.setShowOnlyContactCombo(true);
    contactSelectionComponent.setBorder(BorderFactory.createLineBorder(Color.BLACK));

    notePanel.add(contactSelectionComponent,c);

    i++;

    CambioPanel notePicker = createnotePicker();

    c.gridx = 0;
    c.gridy = i;
    c.gridwidth = 1;
    c.weightx = 0;

    notePanel.add(notePicker,c);

    noteTextArea = new CambioTextArea();
    noteTextArea.setText("test text i am");
    CambioScrollPane panscr= new CambioScrollPane(noteTextArea);

    c.gridx = 1;
    c.gridy = i;
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 0 ;

    notePanel.add( panscr, c );

    CambioPanel buttonPanel = createButtonPanel();

    i++;

    c.gridx = 0;
    c.gridy = i;
    c.gridwidth = 2;

    notePanel.add( buttonPanel , c );


    contactSelectionComponent.addActionListener(new ActionListener()
    {
      @Override public void actionPerformed(ActionEvent e)
      {
        ContactData contactData = contactSelectionComponent.getSelectedContactData();
      }

    });

    return notePanel;
  }


  public CambioPanel createButtonPanel()
  {
    CambioPanel buttonPanel = new CambioPanel();
    buttonPanel.setLayout(new GridBagLayout());
    buttonPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.LINE_START;
    gbc.weightx = 1;
    gbc.weighty = 1;
    gbc.insets = new Insets(5, 5, 5, 5);

    int i = 0;

    gbc.gridx = i;
    gbc.gridy = 0;
    gbc.gridwidth=1;

    buttonPanel.add(new CambioLabel("   "),gbc);

    i++;

    gbc.gridx = i;

    buttonPanel.add(new CambioLabel("   "),gbc);

    i++;

    gbc.gridx = i;

    buttonPanel.add(new CambioLabel("   "),gbc);

    i++;

    gbc.gridx = i;

    buttonPanel.add(new CambioLabel("   "),gbc);

    removeButton = new CambioButton("Remove");
    saveButton = new CambioButton("Save");
    signButton = new CambioButton("Sign");
    closeButton = new CambioButton("Close");

    i++;

    gbc.gridx = i;

    buttonPanel.add(removeButton,gbc);

    i++;

    gbc.gridx = i;

    buttonPanel.add(saveButton,gbc);

    i++;

    gbc.gridx = i;

    i++;

    buttonPanel.add(signButton,gbc);

    i++;

    gbc.gridx = i;

    buttonPanel.add(closeButton,gbc);

    signButton.addActionListener(new ActionListener()
    {
      @Override public void actionPerformed(ActionEvent e)
      {
        if (selectedDatum != null)
        {
          TrainingMedicalNoteData medicalNoteData = selectedDatum;

          ComboBoxModel model = careProviderSelectionComboBox.getModel();
          CareProviderData careProviderData = (CareProviderData) model.getSelectedItem();

          if (careProviderData.getId() == "")
          {
            medicalNoteData.setCareProvider(0);
          }
          else
          {
            medicalNoteData.setCareProvider(Long.parseLong(careProviderData.getId()));
          }

          medicalNoteData.setContact(Long.parseLong(contactSelectionComponent.getSelectedContactId()));
          medicalNoteData.setNoteText(noteTextArea.getText());

          Date date = datePicker.getDate();
          Time time = timePicker.getTime();
          DateTime dateTime = new DateTime(date, time);
          medicalNoteData.setDateTime(dateTime);

          medicalNoteData.setNoteType(noteTypesComboBox.getSelectedItem().toString());
          medicalNoteData.setSigned(1);
          medicalNoteData.setActive(true);

          try
          {
            ClientDataManager.setData(medicalNoteData);
          }
          catch (Exception ex)
          {
            DefaultExceptionHandler.getInstance().handleThrowable(ex);
            logger.error("Error when signing medical note from unsigned overview ", ex);
          }

          selectedDatum = null;
          statusChanged();
        }
      }
    });

    closeButton.addActionListener(new ActionListener()
    {
      @Override public void actionPerformed(ActionEvent e)
      {

      }
    });

    removeButton.addActionListener(new ActionListener()
    {
      @Override public void actionPerformed(ActionEvent e)
      {

        if (selectedDatum != null)
        {
          TrainingMedicalNoteData medicalNoteData = selectedDatum;

          medicalNoteData.setActive(false);

          try
          {
            ClientDataManager.setData(medicalNoteData);
          }
          catch (Exception ex)
          {
            DefaultExceptionHandler.getInstance().handleThrowable(ex);
            logger.error("Error when removing medical note from unsigned overview ", ex);
          }

          selectedDatum = null;
          statusChanged();
        }
      }
    });

    saveButton.addActionListener(new ActionListener()
    {
      @Override public void actionPerformed(ActionEvent e)
      {
        if (selectedDatum != null)
        {
          TrainingMedicalNoteData medicalNoteData = selectedDatum;

          medicalNoteData.setUnit(Long.parseLong(unitSelectionForNote.getSelectedId()));

          ComboBoxModel model = careProviderSelectionComboBox.getModel();
          CareProviderData careProviderData = (CareProviderData) model.getSelectedItem();

          if (careProviderData.getId() == "")
          {
            medicalNoteData.setCareProvider(0);
          }
          else
          {
            medicalNoteData.setCareProvider(Long.parseLong(careProviderData.getId()));
          }

          medicalNoteData.setContact(Long.parseLong(contactSelectionComponent.getSelectedContactId()));
          medicalNoteData.setNoteText(noteTextArea.getText());

          Date date = datePicker.getDate();
          Time time = timePicker.getTime();
          DateTime dateTime = new DateTime(date,time);
          medicalNoteData.setDateTime(dateTime);

          medicalNoteData.setNoteType(noteTypesComboBox.getSelectedItem().toString());
          medicalNoteData.setSigned(0);
          medicalNoteData.setSocId(Framework.getInstance().getActiveSubjectOfCare().id);
          medicalNoteData.setActive(true);

          try
          {
            ClientDataManager.setData(medicalNoteData);
          }
          catch(Exception ex)
          {
            DefaultExceptionHandler.getInstance().handleThrowable(ex);
            logger.error("Error when saving medical note from unsigned overview", ex);
          }

          selectedDatum = null;
          statusChanged();

        }
      }
    });

    return buttonPanel;

  }


  public  CambioPanel createnotePicker()
  {
    CambioPanel notePicker = new CambioPanel();
    notePicker.setLayout(new GridBagLayout());
    notePicker.setBorder(BorderFactory.createLineBorder(Color.BLACK));

    CambioLabel noteType = new CambioLabel("Note Type");
    noteType.setSize(new Dimension(400, 50));
    CambioLabel careProvider = new CambioLabel("Care Provider");
    careProvider.setSize(new Dimension(400,50));
    CambioLabel unit = new CambioLabel("Unit");
    unit.setSize(new Dimension(400,50));
    CambioLabel dateTime = new CambioLabel("Date/time");
    dateTime.setSize(new Dimension(400,50));

    String[] units = { "unit1", "unit2", "unit3", "unit4", "unit5" };
    CambioComboBox unitComboBox = new CambioComboBox(units);

    unitSelectionForNote = new CambioSelectionComboBox(SettingHandlerService.UNIT_SELECTION_FOR_WORKING_UNIT);
    unitSelectionForNote.loadSelection();
    unitSelectionForNote.setPreferredSize(new Dimension(150,20));

    String[] careProviders = { "DR.Nimal", "Anna", "Nurse"};
    CambioComboBox careProviderComboBox = new CambioComboBox(careProviders);

    careProviderSelectionComboBox = new CareProviderSelectionComboBox();

    String[] noteTypes = {"Admisssion","Anestetic","Surgery","Diagnosis","Prescription"};
    noteTypesComboBox = new CambioComboBox(noteTypes);

    datePicker = new CambioDateChooser();
    timePicker = new CambioTimeChooser();

    GridBagConstraints gbc = new GridBagConstraints();

    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.insets = new Insets(2,2,2,2);

    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.gridx = 0;
    gbc.gridy = 0;

    notePicker.add( noteType , gbc);

    gbc.gridwidth = 2;
    gbc.gridx = 1;
    gbc.gridy = 0;

    notePicker.add(noteTypesComboBox , gbc);

    gbc.gridwidth = 1;
    gbc.gridx = 0;
    gbc.gridy = 1;

    notePicker.add(careProvider ,gbc );

    gbc.gridwidth = 2;
    gbc.gridx = 1;
    gbc.gridy = 1;

    notePicker.add(careProviderSelectionComboBox ,gbc);

    gbc.gridwidth = 1;
    gbc.gridx = 0;
    gbc.gridy = 2;

    notePicker.add(unit,gbc);

    gbc.gridwidth = 2;
    gbc.gridx = 1;
    gbc.gridy = 2;

    notePicker.add(unitSelectionForNote,gbc);

    gbc.gridwidth = 1;
    gbc.gridx = 0;
    gbc.gridy = 3;

    notePicker.add(dateTime , gbc);

    gbc.gridwidth = 1;
    gbc.gridx = 1;
    gbc.gridy = 3;

    notePicker.add(datePicker ,gbc);

    gbc.gridwidth = 1;
    gbc.gridx = 2;
    gbc.gridy = 3;

    notePicker.add(timePicker , gbc);

    gbc.gridx = 0;
    gbc.gridy = 4;
    gbc.ipady = 20;

    notePicker.add(new CambioLabel(""),gbc);


    gbc.gridx = 0;
    gbc.gridy = 5;
    gbc.gridwidth = 1;

    notePicker.add(new CambioLabel(""),gbc);


    gbc.gridx = 1;
    gbc.gridwidth = 1;

    unsigned = new CambioLabel("Unsigned");
    unsigned.setForeground(Color.RED);

    notePicker.add(unsigned,gbc);

    gbc.gridx = 2;

    notePicker.add(new CambioLabel(),gbc);


    return notePicker;
  }


  public void loadData()
  {
    contactSelectionComponent.setAllowNoneContact(false);
    contactSelectionComponent.setShowOnlyContactCombo(true);
    contactSelectionComponent.setPatientID(Framework.getInstance().getActiveSubjectOfCare().id);
    contactSelectionComponent.setAllowUnitAddMoreOptions(true);
    contactSelectionComponent.readContacts();

  }

  public void clearContactData()
  {
    contactSelectionComponent.setShowOnlyContactCombo(true);
    contactSelectionComponent.setPatientID(null);
    contactSelectionComponent.clearPanel();
  }


}
