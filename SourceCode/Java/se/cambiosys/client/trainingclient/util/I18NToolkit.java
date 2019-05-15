package se.cambiosys.client.trainingclient.util;

import se.cambiosys.client.framework.DefaultExceptionHandler;
import se.cambiosys.client.framework.components.CambioResourceBundle;
import se.cambiosys.client.framework.components.CambioResourceInterface;

import java.text.MessageFormat;
import java.util.MissingResourceException;

/**
 * <p>Title: I18NToolkit</p>
 * <p>Description: International Toolkit for ContactOverviewClient module</p>
 * <p>Copyright: Copyright (c) Cambio Healthcare Systems</p>
 * <p>Company: Cambio Healthcare Systems</p>
 */
public abstract class I18NToolkit
{
  public static final String MNEMONIC_CHAR = "¤";
  private static final String CONTACTOVERVIEW_LANGUAGE_PATH = "se.cambiosys.client.trainingclient.language";
  private static final String CONTACTOVERVIEW_COMMON_BUNDLE_PATH = CONTACTOVERVIEW_LANGUAGE_PATH + ".CommonLanguage";
  private static CambioResourceBundle
    CONTACTOVERVIEW_COMMON_BUNDLE = CambioResourceBundle.getResourceBundle(CONTACTOVERVIEW_COMMON_BUNDLE_PATH);

  public static void setupBundle(Object p_object)
  {
    if (p_object instanceof CambioResourceInterface)
    {
      CambioResourceInterface cr = (CambioResourceInterface) p_object;
      cr.setupResourceBundles(new String(CONTACTOVERVIEW_COMMON_BUNDLE_PATH));
    }
  }

  /**
   * Returns string without mnemonics
   */
  public static String getResourceString(String p_key)
  {
    return getResourceString(null, p_key);
  }

  /**
   * Returns string without mnemonics
   */
  public static String getResourceString(Object p_object, String p_key)
  {
    return getMnemonicStrippedString(getResourceStringWithMnemonics(p_object, p_key));
  }

  public static String getResourceStringWithMnemonics(String p_key)
  {
    return getResourceStringWithMnemonics(null, p_key);
  }

  /**
   * Returns string with mnemonics
   */
  public static String getResourceStringWithMnemonics(Object p_object, String p_key)
  {
    String returnString = "";
    try
    {
      if (p_object == null)
      {
        returnString = CONTACTOVERVIEW_COMMON_BUNDLE.getResourceString(p_key);
      }
      else if (p_object instanceof CambioResourceInterface)
      {
        CambioResourceInterface cr = (CambioResourceInterface)p_object;
        returnString = cr.getResourceString(p_key);
      }
      else
      {
        returnString = CONTACTOVERVIEW_COMMON_BUNDLE.getResourceString(p_key);
      }
    }
    catch (MissingResourceException ex)
    {
      DefaultExceptionHandler.handleThrowable(ex, false, null);
    }

    return returnString;
  }

  public static String getFormattedString(Object p_class, String key,Object p_value)
  {
    return MessageFormat.format(getResourceString(p_class, key),new Object[] { p_value });
  }

  public static String getFormattedString(Object p_class, String key,Object[] p_values)
  {
    return MessageFormat.format(getResourceString(p_class, key), p_values);
  }

  public static String getFormattedString(String key, Object p_value)
  {
    return MessageFormat.format(getResourceString(key), new Object[]{ p_value });
  }

  public static String getFormattedString(String key, Object[] p_values)
  {
    return MessageFormat.format(getResourceString(key), p_values);
  }

  public static Object[] getYesNoCancelOptions()
  {
    Object[] options = {getResourceString("Yes.cd"),getResourceString("No.cd"),getResourceString("Cancel.cd")};
    return options;
  }

  public static String getMnemonicStrippedString(String p_value)
  {
    String s = (String) p_value;
    int mnemonicIndex = s.indexOf(MNEMONIC_CHAR);
    if (mnemonicIndex < 0)
    {
      // no mnemonic in string
      return s;
    }
    else
    {
      String strippedString = s.replaceAll(MNEMONIC_CHAR, "");
      return strippedString;
    }
  }

  public static Integer getMnemonicInString(String p_value)
  {
    String s = (String) p_value;
    int mnemonicIndex = s.indexOf(MNEMONIC_CHAR);
    if (mnemonicIndex < 0)
    {
      // no mnemonic in string
      return null;
    }
    else
    {
      // and the mnemonic
      s = s.toUpperCase();
      char mnemonic = s.charAt(mnemonicIndex + 1);
      return new Integer(mnemonic);
    }
  }

  public static Integer getMnemonicFromKey(Object p_object, String p_key)
  {
    String rs = getResourceStringWithMnemonics(p_object, p_key);
    return getMnemonicInString(rs);
  }

  public static Integer getMnemonicFromKey(String p_key)
  {
    String rs = getResourceStringWithMnemonics(p_key);
    return getMnemonicInString(rs);
  }

  /**
   * Gets the integer value of the value of the given key.
   *
   * @param p_key the key to get value
   * @return integer value of the first character of value given by respective key.
   */
  public static int getMnemonicCharByKey(String p_key)
  {
    String rs = getResourceString(p_key);
    return rs.charAt(0);
  }
}
