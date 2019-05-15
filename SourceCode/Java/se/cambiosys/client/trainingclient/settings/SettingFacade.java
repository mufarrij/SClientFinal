package se.cambiosys.client.trainingclient.settings;

import se.cambiosys.client.framework.DefaultExceptionHandler;
import se.cambiosys.client.framework.settings.SettingHandler;
import se.cambiosys.spider.DataService.MultiValuedData;

/**
 * This class is used to read setting value from the setting editor
 *
 * @author M.Mufarrij
 * @version 1.0 - 2019/03/13
 */
public final class SettingFacade
{

  private MultiValuedData columnOrderSetting = null;

  private static final SettingFacade instance = new SettingFacade();

  private SettingFacade()
  {

  }

  public static SettingFacade getInstance()
  {
    return instance;
  }

  /**
   * This method is used to load the setting value
   */
  private void initiateDecision()
  {

    try
    {
      MultiValuedData setting = SettingHandler.getInstance()
        .getSettingValue(SettingToolKit.CUSTOM_COLUMN_ORDER_SETTING_SETTING_PATH);

      setTimeFormat(setting);

    }
    catch (Exception e)
    {
      DefaultExceptionHandler.getInstance().handleThrowable(e);
    }
  }

  /**
   * @return prioritized arrival time format
   */
  public MultiValuedData getColumnOrderSetting()
  {
    if (columnOrderSetting == null)
    {
      initiateDecision();
    }

    return columnOrderSetting;
  }

  public void setTimeFormat(MultiValuedData setting)
  {
    columnOrderSetting = setting;
  }
}

