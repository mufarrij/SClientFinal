package se.cambiosys.client.trainingclient.views;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.cambio.soc.SubjectOfCare;
import se.cambiosys.client.clinicalcoding.gui.components.data.CareProviderDataWrapper;
import se.cambiosys.client.countermodule.dataprovider.CareProviderDataProvider;
import se.cambiosys.client.framework.DefaultExceptionHandler;
import se.cambiosys.client.framework.Framework;
import se.cambiosys.client.framework.components.CambioButton;
import se.cambiosys.client.framework.components.CambioComboBox;
import se.cambiosys.client.framework.components.CambioDateChooser;
import se.cambiosys.client.framework.components.CambioInternalFrame;
import se.cambiosys.client.framework.components.CambioLabel;
import se.cambiosys.client.framework.components.CambioPanel;
import se.cambiosys.client.framework.components.CambioScrollPane;
import se.cambiosys.client.framework.components.CambioSelectionComboBox;
import se.cambiosys.client.framework.components.CambioTable;
import se.cambiosys.client.framework.components.CambioTextArea;
import se.cambiosys.client.framework.components.CambioTimeChooser;
import se.cambiosys.client.framework.components.toolkit.DateTimeToolkit;
import se.cambiosys.client.framework.settings.SettingHandler;
import se.cambiosys.client.framework.settings.SettingHandlerService;
import se.cambiosys.client.framework.subjectofcare.SubjectOfCareToolkit;
import se.cambiosys.client.framework.subjectofcare.SubjectOfCareWrapper;
import se.cambiosys.client.framework.units.UnitDataProvider;
import se.cambiosys.client.framework.units.gui.UnitSelectionComponent;
import se.cambiosys.client.healthcaremodel.HCMSettingHandler;
import se.cambiosys.client.healthcaremodel.contact.ContactDataProvider;
import se.cambiosys.client.healthcaremodel.contact.gui.ContactStatusSelectionEditor;
import se.cambiosys.client.resourceplanning.component.CareProviderData;
import se.cambiosys.client.resourceplanning.component.CareProviderSelectionComboBox;
import se.cambiosys.client.trainingclient.Module;
import se.cambiosys.client.trainingclient.common.DatumOverviewComponent;
import se.cambiosys.client.trainingclient.datahandler.ClientDataManager;
import se.cambiosys.client.trainingclient.models.MedicalRecord;
import se.cambiosys.client.trainingclient.models.MedicalRecordModel;
import se.cambiosys.client.healthcaremodel.contact.gui.ContactSelectionComponent;
import se.cambiosys.client.trainingclient.settings.SettingFacade;
import se.cambiosys.spider.DataService.MultiValuedData;
import se.cambiosys.spider.HealthCareModel.ContactData;
import se.cambiosys.spider.HealthCareModel.ContactFilter;
import se.cambiosys.spider.StructureService.Date;
import se.cambiosys.spider.StructureService.DateTime;
import se.cambiosys.spider.StructureService.Time;
import se.cambiosys.spider.UnitService.UnitData;
import se.cambiosys.spider.medicalrecordsmodule.TrainingMedicalNoteData;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MedicalRecordView  extends CambioInternalFrame
{

  private static MedicalRecordView medicalRecordView = null;

  private ContactSelectionComponent contactSelectionComponent;
  private CambioSelectionComboBox unitSelection;
  private CambioSelectionComboBox unitSelectionForNote;
  private CareProviderSelectionComboBox careProviderSelectionComboBox;
  private CambioComboBox noteTypesComboBox;
  private CambioDateChooser datePicker;
  private CambioTimeChooser timePicker;

  private CambioButton  removeButton;
  private CambioButton  saveButton;
  private CambioButton  signButton;
  private CambioButton  closeButton;
  private CambioButton  newButton;
  private CambioLabel unsigned;
  private CambioTextArea noteTextArea;

  private ContactData[] currentContactData;
  private DefaultMutableTreeNode root = new DefaultMutableTreeNode("AllContacts");

  private static List<MedicalRecord> medicalRecordList = new ArrayList<>();
  private static MedicalRecordModel  medicalRecordModel = new MedicalRecordModel(medicalRecordList);
  private static Map<String , TrainingMedicalNoteData>  cashedMedicalNotes = new HashMap<>();

  private static long selectedContactId ;

  private CambioTable medicalRecordTable;

  private static final Logger logger = LoggerFactory.getLogger(MedicalRecordView.class);

  private static String noteText = "";
  private static boolean edited = false;
  private static boolean signed = false;



  private MedicalRecordView()
  {
    try
    {
      setSOCRelated(new String[] { SubjectOfCareToolkit.PATIENT });
      String currentUserId = Framework.getInstance().getCurrentUserId();
      this.setTitle(currentUserId);
      logger.error("Initializing MedicalRecordView GUI");
      initGUI();
      this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
    catch(Exception e)
    {
      DefaultExceptionHandler.getInstance().handleThrowable(e);
    }
  }

  public static MedicalRecordView getInstance()
  {
    if(medicalRecordView == null)
    {
      CambioPanel cambioPanel;
      medicalRecordView = new MedicalRecordView();
    }
    return medicalRecordView;
  }


  public static void  exit()
  {
    medicalRecordView = null;
  }

  @Override
  public void setActiveSOC(SubjectOfCareWrapper soc) throws Exception
  {
    if(Framework.getInstance().getActiveSubjectOfCare().id!=null)
    {
      loadData();
    }
    else
    {
      clearContactData();
    }
    super.showWindowContent();
  }

  @Override
  public void updateActiveSOCData(SubjectOfCareWrapper soc)
  {
    super.showWindowContent();
  }


  public CambioPanel createUnitPanel()
  {
     CambioPanel unitPanel = new CambioPanel();
     CambioLabel unitLabel = new CambioLabel(" Unit:");
     //unitLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

     String[] units = { "unit1", "unit2", "unit3", "unit4", "unit5" };
     List<String> unitList = new ArrayList();
     unitList.add("unit1");
     unitList.add("unit2");
     CambioComboBox unitComboBox = new CambioComboBox(unitList.toArray());
     unitSelection = new CambioSelectionComboBox(SettingHandlerService.UNIT_SELECTION_FOR_WORKING_UNIT);
     unitSelection.loadSelection();
     unitSelection.setPreferredSize(new Dimension(150,20));
     UnitSelectionComponent unitSelectionComponent = new UnitSelectionComponent();

     root = new DefaultMutableTreeNode("AllContacts");
     /*DefaultMutableTreeNode sub1= new DefaultMutableTreeNode("sub1");
     DefaultMutableTreeNode sub2 = new DefaultMutableTreeNode("sub2");
     DefaultMutableTreeNode sub3 = new DefaultMutableTreeNode("sub2");

     root.add(sub1);
     root.add(sub2);
     root.add(sub3);*/

     JTree tree = new JTree(root);

     CambioScrollPane unitScrollPane = new CambioScrollPane(tree);
     unitScrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
     unitScrollPane.setSize(new Dimension(150,650));

     unitPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
     unitPanel.setLayout(new GridBagLayout());
     //unitPanel.setPreferredSize(new Dimension(200,800));

     GridBagConstraints gbc = new GridBagConstraints();
     gbc.fill = GridBagConstraints.HORIZONTAL;
     gbc.anchor = GridBagConstraints.NORTHWEST;
     gbc.weightx = 1;
     gbc.weighty = 0;
     gbc.insets = new Insets(2, 2, 0, 2);

     int i = 0;

     gbc.gridx = 0;
     gbc.gridy = i;
     gbc.weightx = 0;
     gbc.gridwidth = 1;
     unitPanel.add(unitLabel, gbc);

     gbc.gridx = 1;
     gbc.gridy = i;
     gbc.weightx = 0;
     gbc.gridwidth = 1;
     gbc.fill = GridBagConstraints.HORIZONTAL;
     unitPanel.add(unitSelection, gbc);

     i++;

     gbc.gridx = 0;
     gbc.gridy = i;
     gbc.weightx = 1;
     gbc.weighty = 1;
     gbc.gridwidth = 2;
     gbc.anchor = GridBagConstraints.NORTH;
     gbc.fill = GridBagConstraints.BOTH;
     unitPanel.add(unitScrollPane, gbc);

     unitSelection.addActionListener(new ActionListener()
     {
       @Override public void actionPerformed(ActionEvent e)
       {
         JFrame f = new JFrame();
         JOptionPane.showMessageDialog(f, unitSelection.getSelectedId());
         ContactData data = contactSelectionComponent.getSelectedContactData();
         contactSelectionComponent.setSelectedCareUnit(unitSelection.getSelectedId());

         try
         {
           if(Framework.getInstance().getActiveSubjectOfCare().id != null)
           {
             String[] settingPath =
               new String[] { "ContactStatusSetting", "AllowNewContactChoice", "UseSelectionForContactSearch",
                              "SuggestContact", "UseInfoClass", "MandatoryConnectionToCareContact" };
             MultiValuedData[] filterdata =
               SettingHandler.getInstance().getSettingValues(HCMSettingHandler.PARENT_PATH, settingPath);
             String statusValue = filterdata[0].stringValue;
             ContactFilter m_filter = ContactStatusSelectionEditor.getContactFilter(statusValue);

             String[] perfUnitValues = null;
             String[] respUnitValues = null;

             //List<TrainingMedicalNoteData> unsigned = ClientDataManager.getUnsignedData(new long[]{97668,51077},new long[]{3557,1059,3555},new long[]{1,2,3});
             List<TrainingMedicalNoteData> unsigned = getUnsignedMedicalNotes(new String[]{"1","2","3","4"},new String[]{"3557","1059","1244"},Framework.getInstance().getActiveSubjectOfCare().id);

             currentContactData = ContactDataProvider.readContacts(Framework.getInstance().getActiveSubjectOfCare().id,
                                                                   m_filter,
                                                                   perfUnitValues,
                                                                   respUnitValues);
             List<ContactData> selectedContacts = new ArrayList<ContactData>();

             for (int i = 0; i < currentContactData.length; i++)
             {
               if (currentContactData[i].staffed.performingUnit.equals(unitSelection.getSelectedId()))
               {
                 selectedContacts.add((ContactData) currentContactData[i]);
               }
             }

             root.removeAllChildren();

             for (int i = 0; i < selectedContacts.size(); i++)
             {
               String contactAsString = ContactDataProvider
               .getStringForContact(selectedContacts.get(i), HCMSettingHandler.getContactPresentationSettingValue());
               StringBuilder stringBuilder = new StringBuilder(contactAsString);
               stringBuilder.append("-");
               stringBuilder.append(selectedContacts.get(i).versioned.id);
               DefaultMutableTreeNode node = new DefaultMutableTreeNode(stringBuilder);
               root.add(node);
             }

             ((DefaultTreeModel) tree.getModel()).reload();
           }
         }
         catch (Exception ex){
           DefaultExceptionHandler.getInstance().handleThrowable(ex);
         }
       }

     });

    tree.addTreeSelectionListener(new TreeSelectionListener() {

      @Override
      public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode selectedNode =
          (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();

        if(selectedNode !=null)
        {

          String selection = selectedNode.toString();

          if(!selection.equals("AllContacts"))
          {

            String idString = selection.split("-")[1];
            selectedContactId = Long.parseLong(idString);
            try
            {
              List<TrainingMedicalNoteData> medicalNoteList = ClientDataManager.getMedicalNoteDataByContactId(selectedContactId);
              Iterator iterator = medicalNoteList.iterator();
              medicalRecordList.clear();
              cashedMedicalNotes.clear();

              while (iterator.hasNext())
              {
                TrainingMedicalNoteData medicalNoteData = (TrainingMedicalNoteData) iterator.next();
                cashedMedicalNotes.put(medicalNoteData.versionedData.id,medicalNoteData);

                MedicalRecord medicalRecord = new MedicalRecord();
                medicalRecord.setId(medicalNoteData.versionedData.id);
                medicalRecord.setCareProvider(Long.toString(medicalNoteData.careProvider));
                //String careProvider = CareProviderDataProvider.newInstance().getMainStaffId(Long.toString(medicalNoteData.careProvider));



                //medicalRecord.setCareProvider(careProvider);
                medicalRecord.setNoteType(medicalNoteData.noteType);
                medicalRecord.setDateTime(DateTimeToolkit.getInstance().toString(medicalNoteData.dateTime));

                ContactData contactData = ContactDataProvider.getContactDataById(Long.toString(medicalNoteData.contact));
                String contactString = ContactDataProvider.getStringForContact(contactData,HCMSettingHandler.getContactPresentationSettingValue());
                medicalRecord.setContact(contactString);

                UnitData unitData = UnitDataProvider.getInstance().getUnitDataByID(Long.toString(medicalNoteData.unit));
                medicalRecord.setUnit(unitData.name);


                medicalRecordList.add(medicalRecord);

              }

              medicalRecordModel.fireTableDataChanged();

            }
            catch (Exception ex)
            {
              DefaultExceptionHandler.getInstance().handleThrowable(ex);
            }
          }
          else
            {
              medicalRecordList.clear();
              medicalRecordModel.fireTableDataChanged();
            }
        }
      }
    });

     return unitPanel;

  }


  public static void reload(){

    medicalRecordModel.fireTableDataChanged();
  }


  public CambioPanel createTablePanel()
  {
    //MedicalRecord record = new MedicalRecord("2019-06-13 12:10:10","example","sc","sample","id_contact");
    //medicalRecordList = new ArrayList<>();
    /*recordList.add(record);
    recordList.add(record);
    recordList.add(record);*/
    //MedicalRecordModel medicalRecordModel = new MedicalRecordModel(medicalRecordList);

    medicalRecordTable = new CambioTable();
    medicalRecordTable.setModel(medicalRecordModel);

    //reading column order setting for the patient table
    MultiValuedData columnOrder = SettingFacade.getInstance().getColumnOrderSetting();
    medicalRecordTable.customizeColumnModel(columnOrder);

    CambioScrollPane medicalRecordTablePane = new CambioScrollPane(medicalRecordTable);
    CambioPanel medicalRecordPanel = new CambioPanel();
    medicalRecordPanel.setLayout(new GridBagLayout());

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.LINE_START;
    gbc.weightx = 1;
    gbc.weighty = 1;
    gbc.insets = new Insets(5, 5, 5, 5);

    int i=0;

    gbc.gridx = 0;
    gbc.gridy = i;
    gbc.gridwidth = 4;
    medicalRecordPanel.add(medicalRecordTablePane, gbc);

    medicalRecordTable.addMouseListener(new MouseAdapter()
    {
      @Override public void mouseClicked(MouseEvent e)
      {
          edited = false;
          int row = medicalRecordTable.getSelectedRow();
          MedicalRecord medicalRecord = medicalRecordModel.getMedicalRecordListList().get(row);
          TrainingMedicalNoteData medicalNote = (TrainingMedicalNoteData) cashedMedicalNotes.get(medicalRecord.getId());

          if(medicalNote.signed == 1)
          {
            signed = true;
            saveButton.setEnabled(false);
            signButton.setEnabled(false);
            setInputFieldsActive(false);

          }
          else{
            signed = false;
            saveButton.setEnabled(false);
            signButton.setEnabled(true);
            setInputFieldsActive(true);
          }

          contactSelectionComponent.setSelectedContact(Long.toString(medicalNote.contact));
          noteTypesComboBox.setSelectedItem(medicalNote.noteType);
          unitSelectionForNote.setSelectedId(Long.toString(medicalNote.unit));
          noteTextArea.setText(medicalNote.noteText);
          datePicker.setDate(medicalNote.dateTime.date);
          timePicker.setTime(medicalNote.dateTime.time);
          noteText = medicalNote.noteText;
          //careProviderSelectionComboBox.setSelectedIndex((int)medicalNote.careProvider);
        /*if(getCareProvider(medicalNote.careProvider).equals("$Doctor12* Doctor12*"))
        {
          careProviderSelectionComboBox.setSelectedIndex(1);
        }
        else if(getCareProvider(medicalNote.careProvider).equals("Nurse6* Nurse6*"))
        {
          careProviderSelectionComboBox.setSelectedIndex(2);
        }
        else if(getCareProvider(medicalNote.careProvider).equals("s%9Doctor13* Doctor13*"))
        {
          careProviderSelectionComboBox.setSelectedIndex(3);
        }
        else if(getCareProvider(medicalNote.careProvider).equals("Undefined name"))
        {
          careProviderSelectionComboBox.setSelectedIndex(4);
        }
        else
        {
          careProviderSelectionComboBox.setSelectedIndex(0);
        }*/

        if(medicalNote.careProvider == 0)
        {
          careProviderSelectionComboBox.setSelectedIndex(0);
        }

        CareProviderData careProviderData = new CareProviderData(Long.toString(medicalNote.careProvider),getCareProvider(medicalNote.careProvider));
        careProviderSelectionComboBox.setSelectedItem(careProviderData);

        if( medicalNote.signed == 0)
          {
            addUnsignedLabel();
          }
          else
            {
              removeUnsignedLabel();
            }

      }
    });


    return medicalRecordPanel;
  }



  public CambioPanel createTablePanelNew()
  {
    MedicalRecord record = new MedicalRecord("1","2019-06-13 12:10:10","example","sc","sample","id_contact");
    List<MedicalRecord> recordList = new ArrayList<MedicalRecord>();
    recordList.add(record);
    recordList.add(record);
    recordList.add(record);
    MedicalRecordModel medicalRecordModel = new MedicalRecordModel(recordList);

    CambioTable userTable = new CambioTable();
    userTable.setModel(medicalRecordModel);

    //reading column order setting for the patient table
    MultiValuedData columnOrder = SettingFacade.getInstance().getColumnOrderSetting();
    userTable.customizeColumnModel(columnOrder);

    CambioScrollPane medicalRecordTablePane = new CambioScrollPane(userTable);
    CambioPanel medicalRecordPanel = new CambioPanel();
    medicalRecordPanel.setLayout(new GridBagLayout());

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.LINE_START;
    gbc.weightx = 1;
    gbc.weighty = 1;
    gbc.insets = new Insets(5, 5, 5, 5);

    int i=0;

    gbc.gridx = 0;
    gbc.gridy = i;
    gbc.gridwidth = 4;
    medicalRecordPanel.add(medicalRecordTablePane, gbc);

    return medicalRecordPanel;
  }


  public CambioPanel createNewButtonPanel()
  {
    CambioPanel newButtonPanel = new CambioPanel();
    newButtonPanel.setLayout(new GridBagLayout());

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.LINE_START;
    gbc.weightx = 1;
    gbc.weighty = 1;
    gbc.insets = new Insets(5, 5, 5, 5);

    int i = 0;

    gbc.gridx = i;
    gbc.gridy = 0;


    newButtonPanel.add(new CambioLabel("   "),gbc);

    i++;
    gbc.gridx = i;
    gbc.gridwidth=1;
    newButtonPanel.add(new CambioLabel("   "),gbc);

    i++;
    gbc.gridx = i;
    gbc.gridwidth=1;
    newButtonPanel.add(new CambioLabel("   "),gbc);

    i++;
    gbc.gridx = i;
    gbc.gridwidth=1;
    newButtonPanel.add(new CambioLabel("   "),gbc);

    i++;
    gbc.gridx = i;
    gbc.gridwidth=1;
    newButtonPanel.add(new CambioLabel("   "),gbc);

    i++;
    gbc.gridx = i;
    gbc.gridwidth=1;
    newButtonPanel.add(new CambioLabel("   "),gbc);

    i++;
    gbc.gridx = i;
    gbc.gridwidth=1;
    newButtonPanel.add(new CambioLabel("   "),gbc);

    i++;
    gbc.gridx = i;
    gbc.gridwidth=1;
    newButtonPanel.add(new CambioLabel("   "),gbc);


    newButton = new CambioButton("New");

    i++;
    gbc.gridx=i;
    newButtonPanel.add(newButton,gbc);

    if(!Module.authorizeWriteCurrentUser())
    {
      newButton.setEnabled(false);
    }

    newButton.addActionListener(new ActionListener()
    {
      @Override public void actionPerformed(ActionEvent e)
      {
        noteText = "";
        addUnsignedLabel();
        saveButton.setEnabled(true);
        signButton.setEnabled(true);

        medicalRecordTable.clearSelection();
        unitSelectionForNote.setSelectedIndex(0);
        careProviderSelectionComboBox.setSelectedIndex(0);
        noteTypesComboBox.setSelectedIndex(0);
        datePicker.setDate(DateTimeToolkit.getInstance().getDate());
        timePicker.setTime(DateTimeToolkit.getInstance().getTime());
        noteTextArea.setText("");
      }
    });



    return newButtonPanel;
  }

  public CambioPanel createButtonPanel()
  {
    CambioPanel buttonPanel = new CambioPanel();
    buttonPanel.setLayout(new GridBagLayout());

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

    saveButton.addActionListener(new ActionListener()
    {
      @Override public void actionPerformed(ActionEvent e)
      {
        if(medicalRecordTable.getSelectedRows().length == 0 )
        {
          TrainingMedicalNoteData medicalNote = new TrainingMedicalNoteData();

          medicalNote.setUnit(Long.parseLong(unitSelectionForNote.getSelectedId()));
          String careProvider = careProviderSelectionComboBox.getSelectedItem().toString();
          //medicalNote.setCareProvider(careProviderSelectionComboBox.getSelectedIndex());

          ComboBoxModel model = careProviderSelectionComboBox.getModel();
          CareProviderData careProviderData = (CareProviderData) model.getSelectedItem();

          if (careProviderData.getId() == "")
          {
            medicalNote.setCareProvider(0);
          }
          else
          {
            medicalNote.setCareProvider(Long.parseLong(careProviderData.getId()));
          }

          //medicalNote.setCareProvider(getCareProviderId(careProvider));
          medicalNote.setContact(Long.parseLong(contactSelectionComponent.getSelectedContactId()));
          medicalNote.setNoteText(noteTextArea.getText());

          Date date = datePicker.getDate();
          Time time = timePicker.getTime();
          DateTime dateTime = new DateTime(date,time);
          medicalNote.setDateTime(dateTime);

          medicalNote.setNoteType(noteTypesComboBox.getSelectedItem().toString());
          medicalNote.setSigned(0);
          medicalNote.setSocId(Framework.getInstance().getActiveSubjectOfCare().id);
          medicalNote.setActive(true);
          TrainingMedicalNoteData medicalNoteData = medicalNote;
          try
          {
            ClientDataManager.saveData(medicalNote);
          }
          catch(Exception ex)
          {
            DefaultExceptionHandler.getInstance().handleThrowable(ex);
            logger.error("Error when saving medical note from unsigned overview", ex);
          }
        }
        else
        {
          int row = medicalRecordTable.getSelectedRow();
          MedicalRecord medicalRecord = medicalRecordModel.getMedicalRecordListList().get(row);
          TrainingMedicalNoteData medicalNote = (TrainingMedicalNoteData) cashedMedicalNotes.get(medicalRecord.getId());

          medicalNote.setUnit(Long.parseLong(unitSelectionForNote.getSelectedId()));
          medicalNote.setCareProvider(careProviderSelectionComboBox.getSelectedIndex());
          medicalNote.setContact(Long.parseLong(contactSelectionComponent.getSelectedContactId()));
          medicalNote.setNoteText(noteTextArea.getText());

          Date date = datePicker.getDate();
          Time time = timePicker.getTime();
          DateTime dateTime = new DateTime(date,time);
          medicalNote.setDateTime(dateTime);

          medicalNote.setNoteType(noteTypesComboBox.getSelectedItem().toString());
          medicalNote.setSigned(0);
          medicalNote.setSocId(Framework.getInstance().getActiveSubjectOfCare().id);
          TrainingMedicalNoteData medicalNoteData = medicalNote;
          try
          {
            ClientDataManager.setData(medicalNote);
          }
          catch(Exception ex)
          {
            DefaultExceptionHandler.getInstance().handleThrowable(ex);
          }
        }

        refreshTable();
      }
    });


    signButton.addActionListener(new ActionListener()
    {
      @Override public void actionPerformed(ActionEvent e)
      {
         removeUnsignedLabel();

         if(medicalRecordTable.getSelectedRows().length != 0 )
         {
           int row = medicalRecordTable.getSelectedRow();
           MedicalRecord medicalRecord = medicalRecordModel.getMedicalRecordListList().get(row);
           TrainingMedicalNoteData medicalNote = (TrainingMedicalNoteData) cashedMedicalNotes.get(medicalRecord.getId());

           medicalNote.setSigned(1);

           try
           {
             ClientDataManager.setData(medicalNote);
           }
           catch(Exception ex)
           {
             DefaultExceptionHandler.getInstance().handleThrowable(ex);
             logger.error("Error when signing medical note from unsigned overview ", ex);
           }

           refreshTable();
         }
      }
    });

    /*signButton.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        unsigned.setText("");
        unsigned.setForeground(Color.WHITE);
      }
    });*/

    closeButton.addActionListener(new ActionListener()
    {
      @Override public void actionPerformed(ActionEvent e)
      {
        Module.medicalRecordView.setVisible(false);
      }
    });

    removeButton.addActionListener(new ActionListener()
    {
      @Override public void actionPerformed(ActionEvent e)
      {
        if(medicalRecordTable.getSelectedRows().length != 0)
        {
          int row = medicalRecordTable.getSelectedRow();
          MedicalRecord medicalRecord = medicalRecordModel.getMedicalRecordListList().get(row);
          TrainingMedicalNoteData medicalNote = (TrainingMedicalNoteData) cashedMedicalNotes.get(medicalRecord.getId());

          medicalNote.setActive(false);

          try
          {
            ClientDataManager.setData(medicalNote);
          }
          catch(Exception ex)
          {
            DefaultExceptionHandler.getInstance().handleThrowable(ex);
            logger.error("Error when removing medical note from unsigned overview ", ex);
          }

          refreshTable();
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
    noteType.setSize(new Dimension(400,50));
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

    //combo
    String[] contact = { "c1", "c2", "c3", "c4", "c5" };
    CambioComboBox contactComboBox = new CambioComboBox(contact);

    contactSelectionComponent = new ContactSelectionComponent();
    contactSelectionComponent.setShowOnlyContactCombo(true);

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


    /*contactSelectionComponent.addActionListener(new ActionListener()
    {
      @Override public void actionPerformed(ActionEvent e)
      {
        saveButton.setEnabled(true);
      }
    });*/


    noteTextArea.addKeyListener(new KeyListener()
    {
      @Override public void keyTyped(KeyEvent e)
      {
        if(noteText !="")
        {
          if(!signed)
          {
            if (!noteText.equals(noteTextArea.getText()))
            {
              saveButton.setEnabled(true);
            }
            else
            {
              if (!edited)
              {
                saveButton.setEnabled(false);
              }
            }
          }
          else{
            saveButton.setEnabled(false);
          }
        }
      }

      @Override public void keyPressed(KeyEvent e)
      {
        if (noteText != "")
        {
          if (!signed)
          {
            if (!noteText.equals(noteTextArea.getText()))
            {
              saveButton.setEnabled(true);
            }
            else
            {
              if (!edited)
              {
                saveButton.setEnabled(false);
              }
            }
          }
          else
            {
              saveButton.setEnabled(false);
            }
        }
      }

      @Override public void keyReleased(KeyEvent e)
      {
        if (noteText != "")
        {
          if (!signed)
          {
            if (!noteText.equals(noteTextArea.getText()))
            {
              saveButton.setEnabled(true);
            }
            else
            {
              if (!edited)
              {
                saveButton.setEnabled(false);
              }
            }
          }
          else{
            saveButton.setEnabled(false);
          }
        }
      }
    }
    );

    contactSelectionComponent.addKeyListener(new KeyListener()
    {
      @Override public void keyTyped(KeyEvent e)
      {
        if(!signed)
        {
          saveButton.setEnabled(true);
        }
        edited = true;
      }

      @Override public void keyPressed(KeyEvent e)
      {
        if(!signed)
        {
          saveButton.setEnabled(true);
        }
        edited = true;
      }

      @Override public void keyReleased(KeyEvent e)
      {
        if(!signed)
        {
          saveButton.setEnabled(true);
        }
        edited = true;
      }
    });

    contactSelectionComponent.addMouseListener(new MouseListener()
    {
      @Override public void mouseClicked(MouseEvent e)
      {
        if(!signed)
        {
          saveButton.setEnabled(true);
        }
        edited = true;
      }

      @Override public void mousePressed(MouseEvent e)
      {
        if(!signed)
        {
          saveButton.setEnabled(true);
        }
        edited = true;
      }

      @Override public void mouseReleased(MouseEvent e)
      {
        if(!signed)
        {
          saveButton.setEnabled(true);
        }
        edited = true;
      }

      @Override public void mouseEntered(MouseEvent e)
      {
      /*  saveButton.setEnabled(true);
        edited = true;*/
      }

      @Override public void mouseExited(MouseEvent e)
      {
       /* saveButton.setEnabled(true);
        edited = true;*/
      }
    });


    noteTypesComboBox.addKeyListener(new KeyListener()
    {
      @Override public void keyTyped(KeyEvent e)
      {
        if(!signed)
        {
          saveButton.setEnabled(true);
        }
        edited = true;
      }

      @Override public void keyPressed(KeyEvent e)
      {
        if(!signed)
        {
          saveButton.setEnabled(true);
        }
        edited = true;
      }

      @Override public void keyReleased(KeyEvent e)
      {
        if(!signed)
        {
          saveButton.setEnabled(true);
        }
        edited = true;
      }
    });

    noteTypesComboBox.addMouseListener(new MouseListener()
  {
    @Override public void mouseClicked(MouseEvent e)
    {
      if(!signed)
      {
        saveButton.setEnabled(true);
      }
      edited = true;
    }

    @Override public void mousePressed(MouseEvent e)
    {
      if(!signed)
      {
        saveButton.setEnabled(true);
      }
      edited = true;
    }

    @Override public void mouseReleased(MouseEvent e)
    {
      if(!signed)
      {
        saveButton.setEnabled(true);
      }
      edited = true;
    }

    @Override public void mouseEntered(MouseEvent e)
    {
      /*saveButton.setEnabled(true);
      edited = true;*/
    }

    @Override public void mouseExited(MouseEvent e)
    {
      /*saveButton.setEnabled(true);
      edited = true;*/
    }
  });

    careProviderSelectionComboBox.addKeyListener(new KeyListener()
    {
      @Override public void keyTyped(KeyEvent e)
      {
        if(!signed)
        {
          saveButton.setEnabled(true);
        }
        edited = true;
      }

      @Override public void keyPressed(KeyEvent e)
      {
        if(!signed)
        {
          saveButton.setEnabled(true);
        }
        edited = true;
      }

      @Override public void keyReleased(KeyEvent e)
      {
        if(!signed)
        {
          saveButton.setEnabled(true);
        }
        edited = true;
      }
    });

    careProviderSelectionComboBox.addMouseListener(new MouseListener()
    {
      @Override public void mouseClicked(MouseEvent e)
      {
        if(!signed)
        {
          saveButton.setEnabled(true);
        }
        edited = true;
      }

      @Override public void mousePressed(MouseEvent e)
      {
        if(!signed)
        {
          saveButton.setEnabled(true);
        }
        edited = true;
      }

      @Override public void mouseReleased(MouseEvent e)
      {
        if(!signed)
        {
          saveButton.setEnabled(true);
        }
        edited = true;
      }

      @Override public void mouseEntered(MouseEvent e)
      {
       /* saveButton.setEnabled(true);
        edited = true;*/
      }

      @Override public void mouseExited(MouseEvent e)
      {
        /*saveButton.setEnabled(true);
        edited = true;*/
      }
    });

    unitSelectionForNote.addKeyListener(new KeyListener()
    {
      @Override public void keyTyped(KeyEvent e)
      {
        if(!signed)
        {
          saveButton.setEnabled(true);
        }
        edited = true;
      }

      @Override public void keyPressed(KeyEvent e)
      {
        if(!signed)
        {
          saveButton.setEnabled(true);
        }
        edited = true;
      }

      @Override public void keyReleased(KeyEvent e)
      {
        if(!signed)
        {
          saveButton.setEnabled(true);
        }
        edited = true;
      }
    });

    unitSelectionForNote.addMouseListener(new MouseListener()
    {
      @Override public void mouseClicked(MouseEvent e)
      {
        if(!signed)
        {
          saveButton.setEnabled(true);
        }
        edited = true;
      }

      @Override public void mousePressed(MouseEvent e)
      {
        if(!signed)
        {
          saveButton.setEnabled(true);
        }
        edited = true;
      }

      @Override public void mouseReleased(MouseEvent e)
      {
        if(!signed)
        {
          saveButton.setEnabled(true);
        }
        edited = true;
      }

      @Override public void mouseEntered(MouseEvent e)
      {
       /* saveButton.setEnabled(true);
        edited = true;*/
      }

      @Override public void mouseExited(MouseEvent e)
      {
        /*saveButton.setEnabled(true);
        edited = true;*/
      }
    });


    datePicker.addCalendarKeyListener(new KeyListener()
    {
      @Override public void keyTyped(KeyEvent e)
      {
        if(!signed)
        {
          saveButton.setEnabled(true);
        }
        edited = true;
      }

      @Override public void keyPressed(KeyEvent e)
      {
        if(!signed)
        {
          saveButton.setEnabled(true);
        }
        edited = true;
      }

      @Override public void keyReleased(KeyEvent e)
      {
        if(!signed)
        {
          saveButton.setEnabled(true);
        }
        edited = true;
      }
    });

    datePicker.addCalendarChangeListener(new ActionListener()
    {
      @Override public void actionPerformed(ActionEvent e)
      {
        if(!signed)
        {
          saveButton.setEnabled(true);
        }
        edited = true;
      }
    });

    timePicker.addKeyListener(new KeyListener()
    {
      @Override public void keyTyped(KeyEvent e)
      {
        if(!signed)
        {
          saveButton.setEnabled(true);
        }
        edited = true;
      }

      @Override public void keyPressed(KeyEvent e)
      {
        if(!signed)
        {
          saveButton.setEnabled(true);
        }
        edited = true;
      }

      @Override public void keyReleased(KeyEvent e)
      {
        if(!signed)
        {
          saveButton.setEnabled(true);
        }
        edited = true;
      }
    });

    timePicker.addCalendarChangeListener(new ActionListener()
    {
      @Override public void actionPerformed(ActionEvent e)
      {
        if(!signed)
        {
          saveButton.setEnabled(true);
        }
        edited = true;
      }
    });

    return notePanel;
  }

  public void initGUI()
  {
    CambioPanel mainPanel = new CambioPanel(new GridBagLayout());
    this.getContentPane().add(mainPanel);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.weightx = 1;
    gbc.weighty = 1;
    gbc.insets = new Insets(2, 2, 2, 2);

    int i = 0;

    gbc.gridx = 0;
    gbc.gridy = i;
    gbc.weightx = 1;
    gbc.gridwidth = 1;

    CambioPanel unitPanel = this.createUnitPanel();
    unitPanel.setPreferredSize(new Dimension(200,800));
    mainPanel.add(unitPanel,gbc);


    CambioPanel tablePanel = createTablePanel();
    tablePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    tablePanel.setPreferredSize(new Dimension(800,215));

    gbc.gridx =1;
    gbc.weightx = 1;
    gbc.gridwidth = 2;

    CambioPanel newButtonPanel = createNewButtonPanel();
    //newButtonPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    newButtonPanel.setPreferredSize(new Dimension(800,35));

    CambioPanel notePanel = createnotePicker();
    notePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    notePanel.setPreferredSize(new Dimension(800,350));

    CambioPanel tableNotePanel = new CambioPanel(new GridBagLayout());
    tableNotePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.LINE_START;
    c.weightx = 1;
    c.weighty = 1;
    c.insets = new Insets(2, 2, 2, 2);

    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 1;
    c.gridwidth = 2;

    tableNotePanel.add(tablePanel,c);

    c.gridy = 1;

    tableNotePanel.add(newButtonPanel , c);

    c.gridy = 2;

    CambioPanel notePanel1 = createNotePanel();

    tableNotePanel.add(notePanel1,c);


       /* c.gridy = 2;

        //combo
        String[] contact = { "c1", "c2", "c3", "c4", "c5" };
        JComboBox contactComboBox = new JComboBox(contact);

        tableNotePanel.add(contactComboBox,c);

        c.gridy = 3;
        c.gridwidth = 1;

        tableNotePanel.add(notePanel,c);

        JTextArea noteTextArea = new JTextArea();
        noteTextArea.setText("test text i am");
        JScrollPane panscr= new JScrollPane(noteTextArea);

        c.gridx = 1;
        c.fill = GridBagConstraints.BOTH;
        tableNotePanel.add(panscr,c);*/

    mainPanel.add(tableNotePanel,gbc);

    this.setSize(800,800);
    this.setResizable(false);
    this.pack();
    this.setVisible(true);

  }





  /*public void initGUI()
  {
    CambioPanel mainPanel = new CambioPanel(new GridBagLayout());
    this.getContentPane().add(mainPanel);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.weightx = 1;
    gbc.weighty = 1;
    gbc.insets = new Insets(2, 2, 2, 2);

    int i = 0;

    gbc.gridx = 0;
    gbc.gridy = i;
    gbc.weightx = 0.0;
    gbc.gridwidth = 1;

    CambioPanel unitPanel = this.createUnitPanel();
    mainPanel.add(unitPanel,gbc);


    CambioPanel tablePanel = createTablePanel();
    tablePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    tablePanel.setPreferredSize(new Dimension(800,210));

    gbc.gridx =1;
    gbc.weightx = 1;

    CambioPanel newButtonPanel = createNewButtonPanel();
    //newButtonPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    newButtonPanel.setPreferredSize(new Dimension(800,40));

    CambioPanel notePanel = new CambioPanel();
    notePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    notePanel.setPreferredSize(new Dimension(800,350));

    CambioPanel tableNotePanel = new CambioPanel(new GridBagLayout());
    tableNotePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.weightx = 0;
    c.weighty = 0;
    c.insets = new Insets(2, 2, 2, 2);

    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0.0;
    c.gridwidth = 1;

    tableNotePanel.add(tablePanel,c);

    c.gridy = 1;

    tableNotePanel.add(newButtonPanel , c);

    c.gridy = 2;

    tableNotePanel.add(notePanel,c);

    mainPanel.add(tableNotePanel,gbc);

    this.setSize(800,800);
    this.setResizable(false);
    this.pack();
    this.setVisible(true);

  }

  public CambioPanel createNewButtonPanel()
  {
    CambioPanel newButtonPanel = new CambioPanel();
    newButtonPanel.setLayout(new GridBagLayout());

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.LINE_START;
    gbc.weightx = 1;
    gbc.weighty = 1;
    gbc.insets = new Insets(5, 5, 5, 5);

    gbc.gridx = 0;
    gbc.gridy = 0;
    CambioLabel  empLabel = new CambioLabel();

    newButtonPanel.add(empLabel,gbc);

    CambioLabel  empLabel1 = new CambioLabel();

    gbc.gridx = 1;
    gbc.gridwidth=1;
    newButtonPanel.add(empLabel1,gbc);

    CambioLabel empLabel2 = new CambioLabel();
    gbc.gridx =2;
    newButtonPanel.add(empLabel2,gbc);


    CambioLabel empLabel3 = new CambioLabel();
    gbc.gridx =3;
    newButtonPanel.add(empLabel3,gbc);

    CambioLabel empLabel4 = new CambioLabel();
    gbc.gridx =4;
    newButtonPanel.add(empLabel4,gbc);


    CambioButton  newButton = new CambioButton("New");


    gbc.gridx=5;
    newButtonPanel.add(newButton,gbc);

    return newButtonPanel;
  }*/

  public void loadData()
  {
    contactSelectionComponent.setAllowNoneContact(false);
    contactSelectionComponent.setShowOnlyContactCombo(true);
    contactSelectionComponent.setPatientID(Framework.getInstance().getActiveSubjectOfCare().id);
    contactSelectionComponent.setAllowUnitAddMoreOptions(true);
    contactSelectionComponent.setSelectedCareUnit(unitSelection.getSelectedId());
    contactSelectionComponent.readContacts();

  }

  public void clearContactData()
  {
    contactSelectionComponent.setShowOnlyContactCombo(true);
    contactSelectionComponent.setPatientID(null);
    contactSelectionComponent.clearPanel();
  }

  private void removeUnsignedLabel()
  {
    unsigned.setText("");
    unsigned.paintImmediately(unsigned.getVisibleRect());
  }

  private void addUnsignedLabel()
  {
    unsigned.setText("Unsigned");
    unsigned.setForeground(Color.RED);
    unsigned.paintImmediately(unsigned.getVisibleRect());
  }


  public void refreshTable()
  {
    if (selectedContactId != 0)
    {
      try
      {
        List<TrainingMedicalNoteData> medicalNoteList = ClientDataManager.getMedicalNoteDataByContactId(selectedContactId);
        Iterator iterator = medicalNoteList.iterator();
        medicalRecordList.clear();
        cashedMedicalNotes.clear();

        while (iterator.hasNext())
        {
          TrainingMedicalNoteData medicalNoteData = (TrainingMedicalNoteData) iterator.next();
          cashedMedicalNotes.put(medicalNoteData.versionedData.id, medicalNoteData);

          MedicalRecord medicalRecord = new MedicalRecord();
          medicalRecord.setId(medicalNoteData.versionedData.id);
          medicalRecord.setCareProvider(Long.toString(medicalNoteData.careProvider));
          medicalRecord.setNoteType(medicalNoteData.noteType);
          medicalRecord.setDateTime(DateTimeToolkit.getInstance().toString(medicalNoteData.dateTime));

          ContactData contactData = ContactDataProvider.getContactDataById(Long.toString(medicalNoteData.contact));
          String contactString = ContactDataProvider.getStringForContact(contactData, HCMSettingHandler.getContactPresentationSettingValue());
          medicalRecord.setContact(contactString);

          UnitData unitData = UnitDataProvider.getInstance().getUnitDataByID(Long.toString(medicalNoteData.unit));
          medicalRecord.setUnit(unitData.name);

          medicalRecordList.add(medicalRecord);

        }

        medicalRecordModel.fireTableDataChanged();

      }
      catch (Exception ex)
      {
        DefaultExceptionHandler.getInstance().handleThrowable(ex);
      }
    }
  }

  public  static long[] getContactIdsBySoc(String socid) throws  Exception
  {
    ContactData[] currentContactData;

    String[] settingPath =
      new String[] { "ContactStatusSetting", "AllowNewContactChoice", "UseSelectionForContactSearch",
                     "SuggestContact", "UseInfoClass", "MandatoryConnectionToCareContact" };


    MultiValuedData[] filterdata =
      SettingHandler.getInstance().getSettingValues(HCMSettingHandler.PARENT_PATH, settingPath);
    String statusValue = filterdata[0].stringValue;
    ContactFilter m_filter = ContactStatusSelectionEditor.getContactFilter(statusValue);



    if(socid != null && socid != "")
    {
      currentContactData = ContactDataProvider.readContacts(socid,
                                                            m_filter,
                                                            null,
                                                            null);
      long[] idArray = new long[currentContactData.length];

      for( int i=0 ; i < currentContactData.length ; i++ )
      {
        idArray[i] = Long.parseLong(currentContactData[i].versioned.id);
      }

      return idArray;
    }


    return null;
  }

   public static List<TrainingMedicalNoteData> getUnsignedMedicalNotes(String[] careProviders , String[] unitIds ,String socId )throws Exception
   {
     long[] careProviderIds = convertToLongIds(careProviders);
     long[] unitId = convertToLongIds(unitIds);
     long[] contactIds = getContactIdsBySoc(socId);

     List<TrainingMedicalNoteData> unsignedData = ClientDataManager.getUnsignedData(contactIds,unitId,careProviderIds);

     return unsignedData;
   }

   public static long[] convertToLongIds(String[] ids)
   {
     long[] idArray = new long[ids.length];

     for( int i=0 ; i < ids.length ; i++ )
     {
       idArray[i] = Long.parseLong(ids[i]);
     }

     return idArray;
   }

  public static List<TrainingMedicalNoteData> getAllUnsignedMedicalNote(String[] careProviders, String[] unitIds)
    throws Exception
  {
    long[] careProviderIds = convertToLongIds(careProviders);
    long[] unitId = convertToLongIds(unitIds);

    List<TrainingMedicalNoteData> unsignedData = ClientDataManager.getAllUnsignedData(unitId, careProviderIds);

    return unsignedData;
  }

  public static long getCareProviderId(String careProvider)
  {
    long careProviderId = 0;
    if(careProvider.equals("$Doctor12* Doctor12*"))
    {
      careProviderId = Long.parseLong("534591");
    }
    else if(careProvider.equals("Nurse6* Nurse6*"))
    {
      careProviderId = Long.parseLong("534477");
    }
    else if(careProvider.equals("s%9Doctor13* Doctor13*"))
    {
      careProviderId = Long.parseLong("534592");
    }
    else if(careProvider.equals("Undefined name"))
    {
      careProviderId = Long.parseLong("534465");
    }
    else if(careProvider.equals("(None)"))
    {
      careProviderId = 0;
    }

    return careProviderId;
  }


  public static String getCareProvider(long careProviderId)
  {
    String careProvider = "";
    if (careProviderId == 534591)
    {
      careProvider = "$Doctor12* Doctor12*";
    }
    else if (careProviderId == 534477)
    {
      careProvider = "Nurse6* Nurse6*";
    }
    else if (careProviderId == 534592)
    {
      careProvider = "s%9Doctor13* Doctor13*";
    }
    else if (careProviderId == 534465)
    {
      careProvider = "Undefined name";
    }
    else if(careProviderId == 534475 )
    {
      careProvider = "Nurse4* Nurse4*";
    }
    else if(careProviderId == 534463 )
    {
      careProvider = "Doctor4* Doctor4*";
    }

    else if (careProviderId == 533436)
    {
      careProvider = "<space>Doctor3* Doctor3*" ;
    }
    else if (careProviderId == 533434)
    {
      careProvider = "9%Doctor1* Doctor1*";
    }
    else if(careProviderId ==  533437)
    {
      careProvider = "Nurse1* Nurse1*";
    }
    else if(careProviderId == 533438 )
    {
      careProvider = "Nurse2* Nurse2*";
    }
    else if(careProviderId == 533439 )
    {
      careProvider = "Nurse3* Nurse3*";
    }
    else if(careProviderId ==  534476)
    {
      careProvider ="Nurse5* Nurse5*" ;
    }
    else if(careProviderId == 534582 )
    {
      careProvider = "Nurse11* Nurse11*";
    }
    else if(careProviderId == 534580 )
    {
      careProvider = "Doctor11* Doctor11*";
    }
    else if(careProviderId == 534464 )
    {
      careProvider = "Doctor5* Doctor5*";
    }
    else if(careProviderId == 533437 )
    {
      careProvider = "Nurse1* Nurse1*" ;
    }
    else if(careProviderId == 533438 )
    {
      careProvider = "Nurse2* Nurse2*";
    }
    else if(careProviderId == 533439 )
    {
      careProvider = "Nurse3* Nurse3*";
    }
    else if(careProviderId == 534481 )
    {
      careProvider = "Nurse10* Nurse10*";
    }
    else if(careProviderId == 534474 )
    {
      careProvider = "Doctor10* Doctor10*";
    }

    return careProvider;
  }

  private void setInputFieldsActive(boolean active )
  {
    contactSelectionComponent.setEnabled(active);
    noteTypesComboBox.setEnabled(active);
    unitSelectionForNote.setEnabled(active);
    noteTextArea.setEnabled(active);
    datePicker.setEnabled(active);
    timePicker.setEnabled(active);
    careProviderSelectionComboBox.setEnabled(active);

  }


  }




