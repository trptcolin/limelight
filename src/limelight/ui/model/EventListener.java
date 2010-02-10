//- Copyright © 2008-2009 8th Light, Inc. All Rights Reserved.
//- Limelight and all included source files are distributed under terms of the GNU LGPL.

package limelight.ui.model;

import limelight.Context;
import limelight.ui.Panel;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.awt.*;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class EventListener implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener
{
  private final Panel panel;
  public Panel pressedPanel;
  public Panel hooveredPanel;

  public EventListener(Panel panel)
  {
    this.panel = panel;
  }

  private Panel panelFor(Point point)
  {
    return panel.getOwnerOfPoint(point);
  }

  public void mouseClicked(MouseEvent e)
  {
    // IGNORE
  }

  public void mousePressed(MouseEvent e)
  {
    pressedPanel = panelFor(e.getPoint());
    if (pressedPanel instanceof TextPanel)
    {
      for (TextPanel textPanel : getAllTextPanels())
      {
        textPanel.unselect();
      }
    }
    pressedPanel.mousePressed(e);
  }

  public void mouseReleased(MouseEvent e)
  {
    Panel releasedPanel = panelFor(e.getPoint());
    releasedPanel.mouseReleased(e);
    if(releasedPanel == pressedPanel)
    {
      releasedPanel.mouseClicked(e);
    }
  }

  public void mouseEntered(MouseEvent e)
  {
    // IGNORE
  }

  public void mouseExited(MouseEvent e)
  {
    // IGNORE
  }

  public void mouseDragged(MouseEvent e)
  {
    Panel panel = panelFor(e.getPoint());
    if(panel != hooveredPanel)
      transition(panel, e);
    panel.mouseDragged(e);
  }

  private void transition(Panel panel, MouseEvent e)
  {
    if(hooveredPanel == null)
    {
      panel.mouseEntered(e);
      enter(panel, panel, e);
    }
    else if(hooveredPanel.isDescendantOf(panel))
      exit(hooveredPanel, panel, e);
    else if(panel.isDescendantOf(hooveredPanel))
      enter(panel, hooveredPanel, e);
    else
    {
      Panel ancestor = hooveredPanel.getClosestCommonAncestor(panel);
      exit(hooveredPanel, ancestor, e);
      enter(panel, ancestor, e);
    }
    hooveredPanel = panel;
  }

  private void enter(Panel descendant, Panel ancestor, MouseEvent e)
  {
    if(descendant == ancestor || descendant == null)
      return;
    enter(descendant.getParent(), ancestor, e);
    descendant.mouseEntered(e);
  }

  private void exit(Panel descendant, Panel ancestor, MouseEvent e)
  {
    while(descendant != ancestor && !(descendant instanceof RootPanel))
    {
      if(descendant != null)
      {
        descendant.mouseExited(e);
        descendant = descendant.getParent();
      }
    }
  }

  public void mouseMoved(MouseEvent e)
  {
    Panel panel = panelFor(e.getPoint());
    if(panel != hooveredPanel)
      transition(panel, e);
    panel.mouseMoved(e);
  }

  public void mouseWheelMoved(MouseWheelEvent e)
  {
    panelFor(e.getPoint()).mouseWheelMoved(e);
  }

  public void keyTyped(KeyEvent e)
  {
    if (pressedPanel != null)
      pressedPanel.keyTyped(e);
    panel.keyTyped(e);
  }

  public void keyPressed(KeyEvent e)
  {
    if (isSelectAllEvent(e))
    {
      for (TextPanel textPanel : getAllTextPanels())
      {
        textPanel.keyPressed(e);
      }
    }
    else if (isCopyKeyEvent(e))
    {
      StringBuffer selectionText = new StringBuffer();
      for (TextPanel textPanel : getAllTextPanels())
      {
        try
        {
          StringSelection currentSelection = textPanel.getCurrentSelection();
          if (currentSelection != null)
            selectionText.append((String) currentSelection.getTransferData(DataFlavor.stringFlavor));
        }
        catch (UnsupportedFlavorException e1)
        {
        }
        catch (IOException e1)
        {
        }
      }
      StringSelection selection = new StringSelection(selectionText.toString());
      Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
    }
    else
    {
      if (pressedPanel != null)
        pressedPanel.keyPressed(e);
    }
  }

  public void keyReleased(KeyEvent e)
  {
    if (pressedPanel != null)
      pressedPanel.keyReleased(e);
    panel.keyReleased(e);
  }

  public List<TextPanel> getAllTextPanels()
  {
    LinkedList<TextPanel> textPanels = new LinkedList<TextPanel>();
    if (panel == null || panel.getRoot() == null)
      return textPanels;

    for (Panel child : panel.getRoot().getChildren())
    {
      if (child instanceof BasePanel)
      {
        for (TextPanel textPanel : ((BasePanel) child).getAllTextPanels())
        {
          textPanels.add(textPanel);
        }
      }
    }
    return textPanels;
  }

  public static boolean isSelectAllEvent(KeyEvent e)
  {
    boolean hasPrimaryModifierDown = Context.instance().os.hasPrimaryModifierDown(e);
    char keyChar = e.getKeyChar();
    return hasPrimaryModifierDown && (keyChar == 'a');
  }

  public static boolean isCopyKeyEvent(KeyEvent e)
  {
    boolean hasPrimaryModifierDown = Context.instance().os.hasPrimaryModifierDown(e);
    char keyChar = e.getKeyChar();
    return hasPrimaryModifierDown && (keyChar == 'c' || keyChar == 'x');
  }
  
  
}
