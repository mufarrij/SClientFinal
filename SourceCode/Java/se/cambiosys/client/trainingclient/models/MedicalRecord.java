package se.cambiosys.client.trainingclient.models;

import se.cambiosys.spider.StructureService.DateTime;

public class MedicalRecord
{

  private String id;
  private String dateTime;
  private String noteType;
  private String careProvider;
  private String unit;
  private String contact;

  public MedicalRecord(){}

  public MedicalRecord(String id , String dateTime, String noteType, String careProvider, String unit, String contact)
  {
    this.id = id;
    this.dateTime = dateTime;
    this.noteType = noteType;
    this.careProvider = careProvider;
    this.unit = unit;
    this.contact = contact;
  }

  public String getId() { return id;}

  public String getDateTime()
  {
    return dateTime;
  }

  public String getNoteType()
  {
    return noteType;
  }

  public String getCareProvider()
  {
    return careProvider;
  }

  public String getUnit()
  {
    return unit;
  }

  public String getContact()
  {
    return contact;
  }

  public void setId(String id)
  {
    this.id = id ;
  }


  public void setDateTime(String dateTime)
  {
    this.dateTime = dateTime;
  }

  public void setNoteType(String noteType)
  {
    this.noteType = noteType;
  }

  public void setCareProvider(String careProvider)
  {
    this.careProvider = careProvider;
  }

  public void setUnit(String unit)
  {
    this.unit = unit;
  }

  public void setContact(String contact)
  {
    this.contact = contact;
  }
}
