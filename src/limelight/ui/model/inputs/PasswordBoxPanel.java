//- Copyright � 2008-2009 8th Light, Inc. All Rights Reserved.
//- Limelight and all included source files are distributed under terms of the GNU LGPL.

package limelight.ui.model.inputs;

import limelight.ui.model.TextAccessor;
import limelight.styles.Style;

import java.awt.*;
import java.awt.event.MouseEvent;

public class PasswordBoxPanel extends AwtInputPanel
{
  private PasswordBox textBox;

  protected Component createComponent()
  {
    return textBox = new PasswordBox(this);
  }

  protected TextAccessor createTextAccessor()
  {
    return new TextBoxTextAccessor(textBox);
  }

  protected void setDefaultStyles(Style style)
  {
    style.setDefault(Style.WIDTH, "100");
    style.setDefault(Style.HEIGHT, "" + textBox.getPreferredSize().height);
  }

  public PasswordBox getPasswordBox()
  {
    return textBox;
  }

  public String getText()
  {
    return textBox.getText();
  }

  public void setText(String text)
  {
    textBox.setText(text);
  }

  private static class TextBoxTextAccessor implements TextAccessor
  {
    private final PasswordBox textBox;

    public TextBoxTextAccessor(PasswordBox textBox)
    {
      this.textBox = textBox;
    }

    public void setText(String text)
    {
      textBox.setText(text);
    }

    public String getText()
    {
      return textBox.getText();
    }
  }

  public void mouseEntered(MouseEvent e)
  {
    getRoot().setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
  }

  public void mouseExited(MouseEvent e)
  {
    getRoot().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
  }
}
