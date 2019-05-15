package se.cambiosys.client.trainingclient.models;

import se.cambiosys.spider.StructureService.DateTime;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class MedicalRecordModel extends AbstractTableModel
{
   private List<MedicalRecord> medicalRecordList;

   private static final String[] columnNames = new String[]
     {
        "Date/Time","Note type","Care provider","Unit","Contact"
     };
   private static final Class[] columnClass = new Class[] {
     String.class , String.class,String.class,String.class,String.class
   };

   public MedicalRecordModel(List<MedicalRecord> medicalRecordList){
     this.medicalRecordList = medicalRecordList;
   }

   public List<MedicalRecord> getMedicalRecordListList()
   {
     return this.medicalRecordList;
   }

   public void setMedicalRecordList(List<MedicalRecord> medicalRecordList)
   {
     this.medicalRecordList = medicalRecordList;
   }

  @Override
  public String getColumnName(int column)
  {
    return columnNames[column];
  }

  @Override
  public Class<?> getColumnClass(int columnIndex)
  {
    return columnClass[columnIndex];
  }

  @Override
  public int getColumnCount()
  {
    return columnNames.length;
  }

  @Override
  public int getRowCount()
  {
    return medicalRecordList.size();
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex)
  {
    MedicalRecord row = medicalRecordList.get(rowIndex);
    if(0 == columnIndex) {
      return row.getDateTime();
    }
    else if(1 == columnIndex) {
      return row.getNoteType();
    }
    else if(2 == columnIndex) {
      return row.getCareProvider();
    }
    else if(3 == columnIndex) {
      return row.getUnit();
    }
    else if(4 == columnIndex) {
      return row.getContact();
    }
    return null;
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex)
  {
    return true;
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex)
  {
    MedicalRecord row = medicalRecordList.get(rowIndex);
    if(0 == columnIndex) {
      row.setDateTime((String) aValue);
    }
    else if(1 == columnIndex) {
      row.setNoteType((String) aValue);
    }
    else if(2 == columnIndex) {
      row.setCareProvider((String) aValue);
    }
    else if(3 == columnIndex) {
      row.setUnit((String) aValue);
    }
    else if(4 == columnIndex) {
      row.setContact((String) aValue);
    }
  }

  public void removeRow(int row) {
    // remove a row from your internal data structure
    medicalRecordList.remove(row);
  }

}
