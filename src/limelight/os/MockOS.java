//- Copyright � 2008-2009 8th Light, Inc. All Rights Reserved.
//- Limelight and all included source files are distributed under terms of the GNU LGPL.

package limelight.os;

import java.awt.event.KeyEvent;

public class MockOS extends OS
{
  public boolean systemPropertiesConfigured;

  protected void turnOnKioskMode()
  {
  }

  protected void turnOffKioskMode()
  {
  }

  protected void launch(String URL)
  {
  }

  public void configureSystemProperties()
  {
    systemPropertiesConfigured = true;
  }

  @Override
  public boolean hasPrimaryModifierDown(KeyEvent e)
  {
    return false;
  }
}
