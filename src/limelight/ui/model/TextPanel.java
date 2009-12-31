//- Copyright © 2008-2009 8th Light, Inc. All Rights Reserved.
//- Limelight and all included source files are distributed under terms of the GNU LGPL.

package limelight.ui.model;

import limelight.styles.Style;
import limelight.styles.StyleDescriptor;
import limelight.styles.StyleObserver;
import limelight.styles.abstrstyling.StyleAttribute;
import limelight.ui.Panel;
import limelight.ui.api.Prop;
import limelight.ui.api.PropablePanel;
import limelight.ui.api.Scene;
import limelight.util.*;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.font.*;
import java.awt.geom.AffineTransform;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.*;
import java.util.List;

public class TextPanel extends BasePanel implements StyleObserver
{
  private String text;
  private PropablePanel panel;
  private double consumedHeight;
  private double consumedWidth;
  private Graphics2D graphics;
  private boolean textChanged;
  public static FontRenderContext staticFontRenderingContext;
  private Box consumableArea;

  private LinkedList<TextLayout> lines;
  private List<StyledString> textChunks;

  public Point endSelectionPoint;
  public Point startSelectionPoint;
  public LinkedList<String> actualLineTexts;
  private StringSelection currentSelection;

  //TODO MDM panel is not really needed here.  It's the same as parent.
  public TextPanel(PropablePanel panel, String text)
  {
    this.panel = panel;
    this.text = text;
    markAsNeedingLayout();
  }

  public String getText()
  {
    return text;
  }

  public void setText(String text)
  {
    boolean differentText = !Util.equal(text, this.text);
    if(!needsLayout() && differentText)
      markAsNeedingLayout();
    this.text = text;
    if(differentText)
    {
      markAsNeedingLayout();
      propagateSizeChangeUp(getParent());
      getParent().markAsNeedingLayout();
    }
  }

  public Panel getPanel()
  {
    return panel;
  }

  public void paintOn(Graphics2D graphics)
  {
    graphics.setColor(getTextColorFromStyle(getStyle()));
    if(lines == null)
    {
      return;
    }
    synchronized(this)
    {
      float y = 0;
      boolean firstEndpointDrawn = false;
      boolean lastEndpointDrawn = false;

      StringBuffer highlightedText = new StringBuffer();

      for(int lineIndex = 0; lineIndex < lines.size(); lineIndex++)
      {
        LineProcessor lineProcessor = new LineProcessor(this, graphics, y, firstEndpointDrawn, lastEndpointDrawn, highlightedText, lineIndex).invoke();
        firstEndpointDrawn = lineProcessor.isFirstEndpointDrawn();
        lastEndpointDrawn = lineProcessor.isLastEndpointDrawn();
        y = lineProcessor.getY();
      }

      setSelection(highlightedText);
    }
  }

  private void setSelection(StringBuffer highlightedText)
  {
    if (!highlightedText.toString().equals(""))
    {
      currentSelection = new StringSelection(highlightedText.toString());
    }
  }

  public Style getStyle()
  {
    return panel.getStyle();
  }

  public void doLayout()
  {
    TextPanelLayout.instance.doLayout(this);
  }

  public Layout getDefaultLayout()
  {
    return TextPanelLayout.instance;
  }

  public void consumableAreaChanged()
  {
    markAsNeedingLayout();
  }

  public void compile()
  {
    buildLines();
    calculateDimensions();
    flushChanges();
    snapToSize();
    markAsDirty();
  }

  public void snapToSize()
  {
    setSize((int) (consumedWidth + 0.5), (int) (consumedHeight + 0.5));
  }

  public synchronized void buildLines()
  {
    consumableArea = panel.getChildConsumableArea();
    textChunks = new LinkedList<StyledString>();
    lines = new LinkedList<TextLayout>();

    if(text != null && text.length() > 0)
    {
      StyledTextParser parser = new StyledTextParser();
      LinkedList<StyledText> styledParagraph = parser.parse(text);

      Font font = getFontFromStyle(getStyle());
      Color color = getTextColorFromStyle(getStyle());

      textChunks = new LinkedList<StyledString>();
      for (StyledText styledLine : styledParagraph)
      {
        addTextChunk(font, color, styledLine);
      }
      closeParagraph();
      addLines();
    }
  }

  private synchronized void addTextChunk(Font defaultFont, Color defaultColor, StyledText styledLine)
  {
    Font font;
    Color color;
    String line = styledLine.getText();
    String tagName = styledLine.getStyle();

    if(!Util.equal(tagName,"default"))
    {
      Style tagStyle = getStyleFromTag(tagName);

      if(tagStyle != null)
      {
        if(!tagStyle.hasObserver(this))
        {
          tagStyle.addObserver(this);
        }
        font = getFontFromStyle(tagStyle);
        color = getTextColorFromStyle(tagStyle);
      }
      else
      {
        // unrecognized style tag
        font = defaultFont;
        color = defaultColor;
      }
    }
    else
    {
      font = defaultFont;
      color = defaultColor;
    }

    if(line.length() == 0)
    {
      line = " ";
    }

    textChunks.add(new StyledString(font, line, color));
  }

  private Color getTextColorFromStyle(Style tagStyle)
  {
    return tagStyle.getCompiledTextColor().getColor();
  }

  private Font getFontFromStyle(Style style)
  {
    return new Font(style.getCompiledFontFace().getValue(), style.getCompiledFontStyle().toInt(), style.getCompiledFontSize().getValue());
  }

  protected Style getStyleFromTag(String tagName)
  {
    Prop prop = ((PropablePanel) getPanel()).getProp();
    Scene scene = prop.getScene();
    Map styles = scene.getStyles();
    return (Style) styles.get(tagName);
  }

  private void closeParagraph()
  {
    Font font = getFontFromStyle(getStyle());
    textChunks.add(new StyledString(font, "\n", new Color(0, 0, 0, 0)));
  }

  private synchronized void addLines()
  {
    AttributedString aText = prepareAttributedString();
    AttributedCharacterIterator styledTextIterator = aText.getIterator();

    List<Integer> newlineLocations = getNewlineLocations(styledTextIterator);

    LineBreakMeasurer lbm = new LineBreakMeasurer(styledTextIterator, getRenderContext());

    int currentNewline = 0;
    int end = styledTextIterator.getEndIndex();

    actualLineTexts = new LinkedList<String>();
    int textPosition = 0;

    while (lbm.getPosition() < end)
    {
      boolean shouldEndLine = false;

      while(!shouldEndLine)
      {
        float width1 = (float) consumableArea.width;
        textPosition = lbm.getPosition();
        TextLayout layout = lbm.nextLayout(width1, newlineLocations.get(currentNewline) + 1, false);


        if (layout != null)
        {
          lines.add(layout);
        }
        else
        {
          shouldEndLine = true;
        }

        if (lbm.getPosition() == newlineLocations.get(currentNewline) + 1)
        {
          currentNewline += 1;
        }

        if (lbm.getPosition() == styledTextIterator.getEndIndex())
        {
          shouldEndLine = true;
        }


        String lineText = substringAttributed(aText, textPosition, lbm.getPosition());
        actualLineTexts.add(lineText);
      }
    }
  }

  private String substringAttributed(AttributedString aText, int startPosition, int endPosition)
  {
    AttributedCharacterIterator iterator = aText.getIterator();
    boolean inRange = false;
    StringBuffer buf = new StringBuffer();

    for (char c = iterator.first(); c != AttributedCharacterIterator.DONE; c = iterator.next())
    {
      if (iterator.getIndex() == startPosition)
      {
        inRange = true;
      }
      else if (iterator.getIndex() == endPosition)
      {
        inRange = false;
      }

      if (inRange)
      {
        buf.append(c);
      }
    }

    return buf.toString();
  }

  private List<Integer> getNewlineLocations(AttributedCharacterIterator styledTextIterator)
  {
    List<Integer> newlineLocations = new ArrayList<Integer>();
    for (char c = styledTextIterator.first(); c != AttributedCharacterIterator.DONE; c = styledTextIterator.next())
    {
      if (c == '\n')
        newlineLocations.add(styledTextIterator.getIndex());
    }
    return newlineLocations;
  }

  private AttributedString prepareAttributedString()
  {
    StringBuffer buf = new StringBuffer();
    List<Integer> fontIndexes = new ArrayList<Integer>();
    List<Font> fonts = new ArrayList<Font>();
    List<Color> colors = new ArrayList<Color>();

    int i = 0;
    for (StyledString textChunk : getTextChunks())
    {
      buf.append(textChunk.text);
      fontIndexes.add(i);
      fonts.add(textChunk.font);
      colors.add(textChunk.color);
      i = i + textChunk.text.length();
    }

    AttributedString aText = new AttributedString(buf.toString());
    for (int fontIndex = 0; fontIndex < fonts.size(); fontIndex++)
    {
      int startIndex = fontIndexes.get(fontIndex);
      int endIndex;
      if(fontIndex == fonts.size() - 1)
        endIndex = buf.length();
      else
        endIndex = fontIndexes.get(fontIndex + 1);
      aText.addAttribute(TextAttribute.FONT, fonts.get(fontIndex), startIndex, endIndex);
      aText.addAttribute(TextAttribute.FOREGROUND, colors.get(fontIndex), startIndex, endIndex);
    }
    return aText;
  }

  public List<StyledString> getTextChunks()
  {
    return textChunks;
  }

  public FontRenderContext getRenderContext()
  {
    if(staticFontRenderingContext == null)
    {
      AffineTransform affineTransform = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getDefaultTransform();
      staticFontRenderingContext = new FontRenderContext(affineTransform, true, false);
    }
    return staticFontRenderingContext;
  }

  private void calculateDimensions()
  {
    consumedHeight = 0;
    consumedWidth = 0;
    synchronized(this)
    {
      for(TextLayout layout : lines)
      {
        consumedHeight += (layout.getAscent() + layout.getDescent() + layout.getLeading());
        double lineWidth = widthOf(layout);
        if(lineWidth > consumedWidth)
          consumedWidth = lineWidth;
      }
    }
  }

  public double widthOf(TextLayout layout)
  {
    return layout.getBounds().getWidth() + layout.getBounds().getX();
  }

  public void setGraphics(Graphics graphics)
  {
    this.graphics = (Graphics2D) graphics;
  }

  public boolean textChanged()
  {
    return textChanged;
  }

  public void flushChanges()
  {
    textChanged = false;
  }

  public Box getChildConsumableArea()
  {
    return null;
  }

  public Box getBoxInsidePadding()
  {
    return null;
  }

  public Graphics2D getGraphics()
  {
    if(graphics != null)
      return graphics;
    else
      return panel.getGraphics();
  }

  public String toString()
  {
    return "Text: <" + getText() + ">";
  }

  public boolean canBeBuffered()
  {
    return false;
  }

  public List<TextLayout> getLines()
  {
    return lines;
  }

  public void styleChanged(StyleDescriptor descriptor, StyleAttribute value)
  {
    markAsNeedingLayout();
    getParent().markAsNeedingLayout();
    propagateSizeChangeDown();
    propagateSizeChangeUp(getParent().getParent());
  }

  @Override
  public void mousePressed(MouseEvent e)
  {
    super.mousePressed(e);
    startSelectionPoint = translatedEvent(e).getPoint();
    endSelectionPoint = new Point(startSelectionPoint);
  }

  @Override
  public void mouseDragged(MouseEvent e)
  {
    super.mouseDragged(e);
    handleEndPoint(e);
  }

  @Override
  public void mouseReleased(MouseEvent e)
  {
    super.mouseReleased(e);
    handleEndPoint(e);
  }

  @Override
  public void mouseEntered(MouseEvent e)
  {
    if (getRoot().getCursor().getType() == Cursor.DEFAULT_CURSOR)
      getRoot().setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
  }

  @Override
  public void mouseExited(MouseEvent e)
  {
    if (getRoot().getCursor().getType() == Cursor.TEXT_CURSOR)
      getRoot().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
  }

  @Override
  public void keyPressed(KeyEvent e)
  {
    if (isCopyKeyEvent(e) && currentSelection != null)
    {
      Toolkit.getDefaultToolkit().getSystemClipboard().setContents(currentSelection, currentSelection);
    }
  }

  private void handleEndPoint(MouseEvent e)
  {
    endSelectionPoint = translatedEvent(e).getPoint();
    repaint();
  }

  protected class StyledString
  {
    protected String text;
    protected Font font;
    protected Color color;

    private StyledString(Font font, String text, Color color)
    {
      this.font = font;
      this.text = text;
      this.color = color;
    }

    public int getCharacterCount()
    {
      return text.length();
    }

    public String toString()
    {
      return text + "(font: " + font + ", color: " + color + ")";
    }
  }

}
