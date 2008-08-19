package limelight.ui.model.inputs;

import limelight.ui.model.PropPanel;
import limelight.ui.model.TextAccessor;
import limelight.styles.Style;

import java.awt.*;

public class ComboBoxPanel extends InputPanel
{
  private ComboBox comboBox;
  private ContainerStub container;

  public ComboBox getComboBox()
  {
    return comboBox;
  }

  protected Component createComponent()
  {
    comboBox = new ComboBox(this);

    container = new ContainerStub(this);
    container.add(comboBox);

    return comboBox;
  }

  protected TextAccessor createTextAccessor()
  {
    return new ComboBoxTextAccessor(comboBox);
  }

  protected void setDefaultStyles(Style style)
  {
    style.setDefault(Style.WIDTH, "120");
    style.setDefault(Style.HEIGHT, "" + comboBox.getPreferredSize().height);
  }

  private static class ComboBoxTextAccessor implements TextAccessor
  {
    private ComboBox comboBox;

    public ComboBoxTextAccessor(ComboBox comboBox)
    {
      this.comboBox = comboBox;
    }

    public void setText(String text)
    {
      comboBox.setSelectedItem(text);
    }

    public String getText()
    {
      return comboBox.getSelectedItem().toString();
    }
  }
}